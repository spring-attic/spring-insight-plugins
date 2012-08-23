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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;

/**
 * 
 */
public class MessageSendOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
	/*
	 * NOTE: we use a port other than the default for SMTP (25) to avoid conflicts
	 *  with any running services on the build environment
	 */
	private static final int    TEST_PORT=7365;
	private static SMTPServer   SERVER;

	@BeforeClass
	public static void startEmbeddedServer () {
		assertNull("Server already initialized", SERVER);
		SERVER = new SMTPServer(new SimpleMessageListenerAdapter(SmtpServerListener.INSTANCE));
		SERVER.setHostName("localhost");
		SERVER.setPort(TEST_PORT);
		SERVER.start();
		System.out.println("Started embedded server on port " + SERVER.getPort());
	}

	@AfterClass
	public static void stopEmbeddedServer () {
		if (SERVER != null) {
			SERVER.stop();
			System.out.println("Stopped embedded server on port " + SERVER.getPort());
			SERVER = null;
		}
	}

	// cant test this because the transport will actually use the 25 port which will raise an exception
	//@Test
	public void testSendMessageWithDefaultPort(){
		testSendMessage(-1);
	}

	@Test
	public void testSendMessageWithPort(){
		testSendMessage(TEST_PORT);
	}

	private void testSendMessage (int port) {
		JavaMailSenderImpl  sender;
		sender = new JavaMailSenderImpl();
		sender.setProtocol(JavaMailSenderImpl.DEFAULT_PROTOCOL);
		sender.setPort(port);

		SimpleMailMessage   message=new SimpleMailMessage();
		message.setFrom("from@com.springsource.insight.plugin.mail");
		message.setTo("to@com.springsource.insight.plugin.mail");
		message.setCc("cc@com.springsource.insight.plugin.mail");
		message.setBcc("bcc@com.springsource.insight.plugin.mail");

		Date    now=new Date(System.currentTimeMillis());
		message.setSentDate(now);
		message.setSubject(now.toString());
		message.setText("Test at " + now.toString());
		sender.send(message);

		Operation op = getLastEntered();
		assertNotNull("No operation extracted", op);
		assertEquals("Mismatched operation type", MailDefinitions.SEND_OPERATION, op.getType());
		assertEquals("Mismatched protocol", sender.getProtocol(), op.get(MailDefinitions.SEND_PROTOCOL, String.class));
		assertEquals("Mismatched host", sender.getHost(), op.get(MailDefinitions.SEND_HOST, String.class));
		if (port == -1) {
			assertEquals("Mismatched port", 25, op.get(MailDefinitions.SEND_PORT, Number.class).intValue());
		}
		else {
			assertEquals("Mismatched port", sender.getPort(), op.get(MailDefinitions.SEND_PORT, Number.class).intValue());
		}

		if (getAspect().collectExtraInformation()) {
			assertAddresses(op, MailDefinitions.SEND_SENDERS, 1);
			assertAddresses(op, MailDefinitions.SEND_RECIPS, 3);

			OperationMap    details=op.get(MailDefinitions.SEND_DETAILS, OperationMap.class);
			assertNotNull("No details extracted", details);
			assertEquals("Mismatched subject", message.getSubject(), details.get(MailDefinitions.SEND_SUBJECT, String.class));
		}
	}

	private void assertAddresses (Operation op, String type, int expectedSize) {
		OperationList   list=op.get(type, OperationList.class);
		assertNotNull("No addresses of type=" + type, list);
		assertEquals("Mismatched number of " + type + " addresses", expectedSize, list.size());

		for (int    index=0; index < list.size(); index++) {
			OperationMap    addrEntry=list.get(index, OperationMap.class);
			assertNotNull("Missing " + type + " entry #" + index, addrEntry);

			String  typeValue=addrEntry.get("type", String.class),
			addrValue=addrEntry.get("value", String.class);
			assertEquals("Mismatched " + type + " type for entry #" + index, "rfc822", typeValue);
			assertNotNull("No " + type + " value for entry #" + index, addrValue);

			int     domainIndex=addrValue.lastIndexOf('@');
			String  domain=addrValue.substring(domainIndex);
			assertEquals("Mismatched " + type + "domain for entry #" + index,
					"@com.springsource.insight.plugin.mail", domain);
		}
	}

	@Override
	public MessageSendOperationCollectionAspect getAspect() {
		return MessageSendOperationCollectionAspect.aspectOf();
	}

	static class SmtpServerListener implements SimpleMessageListener {
		static final SmtpServerListener INSTANCE=new SmtpServerListener();
		private SmtpServerListener () {
			super();
		}

		public boolean accept(String from, String recipient) {
			return ((from != null) && (from.length() > 0))
			|| ((recipient != null) && (recipient.length() > 0))
			;
		}

		public void deliver(String from, String recipient, InputStream data)
		throws TooMuchDataException, IOException {
			assertTrue("Bad data copied", IOUtils.copy(data, NullOutputStream.NULL_OUTPUT_STREAM) > 0);
		}
	}
}
