package io.konig.core.vocab;

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
import org.openrdf.model.impl.URIImpl;

public enum TemporalUnit {

	YEAR("http://www.w3.org/2006/time#unitYear"),
	MONTH("http://www.w3.org/2006/time#unitMonth"),
	WEEK("http://www.w3.org/2006/time#unitWeek"),
	DAY("http://www.w3.org/2006/time#unitDay"),
	HOUR("http://www.w3.org/2006/time#unitHour"),
	MINUTE("http://www.w3.org/2006/time#unitMinute"),
	SECOND("http://www.w3.org/2006/time#unitSecond") ;
	
	private URI uri;
	
	
	private TemporalUnit(String value) {
		uri = new URIImpl(value);
	}
	
	public URI getURI() {
		return uri;
	}
	
	public String toString() {
		return uri.getLocalName();
	}
	
	public static TemporalUnit fromURI(URI value) {
		
		for (TemporalUnit t : values()) {
			if (t.getURI().equals(value)) {
				return t;
			}
		}
		
		return null;
	}
}
