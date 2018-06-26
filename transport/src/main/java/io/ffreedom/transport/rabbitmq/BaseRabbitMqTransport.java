package io.ffreedom.transport.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import io.ffreedom.common.functional.ShutdownEvent;
import io.ffreedom.common.log.LoggerFactory;
import io.ffreedom.transport.rabbitmq.config.ConnectionConfigurator;

public abstract class BaseRabbitMqTransport {

	// 连接RabbitMQ Server使用的组件
	protected ConnectionFactory connectionFactory;
	protected Connection connection;
	protected Channel channel;

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
	public BaseRabbitMqTransport(String tag, ConnectionConfigurator<?> configurator) {
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
			logger.info("newConnection() finsh, tag : " + tag + ", connection hash code : " + connection.hashCode());
			connection.addShutdownListener((shutdownSignalException) -> {
				// 输出错误信息到控制台
				logger.info("ShutdownListener -> " + shutdownSignalException.getMessage());
				// 如果回调函数不为null, 则执行此函数
				if (shutdownEvent != null) {
					shutdownEvent.shutdownHandle(shutdownSignalException);
				}
			});
			channel = connection.createChannel();
			logger.info("createChannel() finsh, channel number is : " + channel.getChannelNumber());
			logger.info("all connection call successful...");
		} catch (IOException e) {
			logger.error("IOException ->" + e.getMessage());
			logger.error(e.getStackTrace());
		} catch (TimeoutException e) {
			logger.error("TimeoutException ->" + e.getMessage());
			logger.error(e.getStackTrace());
		}
	}

	public boolean isConnected() {
		return (connection != null && connection.isOpen()) && (channel != null && channel.isOpen());
	}

	protected void closeConnection() {
		try {
			if (channel != null && channel.isOpen()) {
				channel.close();
			}
			if (connection != null && connection.isOpen()) {
				connection.close();
			}
		} catch (IOException e) {
			logger.error("method closeConnection ->" + e.getMessage());
			logger.error(e.getStackTrace());
		} catch (TimeoutException e) {
			logger.error("method closeConnection ->" + e.getMessage());
			logger.error(e.getStackTrace());
		}
	}

}