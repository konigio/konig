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

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;

public class SimpleValueFormat implements ValueFormat {


	private String text;
	private List<Element> elements;
	
	
	public SimpleValueFormat(String text) {
		this.text = text.trim();
		
		compile();
		
	}

	private void compile() {
		
		elements = new ArrayList<>();
		
		StringBuilder buffer = new StringBuilder();
		
		for (int i=0; i<text.length(); ) {
			int c = text.codePointAt(i);
			i += Character.charCount(c);
			
			if (c=='{') {
				if (buffer.length()>0) {
					String value = buffer.toString();
					elements.add(new Element(value));
					buffer = new StringBuilder();
				}
			} else if (c=='}') {
				String value = buffer.toString();
				elements.add(new Variable(value));
				buffer = new StringBuilder();
			} else {
				buffer.appendCodePoint(c);
			}
		}
		
		if (buffer.length()>0) {
			String value = buffer.toString();
			elements.add(new Element(value));
		}
		
	}

	@Override
	public String format(ValueMap map) {
		StringBuilder builder = new StringBuilder();
		for (Element e : elements) {
			String value = e.get(map);
			builder.append(value);
		}
		
		return builder.toString();
		
	}
	
	public String toString() {
		return text;
	}
	private static class Element {
		protected String text;
		
		
		
		public Element(String text) {
			this.text = text;
		}

		String get(ValueMap map) {
			return text;
		}
	}
	
	private static class Variable extends Element {
		
		public Variable(String text) {
			super(text);
		}

		String get(ValueMap map) {
			return map.get(text);
		}
	}

	@Override
	public void traverse(ValueFormatVisitor visitor) {
		
		for (Element e : elements) {
			if (e instanceof Variable) {
				visitor.visitVariable(e.text);
			} else {
				visitor.visitText(e.text);
			}
		}
		
	}
	
	public Value toValue() {
		return new LiteralImpl(text);
	}

	@Override
	public List<String> listVariables() {
		List<String> result = new ArrayList<>();
		for (Element e : elements) {
			if (e instanceof Variable) {
				result.add(e.text);
			}
		}
		
		return result;
	}
	
}
