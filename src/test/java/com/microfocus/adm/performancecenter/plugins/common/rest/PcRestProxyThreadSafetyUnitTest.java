/**
 * Copyright © 2023 Open Text Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microfocus.adm.performancecenter.plugins.common.rest;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests that verify {@link PcRestProxy} is safe to use concurrently from multiple threads.
 *
 * <p>The thread-safety contract relies on two properties:
 * <ol>
 *     <li>A fresh, per-request {@code HttpContext} is created for every request (the mutable
 *         {@code HttpContext} is never shared across threads).</li>
 *     <li>All per-request contexts share the same thread-safe {@code CookieStore}, so a single
 *         authenticated session is reused by every thread.</li>
 * </ol>
 * These are validated below via reflection (white-box) and via a concurrency stress test that
 * drives many simultaneous requests through a stubbed proxy (black-box).
 */
public class PcRestProxyThreadSafetyUnitTest {

    /**
     * White-box check of the fix: {@code createContext()} must return a brand-new context on every
     * call while reusing the single shared cookie store held by the proxy.
     */
    @Test
    public void createContext_returnsFreshContextSharingCookieStore() throws Exception {
        PcRestProxy proxy = new StubPcRestProxy();

        Method createContext = PcRestProxy.class.getDeclaredMethod("createContext");
        createContext.setAccessible(true);

        HttpContext first = (HttpContext) createContext.invoke(proxy);
        HttpContext second = (HttpContext) createContext.invoke(proxy);

        Assert.assertNotNull("createContext() must not return null", first);
        Assert.assertNotNull("createContext() must not return null", second);
        Assert.assertNotSame("Each request must get its own HttpContext instance", first, second);

        CookieStore firstStore = (CookieStore) first.getAttribute(HttpClientContext.COOKIE_STORE);
        CookieStore secondStore = (CookieStore) second.getAttribute(HttpClientContext.COOKIE_STORE);

        Assert.assertNotNull("Context must expose a cookie store", firstStore);
        Assert.assertSame("All contexts must share the same cookie store", firstStore, secondStore);

        Field cookieStoreField = PcRestProxy.class.getDeclaredField("cookieStore");
        cookieStoreField.setAccessible(true);
        Assert.assertSame("Contexts must reuse the proxy's shared cookie store",
                cookieStoreField.get(proxy), firstStore);
    }

    /**
     * Guards against a regression to a single shared mutable {@code HttpContext} field.
     */
    @Test
    public void proxy_doesNotHoldASharedMutableContextField() throws Exception {
        try {
            PcRestProxy.class.getDeclaredField("context");
            Assert.fail("PcRestProxy must not hold a shared 'context' field; use a per-request context instead");
        } catch (NoSuchFieldException expected) {
            // expected: the shared context field was removed as part of the thread-safety fix
        }
    }

    /**
     * Black-box stress test: hammer a single proxy instance with many concurrent requests and verify
     * every thread receives the correct, isolated response (no cross-talk / corruption).
     */
    @Test
    public void concurrentRequests_eachThreadReceivesItsOwnCorrectResponse() throws Exception {
        final int threadCount = 16;
        final int baseId = 2000;

        StubPcRestProxy proxy = new StubPcRestProxy();
        for (int i = 0; i < threadCount; i++) {
            int id = baseId + i;
            proxy.registerResponse("GET", "/Scripts/" + id,
                    "<Script><ID>" + id + "</ID><Name>script-" + id + "</Name>"
                            + "<TestFolderPath>Subject\\folder</TestFolderPath></Script>");
        }

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<Future<Integer>>();

        try {
            for (int i = 0; i < threadCount; i++) {
                final int expectedId = baseId + i;
                futures.add(pool.submit(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        ready.countDown();
                        start.await();
                        // Returned ID must match the requested ID for every concurrent caller.
                        return proxy.getScript(expectedId).getID();
                    }
                }));
            }

            Assert.assertTrue("Worker threads did not become ready in time",
                    ready.await(10, TimeUnit.SECONDS));
            start.countDown(); // release all threads at once to maximize contention

            for (int i = 0; i < threadCount; i++) {
                int expectedId = baseId + i;
                Assert.assertEquals("Concurrent request returned a mismatched/corrupted response",
                        expectedId, futures.get(i).get(20, TimeUnit.SECONDS).intValue());
            }
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * Minimal {@link PcRestProxy} stub that returns canned responses based on request URL,
     * bypassing real network I/O. Response rules are registered before threads start, so the
     * lookup performed inside {@link #executeRequest(HttpRequestBase)} is read-only and safe to
     * call concurrently.
     */
    private static class StubPcRestProxy extends PcRestProxy {

        private final List<ResponseRule> responses = new ArrayList<ResponseRule>();

        StubPcRestProxy() throws PcException {
            super("http", "localhost", false, "DANIEL", "proj1", "", "", "");
        }

        void registerResponse(String method, String urlContains, String body) {
            responses.add(new ResponseRule(method, urlContains, body));
        }

        @Override
        protected String executeRequest(HttpRequestBase request) {
            String url = request.getURI().toString();
            ResponseRule bestMatch = null;
            for (ResponseRule rule : responses) {
                if (rule.matches(request.getMethod(), url)) {
                    if (bestMatch == null || rule.urlContains.length() > bestMatch.urlContains.length()) {
                        bestMatch = rule;
                    }
                }
            }
            if (bestMatch != null) {
                return bestMatch.body;
            }
            throw new AssertionError("No mocked response for request: " + request.getMethod() + " " + url);
        }
    }

    private static class ResponseRule {
        private final String method;
        private final String urlContains;
        private final String body;

        private ResponseRule(String method, String urlContains, String body) {
            this.method = method;
            this.urlContains = urlContains;
            this.body = body;
        }

        private boolean matches(String requestMethod, String url) {
            return method.equalsIgnoreCase(requestMethod) && url.contains(urlContains);
        }
    }
}
