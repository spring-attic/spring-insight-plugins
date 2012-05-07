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

import java.lang.ref.WeakReference;

import javax.jms.Message;

/**
 * A helper class that holds a {@link Message} and keeps a constant hash code.<br>
 * <br>
 * (durning tests we notice that for some jms implementations the message hash code before send <br>
 * is not the same as the message hash code after send)
 */
final class MessageWrapper {
    /**
     * Weak reference - to avoid memory leaks 
     */
    WeakReference<Message> weakMessage;
    
    /**
     * message hash code
     */
    private int messageHash = -1;
    
    /**
     * Creates a wrapper for a given {@code message}
     * 
     * @param message jms message
     */
    private MessageWrapper(Message message) {
        this.weakMessage = new WeakReference<Message>(message);
        this.messageHash = System.identityHashCode(message);
    }
    
    @Override
    public int hashCode() {
        return messageHash;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean e = this == obj;
        
        if (!e) {
            if (obj instanceof MessageWrapper) {
                MessageWrapper o = (MessageWrapper) obj;
                Message message = weakMessage.get();
                Message omessage = o.weakMessage.get();
                e = message != null &&  omessage != null && omessage == message;
            }
        }
        
        return e;
    }
    
    /**
     * A convenient way to create a wrapper
     *  
     * @param message jms message
     * @return wrapper for the given {@code message}
     */
    static MessageWrapper instance(Message message){
        return new MessageWrapper(message);
    }

}