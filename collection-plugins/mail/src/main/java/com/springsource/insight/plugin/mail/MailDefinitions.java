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
package com.springsource.insight.plugin.mail;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.springsource.insight.intercept.operation.OperationType;

/**
 * 
 */
public final class MailDefinitions {
	private MailDefinitions() {
		// no instance
	}

	public static final OperationType   SEND_OPERATION=OperationType.valueOf("javax.mail.send");
	// useful operation attributes
	public static final String  SEND_PROTOCOL="protocol",
	SEND_HOST="host",
	SEND_PORT="port",
	SEND_DETAILS="details",
	SEND_SUBJECT="subject",
	SEND_SENDERS="senders",
	SEND_RECIPS="recipients"; 

	public static final Map<String, Integer> protocolToPortMap = 
		Collections.unmodifiableMap(new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER)
				{
                    private static final long serialVersionUID = 1L;

                    {
						put("SMTP", Integer.valueOf(25));
						put("IMAP4", Integer.valueOf(143));
						put("POP3", Integer.valueOf(110));
					}
				}
		);
}
