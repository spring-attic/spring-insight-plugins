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

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.util.ArrayUtil;
import com.springsource.insight.util.StringUtil;


/**
 *
 */
public abstract aspect MailOperationCollectionSupport extends MethodOperationCollectionAspect {
    protected static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();

    protected MailOperationCollectionSupport() {
        super();
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        return super.createOperation(jp).type(MailDefinitions.SEND_OPERATION);
    }

    @Override
    public String getPluginName() {
        return MailPluginRuntimeDescriptor.PLUGIN_NAME;
    }

    protected OperationMap addMessageDetails(Operation op, Message msg) {
        op.label("Send Mail: " + getSafeSubject(msg));
        return addMessageDetails(op.createMap(MailDefinitions.SEND_DETAILS), msg);
    }

    protected OperationMap addMessageDetails(OperationMap op, Message msg) {
        try {
            return op.putAnyNonEmpty(MailDefinitions.SEND_SUBJECT, getSafeSubject(msg))
                    .putAnyNonEmpty("content-type", msg.getContentType())
                    .put(MailDefinitions.SEND_DATE, MailDefinitions.getSendDate(msg.getSentDate()))
                    .put("size", msg.getSize())
                    ;
        } catch (MessagingException e) {
            _logger.warning(e.getClass().getSimpleName() + " while add message details " + e.getMessage());
            return op;
        }
    }

    protected static final String getSafeSubject(Message msg) {
        try {
            return msg.getSubject();
        } catch (MessagingException e) {
            return e.getClass().getSimpleName();    // TODO consider something else to return
        }
    }

    protected Operation addAddresses(Operation op, Message msg) {
        try {
            addAddresses(op.createList(MailDefinitions.SEND_SENDERS), msg.getFrom());
        } catch (MessagingException e) {
            _logger.warning(e.getClass().getSimpleName() + " while get senders: " + e.getMessage());
        }

        try {
            addAddresses(op.createList(MailDefinitions.SEND_RECIPS), msg.getAllRecipients());
        } catch (MessagingException e) {
            _logger.warning(e.getClass().getSimpleName() + " while get recipients: " + e.getMessage());
        }

        return op;
    }

    protected OperationList addAddresses(OperationList op, Address... addrs) {
        if (ArrayUtil.length(addrs) <= 0) {
            return op;
        }

        for (Address a : addrs) {
            if (a == null) {
                continue;
            }

            addRecipient(op, a.getType(), a.toString());
        }

        return op;
    }

    protected OperationList addRecipient(OperationList op, String type, String addr) {
        if (StringUtil.isEmpty(addr)) {
            return op;
        }

        op.createMap()
                .put(MailDefinitions.RECIP_TYPE, type)
                .put(MailDefinitions.RECIP_VALUE, addr)
        ;

        return op;
    }

    boolean collectExtraInformation() {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }

    @Override
    public boolean isMetricsGenerator() {
        return true; // This provides an external resource
    }
}
