/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.integration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.springsource.insight.util.StringUtil;

/**
 *
 */
public class MessageListenerProps {
    private static final Map<String, MessageListenerProps> messageListenerPropsCache = new ConcurrentHashMap<String, MessageListenerProps>();

    public final String outputChannelName;
    public final String queueNames;
    public final String adapterBeanName;
    public final String adapterBeanType;

    public MessageListenerProps(String outChannelName, String queueNamesList, String assignedAdapterBeanName, String assignedAdapterBeanType) {
        outputChannelName = outChannelName;
        queueNames = queueNamesList;
        adapterBeanName = assignedAdapterBeanName;
        adapterBeanType = assignedAdapterBeanType;
    }

    public static final MessageListenerProps putMessageListenerProps(String key, MessageListenerProps props) {
        return (props == null) ? messageListenerPropsCache.remove(key) : messageListenerPropsCache.put(key, props);
    }

    public static final MessageListenerProps getMessageListenerProps(String key) {
        if (StringUtil.isEmpty(key)) {
            return null;
        } else {
            return messageListenerPropsCache.get(key);
        }
    }
}

