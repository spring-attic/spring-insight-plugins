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

import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.URLName;

import org.aspectj.lang.JoinPoint;

import com.springsource.insight.collection.method.MethodOperationCollectionAspect;
import com.springsource.insight.intercept.InterceptConfiguration;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationList;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.trace.FrameBuilder;

/**
 * 
 */
public aspect MessageSendOperationCollectionAspect extends MethodOperationCollectionAspect {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    private final Logger    logger=Logger.getLogger(getClass().getName());
    public MessageSendOperationCollectionAspect () {
        super();
    }

    @Override
    public String getPluginName() {
        return "javax-mail";
    }

    public pointcut collectionPoint()
        : execution(* javax.mail.Transport.sendMessage(..))
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[]    args=jp.getArgs();
        return createOperation(super.createOperation(jp).type(MailDefinitions.SEND_OPERATION),
                               (Transport) jp.getTarget(), (Message) args[0], (Address[]) args[1]);
    }

    Operation createOperation (Operation op, Transport transport, Message msg, Address[] recips) {
        URLName urlName=transport.getURLName();
        try
        {
            op.label("Send Mail: " + msg.getSubject());
        }
        catch(MessagingException e)
        {
            logger.warning(e.getClass().getSimpleName() + " while get subject: " + e.getMessage());
        }
        
        createOperation(op, urlName);

        if (collectExtraInformation()) {
            try
            {
                addAddresses(op.createList(MailDefinitions.SEND_SENDERS), msg.getFrom());
            }
            catch(MessagingException e)
            {
                logger.warning(e.getClass().getSimpleName() + " while get senders: " + e.getMessage());
            }

            // TODO consider using getRecipients(RecipientType) call instead
            addAddresses(op.createList(MailDefinitions.SEND_RECIPS), recips);

            try
            {
                addMessageDetails(op.createMap(MailDefinitions.SEND_DETAILS), msg);
            }
            catch(MessagingException e)
            {
                logger.warning(e.getClass().getSimpleName() + " while get message details: " + e.getMessage());
            }
        }
        return op;
    }

    OperationMap addMessageDetails (OperationMap op, Message msg) throws MessagingException {
        return op.putAnyNonEmpty(MailDefinitions.SEND_SUBJECT, msg.getSubject())
          .putAnyNonEmpty("content-type", msg.getContentType())
          .putAnyNonEmpty("sent-date", msg.getSentDate())
          .put("size", msg.getSize())
          ;
    }

    static String getSafeSubject (Message msg)
    {
        try
        {
            return msg.getSubject();
        }
        catch(MessagingException e) // TODO consider something else to return
        {
            return e.getClass().getSimpleName();
        }
    }

    Operation createOperation (Operation op, URLName urlName) {
        String protocol = urlName.getProtocol();
		op.put(MailDefinitions.SEND_HOST, urlName.getHost())
          // protocol may return null/empty if some default is assumed 
          .putAnyNonEmpty(MailDefinitions.SEND_PROTOCOL, protocol);

        final int   port=urlName.getPort();
        if (port > 0) { // non-positive values usually indicate the default
            op.put(MailDefinitions.SEND_PORT, port);
        }
        else {
        	Number defaultValue= MailDefinitions.protocolToPortMap.get(protocol);
        	if (defaultValue != null) {
        		op.put(MailDefinitions.SEND_PORT, defaultValue.intValue());
        	} 
        }

        return op;
    }

    OperationList addAddresses (OperationList op, Address[] addrs) {
        if ((addrs == null) || (addrs.length <= 0)) {
            return op;
        }

        for (Address a : addrs) {
            op.createMap()
              .put("type", a.getType())
              .put("value", a.toString())
              ;
        }
        return op;
    }

    boolean collectExtraInformation ()
    {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }
}
