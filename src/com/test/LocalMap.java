package com.test;

public interface LocalMap<K,V> {

	int size();
	
	void remove();
	
	boolean isEmpty();
	
	interface LocalEntry<K,V>{
		V getValue(K k);
	}
}
