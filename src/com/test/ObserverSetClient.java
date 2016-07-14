package com.test;

import java.util.HashSet;

public class ObserverSetClient {

	public static void main(String[] args) {
		/*
		 * ObserverSet<Integer> observerSet = new ObserverSet<Integer>(new
		 * HashSet<Integer>()); observerSet.addObserver(new
		 * SetObserver<Integer>() {
		 * 
		 * @Override public void added(ObserverSet set, Integer e) {
		 * System.out.println("e=" + e); } }); for(int i=0;i<100;i++){
		 * observerSet.add(i); }
		 */

		ObserverSet<Integer> observerSet = new ObserverSet<Integer>(new HashSet<Integer>());
		observerSet.addObserver(new SetObserver<Integer>() {
			@Override
			public void added(ObserverSet set, Integer e) {
				System.out.println("e=" + e);
				if (e == 23) {
					set.removeObserver(this);
				}
			}
		});
		for (int i = 0; i < 100; i++) {
			observerSet.add(i);
		}
	}
}
