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
package com.springsource.insight.plugin.ldap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationFields;
import com.springsource.insight.intercept.topology.AbstractExternalResourceAnalyzer;
import com.springsource.insight.intercept.topology.ExternalResourceDescriptor;
import com.springsource.insight.intercept.topology.ExternalResourceType;
import com.springsource.insight.intercept.topology.MD5NameGenerator;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.ListUtil;
import com.springsource.insight.util.StringUtil;

/**
 * 
 */
public class LdapExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {
	private static final LdapExternalResourceAnalyzer	INSTANCE=new LdapExternalResourceAnalyzer();

    private LdapExternalResourceAnalyzer() {
        super(LdapDefinitions.LDAP_OP);
    }

    public static final LdapExternalResourceAnalyzer getInstance() {
    	return INSTANCE;
    }

    public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> framesList) {
        if (ListUtil.size(framesList) <= 0) {
            return Collections.emptyList();
        }
        
        Set<ExternalResourceDescriptor>    result=
                new HashSet<ExternalResourceDescriptor>(framesList.size());
        for (Frame frame : framesList) {
            ExternalResourceDescriptor  res=extractExternalResourceDescriptor(frame);
            if (res == null) {  // can happen if failed to parse the URI somehow
                continue;
            }
            
            if (!result.add(res))
                continue;   // debug breakpoint
        }

        return result;
    }

    ExternalResourceDescriptor extractExternalResourceDescriptor (Frame frame) {
        Operation   op=frame.getOperation();
        String      uriValue=op.get(OperationFields.CONNECTION_URL, String.class);
        if (StringUtil.getSafeLength(uriValue) <= 0) {
            return null;
        }

        try
        {
            URI uri=new URI(uriValue);
            String color = colorManager.getColor(op);
            
            return new ExternalResourceDescriptor(
                            frame,
                            MD5NameGenerator.getName(uriValue),
                            uriValue,    // label
                            ExternalResourceType.LDAP.name(),
                            uriValue,     // vendor
                            uri.getHost(),
                            resolvePort(uri),
                            color, false);    
        } catch(URISyntaxException e) {
        	 Logger    logger=Logger.getLogger(getClass().getName());
			 logger.warning("Failed to parse " + uriValue + ": " + e.getMessage());
            return null;
        }
    }

    static int resolvePort (URI uri) {
        if (uri == null) {
            return (-1);
        }

        int port=uri.getPort();
        if (port <= 0) {
            return 389;
        }

        return port;
    }
}
