package com.test;

import java.util.HashMap;
import java.util.Map;

public class CloneMap {

	public static void main(String[] args) {
		/*HashMap<String,String> map = new HashMap<String,String>();
		map.put("hello", "world");
		map.put("show", "girl");
		
		HashMap<String,String> tempMap = (HashMap<String, String>) map.clone();
		System.out.println("tempMap="+tempMap);
		map.put("1", "2");
		System.out.println("tempMap="+tempMap);
		System.out.println("map=" + map);*/
		
		Map<String,String> map = new HashMap<String,String>();
		map.put("hello", "world");
		map.put("show", "girl");
		
		Map<String,String> map1 = new HashMap<String, String>(map);
		map.put("hh", "11");
		System.out.println("map1: " + map1);
		System.out.println("map: " + map);
		
		
	}

}
