package io.ffreedom.common.concurrent.map;

import static io.ffreedom.common.datetime.DateTimeUtil.timeToHour;
import static io.ffreedom.common.datetime.DateTimeUtil.timeToMinute;
import static io.ffreedom.common.datetime.DateTimeUtil.timeToSecond;

import java.time.LocalTime;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToLongFunction;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import io.ffreedom.common.datetime.DateTimeUtil;

@NotThreadSafe
public final class ConcurrentLocalTimeMap<V> extends ConcurrentTemporalMap<LocalTime, V, ConcurrentLocalTimeMap<V>> {

	private ConcurrentLocalTimeMap(ToLongFunction<LocalTime> keyToLangFunc, Function<LocalTime, LocalTime> nextKeyFunc,
			BiPredicate<LocalTime, LocalTime> hasNextKey) {
		super(keyToLangFunc, nextKeyFunc, hasNextKey);
	}

	private static ToLongFunction<LocalTime> keyToLangFuncWithHour = key -> timeToHour(key);
	private static Function<LocalTime, LocalTime> nextKeyFuncWithHour = key -> key.plusHours(1);

	private static ToLongFunction<LocalTime> keyToLangFuncWithMinute = key -> timeToMinute(key);
	private static Function<LocalTime, LocalTime> nextKeyFuncWithMinute = key -> key.plusMinutes(1);

	private static ToLongFunction<LocalTime> keyToLangFuncWithSecond = key -> timeToSecond(key);
	private static Function<LocalTime, LocalTime> nextKeyFuncWithSecond = key -> key.plusSeconds(1);

	private static BiPredicate<LocalTime, LocalTime> hasNextKey = (nextKey, endPoint) -> nextKey.isBefore(endPoint)
			|| nextKey.equals(endPoint);

	public final static <V> ConcurrentLocalTimeMap<V> newMapToHour() {
		return new ConcurrentLocalTimeMap<>(keyToLangFuncWithHour, nextKeyFuncWithHour, hasNextKey);
	}

	public final static <V> ConcurrentLocalTimeMap<V> newMapToMinute() {
		return new ConcurrentLocalTimeMap<>(keyToLangFuncWithMinute, nextKeyFuncWithMinute, hasNextKey);
	}

	public final static <V> ConcurrentLocalTimeMap<V> newMapToSecond() {
		return new ConcurrentLocalTimeMap<>(keyToLangFuncWithSecond, nextKeyFuncWithSecond, hasNextKey);
	}

	@Override
	public ConcurrentLocalTimeMap<V> put(@Nonnull LocalTime key, V value) {
		put(keyToLangFunc.applyAsLong(key), value);
		return this;
	}

	public static void main(String[] args) {

		System.out.println(Long.MAX_VALUE);
		System.out.println(DateTimeUtil.datetimeToMillisecond());

	}

}
