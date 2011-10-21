/**
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.redis.util;

/**
 * Util class for Redis plugin
 */
public class RedisUtil {

    private static final int MAX_CHARS = 255;

    public final static String objectToString(Object obj) {
        String result = "null";
        if(obj != null) {
            String str = obj.toString();
            int len = str.length();
            if(len > MAX_CHARS) {
                len = MAX_CHARS;
            }
            result = obj.toString().substring(0, len);
        }
        return result;
    }
}
