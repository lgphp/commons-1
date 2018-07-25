package io.ffreedom.transport.rabbitmq.config;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.MessageProperties;

import io.ffreedom.common.functional.ShutdownEvent;

public class PublisherConfigurator extends ConnectionConfigurator<PublisherConfigurator> {

	/**
	 * 发布者参数
	 */
	private String exchange = "";
	private String routingKey = "";
	private String[] bindQueues = null;
	private String directQueue;
	private BasicProperties msgProperties = MessageProperties.PERSISTENT_BASIC;
	private BuiltinExchangeType exchangeType = BuiltinExchangeType.DIRECT;

	private PublisherConfigurator() {
		super("RabbitMqPublisherConfigurator");
	}

	public static PublisherConfigurator configuration() {
		return new PublisherConfigurator();
	}

	public String getExchange() {
		return exchange;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public String[] getBindQueues() {
		return bindQueues;
	}

	public String getDirectQueue() {
		return directQueue;
	}

	public BasicProperties getMsgProperties() {
		return msgProperties;
	}

	public PublisherConfigurator setMsgProperties(BasicProperties msgProperties) {
		this.msgProperties = msgProperties;
		return this;
	}

	public BuiltinExchangeType getExchangeType() {
		return exchangeType;
	}

	public PublisherConfigurator setModeDirect(String directQueue) {
		this.exchangeType = BuiltinExchangeType.DIRECT;
		this.directQueue = directQueue;
		return this;
	}

	public PublisherConfigurator setModeFanout(String exchange, String routingKey, String[] bindQueues) {
		if (bindQueues == null) {
			throw new IllegalArgumentException("Bind queues not nullable.");
		}
		this.exchangeType = BuiltinExchangeType.FANOUT;
		this.exchange = exchange;
		this.routingKey = routingKey;
		this.bindQueues = bindQueues;
		return this;
	}

	public PublisherConfigurator setModeTopic() {
		this.exchangeType = BuiltinExchangeType.TOPIC;
		return this;
	}

	/**
	 * 配置连接信息 START
	 */

	@Override
	public PublisherConfigurator setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
		return this;
	}

	@Override
	public PublisherConfigurator setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
		return this;
	}

	@Override
	public PublisherConfigurator setDurable(boolean durable) {
		this.durable = durable;
		return this;
	}

	@Override
	public PublisherConfigurator setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
		return this;
	}

	@Override
	public PublisherConfigurator setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
		return this;
	}

	@Override
	public PublisherConfigurator setAutomaticRecovery(boolean automaticRecovery) {
		this.automaticRecovery = automaticRecovery;
		return this;
	}

	@Override

	public PublisherConfigurator setRecoveryInterval(long recoveryInterval) {
		this.recoveryInterval = recoveryInterval;
		return this;
	}

	@Override
	public PublisherConfigurator setHandshakeTimeout(int handshakeTimeout) {
		this.handshakeTimeout = handshakeTimeout;
		return this;
	}

	@Override
	public PublisherConfigurator setShutdownTimeout(int shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
		return this;
	}

	@Override
	public PublisherConfigurator setRequestedHeartbeat(int requestedHeartbeat) {
		this.requestedHeartbeat = requestedHeartbeat;
		return this;
	}

	@Override
	public PublisherConfigurator setShutdownEvent(ShutdownEvent<Exception> shutdownEvent) {
		this.shutdownEvent = shutdownEvent;
		return this;
	}

	/**
	 * 配置连接信息 END
	 */

}
