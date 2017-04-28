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

import io.konig.core.util.ValueFormat.Element;

public class SimpleValueFormat implements ValueFormat {


	protected String text;
	protected List<Element> elements;
	
	public SimpleValueFormat() {}
	
	public SimpleValueFormat(String text) {
		if (text != null) {
			this.text = text.trim();
			
			compile();
		}
		
	}
	
	protected Element createVariable(String text) {
		return new Variable(text);
	}

	protected void compile() {
		
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
				elements.add(createVariable(value));
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
			if (value == null) {
				builder.append('{');
				builder.append(e.text);
				builder.append('}');
			} else {
				value = ValueExpander.expand(value, map);
				builder.append(value);
			}
		}
		
		return builder.toString();
		
	}
	public String toString() {
		return text;
	}
	static class Element implements ValueFormat.Element {
		protected String text;
		
		
		
		public Element(String text) {
			this.text = text;
		}

		String get(ValueMap map) {
			return text;
		}
		
		public String toString() {
			return text;
		}

		@Override
		public ElementType getType() {
			return ValueFormat.ElementType.TEXT;
		}

		@Override
		public String getText() {
			return text;
		}
	}
	
	static class Variable extends Element {
		
		public Variable(String text) {
			super(text);
		}


		@Override
		public ElementType getType() {
			return ValueFormat.ElementType.VARIABLE;
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
	public String getPattern() {
		return text;
	}

	@Override
	public List<? extends ValueFormat.Element> toList() {
		return elements;
	}

	@Override
	public void addText(String text) {
		if (elements == null) {
			elements = new ArrayList<>();
		}
		elements.add(new Element(text));
		if (this.text == null) {
			this.text = text;
		} else {
			this.text += text;
		}
		
	}

	@Override
	public void addVariable(String variable) {
		if (elements == null) {
			elements = new ArrayList<>();
		}
		elements.add(new Variable(variable));
		StringBuilder builder = this.text==null ? new StringBuilder() : new StringBuilder(this.text);
		
		builder.append('{');
		builder.append(variable);
		builder.append('}');
		this.text = builder.toString();
	}
	
	
}
