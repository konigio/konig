package io.konig.core.impl;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;

import io.konig.core.UidGenerator;

public class UidGeneratorImpl implements UidGenerator {
	
	private Random random = new Random(new Date().getTime());
	

	@Override
	public String next() {
		
		ByteBuffer buffer = ByteBuffer.allocate(16);
		
		long random = this.random.nextLong();
		long date = new Date().getTime();
		
		buffer.putLong(random);
		buffer.putLong(date);
		
		byte[] array = buffer.array();
		
		return Base64.encodeBase64URLSafeString(array);
	}
	
	


}
