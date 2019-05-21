package io.konig.core.util;

import java.util.List;

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


public interface ValueFormat {

	String format(ValueMap map);
	
	void traverse(ValueFormatVisitor visitor);
	
	String getPattern();
	
	List<? extends Element> toList();
	
	void addText(String text);
	void addVariable(String variable);
	
	public static enum ElementType {
		TEXT,
		VARIABLE
	}
	
	public interface Element {
		ElementType getType();
		String getText();
		Element clone();
	}
}
