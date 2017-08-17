package io.konig.dao.core;

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


import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public class BoundaryPoint {
	private DataRange range;
	private String value;
	private BoundaryCondition boundaryCondition;
	public BoundaryPoint(String value, BoundaryCondition boundaryCondition) {
		this.value = value;
		this.boundaryCondition = boundaryCondition;
	}
	public String getValue() {
		return value;
	}
	public BoundaryCondition getBoundaryCondition() {
		return boundaryCondition;
	}
	
	public DataRange getRange() {
		return range;
	}
	
	void setRange(DataRange range) {
		this.range = range;
	}
	
	
	public DateTime toDateTime() {
		return ISODateTimeFormat.dateTime().parseDateTime(value);
	}

}
