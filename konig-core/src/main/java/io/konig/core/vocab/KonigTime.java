package io.konig.core.vocab;

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


import org.openrdf.model.URI;

public enum KonigTime {

	DAY(Konig.Day, 86400000L),
	WEEK(Konig.Week, 604800000L),
	MONTH(Konig.Month, 2628000000L),
	YEAR(Konig.Year, 31540000000L);
	
	private URI iri;
	private long milliseconds;
	
	private KonigTime(URI iri, long milliseconds) {
		this.iri = iri;
		this.milliseconds = milliseconds;
	}

	public URI getIri() {
		return iri;
	}

	/**
	 * The number of milliseconds in this unit of time
	 * @return
	 */
	public long getMilliseconds() {
		return milliseconds;
	}
	
	public static KonigTime fromIri(URI uri) {
		for (KonigTime value : KonigTime.values()) {
			if (value.getIri().equals(uri)) {
				return value;
			}
		}
		return null;
	}
	
	

}
