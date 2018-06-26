package io.ffreedom.transport.rabbitmq;

import io.ffreedom.common.charset.Charsets;
import io.ffreedom.common.utils.ThreadUtil;
import io.ffreedom.transport.rabbitmq.config.PublisherConfigurator;
import io.ffreedom.transport.rabbitmq.config.ReceiverConfigurator;

public class RabbitMqConnectTest {

	private static String host = "192.168.1.165";
	private static int port = 5672;
	private static String username = "kafangMQ";
	private static String password = "kafangMQpass";
	private static String queue0 = "test1";
	private static String queue1 = "test2";
	private static boolean automaticRecovery = true;

	public static void main(String[] args) {

		// PublisherConfigurator pubConfigurator0 =
		// PublisherConfigurator.configuration().setHost(host).setPort(port)
		// .setUsername(username).setPassword(password).setModeDirect(queue0).setAutomaticRecovery(automaticRecovery);

		PublisherConfigurator pubConfigurator0 = PublisherConfigurator.configuration().setHost(host).setPort(port)
				.setUsername(username).setPassword(password).setModeFanout("TestExchange", "TestKey", queue0, queue1)
				.setAutomaticRecovery(automaticRecovery);

		RabbitMqPublisher publisher = new RabbitMqPublisher("PUB_TEST", pubConfigurator0);

		ThreadUtil.startNewThread(() -> {
			int count = 0;
			for (;;) {
				publisher.publish(String.valueOf(++count).getBytes(Charsets.UTF8));
				System.out.println("Send msg -> " + count);
				ThreadUtil.sleep(3000);
			}
		});

		System.out.println(publisher.getName() + " statred....");

		ReceiverConfigurator recvConfigurator0 = ReceiverConfigurator.configuration().setHost(host).setPort(port)
				.setUsername(username).setPassword(password).setReceiveQueue(queue0)
				.setAutomaticRecovery(automaticRecovery);

		RabbitMqReceiver receiver0 = new RabbitMqReceiver("SUB_TEST", recvConfigurator0, (msg) -> {
			System.out.println("receiver_0 msg -> " + new String(msg, Charsets.UTF8));
		});

		ReceiverConfigurator recvConfigurator1 = ReceiverConfigurator.configuration().setHost(host).setPort(port)
				.setUsername(username).setPassword(password).setReceiveQueue(queue1)
				.setAutomaticRecovery(automaticRecovery);

		RabbitMqReceiver receiver1 = new RabbitMqReceiver("SUB_TEST", recvConfigurator1, (msg) -> {
			System.out.println("receiver_1 msg -> " + new String(msg, Charsets.UTF8));
		});

		receiver0.receive();

		receiver1.receive();

		System.out.println(receiver0.getName() + " statred....");

		System.out.println(receiver1.getName() + " statred....");

	}

}