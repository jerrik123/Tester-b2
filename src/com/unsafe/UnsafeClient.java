package com.unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import sun.misc.Unsafe;

public class UnsafeClient {

	public static void main(String[] args) throws SecurityException, NoSuchMethodException, IllegalArgumentException,
			InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		Constructor<Unsafe> c = Unsafe.class.getDeclaredConstructor();
		c.setAccessible(true);
		Unsafe unsafe = c.newInstance(null);
		/*
		 * Demo demo = (Demo) unsafe.allocateInstance(Demo.class); demo.show();
		 */
		Guard guard = new Guard();
		guard.giveAccess(); // false, no access
		
		// Unsafe unsafe = getUnsafe();
		Field f = guard.getClass().getDeclaredField("ACCESS_ALLOWED");
		unsafe.putInt(guard, unsafe.objectFieldOffset(f), 42); // memory
		
		guard.giveAccess(); // true, access granted

	}

	private class Demo {
		private long index = 3;

		public Demo() {
			System.out.println("构造");
		}

		@SuppressWarnings("unused")
		public void show() {
			System.out.println("index= " + index);
		}
	}

	static class Guard {
		private int ACCESS_ALLOWED = 1;

		public boolean giveAccess() {
			return 42 == ACCESS_ALLOWED;
		}
	}
}
