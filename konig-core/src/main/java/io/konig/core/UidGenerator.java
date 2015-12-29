package io.konig.core;

import io.konig.core.impl.UidGeneratorImpl;

public interface UidGenerator {
	
	public static UidGenerator INSTANCE = new UidGeneratorImpl();
	
	String next();

}
