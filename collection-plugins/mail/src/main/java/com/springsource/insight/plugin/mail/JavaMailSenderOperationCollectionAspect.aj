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

import javax.mail.Message;

import org.aspectj.lang.JoinPoint;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.util.ArrayUtil;

/**
 *
 */
public aspect JavaMailSenderOperationCollectionAspect extends MailOperationCollectionSupport {
    public JavaMailSenderOperationCollectionAspect() {
        super();
    }

    public pointcut sendPoint()
            : execution(* JavaMailSender+.send(..))
            || execution(* MailSender+.send(..))
            ;
    // using cflowbelow in case methods delegate to one another
    public pointcut collectionPoint(): sendPoint() && (!cflowbelow(sendPoint()));

    @Override
    protected Operation createOperation(JoinPoint jp) {
        return populateMessageDetails(super.createOperation(jp), jp.getArgs()[0]);
    }

    protected Operation populateMessageDetails(Operation op, Object msg) {
        if (msg == null) {
            return op;
        }

        if (msg instanceof Object[]) {
            Object[] msgs = (Object[]) msg;
            if (ArrayUtil.length(msgs) <= 0) {
                return op;
            }
            return populateMessageDetails(op, ((Object[]) msg)[0]);
        }

        if (msg instanceof Message) {
            Message mailMessage = (Message) msg;
            addMessageDetails(op, mailMessage);

            if (collectExtraInformation()) {
                addAddresses(op, mailMessage);
            }
        } else if (msg instanceof SimpleMailMessage) {
            SimpleMailMessage simpleMessage = (SimpleMailMessage) msg;
            addMessageDetails(op, simpleMessage);

            if (collectExtraInformation()) {
                addAddresses(op, simpleMessage);
            }
        } else if (msg instanceof MimeMessagePreparator) {
            op.label("Send prepared message");
        }

        return op;
    }

    protected Operation addAddresses(Operation op, SimpleMailMessage msg) {
        addAddresses("from", op.createList(MailDefinitions.SEND_SENDERS), msg.getFrom());

        OperationList recips = op.createList(MailDefinitions.SEND_RECIPS);
        addAddresses("to", recips, msg.getTo());
        addAddresses("cc", recips, msg.getCc());
        addAddresses("bcc", recips, msg.getBcc());
        addAddresses("replyTo", recips, msg.getReplyTo());
        return op;
    }

    protected OperationList addAddresses(String type, OperationList op, String... addrs) {
        if (ArrayUtil.length(addrs) <= 0) {
            return op;
        }

        for (String a : addrs) {
            addRecipient(op, type, a);
        }

        return op;
    }

    protected OperationMap addMessageDetails(Operation op, SimpleMailMessage msg) {
        op.label(createLabel(msg));
        return addMessageDetails(op.createMap(MailDefinitions.SEND_DETAILS), msg);
    }

    protected OperationMap addMessageDetails(OperationMap op, SimpleMailMessage msg) {
        return op.putAnyNonEmpty(MailDefinitions.SEND_SUBJECT, msg.getSubject())
                .put(MailDefinitions.SEND_DATE, MailDefinitions.getSendDate(msg.getSentDate()))
                ;
    }

    static String createLabel(SimpleMailMessage msg) {
        return "Send Mail: " + msg.getSubject();
    }
}
