package com.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDateFormatTest {

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		/*for(int i=0;i<100000000;i++){
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
			f.format(new Date());
		}*/
		long end = System.currentTimeMillis();
		System.out.println("time: " + (end-start)/100.0);
		
		System.out.println("-----------------");
		
		start = System.currentTimeMillis();
		for(int i=0;i<100000000;i++){
			DateFormat f = DateFormatUtil.getDateFormat();
			f.format(new Date());
		}
		end = System.currentTimeMillis();
		System.out.println("times: " + (end-start)/100.0);
		
		System.out.println("-----------------");
		
		start = System.currentTimeMillis();
		for(int i=0;i<100000000;i++){
			DateFormat f = DateFormatUtil.getDateFormat2();
			f.format(new Date());
		}
		end = System.currentTimeMillis();
		System.out.println("times: " + (end-start)/100.0);
	}

}
