package io.konig.core.util;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import java.util.Date;
import java.util.Random;

public class RandomGenerator {
	
	private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
	
	private static final String[] LOREM = ("Lorem ipsum dolor sit amet consectetur adipiscing elit "
			+ "Donec eu scelerisque justo ac egestas dolor Sed tempor leo id vestibulum tincidunt "
			+ "purus ipsum pellentesque magna sed dignissim odio mauris sed magna Morbi ac viverra "
			+ "sapien Nullam eu elit a sem convallis consectetur Phasellus ut mollis ex Pellentesque "
			+ "imperdiet sapien sed purus posuere placerat Praesent laoreet purus elementum dui "
			+ "suscipit et congue nisi tempus Sed eu suscipit justo a congue dui Aenean efficitur "
			+ "nisl eget fringilla lobortis Interdum et malesuada fames ac ante ipsum primis in faucibus "
			+ "Nam pellentesque nulla sit amet justo volutpat eget accumsan lacus vulputate Nunc "
			+ "euismod lectus vel nisl consequat auctor Praesent blandit purus fringilla magna blandit "
			+ "blandit Aliquam et lorem ut ante eleifend commodo at ut nisl Proin ut malesuada "
			+ "libero Nam vel neque vitae leo viverra dictum Sed pulvinar lorem in leo bibendum "
			+ "semper Nam consectetur diam et venenatis hendrerit Nulla euismod ante id velit faucibus "
			+ "eget pulvinar nisi porttitor Vestibulum aliquet quam nec risus vehicula sollicitudin Donec "
			+ "at ornare libero Curabitur sodales tortor non semper consectetur Pellentesque a mattis "
			+ "libero In vel efficitur metus quis accumsan massa Donec nisi tortor ultricies sit amet "
			+ "tellus nec bibendum euismod libero Praesent quis lorem at felis ultrices suscipit eget sed "
			+ "neque Suspendisse purus turpis pharetra ut hendrerit a porttitor eget odio Duis commodo "
			+ "porta tellus eget efficitur sapien iaculis sed Nulla tincidunt nisi sed sem ullamcorper "
			+ "auctor Ut luctus feugiat ex vitae ultrices turpis posuere id In et auctor est ut pharetra "
			+ "turpis Morbi vulputate velit urna quis dictum metus luctus eget Morbi eget blandit magna "
			+ "Mauris quis enim ut enim gravida euismod at sed metus Nam porta orci vitae leo porttitor "
			+ "vitae mattis neque tincidunt Vestibulum et venenatis augue nec venenatis lacus Vivamus "
			+ "imperdiet orci sed tellus aliquet quis volutpat libero imperdiet Mauris viverra erat lacus "
			+ "ut mollis justo dapibus a Ut mollis felis lacus sed posuere mi pulvinar sit amet Etiam a "
			+ "dolor gravida tincidunt magna in condimentum elit Nunc eget sem id mauris malesuada aliquet "
			+ "id eu lorem Sed leo nunc gravida et odio non pharetra cursus augue Fusce pretium eros vel "
			+ "dolor maximus faucibus Integer mauris lectus aliquam quis dictum ac rutrum ut erat In "
			+ "ultrices elit sed iaculis feugiat Nulla et metus fermentum maximus lorem sit amet malesuada "
			+ "enim Cras eget bibendum erat Ut at leo est Donec dictum odio vitae pulvinar molestie Mauris "
			+ "lacus lectus maximus quis bibendum eu fermentum eu turpis Donec tempor massa non leo eleifend "
			+ "euismod nec sed quam Pellentesque fringilla lorem a congue feugiat Praesent eu magna mollis "
			+ "nunc euismod suscipit Quisque libero ipsum interdum sit amet quam at auctor tristique augue "
			+ "Sed faucibus sit amet tortor sit amet porta Donec elit quam finibus ac ex ac consequat "
			+ "tincidunt turpis Nunc consequat fermentum dui bibendum dignissim Praesent mollis dignissim "
			+ "mauris at tincidunt").split(" ");
	

	
	private Random random;
	
	public RandomGenerator() {
		random = new Random(new Date().getTime());
	}
	
	public RandomGenerator(long seed) {
		random = new Random(seed);
	}
	
	public String loremIpsum(int wordCount) {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<wordCount; i++) {
			int index = random.nextInt(LOREM.length);
			String word = LOREM[index];
			if (i==0) {
				word = StringUtil.capitalize(word);
			} else {
				builder.append(' ');
			}
			builder.append(word);
		}
		return builder.toString();
	}
	
	public String alphanumeric(int length) {
		StringBuilder builder = new StringBuilder();
		length--;
		int k = random.nextInt(52);
		char c = ALPHANUMERIC.charAt(k);
		builder.append(c);
		for (int i=0; i<length; i++) {
			int index = random.nextInt(ALPHANUMERIC.length());
			c = ALPHANUMERIC.charAt(index);
			builder.append(c);
		}
		
		return builder.toString();
	}

	public boolean nextBoolean() {
		return random.nextBoolean();
	}

	public byte nextByte() {
		return (byte) random.nextInt(Byte.MAX_VALUE);
	}

	/**
	 * Return the next long value within a given range
	 */
	public long nextLong(long start, long end) {
		
		long delta = end - start;
		long value = Math.abs(random.nextLong());
		delta = value%delta;
		return start + delta;
		
	}

	public int nextInt(int maxValue) {
		return random.nextInt(maxValue);
	}
}
