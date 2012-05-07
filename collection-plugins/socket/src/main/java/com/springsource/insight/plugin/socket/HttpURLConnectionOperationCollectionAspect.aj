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
package com.springsource.insight.plugin.socket;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationUtils;
import com.springsource.insight.intercept.plugin.CollectionSettingName;
import com.springsource.insight.intercept.plugin.CollectionSettingsRegistry;
import com.springsource.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.springsource.insight.intercept.trace.ObscuredValueMarker;

/**
 * Note: this aspect covers also {@link javax.net.ssl.HttpsURLConnection} since
 * it is derived from {@link HttpURLConnection}
 */
public aspect HttpURLConnectionOperationCollectionAspect
        extends SocketOperationCollectionAspectSupport {
    /**
     * A comma-separated list of headers to be obscured - default={@link #DEFAULT_OBFUSCATED_HEADERS_LIST}
     */
    protected static final CollectionSettingName    OBFUSCATED_HEADERS_SETTING =
            new CollectionSettingName("obfuscated.headers", "socket", "Comma separated list of HTTP headers whose data requires obfuscation");
    /* see RFC(s) 2616-2617
     * NOTE: the Authorization header is not obfuscated since it is a response
     * header and we do not extract them since there are too many ways responses
     * may be handled in a HttpURLConnection and our aspect might interfere with
     * the user's code... 
     */
    static final String DEFAULT_OBFUSCATED_HEADERS_LIST="Authentication-Info,WWW-Authenticate";
    // NOTE: using a synchronized set in order to allow modification while running
    static final Set<String>    OBFUSCATED_HEADERS=
            Collections.synchronizedSet(new TreeSet<String>(String.CASE_INSENSITIVE_ORDER) {
                private static final long serialVersionUID = 1L;

                {
                    addAll(toHeaderNameSet(DEFAULT_OBFUSCATED_HEADERS_LIST));
                }
            });

    // register a collection setting update listener to update the obfuscated headers
    static {
        CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
        registry.addListener(new CollectionSettingsUpdateListener() {
                public void incrementalUpdate (CollectionSettingName name, Serializable value) {
                   Logger   LOG=Logger.getLogger(HttpURLConnectionOperationCollectionAspect.class.getName());
                   if (OBFUSCATED_HEADERS_SETTING.equals(name) && (value instanceof String)) {
                       if (OBFUSCATED_HEADERS.size() > 0) { // check if replacing or populating
                           LOG.info("incrementalUpdate(" + name + ")" + OBFUSCATED_HEADERS + " => [" + value + "]");
                           OBFUSCATED_HEADERS.clear();
                       }

                       OBFUSCATED_HEADERS.addAll(toHeaderNameSet((String) value));
                   } else if (LOG.isLoggable(Level.FINE)) {
                       LOG.fine("incrementalUpdate(" + name + ")[" + value + "] ignored");
                   }
                }
            });
    }

    public HttpURLConnectionOperationCollectionAspect () {
        super();
    }

    // we have to use 'call' since HttpURLConnection is a core class
    public pointcut collectionPoint() : call(* HttpURLConnection+.connect());

    @Override
    protected Operation createOperation(JoinPoint jp) {
        HttpURLConnection   conn=(HttpURLConnection) jp.getTarget();
        URL                 url=conn.getURL();
        String              host=url.getHost();
        int                 port=url.getPort();
        if (port <= 0) {
            port = url.getDefaultPort();
        }

        Operation   op=createOperation(super.createOperation(jp), SocketDefinitions.CONNECT_ACTION, host, port);
        op.put("method", conn.getRequestMethod())
          .put(OperationFields.URI, url.toExternalForm())
          ;
        if (collectExtraInformation()) {
            fillInMessageHeaders(op.createList("request"), conn.getRequestProperties());
        }
        
        return op;
    }
    
    OperationList fillInMessageHeaders (OperationList headers, Map<String,? extends Collection<String>> hdrsMap) {
        if ((hdrsMap == null) || hdrsMap.isEmpty()) {
            return headers;
        }
 
        SocketCollectOperationContext   context=getSocketCollectOperationContext();
        ObscuredValueMarker             obscuredMarker=context.getObscuredValueMarker();
        for (Map.Entry<String,? extends Collection<String>> hdrEntry : hdrsMap.entrySet()) {
            String              name=hdrEntry.getKey();
            Collection<String>  valsList=hdrEntry.getValue();
            if ((valsList == null) || valsList.isEmpty()) {
                continue;
            }

            for (String value : valsList) {
                if ((value == null) || (value.length() <= 0)) {
                    continue;
                }

                if (OBFUSCATED_HEADERS.contains(name)) {
                    obscuredMarker.markObscured(value);
                }

                OperationUtils.addNameValuePair(headers, name, value);
            }
        }
        
        return headers;
    }

    static Set<String> toHeaderNameSet (String value) {
        Set<String> result=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        String[]    names=value.split(",");
        for (String headerName : names) {
            String   trimmedValue=headerName.trim(); // in case extra whitespace
            if (trimmedValue.length() > 0) {
                result.add(trimmedValue);
            }
        }
        return result;
    }
}
