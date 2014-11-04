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
package com.springsource.insight.plugin.jdbc.parser.parsers;
/* Copyright (c) (c) 2001-2005, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above Copyright (c) notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above Copyright (c) notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE Copyright (c) HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Properties;


/**
 * Wrapper for java.util.Properties to limit values to Specific types and
 * allow saving and loading.<p>
 *
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.0
 */
public class HsqlProperties {

    public static final int NO_VALUE_FOR_KEY = 1;
    protected String fileName;
    protected Properties stringProps;
    protected int[] errorCodes = new int[0];
    protected String[] errorKeys = new String[0];
    protected boolean resource = false;

    public HsqlProperties() {
        stringProps = new Properties();
        fileName = null;
    }

    public HsqlProperties(String name) {
        stringProps = new Properties();
        fileName = name;
    }

    public HsqlProperties(Properties props) {
        stringProps = props;
    }

    public void setFileName(String name) {
        fileName = name;
    }

    public String setProperty(String key, int value) {
        return setProperty(key, Integer.toString(value));
    }

    public String setProperty(String key, boolean value) {
        return setProperty(key, String.valueOf(value));
    }

    public String setProperty(String key, String value) {
        return (String) stringProps.put(key, value);
    }

    public String setPropertyIfNotExists(String key, String value) {
        String resProp = getProperty(key, value);
        return setProperty(key, resProp);
    }

    public Properties getProperties() {
        return stringProps;
    }

    public String getProperty(String key) {
        return stringProps.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return stringProps.getProperty(key, defaultValue);
    }

    public int getIntegerProperty(String key, int defaultValue) {
        String prop = getProperty(key);
        try {
            if (prop != null) {
                return Integer.parseInt(prop);
            } else {
                return defaultValue;
            }
        } catch (NumberFormatException e) {
            return (-1);    // signal the error
        }
    }


    public boolean isPropertyTrue(String key) {
        return isPropertyTrue(key, false);
    }

    public boolean isPropertyTrue(String key, boolean defaultValue) {

        String value = stringProps.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        return value.toLowerCase().equals("true");
    }

    public void removeProperty(String key) {
        stringProps.remove(key);
    }

    public void addProperties(Properties props) {

        if (props == null) {
            return;
        }

        Enumeration<?> keys = props.keys();

        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();

            this.stringProps.put(key, props.get(key));
        }
    }

    public void addProperties(HsqlProperties props) {

        if (props == null) {
            return;
        }

        addProperties(props.stringProps);
    }


    // copied from org.hsqldb.lib.ArrayUtil
    public static Object resizeArray(Object source, int newsize) {
        Object newarray = Array.newInstance(source.getClass().getComponentType(), newsize);
        int oldsize = Array.getLength(source), cpysize = newsize;
        if (oldsize < newsize) {
            cpysize = oldsize;
        }
        System.arraycopy(source, 0, newarray, 0, cpysize);
        return newarray;
    }

    /**
     * Adds the error code and the key to the list of errors. This list
     * is populated during construction or addition of elements and is used
     * outside this class to act upon the errors.
     */
    private void addError(int code, String key) {

        errorCodes = (int[]) resizeArray(errorCodes,
                errorCodes.length + 1);
        errorKeys = (String[]) resizeArray(errorKeys,
                errorKeys.length + 1);
        errorCodes[errorCodes.length - 1] = code;
        errorKeys[errorKeys.length - 1] = key;
    }

    /**
     * Creates and populates an HsqlProperties Object from the arguments
     * array of a Main method. Properties are in the form of "-key value"
     * pairs. Each key is prefixed with the type argument and a dot before
     * being inserted into the properties Object. <p>
     * <p/>
     * "-?" is treated as a key with no value and not inserted.
     */
    public static HsqlProperties argArrayToProps(String[] arg, String type) {

        HsqlProperties props = new HsqlProperties();

        for (int i = 0; i < arg.length; i++) {
            String p = arg[i];

            if (p.startsWith("-?")) {
                props.addError(NO_VALUE_FOR_KEY, p.substring(1));
            } else if (p.charAt(0) == '-') {
                props.setProperty(type + "." + p.substring(1), arg[i + 1]);

                i++;
            }
        }

        return props;
    }

    /**
     * Creates and populates a new HsqlProperties Object using a string
     * such as "key1=value1;key2=value2". <p>
     * <p/>
     * The string that represents the = sign above is specified as pairsep
     * and the one that represents the semicolon is specified as delimiter,
     * allowing any string to be used for either.<p>
     * <p/>
     * Leading / trailing spaces around the keys and values are discarded.<p>
     * <p/>
     * The string is parsed by (1) subdividing into segments by delimiter
     * (2) subdividing each segment in two by finding the first instance of
     * the pairsep (3) trimming each pair of segments from step 2 and
     * inserting into the properties object.<p>
     * <p/>
     * Each key is prefixed with the type argument and a dot before being
     * inserted.<p>
     * <p/>
     * Any key without a value is added to the list of errors.
     */
    public static HsqlProperties delimitedArgPairsToProps(String s,
                                                          String pairsep, String dlimiter, String type) {

        HsqlProperties props = new HsqlProperties();
        int currentpair = 0;

        while (true) {
            int nextpair = s.indexOf(dlimiter, currentpair);

            if (nextpair == -1) {
                nextpair = s.length();
            }

            // find value within the segment
            int valindex = s.substring(0, nextpair).indexOf(pairsep,
                    currentpair);

            if (valindex == -1) {
                props.addError(NO_VALUE_FOR_KEY,
                        s.substring(currentpair, nextpair).trim());
            } else {
                String key = s.substring(currentpair, valindex).trim();
                String value = s.substring(valindex + pairsep.length(),
                        nextpair).trim();

                if (type != null) {
                    key = type + "." + key;
                }

                props.setProperty(key, value);
            }

            if (nextpair == s.length()) {
                break;
            }

            currentpair = nextpair + dlimiter.length();
        }

        return props;
    }

    public Enumeration<?> propertyNames() {
        return stringProps.propertyNames();
    }

    public boolean isEmpty() {
        return stringProps.isEmpty();
    }

    public String[] getErrorKeys() {
        return errorKeys;
    }
    /*
    public static void main(String[] argv) {

        HsqlProperties props1 = delimitedArgPairsToProps("-dbname.0", "=",
            ";", "server");
        HsqlProperties props2 = delimitedArgPairsToProps(
            "filename.cvs;a=123 ;  b=\\delta ;c= another; derrorkey;", "=",
            ";", "textdb");
    }
	 */
}
