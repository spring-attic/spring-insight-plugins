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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.util.StringFormatterUtils;
import com.springsource.insight.util.StringUtil;

/**
 * Utilities for converting method arguments for MongoDB-related operations
 */
public final class MongoArgumentUtils {
	private MongoArgumentUtils () {
		throw new UnsupportedOperationException("No instance");
	}
    /**
     * The maximum length of a string we generate
     */
    public static final int MAX_STRING_LENGTH = 256;

    /**
     * These classes can just be converted willy-nilly
     */
    private static final Class<?>[] SIMPLE_CLASSES = new Class<?>[]{
            String.class, Boolean.class, Byte.class, Character.class,
            Short.class, Integer.class, Long.class, Float.class, Double.class,
            BigInteger.class, BigDecimal.class};

    /**
     * With care, we can treat these MongoDB classes simply too
     */
    private static final Class<?>[] SIMPLE_MONGO_CLASSES = new Class<?>[]{
            ObjectId.class, CommandResult.class, BasicDBList.class,
            BasicDBObject.class, DBCollection.class, WriteConcern.class,
            WriteResult.class};

    /**
     * A little helper interface to convert an {@link Object} to a
     * {@link String}
     *
     * @param <T> Type of object being string-ified
     */
    private interface StringForm<T> {
        /**
         * @param object guaranteed non-null
         * @return The {@link String} representation of the object
         */
        String stringify(T object);
    }

    /**
     * Take an object from one of the "safe" classes, convert to a
     * {@link String} and trim the result, perhaps using ellipses if we truncate
     * it
     */
    public static final StringForm<Object> DefaultStringForm = new StringForm<Object>() {
        public String stringify(final Object object) {
            return object.toString();
        }
    };

    /**
     * For a {@link DBCursor}, we get the {@link DBCollection} name, the query
     * and the keys wanted
     */
    public static final StringForm<DBCursor> DBCursorStringForm = new StringForm<DBCursor>() {
        public String stringify(final DBCursor cursor) {
            return "DBCursor(" + MongoArgumentUtils.toString(cursor.getQuery()) + ", "
                    + MongoArgumentUtils.toString(cursor.getKeysWanted()) + ")";
        }
    };

    /**
     * This type is common for inserts. In fact, even a single insert gets
     * converted to a {@link DBObject}[]
     */
    public static final StringForm<DBObject[]> DBObjectArrayStringForm = new StringForm<DBObject[]>() {
        public String stringify(final DBObject[] array) {
            return "DBObject" + MongoArgumentUtils.toString(array);
        }
    };

    /**
     * A map from a {@link Class} to a helper ({@link StringForm}) that returns
     * a suitable {@link String} value
     * <p/>
     * You'll get used to this style of object creation if you stare at it long
     * enough. It's handy because you don't need to mention the name of the
     * variable (STRING_FORM_MAP) in any of the put() calls, which you'd have to
     * if you did it long hand.
     */
    @SuppressWarnings("synthetic-access")
	public static final Map<Class<?>, StringForm<? extends Object>> STRING_FORM_MAP = new HashMap<Class<?>, StringForm<? extends Object>>() {
		private static final long serialVersionUID = 1L;

		{
            // Wrapper classes
            //
            for (Class<?> cls : SIMPLE_CLASSES) {
                put(cls, DefaultStringForm);
            }

            // MongoDB classes
            //
            for (Class<?> cls : SIMPLE_MONGO_CLASSES) {
                put(cls, DefaultStringForm);
            }

            put(DBCursor.class, DBCursorStringForm);
            put(DBObject[].class, DBObjectArrayStringForm);
        }
    };

    public static List<String> toString(final Object[] array) {
        return toString(array, MAX_STRING_LENGTH);
    }

    /**
     * Convert an {@link Object}[] to a {@link String}. Don't convert more than
     * MAX_ARGS arguments and don't make it more than roughly maxLength long.
     * <p/>
     * Append ellipses to any argument we truncate, or to the whole array if
     * it's too long.
     *
     * @param array
     * @param maxLength
     * @return
     */
    public static List<String> toString(final Object[] array, final int maxLength) {
        return new ArrayList<String>() {
			private static final long serialVersionUID = 1L;

			{
                int soFar = 0;

                for (final Object arg : array) {
                    final String result=MongoArgumentUtils.toString(arg, maxLength - soFar);

                    soFar += result.length();

                    add(result);

                    if (soFar >= maxLength) {
                        break;
                    }
                }
            }
        };
    }

    public static String toString(final Object object) {
        return toString(object, MAX_STRING_LENGTH);
    }

    /**
     * Primitives and "safe" types get a call to {@link #toString()} via the
     * {@link StringForm} helper class; everything else is just the class name.
     *
     * @param object
     * @return
     */
    public static String toString(final Object object, final int maxLength) {
        if (object == null) {
            return StringFormatterUtils.NULL_VALUE_STRING;
        }

        Class<? extends Object> cls = object.getClass();
        @SuppressWarnings("unchecked")
        StringForm<Object> stringForm = (StringForm<Object>) STRING_FORM_MAP.get(cls);

        if (stringForm != null) {
            return StringUtil.chopTailAndEllipsify(stringForm.stringify(object), maxLength);
        }

        return cls.getSimpleName();
    }

    public static String toString(final DBObject dbObject) {
        return dbObject == null ? null : chopTailAndEllipsify(dbObject.toString());
    }

    public static String chopTailAndEllipsify(final String string) {
        return StringUtil.chopTailAndEllipsify(string, MAX_STRING_LENGTH);
    }

    public static Operation putDatabaseDetails (Operation op, DB db) {
    	op.put("dbName", db.getName());
    	
    	Mongo			mongo=db.getMongo();
    	ServerAddress	address=mongo.getAddress();
		op.put("host", address.getHost());
		op.put("port", address.getPort());
		return op;
    }
}
