package io.konig.core.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class UidGeneratorImplTest {

	@Test
	public void test() {
		UidGeneratorImpl generator = new UidGeneratorImpl();
		
		String value = generator.next();
	}

}
