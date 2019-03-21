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
package com.springsource.insight.plugin.jms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import com.springsource.insight.intercept.operation.Operation;

/**
 * A composite class around a {@code synchronized} {@link HashMap}.<br><br>
 * 
 * This class is used by the {@code JMSProducerCollectionAspect} to prevent the situations where we the client invoke producer.send<br> 
 * and the producer implementation invoke another send method.
 * 
 */
class MessageOperationMap {
    /**
     * Default size limit
     */
    private static final int DEFAULT_LIMIT = 100000;
    
    private Map<MessageWrapper, Operation> map;
    
    /**
     * Holds the map size limit
     */
    private int limit;
    
    /**
     * Semaphore to be used in the map clean operation.
     * Only one thread can clean the map
     */
    private Semaphore semaphore;
    
    /**
     * Create a map with a given limit
     * 
     * @param limit map size limit
     */
    MessageOperationMap(@SuppressWarnings("hiding") int limit) {
        this.map = Collections.synchronizedMap(new HashMap<MessageWrapper, Operation>());
        this.limit = limit;
        this.semaphore = new Semaphore(1);
    }
    
    /**
     * Create a map with {@link MessageOperationMap#DEFAULT_LIMIT}
     */
    MessageOperationMap() {
        this(DEFAULT_LIMIT);
    }
    
    void put(MessageWrapper wrapper, Operation op, String sig) {
        if (map.size() >= limit) {
            if (!cleanMap()) {
                return;
            }
        }
        op.put("sig", sig);
        map.put(wrapper, op);
    }
    
    boolean isRelevant(String sig, Operation op) {
        String opSig = op.get("sig", String.class);
        return sig.equals(opSig);
    }
    
    private boolean cleanMap() {
        if (semaphore.tryAcquire()) {
            try {
                List<MessageWrapper> toRemove = new ArrayList<MessageWrapper>();
                for (MessageWrapper wrapper : map.keySet()) {
                    if (wrapper.weakMessage.get() == null) {
                        toRemove.add(wrapper);
                    }
                }
                
                for (MessageWrapper wrapper : toRemove) {
                    map.remove(wrapper);
                }
                
                return true;
            } finally {
                semaphore.release();
            }
        }
        return false;
    }
    
    /**
     * see {@link Map#size()}
     */
    public int size() {
        return map.size();
    }
    
    /**
     * see {@link Map#remove()}
     */
    public Operation remove(Object key) {
        return map.remove(key);
    }

    /**
     * see {@link Map#isEmpty()}
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    /**
     * see  {@link Map#get()}
     */
    public Operation get(Object key) {
        return map.get(key);
    }

}
