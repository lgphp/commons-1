package io.ffreedom.transport.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.ffreedom.common.charset.Charsets;
import io.ffreedom.common.functional.Callback;
import io.ffreedom.common.utils.ThreadUtil;
import io.ffreedom.transport.base.role.Receiver;
import io.ffreedom.transport.rabbitmq.config.ReceiverConfigurator;

public class RabbitMqReceiver extends BaseRabbitMqTransport implements Receiver {

	// 接收消息时使用的回调函数
	private Callback<byte[]> callback;

	// 绑定的Exchange
	@SuppressWarnings("unused")
	// 暂时没有使用
	private String exchange;
	// 连接的Queue
	private String receiveQueue;

	// 自动ACK
	private boolean isAutoAck;
	// 最大自动重试次数
	private int maxAckTotal;

	private String receiverName;

	/**
	 * 
	 * @param configurator
	 * @param callback
	 */
	public RabbitMqReceiver(String tag, ReceiverConfigurator configurator, Callback<byte[]> callback) {
		super(tag, configurator);
		this.callback = callback;
		this.exchange = configurator.getExchange();
		this.receiveQueue = configurator.getReceiveQueue();
		this.isAutoAck = configurator.isAutoAck();
		this.maxAckTotal = configurator.getMaxAckTotal();
		createConnection();
		init();
	}

	private void init() {
		try {
			channel.queueDeclare(receiveQueue, configurator.isDurable(), configurator.isExclusive(),
					configurator.isAutoDelete(), null);
		} catch (IOException e) {
			logger.error("IOException ->" + e.getMessage());
			logger.error(e.getStackTrace());
			destroy();
			logger.error("call destroy() method.");
		}
		this.receiverName = "Receiver->" + configurator.getHost() + ":" + configurator.getPort() + "$" + receiveQueue;
	}

	@Override
	public void receive() {
		try {
			// param1: queue
			// param2: autoAck
			// param3: consumeCallback
			channel.basicConsume(receiveQueue, isAutoAck, new DefaultConsumer(channel) {
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
					if (!isAutoAck) {
						try {
							int ack = 0;
							while (!isConnected()) {
								logger.error("isConnected() == false, ack " + (++ack));
								destroy();
								createConnection();
								ThreadUtil.sleep(configurator.getRecoveryInterval());
								if (ack == maxAckTotal) {
									logger.error("Channel#basicAck() -> failure...");
									break;
								}
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
	public boolean destroy() {
		logger.info("call method -> RabbitSubscriber.destroy()");
		closeConnection();
		return true;
	}

	@Override
	public String getName() {
		return receiverName;
	}

	public static void main(String[] args) {

		ReceiverConfigurator configurator = ReceiverConfigurator.configuration().setHost("192.168.1.152").setPort(5672)
				.setUsername("thadmq").setPassword("root").setReceiveQueue("hello").setAutomaticRecovery(true);

		RabbitMqReceiver receiver = new RabbitMqReceiver("TEST_SUB", configurator, (byte[] msg) -> {
			System.out.println(new String(msg, Charsets.UTF8));
		});

		receiver.receive();

	}

}
