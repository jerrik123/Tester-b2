package com.test;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @ClassName: ArrayComponentTypeTest.java
 * @Description: TODO
 * @author: jie.yang
 * @email: jie.yang@mangocity.com
 * @date: 2016年5月6日 下午1:52:03
 */
public class ArrayComponentTypeTest {

	public static void main(String[] args) {
		Integer[] integerArr = new Integer[3];
		System.out.println(integerArr.getClass().getComponentType());
		
		String[] strArr = (String[]) Array.newInstance(String.class, 3);
		strArr[0] = "hello";
		System.out.println(Arrays.toString(strArr));
		
	}

}
