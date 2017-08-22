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


import java.util.Iterator;

import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;

public class DateTimeIterator implements Iterator<DateTime> {
	private DateTime end;
	private ReadablePeriod interval;
	private DateTime current;

	public DateTimeIterator(DateTime start, DateTime end, ReadablePeriod interval) {
		current = start;
		this.end = end;
		this.interval = interval;
	}

	@Override
	public boolean hasNext() {
		return current != null;
	}

	@Override
	public DateTime next() {
		DateTime result = current;
		if (current != null) {
			current = current.plus(interval);
			if (current.compareTo(end) > 0) {
				current = null;
			}
		}
		
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
		
	}

}
