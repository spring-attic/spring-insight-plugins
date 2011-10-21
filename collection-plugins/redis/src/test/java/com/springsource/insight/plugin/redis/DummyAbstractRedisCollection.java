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

package com.springsource.insight.plugin.redis;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.support.collections.AbstractRedisCollection;

/**
 * Dummy implementation of AbstractRedisCollection for testing
 */
public class DummyAbstractRedisCollection<E> extends AbstractRedisCollection<E> {

    public DummyAbstractRedisCollection() {
        super(null, null);
    }

    public boolean add(E e) {
        return false;
    }

    public boolean addAll(Collection<? extends E> collection) {
        return false;
    }

    public boolean remove(Object object) {
        return false;
    }

    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    public void clear() {
    }

    public Iterator<E> iterator() {
        return null;
    }

    public int size() {
        return 1;
    }

    public DataType getType() {
        return null;
    }
}
