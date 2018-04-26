package io.konig.maven.project.generator;

/*
 * #%L
 * Konig Maven Project Generator
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


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

import io.konig.datasource.DdlFileLocator;

public class XmlSerializer {
	
	private int indentWidth = 3;

	private PrintWriter out;
	private int indent;
	
	public XmlSerializer(Writer out) {
		this.out = out instanceof PrintWriter ? (PrintWriter) out : new PrintWriter(out);
		indent = 0;
	}
	
	public void flush() {
		out.flush();
	}
	
	public int getIndentWidth() {
		return indentWidth;
	}


	public void setIndentWidth(int indentWidth) {
		this.indentWidth = indentWidth;
	}


	public int getIndent() {
		return indent;
	}



	public void setIndent(int indent) {
		this.indent = indent;
	}



	public void write(Object pojo, String tag) {
		beginTag(tag);
		out.println();
		
		if (pojo.getClass().isArray() && pojo.getClass().getComponentType()==FileSet.class) {
			// TODO: Special handling to serialize array of FileSet instances.
			printArray((FileSet[]) pojo);
		} else if (pojo instanceof Collection<?>) {
			printCollection((Collection<?>) pojo);
		} else {
			printProperties(pojo);
		}
		indent();
		endTag(tag);
		
	}


	

	private void printCollection(Collection<?> container) {
		push();
		for (Object e : container) {
			Class<?> type = e.getClass();
			indent();
			if (type == String.class) {
				printSimpleValue(e, "param");
			} else {
				String tag = tagName(type.getSimpleName());
				write(e, tag);
			}
		}
		pop();
		
	}

	private String tagName(String simpleName) {
		StringBuilder builder = new StringBuilder();
		builder.append(Character.toLowerCase(simpleName.charAt(0)));
		for (int i=1; i<simpleName.length(); i++) {
			builder.append(simpleName.charAt(i));
		}
		return builder.toString();
	}

	public void printProperties(Object pojo) {
		
		push();
		List<Field> fieldList = new ArrayList<>();
		addFields(pojo.getClass(), fieldList);
		for (Field field : fieldList) {
			field.setAccessible(true);
			try {
				Object value = field.get(pojo);
				if (value != null) {
					indent();
					String fieldName = field.getName();
					if (isSimpleValue(value)) {
						printSimpleValue(value, fieldName);
					} else {
						write(value, fieldName);
					}
					
				}
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		pop();
		
	}

	private void printSimpleValue(Object value, String fieldName) {

		beginTag(fieldName);
		String text = value.toString();
		if (value instanceof File) {
			text = text.replace('\\', '/');
		}
		out.print(text);
		endTag(fieldName);
		
	}

	private boolean isSimpleValue(Object value) {
		
		return 
			(value instanceof String) ||
			(value instanceof File) ||
			(value.getClass() == Boolean.class);
	}



	private void beginTag(String tag) {
		out.print('<');
		out.print(tag);
		out.print('>');
		
	}
	
	private void endTag(String tag) {
		out.print("</");
		out.print(tag);
		out.println('>');
		
	}


	public void indent() {
		for (int i=0; i<indent*indentWidth; i++) {
			out.print(' ');
		}
		
	}

	private void addFields(Class<?> type, List<Field> fieldList) {
		
		Field[] array = type.getDeclaredFields();
		for (Field field : array) {
			if (!Modifier.isStatic(field.getModifiers())) {
				fieldList.add(field);
			}
		}
		
		Class<?> superClass = type.getSuperclass();
		if (superClass != Object.class && superClass!=null) {
			addFields(superClass, fieldList);
		}
		
	}

	private void push() {
		indent++;
	}
	
	private void pop() {
		indent--;
	}
	
	
	private void printArray(FileSet[] pojo){

		if (pojo != null) {
			
			for (FileSet fileset : pojo) {
				printProperties(fileset);
			}

		}

	}

}
