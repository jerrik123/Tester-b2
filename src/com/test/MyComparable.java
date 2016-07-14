package com.test;

public class MyComparable {
	@SuppressWarnings("unused")
	private static class Pair<K,V>{
		public final K min;
		public final V max;
		public Pair(K min, V max) {
			super();
			this.min = min;
			this.max = max;
		}
		public K getMin() {
			return min;
		}
		public V getMax() {
			return max;
		}
	}

	public static <T extends Comparable<? super T>> Pair<T,T> minmax(T[] array){
	    T temp;
        for(int i = 0; i < array.length - 1; i++) {
            for(int j = 0; j < array.length - i - 1; j++) {
                if(array[j].compareTo(array[j + 1]) > 0) {
                    temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
        return new Pair<T, T>(array[0],array[array.length-1]);
    }
	
	public static void main(String[] args) {
		String[] strs = {"123","hello","224","abc","www"};
		Integer[] intArr = {7,2,1,31,44,65,4};
		Pair<Integer,Integer> pair = minmax(intArr);
		System.out.println(pair.getMin());
		System.out.println(pair.getMax());
	}
}
