package io.ffreedom.commons.chronicle.queue.accessor;

import javax.annotation.concurrent.NotThreadSafe;

import io.ffreedom.commons.chronicle.queue.base.DataReader;
import io.ffreedom.commons.chronicle.queue.base.FileCycle;
import net.openhft.chronicle.queue.ExcerptTailer;

@NotThreadSafe
public final class StringReader extends DataReader<String> {

	private StringReader(ExcerptTailer tailer, FileCycle fileCycle) {
		super(tailer, fileCycle);
	}

	public static StringReader wrap(ExcerptTailer tailer, FileCycle fileCycle) {
		return new StringReader(tailer, fileCycle);
	}

	@Override
	protected String next0() {
		return tailer.readText();
	}

}
