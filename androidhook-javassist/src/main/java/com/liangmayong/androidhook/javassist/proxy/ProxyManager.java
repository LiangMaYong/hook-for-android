package com.liangmayong.androidhook.javassist.proxy;

import java.io.File;

import android.app.Application;
import android.content.Context;
import javassist.ClassPool;

public final class ProxyManager {

	private static boolean isInit = false;
	private static String DEX_FILE_DIR = "";
	private static Application application;

	public static boolean isInit() {
		return isInit;
	}

	/**
	 * init
	 * 
	 * @param application
	 *            application
	 */
	public static void init(Application application) {
		if (isInit) {
			return;
		}
		File file = application.getDir("androidproxy", Context.MODE_PRIVATE);
		if (!file.exists()) {
			file.mkdirs();
		}
		ProxyManager.application = application;
		DEX_FILE_DIR = file.getAbsolutePath();
		ProxyManager.isInit = true;
	}

	/**
	 * getDexCacheDir
	 * 
	 * @return dexcache dir
	 */
	public static String getDexCacheDir() {
		if (!isInit) {
			return "";
		}
		return application.getCacheDir().getAbsolutePath();
	}

	/**
	 * getClassLoader
	 * 
	 * @return classloader
	 */
	public static ClassLoader getClassLoader() {
		if (!isInit) {
			return ClassLoader.getSystemClassLoader();
		}
		return application.getClassLoader();
	}

	/**
	 * getDexFileDir
	 * 
	 * @return dexfile dir
	 */
	public static String getDexFileDir() {
		if (!isInit) {
			return "";
		}
		return DEX_FILE_DIR;
	}

	/**
	 * getClassPool
	 * 
	 * @return pool
	 */
	public static ClassPool getClassPool() {
		if (!isInit) {
			return null;
		}
		try {
			ClassPool pool = ClassPool.getDefault(application.getApplicationContext());
			return pool;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
