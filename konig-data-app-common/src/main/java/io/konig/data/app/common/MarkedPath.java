package io.konig.data.app.common;

/*
 * #%L
 * Konig DAO Core
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


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class MarkedPath {
	
	private String path;
	private String[] elements;
	private int mark;
	
	public MarkedPath(String path) {
		this.path = path;
		elements = path.split("/");
		for (int i=0; i<elements.length; i++) {
			try {
				elements[i] = URLDecoder.decode(elements[i], StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public String currentElement() {
		return (mark>=0 && mark<elements.length) ? elements[mark] : null;
	}
	
	
	public boolean hasNext() {
		return mark<(elements.length-1);
	}
	
	public String next() {
		mark++;
		return currentElement();
	}

	public String getPath() {
		return path;
	}

	public String[] getElements() {
		return elements;
	}

	public int getMark() {
		return mark;
	}

	public void setMark(int mark) {
		this.mark = mark;
	}

}
