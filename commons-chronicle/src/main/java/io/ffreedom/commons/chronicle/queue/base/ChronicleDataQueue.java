package io.ffreedom.commons.chronicle.queue.base;

import static io.ffreedom.common.utils.StringUtil.isPath;

import java.io.File;
import java.util.function.ObjIntConsumer;

import org.slf4j.Logger;

import io.ffreedom.common.env.SystemPropertys;
import io.ffreedom.common.log.CommonLoggerFactory;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

public abstract class ChronicleDataQueue<T, RT extends DataReader<T>, WT extends DataWriter<T>> {

	private final File savePath;
	private String name;

	private SingleChronicleQueue queue;

	private String rootPath;
	private String folder;
	private FileCycle fileCycle;
	private ObjIntConsumer<File> storeFileListener;

	protected Logger logger;

	protected ChronicleDataQueue(BaseBuilder<?> builder) {
		this.rootPath = builder.rootPath;
		this.folder = builder.folder;
		this.fileCycle = builder.fileCycle;
		this.storeFileListener = builder.storeFileListener;
		this.logger = builder.logger;
		this.savePath = new File(rootPath + Chronicle_Queue + folder);
		this.name = folder;
		initChronicleQueue();
	}

	private static final String Chronicle_Queue = "chronicle-queue/";

	private void initChronicleQueue() {
		if (!savePath.exists())
			savePath.mkdirs();
		this.queue = SingleChronicleQueueBuilder.single(savePath).rollCycle(fileCycle.getRollCycle())
				.storeFileListener(this::storeFileHandle).build();
		// TODO 解决CPU缓存行填充问题
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownHandle, "ChronicleQueue-Cleanup"));
	}

	private void shutdownHandle() {
		queue.close();
		logger.info("Run ShutdownHook of {}", name);
	}

	private void storeFileHandle(int cycle, File file) {
		if (storeFileListener != null)
			storeFileListener.accept(file, cycle);
		else
			logger.info("Released file : cycle==[{}], file==[{}]", cycle, file.getAbsolutePath());
	}

	public String getName() {
		return name;
	}

	public String getRootPath() {
		return rootPath;
	}

	public String getFolder() {
		return folder;
	}

	public File getSavePath() {
		return savePath;
	}

	public FileCycle getFileCycle() {
		return fileCycle;
	}

	public SingleChronicleQueue getQueue() {
		return queue;
	}

	@Deprecated
	public boolean deleteFolder() {
		if (savePath.isAbsolute())
			return savePath.delete();
		return false;
	}

	public abstract RT createReader();

	public abstract WT createWriter();

	protected abstract static class BaseBuilder<BT> {

		private String rootPath = SystemPropertys.JAVA_IO_TMPDIR + "/";
		private String folder = "default/";
		private Logger logger = CommonLoggerFactory.getLogger(ChronicleDataQueue.class);
		private FileCycle fileCycle = FileCycle.HOURLY;
		private ObjIntConsumer<File> storeFileListener = null;

		public BT setRootPath(String rootPath) {
			this.rootPath = isPath(rootPath) ? rootPath : rootPath + "/";
			return getThis();
		}

		public BT setFolder(String folder) {
			this.folder = isPath(folder) ? folder : folder + "/";
			return getThis();
		}

		public BT setLogger(Logger logger) {
			this.logger = logger;
			return getThis();
		}

		public BT setFileCycle(FileCycle fileCycle) {
			this.fileCycle = fileCycle;
			return getThis();
		}

		public BT setStoreFileListener(ObjIntConsumer<File> storeFileListener) {
			this.storeFileListener = storeFileListener;
			return getThis();
		}

		protected abstract BT getThis();

	}

}