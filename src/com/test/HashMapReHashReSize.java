package com.test;

import java.util.HashMap;
import java.util.Map;

public class HashMapReHashReSize {

	public static void main(String[] args) {
		Map<String,String> map = new HashMap<String,String>(16);
		for(int i=0;i<10;i++){
			map.put("key_"+i, "value_" + i++);
		}
		System.out.println(map.entrySet());
		
		for(int i=0;i<10;i++){
			map.put("key="+i, "value=" + i++);
		}
		
		System.out.println(map.entrySet());
		/**
		 * [null, key_6=value_6, null, key_4=value_4, null, key_2=value_2, null, key_0=value_0, null, null, null, null, null, null, null, key_8=value_8]
		 */
	}

}
