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

package com.springsource.insight.plugin.rabbitmqClient;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import com.rabbitmq.client.AMQP.Basic.RecoverOk;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Channel.FlowOk;
import com.rabbitmq.client.AMQP.Confirm.SelectOk;
import com.rabbitmq.client.AMQP.Exchange.BindOk;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
import com.rabbitmq.client.AMQP.Exchange.DeleteOk;
import com.rabbitmq.client.AMQP.Exchange.UnbindOk;
import com.rabbitmq.client.AMQP.Queue.PurgeOk;
import com.rabbitmq.client.AMQP.Tx.CommitOk;
import com.rabbitmq.client.AMQP.Tx.RollbackOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.FlowListener;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.operation.Operation;

public class RabbitMQPublishCollectionAspectTest extends AbstractRabbitMQCollectionAspectTestSupport {
    public RabbitMQPublishCollectionAspectTest () {
    	super(RabbitPluginOperationType.PUBLISH);
    }

    @Test
    public void testPublish() throws IOException {
        
        String exchange = "exchange";
        String routingKey = "routingKey";
        boolean mandatory = false;
        boolean immediate = false; 
        BasicProperties props = create();
        byte[] body = new byte[25];
        
        MockChannel channel = new MockChannel();
        
        channel.basicPublish(exchange, routingKey, mandatory, immediate, props, body);
        
        Operation op = assertBasicOperation(props, body);
        assertEquals("Mismatched exchange", exchange, op.get("exchange", String.class));
        assertEquals("Mismatched routing key", routingKey, op.get("routingKey", String.class));
        assertEquals("Mismatched mandatory value", Boolean.valueOf(mandatory), op.get("mandatory", Boolean.class));
        assertEquals("Mismatched immediate value", Boolean.valueOf(immediate), op.get("immediate", Boolean.class));
    }
    
    private BasicProperties create() {
        BasicProperties.Builder builder = new BasicProperties.Builder();
        
        builder.appId("RabbitMQ")
               .contentEncoding("UTF-8")
               .contentType("TEXT")
               .correlationId("None")
               .deliveryMode(Integer.valueOf(3))
               .expiration("Never")
               .messageId("Message-1")
               .priority(Integer.valueOf(4))
               .timestamp(new Date());
        
        return builder.build();
    }
    
    @Override
    public OperationCollectionAspectSupport getAspect() {
        return RabbitMQPublishCollectionAspect.aspectOf();
    }
    
    static final class MockChannel implements Channel {
        public void addShutdownListener(ShutdownListener arg0) {
            // do nothing
        }

        public ShutdownSignalException getCloseReason() {
            return null;
        }

        public boolean isOpen() {
            return false;
        }

        public void notifyListeners() {
            // do nothing
        }

        public void removeShutdownListener(ShutdownListener arg0) {
            // do nothing
        }

        public void abort() throws IOException {
            // do nothing
        }

        public void abort(int arg0, String arg1) throws IOException {
            // do nothing
        }

        public void asyncRpc(Method arg0) throws IOException {
            // do nothing
        }

        public void basicAck(long arg0, boolean arg1) throws IOException {
            // do nothing
        }

        public void basicCancel(String arg0) throws IOException {
            // do nothing
        }

        public String basicConsume(String arg0, Consumer arg1)
                throws IOException {
            return null;
        }

        public String basicConsume(String arg0, boolean arg1, Consumer arg2)
                throws IOException {
            return null;
        }

        public String basicConsume(String arg0, boolean arg1, String arg2,
                Consumer arg3) throws IOException {
            return null;
        }

        public String basicConsume(String arg0, boolean arg1, String arg2,
                boolean arg3, boolean arg4, Map<String, Object> arg5,
                Consumer arg6) throws IOException {
            return null;
        }

        public GetResponse basicGet(String arg0, boolean arg1)
                throws IOException {
            return null;
        }

        public void basicNack(long arg0, boolean arg1, boolean arg2)
                throws IOException {
            // do nothing
        }

        public void basicPublish(String arg0, String arg1,
                BasicProperties arg2, byte[] arg3) throws IOException {
            // do nothing
        }

