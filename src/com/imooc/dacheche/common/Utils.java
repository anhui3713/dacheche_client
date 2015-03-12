package com.imooc.dacheche.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

	
	private static SimpleDateFormat sdf = new SimpleDateFormat();
	
	public static String format(Date source, String pattern) {
		if(source == null) {
			return "";
		}
		sdf.applyPattern(pattern);
		return sdf.format(source);
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void delay(final Runnable run, final long millis) {
		synchronized (run) {
			new Thread() {
				public void run() {
					Utils.sleep(millis);
					run.run();
				}
			}.start();
		}
	}
}
