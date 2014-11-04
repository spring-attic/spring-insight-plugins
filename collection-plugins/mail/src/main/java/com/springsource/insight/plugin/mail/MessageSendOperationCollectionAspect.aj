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
package com.springsource.insight.plugin.mail;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.URLName;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.intercept.operation.Operation;

/**
 *
 */
public aspect MessageSendOperationCollectionAspect extends MailOperationCollectionSupport {
    public MessageSendOperationCollectionAspect() {
        super();
    }

    public pointcut collectionPoint()
            : execution(* javax.mail.Transport.sendMessage(..))
            ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();
        return createOperation(super.createOperation(jp), (Transport) jp.getTarget(), (Message) args[0], (Address[]) args[1]);
    }

    Operation createOperation(Operation op, Transport transport, Message msg, Address... recips) {
        addMessageDetails(op, msg);
        createOperation(op, transport.getURLName());

        if (collectExtraInformation()) {
            try {
                addAddresses(op.createList(MailDefinitions.SEND_SENDERS), msg.getFrom());
            } catch (MessagingException e) {
                _logger.warning(e.getClass().getSimpleName() + " while get senders: " + e.getMessage());
            }

            // TODO consider using getRecipients(RecipientType) call instead
            addAddresses(op.createList(MailDefinitions.SEND_RECIPS), recips);
        }

        return op;
    }

    Operation createOperation(Operation op, URLName urlName) {
        String protocol = urlName.getProtocol();
        op.put(MailDefinitions.SEND_HOST, urlName.getHost())
                // protocol may return null/empty if some default is assumed
                .putAnyNonEmpty(MailDefinitions.SEND_PROTOCOL, protocol);

        final int port = urlName.getPort();
        if (port > 0) { // non-positive values usually indicate the default
            op.put(MailDefinitions.SEND_PORT, port);
        } else {
            Number defaultValue = MailDefinitions.protocolToPortMap.get(protocol);
            if (defaultValue != null) {
                op.put(MailDefinitions.SEND_PORT, defaultValue.intValue());
            }
        }

        return op;
    }
}
