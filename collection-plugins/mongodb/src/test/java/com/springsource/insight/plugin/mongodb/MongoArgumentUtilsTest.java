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

package com.springsource.insight.plugin.mongodb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

import org.bson.types.ObjectId;

import static org.junit.Assert.*;

import org.junit.Test;

public class MongoArgumentUtilsTest {

    @Test
    public void testLongStringTruncatedToLimit() {
        String bigRandom = new BigInteger(2048, new Random()).toString(32);
        assertEquals(bigRandom.substring(0, 256) + "...", MongoArgumentUtils.toString(bigRandom));
    }

    @Test
    public void testNullToString() {
        assertEquals("null", MongoArgumentUtils.toString((Object) null));
    }

    @Test
    public void testStringValue() {
        assertEquals("Slartibartfast",
                MongoArgumentUtils.toString("Slartibartfast"));
    }

    @Test
    public void testBooleanValue() {
        assertEquals("true", MongoArgumentUtils.toString(Boolean.TRUE));
    }

    @Test
    public void testByteValue() {
        assertEquals("42", MongoArgumentUtils.toString((byte) 42));
    }

    @Test
    public void testCharacterValue() {
        assertEquals("A", MongoArgumentUtils.toString('A'));
    }

    @Test
    public void testShortValue() {
        assertEquals("42", MongoArgumentUtils.toString((short) 42));
    }

    @Test
    public void testIntegerValue() {
        assertEquals("42", MongoArgumentUtils.toString(42));
    }

    @Test
    public void testLongValue() {
        assertEquals("42", MongoArgumentUtils.toString((long) 42));
    }

    @Test
    public void testFloatValue() {
        assertEquals("42.0", MongoArgumentUtils.toString((float) 42.0));
    }

    @Test
    public void testDoubleValue() {
        assertEquals("42.0", MongoArgumentUtils.toString(42.0));
    }

    @Test
    public void testBigIntegerValue() {
        assertEquals("42424242",
                MongoArgumentUtils.toString(new BigInteger("42424242")));
    }

    @Test
    public void testBigDecimalValue() {
        assertEquals("42.424242",
                MongoArgumentUtils.toString(new BigDecimal("42.424242")));
    }

    @Test
    public void testObjectIdValue() {
        assertEquals("0123456789abcd0123456789",
                MongoArgumentUtils.toString(new ObjectId("0123456789abcd0123456789")));
    }

    @Test
    public void testUnknownClass() {
        assertEquals("Random", MongoArgumentUtils.toString(new Random()));
    }
}
