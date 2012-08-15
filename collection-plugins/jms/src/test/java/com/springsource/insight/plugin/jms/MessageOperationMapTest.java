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
package com.springsource.insight.plugin.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import javax.jms.Message;

import org.junit.Test;

import com.springsource.insight.intercept.operation.Operation;

public class MessageOperationMapTest {

    @Test
    public void basic() {
        MessageOperationMap map = new MessageOperationMap(1);
        
        Message message = mock(Message.class);
        
        Operation op = new Operation();
        MessageWrapper wrapper = MessageWrapper.instance(message);
        
        map.put(wrapper, op, "sig");
        assertEquals(1, map.size());
        
        Operation mapOp = map.get(MessageWrapper.instance(message)); 
        assertNotNull(mapOp);
        assertEquals(op, mapOp);
        assertTrue(map.isRelevant("sig", mapOp));
        
        map.remove(MessageWrapper.instance(message));
        
        assertTrue(map.isEmpty());
    }
    
    @Test
    public void testClean() {
        MessageOperationMap map = new MessageOperationMap(1);
        Message message = mock(Message.class);
        
        Operation op = new Operation();
        MessageWrapper wrapper = MessageWrapper.instance(message);
        
        map.put(wrapper, op, "sig");
        
        //GC message
        message = null;
        System.runFinalization();
        System.gc();
        
        message = mock(Message.class);
        wrapper = MessageWrapper.instance(message);
        
        map.put(wrapper, op, "sig");
        assertEquals(1, map.size());
        
        Operation mapOp = map.get(MessageWrapper.instance(message)); 
        assertNotNull(mapOp);
        assertEquals(op, mapOp);
        assertTrue(map.isRelevant("sig", mapOp));
    }

}
