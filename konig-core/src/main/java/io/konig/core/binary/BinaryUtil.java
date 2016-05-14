package io.konig.core.binary;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BinaryUtil {

	public static int readUnsignedByte(ByteBuffer input) {
		byte value = input.get();
		return value & 0xFF;
	}
	
	public static int readUnsignedShort(ByteBuffer input) {
		short value = input.getShort();
		return value & 0xFFFF;
	}
	
	public static void writeUnsignedByte(DataOutputStream data, int value) throws IOException {
		data.writeByte((byte)value);
	}
	
	public static void writeUnsignedShort(DataOutputStream data, int value) throws IOException {
		data.writeShort((short)value);
	}

	public static void writeString(DataOutputStream data, String value) throws IOException {
		if (value == null) {
			value = "";
		}
		byte[] array = value.getBytes();
		data.write(array);
		data.writeByte(0);
	}
	


	public static String readString(ByteBuffer data) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while (data.hasRemaining()) {
			byte b = data.get();
			if (b == 0) {
				break;
			}
			out.write(b);
		}
		return new String(out.toByteArray());
	}
	
}
