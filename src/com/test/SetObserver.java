package com.test;

public interface SetObserver<E> {

	void added(ObserverSet<E> set,E e);
}
