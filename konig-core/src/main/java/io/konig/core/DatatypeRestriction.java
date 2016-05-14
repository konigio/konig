package io.konig.core;

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


import org.openrdf.model.URI;

public class DatatypeRestriction {
	
	private URI onDatatype;
	private Number minInclusive;
	private Number maxInclusive;
	private Number minExclusive;
	private Number maxExclusive;
	private Integer length;
	private Integer minLength;
	private Integer maxLength;
	private String pattern;
	
	public URI getOnDatatype() {
		return onDatatype;
	}
	public void setOnDatatype(URI onDatatype) {
		this.onDatatype = onDatatype;
	}
	public Number getMinInclusive() {
		return minInclusive;
	}
	public void setMinInclusive(Number minInclusive) {
		this.minInclusive = minInclusive;
	}
	public Number getMaxInclusive() {
		return maxInclusive;
	}
	public void setMaxInclusive(Number maxInclusive) {
		this.maxInclusive = maxInclusive;
	}
	public Number getMinExclusive() {
		return minExclusive;
	}
	public void setMinExclusive(Number minExclusive) {
		this.minExclusive = minExclusive;
	}
	public Number getMaxExclusive() {
		return maxExclusive;
	}
	public void setMaxExclusive(Number maxExclusive) {
		this.maxExclusive = maxExclusive;
	}
	public Integer getLength() {
		return length;
	}
	public void setLength(Integer length) {
		this.length = length;
	}
	public Integer getMinLength() {
		return minLength;
	}
	public void setMinLength(Integer minLength) {
		this.minLength = minLength;
	}
	public Integer getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	
	

}
