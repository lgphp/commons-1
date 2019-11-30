package io.mercury.common.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.primitive.MutableDoubleSet;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.DoubleHashSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;
import org.eclipse.collections.impl.set.sorted.mutable.TreeSortedSet;
import org.eclipse.collections.impl.utility.Iterate;

import io.mercury.common.utils.ArrayUtil;

public final class MutableSets {

	private MutableSets() {
	}

	/**
	 * primitive set
	 */
	public static MutableIntSet newIntHashSet() {
		return new IntHashSet();
	}

	public static MutableIntSet newIntHashSet(InitialCapacity capacity) {
		return new IntHashSet(capacity.size());
	}

	public static MutableIntSet newIntHashSetWith(int... intValues) {
		if (ArrayUtil.isNullOrEmpty(intValues))
			return newIntHashSet();
		return new IntHashSet(intValues);
	}

	public static MutableDoubleSet newDoubleHashSet() {
		return new DoubleHashSet();
	}

	public static MutableDoubleSet newDoubleHashSet(InitialCapacity capacity) {
		return new DoubleHashSet(capacity.size());
	}

	public static MutableDoubleSet newDoubleHashSetWith(double... doubleValues) {
		if (ArrayUtil.isNullOrEmpty(doubleValues))
			return newDoubleHashSet();
		return new DoubleHashSet(doubleValues);
	}

	public static MutableLongSet newLongHashSet() {
		return new LongHashSet();
	}

	public static MutableLongSet newLongHashSet(InitialCapacity capacity) {
		return new LongHashSet(capacity.size());
	}

	public static MutableLongSet newLongHashSetWith(long... longValues) {
		if (ArrayUtil.isNullOrEmpty(longValues))
			return newLongHashSet();

		return new LongHashSet(longValues);
	}

	/**
	 * set
	 */
	public static <E> MutableSet<E> newUnifiedSet() {
		return new UnifiedSet<>();
	}

	public static <E> MutableSet<E> newUnifiedSet(InitialCapacity capacity) {
		return new UnifiedSet<>(capacity.size());
	}

	public static <E> MutableSet<E> newUnifiedSet(Iterator<E> iterator) {
		MutableSet<E> newUnifiedSet = newUnifiedSet();
		if (iterator != null && iterator.hasNext()) {
			while (iterator.hasNext())
				newUnifiedSet.add(iterator.next());
		}
		return newUnifiedSet;
	}

	public static <E> MutableSet<E> newUnifiedSet(Collection<E> collection) {
		if (collection == null || collection.isEmpty())
			return newUnifiedSet();
		return new UnifiedSet<>(collection);
	}

	public static <E> MutableSortedSet<E> newTreeSortedSet() {
		return new TreeSortedSet<>();
	}

	public static <E> MutableSortedSet<E> newTreeSortedSet(Iterable<E> iterable) {
		if (Iterate.isEmpty(iterable))
			return newTreeSortedSet();
		return new TreeSortedSet<>(iterable);
	}

	public static <E> MutableSortedSet<E> newTreeSortedSet(Comparator<E> comparator) {
		if (comparator == null)
			return newTreeSortedSet();
		return new TreeSortedSet<>(comparator);
	}

}