package io.mercury.common.concurrent.disruptor;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import io.mercury.common.collections.queue.api.SCQueue;
import io.mercury.common.collections.queue.base.LoadContainer;
import io.mercury.common.functional.Processor;
import io.mercury.common.log.CommonLoggerFactory;
import io.mercury.common.thread.ThreadUtil;

/**
 * 
 * @author yellow013
 *
 * @param <T>
 */
public class DistributorQueue<T> extends SCQueue<T> {

	private Logger log = CommonLoggerFactory.getLogger(getClass());

	private Disruptor<LoadContainer<T>> disruptor;

	private LoadContainerEventProducer producer;

	private AtomicBoolean isStop = new AtomicBoolean(false);

	public DistributorQueue(String queueName, BufferSize bufferSize, Processor<T> processor) {
		this(queueName, bufferSize, false, processor);
	}

	public DistributorQueue(String queueName, BufferSize bufferSize, boolean autoRun, Processor<T> processor) {
		this(queueName, bufferSize, autoRun, processor, WaitStrategyOption.BusySpin);
	}

	public DistributorQueue(String queueName, BufferSize bufferSize, boolean autoRun, Processor<T> processor,
			WaitStrategyOption option) {
		super(processor);
		if (queueName != null)
			super.queueName = queueName;
		// if (queueSize == 0 || queueSize % 2 != 0)
		// throw new IllegalArgumentException("queueSize set error...");
		this.disruptor = new Disruptor<>(
				// 实现EventFactory<LoadContainer<>>的Lambda
				LoadContainer::new,
				// 队列容量
				bufferSize.value(),
				// 实现ThreadFactory的Lambda
				(Runnable runnable) -> ThreadUtil.newMaxPriorityThread(runnable,
						"DisruptorQueue-" + super.queueName + "-WorkingThread"),
				// DaemonThreadFactory.INSTANCE,
				// 生产者策略, 使用单生产者
				ProducerType.SINGLE,
				// Waiting策略
				WaitStrategyFactory.getStrategy(option));
		this.disruptor.handleEventsWith((event, sequence, endOfBatch) -> callProcessor(event.unloading()));
		this.producer = new LoadContainerEventProducer(disruptor.getRingBuffer());
		if (autoRun)
			start();
	}

	private void callProcessor(T t) {
		try {
			processor.process(t);
		} catch (Exception e) {
			log.error("processor.process(t) throw exception -> [{}]", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private class LoadContainerEventProducer {

		private final RingBuffer<LoadContainer<T>> ringBuffer;

		private EventTranslatorOneArg<LoadContainer<T>, T> eventTranslator = (event, sequence, t) -> event.loading(t);

		private LoadContainerEventProducer(RingBuffer<LoadContainer<T>> ringBuffer) {
			this.ringBuffer = ringBuffer;
		}

		private void onEvent(T t) {
			ringBuffer.publishEvent(eventTranslator, t);
		}
	}

	@Override
	public boolean enqueue(T t) {
		try {
			if (isStop.get())
				return false;
			producer.onEvent(t);
			return true;
		} catch (Exception e) {
			log.error("producer.onData(t) throw exception -> [{}]", e.getMessage(), e);
			return false;
		}
	}

	@Override
	protected void startProcessThread() {
		disruptor.start();
	}

	@Override
	public void stop() {
		isStop.set(true);
		while (disruptor.getBufferSize() != 0)
			ThreadUtil.sleep(1);
		disruptor.shutdown();
		log.info("Call stop() success, disruptor is shutdown.");
	}

	public static void main(String[] args) {

		DistributorQueue<Integer> queue = new DistributorQueue<>("Test-Queue", BufferSize.POW2_6, true,
				(integer) -> System.out.println(integer));

		ThreadUtil.startNewThread(() -> {
			int i = 0;
			for (;;)
				queue.enqueue(++i);
		});

		ThreadUtil.sleep(10000);

		queue.stop();

	}

}
