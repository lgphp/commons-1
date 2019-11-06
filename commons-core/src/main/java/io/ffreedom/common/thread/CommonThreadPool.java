package io.ffreedom.common.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ffreedom.common.number.RandomNumber;
import io.ffreedom.common.utils.StringUtil;

public final class CommonThreadPool extends ThreadPoolExecutor {

	private BiConsumer<Thread, Runnable> beforeHandler;
	private boolean isBeforeHandle = false;

	private BiConsumer<Runnable, Throwable> afterHandler;
	private boolean isAfterHandle = false;

	private String threadPoolName;

	private final Logger logger = LoggerFactory.getLogger(CommonThreadPool.class);

	private CommonThreadPool(String threadPoolName, ThreadPoolBuilder builder,
			BiConsumer<Thread, Runnable> beforeHandler, BiConsumer<Runnable, Throwable> afterHandler) {
		super(builder.corePoolSize, builder.maximumPoolSize, builder.keepAliveTime, builder.timeUnit,
				builder.workQueue);
		init(threadPoolName, beforeHandler, afterHandler);
	}

	private CommonThreadPool(String threadPoolName, ThreadPoolBuilder builder, ThreadFactory threadFactory,
			BiConsumer<Thread, Runnable> beforeHandler, BiConsumer<Runnable, Throwable> afterHandler) {
		super(builder.corePoolSize, builder.maximumPoolSize, builder.keepAliveTime, builder.timeUnit, builder.workQueue,
				threadFactory);
		init(threadPoolName, beforeHandler, afterHandler);
	}

	private CommonThreadPool(String threadPoolName, ThreadPoolBuilder builder, RejectedExecutionHandler rejectedHandler,
			BiConsumer<Thread, Runnable> beforeHandler, BiConsumer<Runnable, Throwable> afterHandler) {
		super(builder.corePoolSize, builder.maximumPoolSize, builder.keepAliveTime, builder.timeUnit, builder.workQueue,
				rejectedHandler);
		init(threadPoolName, beforeHandler, afterHandler);
	}

	private CommonThreadPool(String threadPoolName, ThreadPoolBuilder builder, ThreadFactory threadFactory,
			RejectedExecutionHandler rejectedHandler, BiConsumer<Thread, Runnable> beforeHandler,
			BiConsumer<Runnable, Throwable> afterHandler) {
		super(builder.corePoolSize, builder.maximumPoolSize, builder.keepAliveTime, builder.timeUnit, builder.workQueue,
				threadFactory, rejectedHandler);
		init(threadPoolName, beforeHandler, afterHandler);
	}

	private void init(String threadPoolName, BiConsumer<Thread, Runnable> beforeHandler,
			BiConsumer<Runnable, Throwable> afterHandler) {
		this.threadPoolName = threadPoolName;
		if (beforeHandler != null) {
			this.beforeHandler = beforeHandler;
			this.isBeforeHandle = true;
		}
		if (afterHandler != null) {
			this.afterHandler = afterHandler;
			this.isAfterHandle = true;
		}
	}

	public static ThreadPoolBuilder newBuilder() {
		return new ThreadPoolBuilder();
	}

	@Override
	protected void beforeExecute(Thread thread, Runnable runnable) {
		logger.debug("Thread name -> {}, Runnable -> {}, execute before.", thread.getName(), runnable.toString());
		if (isBeforeHandle)
			beforeHandler.accept(thread, runnable);
	}

	@Override
	protected void afterExecute(Runnable runnable, Throwable throwable) {
		logger.debug("Runnable -> {}, execute after.", runnable.toString());
		if (isAfterHandle)
			afterHandler.accept(runnable, throwable);
	}

	@Override
	protected void terminated() {
	};

	public String getThreadPoolName() {
		return threadPoolName;
	}

	public final static class ThreadPoolBuilder {

		private int corePoolSize = Runtime.getRuntime().availableProcessors();
		private int maximumPoolSize = Runtime.getRuntime().availableProcessors() * 4;
		private long keepAliveTime = 60;
		private TimeUnit timeUnit = TimeUnit.SECONDS;
		private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
		private ThreadFactory threadFactory;
		private RejectedExecutionHandler rejectedHandler;

		private BiConsumer<Thread, Runnable> beforeHandler;
		private BiConsumer<Runnable, Throwable> afterHandler;

		public ThreadPoolBuilder setCorePoolSize(int corePoolSize) {
			this.corePoolSize = corePoolSize;
			return this;
		}

		public ThreadPoolBuilder setMaximumPoolSize(int maximumPoolSize) {
			this.maximumPoolSize = maximumPoolSize;
			return this;
		}

		public ThreadPoolBuilder setKeepAliveTime(long keepAliveTime, TimeUnit timeUnit) {
			this.keepAliveTime = keepAliveTime;
			this.timeUnit = timeUnit;
			return this;
		}

		public ThreadPoolBuilder setWorkQueue(BlockingQueue<Runnable> workQueue) {
			this.workQueue = workQueue;
			return this;
		}

		public ThreadPoolBuilder setThreadFactory(ThreadFactory threadFactory) {
			this.threadFactory = threadFactory;
			return this;
		}

		public ThreadPoolBuilder setRejectedHandler(RejectedExecutionHandler rejectedHandler) {
			this.rejectedHandler = rejectedHandler;
			return this;
		}

		public ThreadPoolBuilder setBeforeHandler(BiConsumer<Thread, Runnable> beforeHandler) {
			this.beforeHandler = beforeHandler;
			return this;
		}

		public ThreadPoolBuilder setAfterHandler(BiConsumer<Runnable, Throwable> afterHandler) {
			this.afterHandler = afterHandler;
			return this;
		}

		public ThreadPoolExecutor build() {
			return build("");
		}

		public ThreadPoolExecutor build(String threadPoolName) {
			threadPoolName = StringUtil.isNullOrEmpty(threadPoolName) ? "CommonThreadPool-" + RandomNumber.randomInt()
					: threadPoolName;
			if (threadFactory != null && rejectedHandler != null)
				return new CommonThreadPool(threadPoolName, this, threadFactory, rejectedHandler, beforeHandler,
						afterHandler);
			if (threadFactory != null && rejectedHandler == null)
				return new CommonThreadPool(threadPoolName, this, threadFactory, beforeHandler, afterHandler);
			if (threadFactory == null && rejectedHandler != null)
				return new CommonThreadPool(threadPoolName, this, rejectedHandler, beforeHandler, afterHandler);
			else
				return new CommonThreadPool(threadPoolName, this, beforeHandler, afterHandler);
		}

	}

	public static void main(String[] args) {
		int COUNT_BITS = Integer.SIZE - 3;
		System.out.println(Integer.toBinaryString(-1 << COUNT_BITS));
		System.out.println(Integer.toBinaryString(0 << COUNT_BITS));
		System.out.println(Integer.toBinaryString(1 << COUNT_BITS));
		System.out.println(Integer.toBinaryString(2 << COUNT_BITS));
		System.out.println(Integer.toBinaryString(3 << COUNT_BITS));

	}

}
