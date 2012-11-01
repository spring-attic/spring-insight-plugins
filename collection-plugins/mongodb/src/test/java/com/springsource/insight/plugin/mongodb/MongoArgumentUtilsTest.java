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

package com.springsource.insight.plugin.mongodb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.springsource.insight.collection.test.AbstractCollectionTestSupport;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;

public class MongoArgumentUtilsTest extends AbstractCollectionTestSupport {
	public MongoArgumentUtilsTest () {
		super();
	}

    @Test
    public void testLongStringTruncatedToLimit() {
        String 	bigRandom = new BigInteger(2048, new Random()).toString(32);
        String	argVal = MongoArgumentUtils.toString(bigRandom);
        assertEquals("Mismatched result length", MongoArgumentUtils.MAX_STRING_LENGTH, StringUtil.getSafeLength(argVal));
        assertTrue("Not ellipsified", argVal.endsWith(StringUtil.ELLIPSIS));
    }

    @Test
    public void testNullToString() {
        assertEquals(StringFormatterUtils.NULL_VALUE_STRING, MongoArgumentUtils.toString((Object) null));
    }

    @Test
    public void testStringValue() {
    	final String	value="Slartibartfast";
        assertEquals(value, MongoArgumentUtils.toString(value));
    }

    @Test
    public void testBooleanValue() {
        assertEquals(Boolean.TRUE.toString(), MongoArgumentUtils.toString(Boolean.TRUE));
    }

	@Test
    public void testByteValue() {
		Byte	value=Byte.valueOf((byte) 42);
        assertEquals(value.toString(), MongoArgumentUtils.toString(value));
    }

	@Test
    public void testCharacterValue() {
		Character	value=Character.valueOf('A');
        assertEquals(value.toString(), MongoArgumentUtils.toString(value));
    }

	@Test
    public void testShortValue() {
		Short	value=Short.valueOf((short) 42);
        assertEquals(value.toString(), MongoArgumentUtils.toString(value));
    }

	@Test
    public void testIntegerValue() {
		Integer	value=Integer.valueOf(42);
        assertEquals(value.toString(), MongoArgumentUtils.toString(value));
    }

	@Test
    public void testLongValue() {
		Long	value=Long.valueOf(42L);
        assertEquals(value.toString(), MongoArgumentUtils.toString(value));
    }

	@Test
    public void testFloatValue() {
		Float	value=Float.valueOf(42.0f);
        assertEquals(value.toString(), MongoArgumentUtils.toString(value));
    }

	@Test
    public void testDoubleValue() {
		Double	value=Double.valueOf(42.0d);
        assertEquals(value.toString(), MongoArgumentUtils.toString(value));
    }

    @Test
    public void testBigIntegerValue() {
    	String	value="42424242";
        assertEquals(value,  MongoArgumentUtils.toString(new BigInteger(value)));
    }

    @Test
    public void testBigDecimalValue() {
    	String	value="42.424242";
        assertEquals(value, MongoArgumentUtils.toString(new BigDecimal(value)));
    }

    @Test
    public void testObjectIdValue() {
    	String value="0123456789abcd0123456789";
        assertEquals(value, MongoArgumentUtils.toString(new ObjectId(value)));
    }

    @Test
    public void testUnknownClass() {
        assertEquals(Random.class.getSimpleName(), MongoArgumentUtils.toString(new Random()));
    }
}
