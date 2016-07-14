package com.test;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName: MapConvert2JSON.java
 * @Description: TODO
 * @author: jie.yang
 * @email: jie.yang@mangocity.com
 * @date: 2016年5月6日 下午1:52:03
 */
public class MapConvert2JSON {

	public static void main(String[] args) {
		Map<String,Object> map = new HashMap<String, Object>();
		Map<String,Object> map2 = new HashMap<String, Object>();
		map2.put("hello", "123");
		map2.put("world", "333");
		map.put("passList", map2);
		System.out.println(map);
		
		System.out.println("jsonObj: " + (JSONObject)map);
		
		System.out.println((Map<String,Object>)JSON.toJSON(map));
	}
}
