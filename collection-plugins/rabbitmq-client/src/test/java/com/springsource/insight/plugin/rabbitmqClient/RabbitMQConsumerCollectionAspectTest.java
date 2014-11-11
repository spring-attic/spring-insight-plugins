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

package com.springsource.insight.plugin.rabbitmqClient;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;
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
import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;

public class RabbitMQConsumerCollectionAspectTest extends AbstractRabbitMQCollectionAspectTestSupport {
    public RabbitMQConsumerCollectionAspectTest() {
        super(RabbitPluginOperationType.CONSUME);
    }

    @Test
    public void testHandleDelivery() throws IOException {
        MockConsumer consumer = new MockConsumer();

        String consumerTag = "1";
        Envelope envelope = new Envelope(1l, false, "exchange", "routingKey");
        BasicProperties props = create();
        byte[] body = new byte[25];
        assertTrue(RabbitMQConsumerCollectionAspect.opHolder.isEmpty());
        consumer.handleDelivery(consumerTag, envelope, props, body);
        assertTrue(RabbitMQConsumerCollectionAspect.opHolder.isEmpty());
        assertOperation(envelope, props, body, AbstractRabbitMQResourceAnalyzer.RABBIT + "-" + RabbitMQConsumerCollectionAspect.LABEL_PREFIX + "null (exchange#routingKey)");
    }

    @Test
    public void testBasicGet() throws IOException {

        Envelope envelope = new Envelope(1l, false, "exchange", "routingKey");
        BasicProperties props = create();
        byte[] body = new byte[25];

        MockChannel channel = new MockChannel(envelope, props, body);
        assertTrue(RabbitMQConsumerCollectionAspect.opHolder.isEmpty());
        channel.basicGet("rk", false);
        assertTrue(RabbitMQConsumerCollectionAspect.opHolder.isEmpty());
        assertOperation(envelope, props, body, AbstractRabbitMQResourceAnalyzer.RABBIT + "-" + RabbitMQConsumerCollectionAspect.LABEL_PREFIX + "rk (exchange#routingKey)");
    }

    void assertOperation(Envelope envelope, BasicProperties props, byte[] body, String opLabel) {
        Operation op = assertBasicOperation(props, body, opLabel);

        OperationMap envMap = op.get("envelope", OperationMap.class);
        assertNotNull("No envelope", envMap);
        assertEquals("Mismatched delivery tag", Long.valueOf(envelope.getDeliveryTag()), envMap.get("deliveryTag", Long.class));
        assertEquals("Mismatched exchange", envelope.getExchange(), envMap.get("exchange", String.class));
        assertEquals("Mismatched routing key", envelope.getRoutingKey(), envMap.get("routingKey", String.class));
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
        return RabbitMQConsumerCollectionAspect.aspectOf();
    }

    static final class MockConsumer implements Consumer {
        public void handleCancel(String arg0) throws IOException {
            // do nothing
        }

        public void handleCancelOk(String arg0) {
            // do nothing
        }

        public void handleConsumeOk(String arg0) {
            // do nothing
        }

        public void handleDelivery(String consumerTag, Envelope envelope,
                                   BasicProperties props, byte[] body) throws IOException {
            // do nothing
        }

        public void handleRecoverOk() {
            // do nothing
        }

        public void handleShutdownSignal(String arg0, ShutdownSignalException arg1) {
            // do nothing
        }

        public void handleRecoverOk(String consumerTag) {

        }
    }

    private static final class MockChannel implements Channel {

        private Envelope envelope;
        private BasicProperties props;
        private byte[] body;

        public MockChannel(Envelope envelope, BasicProperties props, byte[] body) {
            this.envelope = envelope;
            this.props = props;
            this.body = body;
        }

        public int getChannelNumber() {
            return 0;
        }

        public Connection getConnection() {
            return null;
        }

        public void close() throws IOException {

        }

        public void close(int closeCode, String closeMessage) throws IOException {

        }

        public boolean flowBlocked() {
            return false;
        }

        public void abort() throws IOException {

        }

        public void abort(int closeCode, String closeMessage) throws IOException {

        }

        public void addReturnListener(ReturnListener listener) {

        }

        public boolean removeReturnListener(ReturnListener listener) {
            return false;
        }

        public void clearReturnListeners() {

        }

        public void addFlowListener(FlowListener listener) {

        }

        public boolean removeFlowListener(FlowListener listener) {
            return false;
        }

        public void clearFlowListeners() {

        }

        public void addConfirmListener(ConfirmListener listener) {

        }

        public boolean removeConfirmListener(ConfirmListener listener) {
            return false;
        }

        public void clearConfirmListeners() {

        }

        public Consumer getDefaultConsumer() {
            return null;
        }

        public void setDefaultConsumer(Consumer consumer) {

        }

        public void basicQos(int prefetchSize, int prefetchCount, boolean global) throws IOException {

        }

        public void basicQos(int prefetchCount, boolean global) throws IOException {

        }

