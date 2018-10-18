package io.ffreedom.transport.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import io.ffreedom.common.functional.ShutdownEvent;
import io.ffreedom.common.log.LoggerFactory;
import io.ffreedom.common.log.UseLogger;
import io.ffreedom.transport.base.TransportModule;
import io.ffreedom.transport.rabbitmq.config.ConnectionConfigurator;

abstract class BaseRabbitMqTransport implements TransportModule {

	// 连接RabbitMQ Server使用的组件
	protected ConnectionFactory connectionFactory;
	protected volatile Connection connection;
	protected volatile Channel channel;

	// 存储配置信息对象
	protected ConnectionConfigurator<?> configurator;

	// 停机事件, 在监听到ShutdownSignalException时调用
	protected ShutdownEvent<Exception> shutdownEvent;

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private String tag;

	/**
	 * @param tag
	 * @param configurator
	 */
	protected BaseRabbitMqTransport(String tag, ConnectionConfigurator<?> configurator) {
		if (configurator == null) {
			throw new NullPointerException(tag + ": configurator is null.");
		}
		this.tag = (tag == null) ? "unnamed-" + System.currentTimeMillis() : tag;
		this.configurator = configurator;
		this.shutdownEvent = configurator.getShutdownEvent();
	}

	protected void createConnection() {
		if (connectionFactory == null) {
			connectionFactory = new ConnectionFactory();
			connectionFactory.setHost(configurator.getHost());
			connectionFactory.setPort(configurator.getPort());
			connectionFactory.setUsername(configurator.getUsername());
			connectionFactory.setPassword(configurator.getPassword());
			connectionFactory.setAutomaticRecoveryEnabled(configurator.isAutomaticRecovery());
			connectionFactory.setNetworkRecoveryInterval(configurator.getRecoveryInterval());
			connectionFactory.setHandshakeTimeout(configurator.getHandshakeTimeout());
			connectionFactory.setConnectionTimeout(configurator.getConnectionTimeout());
			connectionFactory.setShutdownTimeout(configurator.getShutdownTimeout());
			connectionFactory.setRequestedHeartbeat(configurator.getRequestedHeartbeat());
		}
		try {
			connection = connectionFactory.newConnection();
			logger.info("Call method connectionFactory.newConnection() finished, tag -> {}, connection id -> {}.", tag,
					connection.getId());
			connection.addShutdownListener(exception -> {
				// 输出错误信息到控制台
				logger.info("Call ShutdownListener -> {}", exception.getMessage());
				// 如果回调函数不为null, 则执行此函数
				if (shutdownEvent != null) {
					shutdownEvent.accept(exception);
				}
			});
			channel = connection.createChannel();
			logger.info("Call method connection.createChannel() finished, tag -> {}, channel number -> {}", tag,
					channel.getChannelNumber());
			logger.info("All connection call method successful...");
		} catch (IOException e) {
			UseLogger.error(logger, e, "Call method createConnection() IOException -> {}", e.getMessage());
		} catch (TimeoutException e) {
			UseLogger.error(logger, e, "Call method createConnection() TimeoutException -> {}", e.getMessage());
		}
	}

	@Override
	public boolean isConnected() {
		return (connection != null && connection.isOpen()) && (channel != null && channel.isOpen());
	}

	protected void closeConnection() {
		try {
			if (channel != null && channel.isOpen()) {
				channel.close();
				logger.info("Channel is closeed!");
			}
			if (connection != null && connection.isOpen()) {
				connection.close();
				logger.info("Connection is closeed!");
			}
		} catch (IOException e) {
			UseLogger.error(logger, e, "Call method closeConnection() IOException -> {}", e.getMessage());
		} catch (TimeoutException e) {
			UseLogger.error(logger, e, "Call method closeConnection() TimeoutException -> {}", e.getMessage());
		}
	}

}
