package com.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtil {
	private DateFormatUtil() {}

	private static DateFormat format = null;
	private static final Object lock = new Object();

	public static DateFormat getDateFormat() {
		if (format == null) {
			synchronized (lock) {
				if (null == format) {
					format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				}
			}
		}
		return format;
	}
	
	public static DateFormat getDateFormat2() {
		if (format == null) {
				if (null == format) {
					format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				}
		}
		return format;
	}
}