        public void basicQos(int prefetchCount) throws IOException {

        }

        public void basicPublish(String exchange, String routingKey, BasicProperties props, byte[] body) throws IOException {

        }

        public void basicPublish(String exchange, String routingKey, boolean mandatory, BasicProperties props, byte[] body) throws IOException {

        }

        public void basicPublish(String exchange, String routingKey, boolean mandatory, boolean immediate, BasicProperties props, byte[] body) throws IOException {

        }

        public DeclareOk exchangeDeclare(String exchange, String type) throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclare(String exchange, String type, boolean durable) throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Map<String, Object> arguments) throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException {
            return null;
        }

        public DeclareOk exchangeDeclarePassive(String name) throws IOException {
            return null;
        }

        public DeleteOk exchangeDelete(String exchange, boolean ifUnused) throws IOException {
            return null;
        }

        public DeleteOk exchangeDelete(String exchange) throws IOException {
            return null;
        }

        public BindOk exchangeBind(String destination, String source, String routingKey) throws IOException {
            return null;
        }

        public BindOk exchangeBind(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
            return null;
        }

        public UnbindOk exchangeUnbind(String destination, String source, String routingKey) throws IOException {
            return null;
        }

        public UnbindOk exchangeUnbind(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException {
            return null;
        }

        public AMQP.Queue.DeclareOk queueDeclare() throws IOException {
            return null;
        }

        public AMQP.Queue.DeclareOk queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException {
            return null;
        }

        public AMQP.Queue.DeclareOk queueDeclarePassive(String queue) throws IOException {
            return null;
        }

        public AMQP.Queue.DeleteOk queueDelete(String queue) throws IOException {
            return null;
        }

        public AMQP.Queue.DeleteOk queueDelete(String queue, boolean ifUnused, boolean ifEmpty) throws IOException {
            return null;
        }

        public AMQP.Queue.BindOk queueBind(String queue, String exchange, String routingKey) throws IOException {
            return null;
        }

        public AMQP.Queue.BindOk queueBind(String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
            return null;
        }


        public AMQP.Queue.UnbindOk queueUnbind(String queue, String exchange, String routingKey) throws IOException {
            return null;
        }


        public AMQP.Queue.UnbindOk queueUnbind(String queue, String exchange, String routingKey, Map<String, Object> arguments) throws IOException {
            return null;
        }


        public PurgeOk queuePurge(String queue) throws IOException {
            return null;
        }


        public GetResponse basicGet(String queue, boolean autoAck) throws IOException {
            GetResponse resp = mock(GetResponse.class);
            when(resp.getBody()).thenReturn(body);
            when(resp.getEnvelope()).thenReturn(envelope);
            when(resp.getProps()).thenReturn(props);

            return resp;
        }


        public void basicAck(long deliveryTag, boolean multiple) throws IOException {

        }


        public void basicNack(long deliveryTag, boolean multiple, boolean requeue) throws IOException {

        }


        public void basicReject(long deliveryTag, boolean requeue) throws IOException {

        }


        public String basicConsume(String queue, Consumer callback) throws IOException {
            return null;
        }


        public String basicConsume(String queue, boolean autoAck, Consumer callback) throws IOException {
            return null;
        }


        public String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, Consumer callback) throws IOException {
            return null;
        }


        public String basicConsume(String queue, boolean autoAck, String consumerTag, Consumer callback) throws IOException {
            return null;
        }


        public String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object> arguments, Consumer callback) throws IOException {
            return null;
        }


        public void basicCancel(String consumerTag) throws IOException {

        }


        public RecoverOk basicRecover() throws IOException {
            return null;
        }


        public RecoverOk basicRecover(boolean requeue) throws IOException {
            return null;
        }


        public void basicRecoverAsync(boolean requeue) throws IOException {

        }


        public AMQP.Tx.SelectOk txSelect() throws IOException {
            return null;
        }


        public CommitOk txCommit() throws IOException {
            return null;
        }


        public RollbackOk txRollback() throws IOException {
            return null;
        }


        public SelectOk confirmSelect() throws IOException {
            return null;
        }


        public long getNextPublishSeqNo() {
            return 0;
        }


        public boolean waitForConfirms() throws InterruptedException {
            return false;
        }


        public boolean waitForConfirms(long timeout) throws InterruptedException, TimeoutException {
            return false;
        }


        public void waitForConfirmsOrDie() throws IOException, InterruptedException {

        }


        public void waitForConfirmsOrDie(long timeout) throws IOException, InterruptedException, TimeoutException {

        }


        public void asyncRpc(Method method) throws IOException {

        }


        public Command rpc(Method method) throws IOException {
            return null;
        }


        public void addShutdownListener(ShutdownListener listener) {

        }


        public void removeShutdownListener(ShutdownListener listener) {

        }


        public ShutdownSignalException getCloseReason() {
            return null;
        }


        public void notifyListeners() {

        }


        public boolean isOpen() {
            return false;
        }
    }}
