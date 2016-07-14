package com.enumeration;

import java.util.EnumMap;
import java.util.EnumSet;

public class EnumTest {

	public static void main(String[] args) {
		/*
		 * FruitsEnum[] fruits = FruitsEnum.values(); String ok = "Hello";
		 * double points = 3.2; int o = 25; for(FruitsEnum fruit : fruits){
		 * System
		 * .out.printf("he say %s,and her points is %+.0f,16进制是%#x%n",ok,points
		 * ,o); } System.out.println(FruitsEnum.Apple);
		 */

		EnumMap<FruitsEnum, String> enumMap = new EnumMap<FruitsEnum, String>(FruitsEnum.class);

		EnumSet<FruitsEnum> currEnumSet = EnumSet.allOf(FruitsEnum.class);
		for (FruitsEnum aLightSetElement : currEnumSet) {
			System.out.println(" 当前 EnumSet 中数据为： " + aLightSetElement);
		}
	}

}
