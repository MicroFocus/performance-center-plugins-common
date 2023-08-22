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
package com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.enums;

public enum SimplifiedThinkTimeTypeValues {

    IGNORE("ignore"),
    REPLAY("replay"),
    MODIFY("modify"),
    RANDOM("random");

    private String value;

    private SimplifiedThinkTimeTypeValues(String value) {
        this.value = value;
    }

    public static SimplifiedThinkTimeTypeValues get(String val) {
        for (SimplifiedThinkTimeTypeValues simplifiedThinkTimeTypeValues : SimplifiedThinkTimeTypeValues.values()) {
            if (val.equals(simplifiedThinkTimeTypeValues.value()))
                return simplifiedThinkTimeTypeValues;
        }
        return null;
    }

    public String value() {
        return value;
    }
}
