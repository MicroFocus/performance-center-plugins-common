package com.microfocus.adm.performancecenter.plugins.common.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class IntegrationTestConfig {

    private static final String RESOURCE_NAME = "integration-tests.properties";
    private static final boolean PROPERTIES_FILE_PRESENT = hasResource(RESOURCE_NAME);
    private static final Properties PROPERTIES = loadProperties();

    private IntegrationTestConfig() {
    }

    static String get(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue.trim();
        }

        String envValue = System.getenv(toEnvKey(key));
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        String fileValue = PROPERTIES.getProperty(key);
        if (fileValue != null && !fileValue.trim().isEmpty()) {
            return fileValue.trim();
        }

        return defaultValue;
    }

    static boolean isConfigured() {
        return hasPropertiesFile()
                && hasUsableFileValues("pc.lre.server", "pc.alm.domain", "pc.alm.project")
                && ((hasUsableFileValues("pc.alm.user", "pc.alm.password"))
                || (hasUsableFileValues("pc.lre.idKey", "pc.lre.secretKey")));
    }

    static boolean hasPropertiesFile() {
        return PROPERTIES_FILE_PRESENT;
    }

    static boolean hasFileValues(String... keys) {
        for (String key : keys) {
            if (!hasFileValue(key)) {
                return false;
            }
        }
        return true;
    }

    static boolean hasUsableFileValues(String... keys) {
        for (String key : keys) {
            if (!hasUsableFileValue(key)) {
                return false;
            }
        }
        return true;
    }

    static boolean hasFileValue(String key) {
        String value = PROPERTIES.getProperty(key);
        return value != null && !value.trim().isEmpty();
    }

    static boolean hasUsableFileValue(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            return false;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return false;
        }
        return !isPlaceholderValue(normalized);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = IntegrationTestConfig.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignored) {
            // Best-effort load: tests can still use system properties or env vars.
        }
        return properties;
    }

    private static String toEnvKey(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    private static boolean hasResource(String resourceName) {
        return IntegrationTestConfig.class.getClassLoader().getResource(resourceName) != null;
    }

    private static boolean isPlaceholderValue(String value) {
        String lower = value.toLowerCase();
        return lower.contains("your-")
                || lower.contains("your_")
                || lower.contains("example")
                || lower.contains("changeme")
                || lower.contains("placeholder")
                || lower.equals("<set-me>")
                || lower.equals("<required>");
    }
}

