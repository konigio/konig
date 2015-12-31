package io.konig.core.impl;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
