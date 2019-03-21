/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.mail;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import com.springsource.insight.collection.test.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.ArrayUtil;

/**
 *
 */
public class JavaMailSenderOperationCollectionAspectTest extends OperationCollectionAspectTestSupport {
    private TestJavaMailSender sender;

    public JavaMailSenderOperationCollectionAspectTest() {
        super();
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        sender = new TestJavaMailSender();
    }

    @Test
    public void testSendSimpleMessageInstance() throws Exception {
        SimpleMailMessage msg = createSimpleMailMessage("testSendSimpleMessageInstance", "from@a", "to@a", "cc@a", "bcc@a", "replyTo@a");
        sender.send(msg);
        assertSendOperation(msg);
    }

    @Test
    public void testSendSimpleMessageArray() throws Exception {
        SimpleMailMessage msg = createSimpleMailMessage("testSendSimpleMessageArray", "from@a", "to@a", "cc@a", "bcc@a", "replyTo@a");
        sender.send(new SimpleMailMessage[]{msg});
        assertSendOperation(msg);
    }

    @Override
    public JavaMailSenderOperationCollectionAspect getAspect() {
        return JavaMailSenderOperationCollectionAspect.aspectOf();
    }

    protected Operation assertSendOperation(SimpleMailMessage msg) {
        Operation op = getLastEntered();
        assertNotNull("No operation collected", op);
        assertEquals("Mismatched operation type", MailDefinitions.SEND_OPERATION, op.getType());
        assertEquals("Mismatched label", JavaMailSenderOperationCollectionAspect.createLabel(msg), op.getLabel());
        assertSenders(op, msg);
        assertRecipients(op, msg);
        return op;
    }

    protected OperationList assertSenders(Operation op, SimpleMailMessage msg) {
        return assertSenders(op.get(MailDefinitions.SEND_SENDERS, OperationList.class), msg);
    }

    protected OperationList assertSenders(OperationList op, SimpleMailMessage msg) {
        return assertAddresses("from", op, msg.getFrom());
    }

    protected Operation assertRecipients(Operation op, SimpleMailMessage msg) {
        OperationList recips = op.get(MailDefinitions.SEND_RECIPS, OperationList.class);
        assertNotNull("Missing recipients list", recips);
        assertAddresses("to", recips, msg.getTo());
        assertAddresses("cc", recips, msg.getCc());
        assertAddresses("bcc", recips, msg.getBcc());
        assertAddresses("replyTo", recips, msg.getReplyTo());
        return op;
    }

    protected OperationList assertAddresses(String type, OperationList op, String... addrs) {
        assertNotNull(type + ": no address list", op);

        Set<String> addrSet = new TreeSet<String>(Arrays.asList(addrs));
        for (int index = 0; index < op.size(); index++) {
            OperationMap map = op.get(index, OperationMap.class);
            assertNotNull(type + ": missing recipient at index " + index, map);

            String rType = map.get(MailDefinitions.RECIP_TYPE, String.class);
            String value = map.get(MailDefinitions.RECIP_VALUE, String.class);
            if (!type.equals(rType)) {
                continue;
            }

            assertNotNull(type + ": missing recipient value at index " + index, value);
            assertTrue(type + ": missing recipient " + value, addrSet.remove(value));
        }

        assertEquals(type + ": orphan addresses - " + addrSet, 0, addrSet.size());
        return op;
    }

    static SimpleMailMessage createSimpleMailMessage(String subject, String from, String to, String cc, String bcc, String replyTo) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setTo(to);
        msg.setCc(cc);
        msg.setBcc(bcc);
        msg.setReplyTo(replyTo);
        msg.setText("Blah");
        return msg;
    }

    static class TestJavaMailSender implements JavaMailSender {
        private List<Object> sent;

        public TestJavaMailSender() {
            super();
        }

        List<Object> getSentMessages() {
            return sent;
        }

        private void setMessages(Object... msgs) {
            assertTrue("No messages sent", ArrayUtil.length(msgs) > 0);
            assertNull("Multiple sends", sent);
            sent = Arrays.asList(msgs);
        }

        public void send(SimpleMailMessage simpleMessage) throws MailException {
            send(new SimpleMailMessage[]{simpleMessage});    // delegated on purpose to check cflowbelow
        }

        public void send(SimpleMailMessage[] simpleMessages)
                throws MailException {
            setMessages((Object[]) simpleMessages);
        }

        public void send(MimeMessage mimeMessage) throws MailException {
            send(new MimeMessage[]{mimeMessage});    // delegated on purpose to check cflowbelow
        }

        public void send(MimeMessage[] mimeMessages) throws MailException {
            setMessages((Object[]) mimeMessages);
        }

        public void send(MimeMessagePreparator mimeMessagePreparator)
                throws MailException {
            send(new MimeMessagePreparator[]{mimeMessagePreparator});    // delegated on purpose to check cflowbelow
        }

        public void send(MimeMessagePreparator[] mimeMessagePreparators)
                throws MailException {
            setMessages((Object[]) mimeMessagePreparators);
        }

        public MimeMessage createMimeMessage() {
            throw new UnsupportedOperationException("N/A");
        }

        public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
            throw new MailPreparationException("N/A");
        }
    }
}
