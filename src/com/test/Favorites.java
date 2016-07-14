package com.test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Favorites {

	private Map<Class<?>,Object> favoritesMap = new HashMap<Class<?>,Object>();
	
	public <T> void putFavorite(Class<T> type,T instance){
		favoritesMap.put(type, instance);
	}
	
	public <T> T getFavorite(Class<T> type){
		return type.cast(favoritesMap.get(type));
	}
	
	public static void main(String[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Favorites favorites = new Favorites();
		favorites.putFavorite(Integer.class, 123);
		
		List<String> list = new ArrayList<String>();
		list.add("1");
		list.getClass().getMethod("add", Object.class).invoke(list, 4);
		System.out.println(list.toString());
	}
}
