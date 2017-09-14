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


import java.io.IOException;
import java.io.Writer;

import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class ChartWriterFactory {
	private static final String HOUR = "HH:00 on MMM dd, yyyy";
	private static final String DATE = "MMM dd, yyyy";
	private static final String MONTH = "MMM, yyyy";
	private static final String YEAR = "yyyy";
	

	public ChartWriter createChartWriter(Chart chart, Writer out) throws DaoException {
		
		
		switch (chart.getKey().toString()) {
		case FusionCharts.MSLINE_KEY :
			return new FusionMultiLineChartWriter(jsonGenerator(out), categoryFormatter(chart), dataFormatter(chart));
		case FusionCharts.MAP_KEY :
			return new FusionMapChartWriter(jsonGenerator(out), dataFormatter(chart));
		case FusionCharts.PIE_KEY :
			return new FusionPieChartWriter(jsonGenerator(out), dataFormatter(chart));
		case FusionCharts.BAR_KEY :
			return new FusionBarChartWriter(jsonGenerator(out), categoryFormatter(chart), dataFormatter(chart));
		}
		
		throw new DaoException("Unsupported ChartWriter: " + chart.getKey().toString());
	}

	private Formatter dataFormatter(Chart chart) {
		return new SimpleFormatter();
	}

	private JsonGenerator jsonGenerator(Writer out) throws DaoException {
		JsonFactory factory = new JsonFactory();
		try {
			return factory.createGenerator(out);
		} catch (IOException e) {
			throw new DaoException(e);
		}
	}

	private Formatter categoryFormatter(Chart chart) {
		
		ChartCategories categories = chart.getCategories();
		if (categories instanceof DateTimeCategories) {
			String pattern = null;
			DateTimeCategories c = (DateTimeCategories) categories;
			Period period = c.getInterval();
			
			if (period.getHours() > 0) {
				pattern = HOUR;
			} else if (period.getDays()>0) {
				pattern = DATE;
			} else if (period.getMonths()>0) {
				pattern = MONTH;
			} else if (period.getYears()>0) {
				pattern = YEAR;
			} else {
				throw new RuntimeException("Unsupported period: " + period.toString());
			}
			return new TemporalFormatter(DateTimeFormat.forPattern(pattern));
		} 
		if (categories instanceof LabelCategories) {
			return new Formatter() {				
				@Override
				public String format(Object value) {
					return (String) value;
				}
			};
		}
		// TODO: support other categories
		throw new RuntimeException("Category type not supported");
		
		
	}
}
