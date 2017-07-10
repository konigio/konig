package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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


public class ShapeBean {

	private String shapeNamespacePrefix;
	private String shapeLocalName;
	
	private String classNamespacePrefix;
	private String classLocalName;
	public String getShapeNamespacePrefix() {
		return shapeNamespacePrefix;
	}
	public void setShapeNamespacePrefix(String shapeNamespacePrefix) {
		this.shapeNamespacePrefix = shapeNamespacePrefix;
	}
	public String getShapeLocalName() {
		return shapeLocalName;
	}
	public void setShapeLocalName(String shapeLocalName) {
		this.shapeLocalName = shapeLocalName;
	}
	public String getClassNamespacePrefix() {
		return classNamespacePrefix;
	}
	public void setClassNamespacePrefix(String classNamespacePrefix) {
		this.classNamespacePrefix = classNamespacePrefix;
	}
	public String getClassLocalName() {
		return classLocalName;
	}
	public void setClassLocalName(String classLocalName) {
		this.classLocalName = classLocalName;
	}
	
	

}
