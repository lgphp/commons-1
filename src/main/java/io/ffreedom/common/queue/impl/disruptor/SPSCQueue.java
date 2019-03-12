package io.ffreedom.common.queue.impl.disruptor;

import org.slf4j.Logger;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import io.ffreedom.common.functional.Processor;
import io.ffreedom.common.log.LoggerFactory;
import io.ffreedom.common.queue.api.SCQueue;
import io.ffreedom.common.queue.impl.base.LoadContainer;
import io.ffreedom.common.utils.ThreadUtil;

/**
 * 
 * @author Peng.Jin
 *
 * @param <T>
 */

public class SPSCQueue<T> extends SCQueue<T> {

	private Logger logger = LoggerFactory.getLogger(SPSCQueue.class);

	private Disruptor<LoadContainer<T>> disruptor;

	private LoadContainerEventProducer producer;

	private volatile boolean isStop = false;

	public SPSCQueue(int queueSize, boolean autoRun, Processor<T> processor) {
		this(queueSize, autoRun, processor, WaitStrategyOption.BusySpin);
	}

	public SPSCQueue(int queueSize, boolean autoRun, Processor<T> processor, WaitStrategyOption option) {
		super(processor);
		if (queueSize == 0 || queueSize % 2 != 0)
			throw new IllegalArgumentException("queueSize set error...");
		this.disruptor = new Disruptor<>(
				// 实现EventFactory<LoadContainer<>>的Lambda
				LoadContainer::new,
				// 队列容量
				queueSize,
				// 实现ThreadFactory的Lambda
				// (runnable) -> {
				// return ThreadUtil.newThread(runnable, "disruptor-working-thread");
				// }
				DaemonThreadFactory.INSTANCE,
				// 生产者策略, 使用单生产者
				ProducerType.SINGLE,
				// Waiting策略
				WaitStrategyFactory.newWaitStrategy(option));
		this.disruptor.handleEventsWith((event, sequence, endOfBatch) -> callProcessor(event.unloading()));
		this.producer = new LoadContainerEventProducer(disruptor.getRingBuffer());
		if (autoRun)
			start();
	}

	private void callProcessor(T t) {
		try {
			processor.process(t);
		} catch (Exception e) {
			logger.error("processor.process(t) throw exception -> [{}]", e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public SPSCQueue(int queueSize) {
		this(queueSize, false, null);
	}

	private class LoadContainerEventProducer {

		private final RingBuffer<LoadContainer<T>> ringBuffer;

		private LoadContainerEventProducer(RingBuffer<LoadContainer<T>> ringBuffer) {
			this.ringBuffer = ringBuffer;
		}

		public void onData(T t) {
			ringBuffer.publishEvent(new EventTranslatorOneArg<LoadContainer<T>, T>() {
				public void translateTo(LoadContainer<T> event, long sequence, T t) {
					event.loading(t);
				}
			}, t);
		}
	}

	@Override
	public boolean enqueue(T t) {
		try {
			if (isStop)
				return false;
			this.producer.onData(t);
			return true;
		} catch (Exception e) {
			logger.error("producer.onData(t) throw exception -> [{}]", e.getMessage(), e);
			return false;
		}
	}

	@Override
	public void start() {
		this.disruptor.start();
	}

	@Override
	public void stop() {
		this.isStop = true;
		while (disruptor.getBufferSize() != 0)
			ThreadUtil.sleep(1);
		disruptor.shutdown();
		logger.info("Call stop() success, disruptor is shutdown.");
	}

	public static void main(String[] args) {

		SPSCQueue<Integer> queue = new SPSCQueue<>(64, true,
				(integer) -> System.out.println("********************************************"));

		ThreadUtil.startNewThread(() -> {
			int i = 0;
			for (;;)
				queue.enqueue(++i);
		});

		ThreadUtil.sleep(10000);

		queue.stop();

	}

	@Override
	public String getQueueName() {
		// TODO Auto-generated method stub
		return null;
	}

}