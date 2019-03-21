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

package com.springsource.insight.plugin.akka;

import com.springsource.insight.intercept.operation.OperationType;

public class AkkaDefinitions {

    public static final class OperationTypes {
        public static final OperationType AKKA_OP_ACTOR_REF = OperationType.valueOf("akka_actor_ref");
        public static final OperationType AKKA_OP_UNTYPED_ACTOR = OperationType.valueOf("akka_untyped_actor");
        public static final OperationType AKKA_OP_TYPED_ACTOR = OperationType.valueOf("akka_typed_actor");
    }

    public static final class Labels {
        public static final String AKKA = "Akka";
        public static final String ACTOR = "Actor";
        public static final String ACTOR_REF = "ActorRef";
        public static final String MESSAGE = "MessageType";
        public static final String PATH = "Path";
        public static final String ROUTER = "Router";
        public static final String SENDER = "Sender";
        public static final String SYSTEM = "System";
        public static final String TYPED_ACTOR = "Typed Actor";
        public static final String UNTYPED_ACTOR = "Untyped Actor";
    }

}
