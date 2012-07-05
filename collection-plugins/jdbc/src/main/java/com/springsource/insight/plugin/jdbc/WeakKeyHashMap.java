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
package com.springsource.insight.plugin.jdbc;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to store PreparedStatements and associated JdbcOperations in memory
 * via WeakReference.
 *
 * If a user calls connection.prepareStatement() and never calls execute(),
 * the prepared statement must still be purged from memory.
 * 
 * This class provides thread-safe, synchronized access.
 * 
 * You must be confident that these keys and values are not purged
 * too early. See the notes on their use in other classes to verify this
 * behavior.
 *
 * We may also be able to replace this with the standard WeakHashMap
 * implementation, though that implementation should then be synchronized.
 */
public class WeakKeyHashMap<K, V> {
    private final ConcurrentHashMap<WeakRef<K, V>, WeakRef<K, V>> map = new ConcurrentHashMap<WeakRef<K, V>, WeakRef<K, V>>();
    private final ReferenceQueue<V> refQueue = new ReferenceQueue<V>();

    public int size() {
        return map.size();
    }
    
    public void put(K key, V val) {
        WeakRef<K, V> newRef = new WeakRef<K, V>(key, refQueue, val);
        WeakRef<K, V> oldRef = map.put(newRef, newRef);
        if (oldRef != null) {
            // System.out.println("** Overwrote old reference to statement=" + dumpObj(key));
        }
        cleanupRefQueue();
    }
    
    public V get(K key) {
        WeakRef<K, V> lookupRef = new WeakRef<K, V>(key, refQueue, null);
        WeakRef<K, V> ref = map.get(lookupRef);
        if (ref == null) {
            // Reference lost
            // System.out.println("** Lost reference to statement=" + dumpObj(key));
            return null;
        } else {
            return ref.getHardRef();
        }
    }

    public void remove(K key) {
        WeakRef<K, V> lookupRef = new WeakRef<K, V>(key, refQueue, null);
        WeakRef<K, V> ref = map.remove(lookupRef);
        if (ref == null) {
            // System.out.println("** Reference no longer available anyway to statement=" + dumpObj(key));
        }
    }

    private void cleanupRefQueue() {
        @SuppressWarnings("rawtypes")
		Reference ref;
        while ((ref = refQueue.poll()) != null) {
            @SuppressWarnings("unchecked")
			WeakRef<K, V> softRef = (WeakRef<K, V>)ref;
            if (map.remove(softRef) == null) {
                // System.out.println("Remove cleared ref, but it wasn't found in our map!");
            }
        }
    }

    public static String dumpObj(Object obj) {
        return obj.getClass() + ":" + System.identityHashCode(obj);
    }
    
    private static class WeakRef<A, B> extends WeakReference<A> {
        private final int hashCode;
        private final B hardRef;
        
        @SuppressWarnings({ "unchecked", "rawtypes"})
		public WeakRef(A obj, ReferenceQueue queue, B hardReference) {
            super(obj, queue);
            this.hashCode = System.identityHashCode(obj);
            this.hardRef = hardReference;
        }
        
        public B getHardRef() {
            return hardRef;
        }
        
        @Override
        public int hashCode() {
            return hashCode;
        }
        
        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            @SuppressWarnings("rawtypes")
			WeakRef o = (WeakRef)other;
            return o.get() == get();
        }
    }
}
