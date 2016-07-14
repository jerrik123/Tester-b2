package com.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ObserverSet<E> extends ArrayList<E> {
	public ObserverSet(Set<E> set) {
		super(set);
	}

	private List<SetObserver<E>> observersList = new ArrayList<SetObserver<E>>();

	public void addObserver(SetObserver<E> observer) {
		System.out.println("addObserver begin()...");
		synchronized (observersList) {
			observersList.add(observer);
		}
		System.out.println("addObserver end()...");
	}

	public void removeObserver(SetObserver<E> observer) {
		System.out.println("removeObserver begin()...");
		synchronized (observersList) {
			observersList.remove(observer);
		}
		System.out.println("removeObserver end()...");
	}

	public void notifyAddElement(E ele) {
		System.out.println("notifyAddElement begin()...");
		synchronized (observersList) {
			for (SetObserver<E> observer : observersList) {
				observer.added(this, ele);
			}
		}
		System.out.println("notifyAddElement begin()...");
	}

	public boolean add(E e) {
		System.out.println("add begin()...");
		boolean added = super.add(e);
		if (added) {
			notifyAddElement(e);
		}
		return added;
	}

}
