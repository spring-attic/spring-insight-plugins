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

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.support.collections.RedisMap;

/**
 * Dummy implementation of RedisMap
 */
public class DummyRedisMapImpl<K,V> extends ConcurrentHashMap<K,V> implements RedisMap<K, V> {


    public V put(K key, V value) {
        return null;
    }

    public V putIfAbsent(K key, V value) {
        return null;
    }

    public void putAll(Map<? extends K, ? extends V> map) {
    }

    public V replace(K key, V value) {
        return null;
    }

    public boolean replace(K key, V oldValue, V newValue) {
        return false;
    }

    public V get(Object key) {
        return null;
    }

    public V remove(Object key) {
        return null;
    }

    public boolean remove(Object key, Object value) {
        return false;
    }

    public Long increment(K key, long delta) {
        return null;
    }

    public org.springframework.data.redis.core.RedisOperations<java.lang.String,?> getOperations() {
        return null;
    }

    public Boolean expire(long timeout, TimeUnit unit) {
        return Boolean.FALSE;
    }

    public String getKey() {
        return null;
    }

    public Boolean expireAt(Date date) {
        return null;
    }

    public Long getExpire() {
        return null;
    }

    public DataType getType() {
        return null;
    }

    public Boolean persist() {
        return Boolean.TRUE;
    }

    public void rename(String k) {
    }
}
