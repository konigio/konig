package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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


public class Link implements Comparable<Link> {

	private String name;
	private String href;
	private String className;
	private String iconSrc;
	
	public Link(String name, String href) {
		this.name = name;
		this.href = href;
	}

	public Link(String name, String href, String className) {
		this.name = name;
		this.href = href;
		this.className = className;
	}
	
	

	public Link(String name, String href, String className, String iconSrc) {
		this.name = name;
		this.href = href;
		this.className = className;
		this.iconSrc = iconSrc;
	}

	public String getClassName() {
		return className;
	}

	public String getName() {
		return name;
	}

	public String getHref() {
		return href;
	}

	@Override
	public int compareTo(Link o) {
		return name.compareToIgnoreCase(o.getName());
	}

	public static Link create(String name, String href) {
		if (name != null) {
			return new Link(name, href);
		}
		return null;
	}
	
	public String getIconSrc() {
		return iconSrc;
	}
	
}
