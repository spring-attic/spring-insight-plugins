/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
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
package com.springsource.insight.plugin.jms;

import java.util.EnumSet;

import com.springsource.insight.util.StringUtil;

enum DestinationType {
    Queue("Queue", false),
    Topic("Topic", false),
    TemporaryQueue("Temporary queue", true),
    TemporaryTopic("Temporary topic", true),
    Unknown("Unknown", false);

    public static final EnumSet<DestinationType> enumSet = EnumSet.allOf(DestinationType.class);

    private boolean temporary;
    private String label;

    DestinationType(String lbl, boolean temp) {
        this.label = lbl;
        this.temporary = temp;
    }

    public String getLabel() {
        return label;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public static DestinationType fromLabel(String lbl) {
        if (StringUtil.isEmpty(lbl)) {
            return null;
        }

        for (DestinationType type : enumSet) {
            if (lbl.equals(type.label)) {
                return type;
            }
        }

        return null;
    }
}