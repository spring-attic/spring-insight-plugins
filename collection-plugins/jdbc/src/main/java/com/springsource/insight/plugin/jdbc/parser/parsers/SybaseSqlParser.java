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

import java.util.Collections;
import java.util.List;

import com.springsource.insight.plugin.jdbc.parser.AbstractSqlParser;
import com.springsource.insight.plugin.jdbc.parser.JdbcUrlMetaData;
import com.springsource.insight.plugin.jdbc.parser.SimpleJdbcUrlMetaData;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringUtil;

/**
 * @see <A HREF="https://www.razorsql.com/docs/help_sybase.html">Sybase JDBC Driver and URL Information</A>
 */
public class SybaseSqlParser extends AbstractSqlParser {
	public static final String	VENDOR="sybase", SUB_TYPE="Tds";
	protected static final String	SERVICE_NAME_ATTR="ServiceName=";
	public SybaseSqlParser () {
		super(VENDOR);
	}

    public List<JdbcUrlMetaData> parse(String connectionUrl, String vendorName) {
		String	url=stripUrlPrefix(connectionUrl);
		if (StringUtil.isEmpty(url)) {
			return Collections.emptyList();
		}

		String[]	parts=url.split(":");
		if (ArrayUtil.length(parts) <= 0) {
			return Collections.emptyList();
		}

		String	host=parts[0];
		String	portValue=getDefaultPortString();
		String	dbName=getDefaultDatbaseName(), svcName=null;
		String	portPart=(parts.length > 1) ? parts[1] : null;
		if (StringUtil.isEmpty(portPart)) {
			int	sep=host.indexOf('?');	// check if have any extensions
			if (sep > 0) {
				svcName = extractServiceName(host);
				host = host.substring(0, sep).trim();
			} else {
				svcName = extractServiceName(url);
			}
		} else {
			int	sep=portPart.indexOf('?');	// check if have any extensions
			if (sep > 0) {
				svcName = extractServiceName(portPart);
				portValue = portPart.substring(0, sep).trim();
			} else {
				portValue = portPart;
			}
		}

		int	port=parsePort(connectionUrl, portValue);
		if (!StringUtil.isEmpty(svcName)) {
			dbName = svcName;
		}

		return Collections.<JdbcUrlMetaData>singletonList(new SimpleJdbcUrlMetaData(host, port, dbName, connectionUrl, vendorName));
	}

	@Override
	protected String createUrlPrefix(String vendor) {
		return super.createUrlPrefix(vendor) + SUB_TYPE + ":";
	}

	protected String extractServiceName (String connectionUrl) {
    	if (StringUtil.isEmpty(connectionUrl)) {
    		return null;
    	}

    	int	namePos=connectionUrl.indexOf(SERVICE_NAME_ATTR);
    	if (namePos <= 0) {
    		return null;
    	}

    	char	chPrev=connectionUrl.charAt(namePos - 1);
    	if (":?;".indexOf(chPrev) < 0) {
    		return null;	// make sure preceded by a valid separator
    	}

    	String	remainAttrs=connectionUrl.substring(namePos + SERVICE_NAME_ATTR.length()).trim();
    	if (StringUtil.isEmpty(remainAttrs)) {
    		return null;
    	}

    	// check if more following attributes
    	for (int	pos=0; pos < remainAttrs.length(); pos++) {
    		char	tch=remainAttrs.charAt(pos);
    		if (":?; &".indexOf(tch) >= 0) {
    			return (pos == 0) ? null : remainAttrs.substring(0, pos);
    		}
    	}

    	return remainAttrs;
    }
}
