package io.ffreedom.common.collect;

import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.impl.map.immutable.ImmutableMapFactoryImpl;

public final class ImmutableMaps {

	private ImmutableMaps() {
	}

	/**
	 * immutable map
	 */
	public static <K, V> ImmutableMap<K, V> newImmutableMap(Supplier<Map<K, V>> supplier) {
		if (supplier == null)
			return ImmutableMapFactoryImpl.INSTANCE.empty();
		return newImmutableMap(supplier.get());
	}

	public static <K, V> ImmutableMap<K, V> newImmutableMap(Map<K, V> map) {
		if (map == null || map.isEmpty())
			return ImmutableMapFactoryImpl.INSTANCE.empty();
		return ImmutableMapFactoryImpl.INSTANCE.withAll(map);
	}

}