        public void basicPublish(String arg0, String arg1, boolean arg2,
                boolean arg3, BasicProperties arg4, byte[] arg5)
                throws IOException {
            // do nothing
        }

        public void basicQos(int arg0) throws IOException {
            // do nothing
        }

        public void basicQos(int arg0, int arg1, boolean arg2)
                throws IOException {
            // do nothing
        }

        public RecoverOk basicRecover() throws IOException {
            return null;
        }

        public RecoverOk basicRecover(boolean arg0) throws IOException {
            return null;
        }

        public void basicRecoverAsync(boolean arg0) throws IOException {
            // do nothing
        }

        public void basicReject(long arg0, boolean arg1) throws IOException {
            // do nothing
        }

        public void close() throws IOException {
            // do nothing
        }

        public void close(int arg0, String arg1) throws IOException {
            // do nothing
        }

        public SelectOk confirmSelect() throws IOException {
            return null;
        }

        public BindOk exchangeBind(String arg0, String arg1, String arg2)
                throws IOException {
            return null;
        }

        public BindOk exchangeBind(String arg0, String arg1, String arg2,
                Map<String, Object> arg3) throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclare(String arg0, String arg1)
                throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclare(String arg0, String arg1, boolean arg2)
                throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclare(String arg0, String arg1,
                boolean arg2, boolean arg3, Map<String, Object> arg4)
                throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclare(String arg0, String arg1,
                boolean arg2, boolean arg3, boolean arg4,
                Map<String, Object> arg5) throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclarePassive(String arg0) throws IOException {
            return null;
        }

        public DeleteOk exchangeDelete(String arg0) throws IOException {
            return null;
        }

        public DeleteOk exchangeDelete(String arg0, boolean arg1)
                throws IOException {
            return null;
        }

        public UnbindOk exchangeUnbind(String arg0, String arg1, String arg2)
                throws IOException {
            return null;
        }

        public UnbindOk exchangeUnbind(String arg0, String arg1, String arg2,
                Map<String, Object> arg3) throws IOException {
            return null;
        }

        public FlowOk flow(boolean arg0) throws IOException {
            return null;
        }

        public int getChannelNumber() {
            return 0;
        }

        public ConfirmListener getConfirmListener() {
            return null;
        }

        public Connection getConnection() {
            return null;
        }

        public Consumer getDefaultConsumer() {
            return null;
        }

        public FlowOk getFlow() {
            return null;
        }

        public FlowListener getFlowListener() {
            return null;
        }

        public long getNextPublishSeqNo() {
            return 0;
        }

        public ReturnListener getReturnListener() {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.BindOk queueBind(String arg0,
                String arg1, String arg2) throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.BindOk queueBind(String arg0,
                String arg1, String arg2, Map<String, Object> arg3)
                throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.DeclareOk queueDeclare()
                throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.DeclareOk queueDeclare(
                String arg0, boolean arg1, boolean arg2, boolean arg3,
                Map<String, Object> arg4) throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.DeclareOk queueDeclarePassive(
                String arg0) throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.DeleteOk queueDelete(String arg0)
                throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.DeleteOk queueDelete(String arg0,
                boolean arg1, boolean arg2) throws IOException {
            return null;
        }

        public PurgeOk queuePurge(String arg0) throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.UnbindOk queueUnbind(String arg0,
                String arg1, String arg2) throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Queue.UnbindOk queueUnbind(String arg0,
                String arg1, String arg2, Map<String, Object> arg3)
                throws IOException {
            return null;
        }

        public Method rpc(Method arg0) throws IOException {
            return null;
        }

        public void setConfirmListener(ConfirmListener arg0) {
            // do nothing
        }

        public void setDefaultConsumer(Consumer arg0) {
            // do nothing
        }

        public void setFlowListener(FlowListener arg0) {
            // do nothing
        }

        public void setReturnListener(ReturnListener arg0) {
            // do nothing
        }

        public CommitOk txCommit() throws IOException {
            return null;
        }

        public RollbackOk txRollback() throws IOException {
            return null;
        }

        public com.rabbitmq.client.AMQP.Tx.SelectOk txSelect()
                throws IOException {
            return null;
        }
    }
}
