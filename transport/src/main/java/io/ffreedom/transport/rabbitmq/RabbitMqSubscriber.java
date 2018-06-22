package io.ffreedom.transport.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.ffreedom.common.charset.Charsets;
import io.ffreedom.common.functional.Callback;
import io.ffreedom.common.utils.ThreadUtil;
import io.ffreedom.transport.base.role.Subscriber;
import io.ffreedom.transport.rabbitmq.config.RabbitMqConfigurator;

public class RabbitMqSubscriber extends BaseRabbitMqTransport implements Subscriber {

	// 接收消息时使用的回调函数
	private Callback<byte[]> callback;

	private String subscriberName;

	/**
	 * 
	 * @param configurator
	 * @param callback
	 */
	public RabbitMqSubscriber(String tag, RabbitMqConfigurator configurator, Callback<byte[]> callback) {
		super(tag, configurator);
		this.callback = callback;
		this.subscriberName = "Sub->" + configurator.getHost() + ":" + configurator.getPort() + "$"
				+ configurator.getQueue();
		createConnection();
		init();
	}

	private void init() {
		try {
			channel.queueDeclare(configurator.getQueue(), configurator.isDurable(), configurator.isExclusive(),
					configurator.isAutoDelete(), null);
		} catch (IOException e) {
			logger.error("IOException ->" + e.getMessage());
			logger.error(e.getStackTrace());
			destroy();
			logger.error("call destroy() method.");
		}
	}

	@Override
	public void subscribe() {
		try {
			// param1: queue
			// param2: autoAck
			// param3: consumeCallback
			channel.basicConsume(configurator.getQueue(), configurator.isAutoAck(), new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					try {
						callback.onEvent(body);
					} catch (Exception e) {
						logger.error("Callback#onEvent -> " + e.getMessage());
						logger.error(e.getStackTrace());
						// 退回消息->关闭连接
						logger.info("Reject Msg : " + new String(body, Charsets.UTF8));
						channel.basicReject(envelope.getDeliveryTag(), true);
						destroy();
					}
					if (!configurator.isAutoAck()) {
						try {
							int ack = 0;
							while (!isConnected()) {
								logger.error("isConnected() == false, ack " + (++ack));
								destroy();
								createConnection();
								ThreadUtil.sleep(configurator.getRecoveryInterval());
							}
							channel.basicAck(envelope.getDeliveryTag(), false);
						} catch (IOException e) {
							logger.error("Channel#basicAck() -> " + e.getMessage());
							logger.error(e.getStackTrace());
						}
					}
				}
			});
		} catch (IOException e) {
			logger.error("Channel#basicConsume() -> " + e.getMessage());
			logger.error(e.getStackTrace());
		}
	}

	@Override
	public void destroy() {
		logger.info("call method -> RabbitSubscriber.destroy()");
		closeConnection();
	}

	@Override
	public String getName() {
		return subscriberName;
	}

	public static void main(String[] args) {

		RabbitMqConfigurator configurator = RabbitMqConfigurator.builder().setHost("192.168.1.152").setPort(5672)
				.setUsername("thadmq").setPassword("root").setQueue("hello").setAutomaticRecovery(true).build();

		RabbitMqSubscriber subscriber = new RabbitMqSubscriber("TEST_SUB", configurator, (byte[] msg) -> {
			System.out.println(new String(msg, Charsets.UTF8));
		});

		subscriber.subscribe();

	}

}
