package io.konig.validation;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


public class RationalNumber {
	
	public static RationalNumber UNDEFINED = new RationalNumber(0,0);

	private int numerator;
	private int denominator;
	
	public RationalNumber(int numerator, int denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	public static RationalNumber combine(RationalNumber...value) {
		int numerator = 0;
		int denominator = 0;
		for (RationalNumber number : value) {
			if (number != null) {
				numerator += number.getNumerator();
				denominator += number.getDenominator();
			}
		}
		
		return new RationalNumber(numerator, denominator);
	}
	
	public static RationalNumber safeValue(RationalNumber value) {
		return value == null ? UNDEFINED : value;
	}

	public int getNumerator() {
		return numerator;
	}

	public int getDenominator() {
		return denominator;
	}
	
	public float getValue() {
		return denominator==0 ? 0 : ((float)numerator)/((float)denominator);
	}
	
	
	public float asPercentage() {
		return getValue()*100;
	}
	
}
