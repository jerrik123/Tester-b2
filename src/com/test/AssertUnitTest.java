package com.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class AssertUnitTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testAssert1() {
		assert false;
		System.out.println(1);
	}

}
