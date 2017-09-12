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
import org.joda.time.Period;

public class DateTimeCategories implements ChartCategories {
	private DataRange range;
	private DateTime start;
	private DateTime end;
	private Period interval;

	public DateTimeCategories(DateTime start, DateTime end, Period interval) {
		this.start = start;
		this.end = end;
		this.interval = interval;
	}
	
	

	public DateTimeCategories(DataRange range, DateTime start, DateTime end, Period interval) {
		this.range = range;
		this.start = start;
		this.end = end;
		this.interval = interval;
	}



	@Override
	public Iterator<? extends Object> iterator() {
		return new DateTimeIterator(start, end, interval);
	}


	public DataRange getRange() {
		return range;
	}



	public Period getInterval() {
		return interval;
	}
	
	

}
