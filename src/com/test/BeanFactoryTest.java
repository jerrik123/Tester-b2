package com.test;

public class BeanFactoryTest {

	public static void main(String[] args) {
		getBean("",String.class);
	}
	
	public static <T> T getBean(String id,Class<T> type){
		return type.cast(new Object());
	}

}
