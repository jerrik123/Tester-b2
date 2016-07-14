package com.log;

import org.apache.log4j.Logger;

public class Log4jTester {

	private static Logger LOGGER = Logger.getLogger(Log4jTester.class);
	
	public static void main(String[] args) {
		
		/*LOGGER.debug("debug");
		LOGGER.warn("ware");
		LOGGER.info("info");
		LOGGER.error("bad error.");
		try {
			throw new NullPointerException("空指针异常");
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}*/
		show();
	}
	
	public static void show(){
		LOGGER.info("show begin()...");
		LOGGER.info("result: 0");
		LOGGER.debug("haha");
		LOGGER.error("参数越界异常...");
	}
}
