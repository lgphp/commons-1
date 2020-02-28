package io.mercury.common.sys;

import java.io.File;

import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

import io.mercury.common.collections.ImmutableMaps;
import io.mercury.common.collections.MutableMaps;

public final class SysProperties {

	/**
	 * System.getProperties()
	 */
	public static final ImmutableMap<String, String> All = ImmutableMaps.newMap(() -> {
		MutableMap<String, String> map = MutableMaps.newUnifiedMap();
		System.getProperties().entrySet()
				.forEach(entity -> map.put(entity.getKey().toString(), entity.getValue().toString()));
		return map;
	});

	public static final String get(String key) {
		return All.get(key);
	}

	/*
	 * System.getProperty("os.name")
	 */
	public static final String OS_NAME = System.getProperty("os.name");

	/*
	 * System.getProperty("java.home")
	 */
	public static final String JAVA_HOME = System.getProperty("java.home");

	/*
	 * System.getProperty("java.class.path")
	 */
	public static final String JAVA_CLASS_PATH = System.getProperty("java.class.path");

	/*
	 * System.getProperty("java.version")
	 */
	public static final String JAVA_VERSION = System.getProperty("java.version");

	/*
	 * System.getProperty("java.vm.name")
	 */
	public static final String JAVA_VM_NAME = System.getProperty("java.vm.name");

	/*
	 * System.getProperty("java.runtime.name")
	 */
	public static final String JAVA_RUNTIME_NAME = System.getProperty("java.runtime.name");

	/*
	 * System.getProperty("java.runtime.version")
	 */
	public static final String JAVA_RUNTIME_VERSION = System.getProperty("java.runtime.version");

	/*
	 * System.getProperty("java.io.tmpdir")
	 */
	public static final String JAVA_IO_TMPDIR = System.getProperty("java.io.tmpdir");

	/**
	 * tmpdir file
	 */
	public static final File JAVA_IO_TMPDIR_FILE = new File(JAVA_IO_TMPDIR + "/");

	/*
	 * System.getProperty("user.name")
	 */
	public static final String USER_NAME = System.getProperty("user.name");

	/*
	 * System.getProperty("user.home")
	 */
	public static final String USER_HOME = System.getProperty("user.home");

	/*
	 * user.home file
	 */
	public static final File USER_HOME_FILE = new File(USER_HOME + "/");

	/*
	 * System.getProperty("user.dir")
	 */
	public static final String USER_DIR = System.getProperty("user.dir");

	/**
	 * user.dir file
	 */
	public static final File USER_DIR_FILE = new File(USER_DIR + "/");

	/*
	 * System.getProperty("user.timezone")
	 */
	public static final String USER_TIMEZONE = System.getProperty("user.timezone");

	/*
	 * System.getProperty("user.language")
	 */
	public static final String USER_LANGUAGE = System.getProperty("user.language");

	/*
	 * System.getProperty("user.country")
	 */
	public static final String USER_COUNTRY = System.getProperty("user.country");

	public static void main(String[] args) {
		All.forEachKeyValue((key, value) -> System.out.println(key + " -> " + value));
	}

}
