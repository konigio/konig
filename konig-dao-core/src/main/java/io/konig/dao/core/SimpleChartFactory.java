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


import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.sql.runtime.EntityStructure;

public class SimpleChartFactory implements ChartFactory {
	
	private static final String MIN_INCLUSIVE = ".minInclusive";
	private static final String MIN_EXCLUSIVE = ".minExclusive";
	private static final String MAX_INCLUSIVE = ".maxInclusive";
	private static final String MAX_EXCLUSIVE = ".maxExclusive";
	private static final String INTERVALSTART = ".intervalStart";
	private static final String X_AXIS = "xAxis";
	private static final String Y_AXIS = "yAxis";
	private static final String VIEW = ".view";
	private static final int OFFSET = MIN_INCLUSIVE.length();

	private ChartSeriesFactory seriesFactory;
	

	public SimpleChartFactory(ChartSeriesFactory seriesFactory) {
		this.seriesFactory = seriesFactory;
	}

	@Override
	public Chart createChart(ShapeQuery query, EntityStructure struct) throws DaoException {
		String value = query.getParameters().get(VIEW);
		switch(value) {
			case FusionCharts.MSLINE_MEDIA_TYPE :
				return createMultiSeriesLineChart(query,struct);
			case FusionCharts.MAP_MEDIA_TYPE :
				return createMapChart(query,struct);
			case FusionCharts.PIE_MEDIA_TYPE :
				return createPieChart(query,struct);
			default :
				throw new DaoException("Invalid media type for charts: " + value);	
		}
		
	}
	
	/*
	 * The method createMultiSeriesLineChart used to create the line chart 
	 * and to create the categories based on the dimension requested 
	 * for example if the requested dimension is based on the time interval, 
	 * it should create the time interval as the xAxis and measure as yAxis
	 * or else if the requested for other dimension like grade or subject,
	 * it should create the grades as the xAxis
	 */
	public Chart createMultiSeriesLineChart(ShapeQuery query, EntityStructure struct)throws DaoException {
		String xAxis = query.getParameters().get(X_AXIS);
		Chart chart = new Chart();
		chart.setCaption(struct.getComment());
		if(xAxis.endsWith(INTERVALSTART)) {
			chart.setCategories(createCategories(query, struct));
		} else {
			chart.setCategories(new LabelCategories());
		}
		chart.setDataset(createChartDataset(query, struct, chart.getCategories()));
		return chart;
	}
	
	public Chart createMapChart(ShapeQuery query, EntityStructure struct)throws DaoException {
		Chart chart = new Chart();
		chart.setCaption(struct.getComment());
		chart.setDataset(createChartDataset(query, struct, null));
		return chart;
	}
	
	public Chart createPieChart(ShapeQuery query, EntityStructure struct)throws DaoException {
		String xAxis = query.getParameters().get(X_AXIS);
		Chart chart = new Chart();
		chart.setCaption(struct.getComment());
		chart.setDataset(createChartDataset(query, struct, null));
		return chart;
	}
	private ChartDataset createChartDataset(ShapeQuery query, EntityStructure struct, ChartCategories chartCategories) 
	throws DaoException {		
		String xAxis = query.getParameters().get(X_AXIS);
		String yAxis = query.getParameters().get(Y_AXIS);
		// For now, we only support a single series
		ChartDataset dataset = new ChartDataset();
		FieldPath measure = FieldPath.createFieldPath(yAxis, struct);
		if (measure == null) {
			throw new DaoException("measure path not found in Shape: " + query.getShapeId());
		}
	
		FieldPath dimension = FieldPath.createFieldPath(xAxis, struct);	
		if (dimension == null) {
			throw new DaoException("dimension path not found in Shape:  " + query.getShapeId());
		}
					
		ChartSeriesRequest request = ChartSeriesRequest.builder()
				.setDimension(dimension)
				.setMeasure(measure)
				.setQuery(query)
				.setStruct(struct)
				.build();
		dataset.add(seriesFactory.createChartSeries(request));
		return dataset;
	}

	


	private ChartCategories createCategories(ShapeQuery query, EntityStructure struct) throws DaoException {
		
		// For now, we only support DateTimeCategories
		
		DataRange range = dataRange(query, struct);
		
		DateTime start = toDateTime(range, range.getStartPoint());
		DateTime end = toDateTime(range, range.getEndPoint());
		Period duration = toPeriod(query);
		return new DateTimeCategories(range, start, end, duration);
	}


	private Period toPeriod(ShapeQuery query) throws DaoException {
		String value = query.getParameters().get("timeInterval.durationUnit");
		if (value == null) {
			throw new DaoException("durationUnit is not defined");
		}
		value = value.toLowerCase();
		switch (value) {
		
		case "second" :
			return Period.seconds(1);
			
		case "hour" :
			return Period.hours(1);
			
		case "day" :
			return Period.days(1);
			
		case "week" :
			return Period.weeks(1);
			
		case "month" :
			return Period.months(1);
			
		case "quarter" :
			return Period.months(3);
			
		case "year" :
			return Period.years(1);
			
		}
		throw new DaoException("Invalid durationUnit: " + value);
	}

	private DateTime toDateTime(DataRange range, BoundaryPoint point) throws DaoException {
		String value = point.getValue();
		URI fieldType = range.getPath().lastField().getFieldType();
		if (fieldType.equals(XMLSchema.DATE) || fieldType.equals(XMLSchema.DATETIME)) {
			return new DateTime(value).toLocalDateTime().toDateTime(DateTimeZone.UTC);
		}
		
		throw new DaoException("Unsupported data type: " + fieldType.stringValue());
	}

	private DataRange dataRange(ShapeQuery query, EntityStructure struct) throws DaoException {
		
		String path = null;
		BoundaryPoint start = null;
		BoundaryPoint end = null;
		for (Entry<String,String> e : query.getParameters().entrySet()) {
			String key = e.getKey();
			String value = e.getValue();
			
			if (key.endsWith(MIN_INCLUSIVE)) {
				path = path(key, path);
				start = new BoundaryPoint(value, BoundaryCondition.INCLUSIVE);
			} else if (key.endsWith(MIN_EXCLUSIVE)) {
				path = path(key, path);
				start = new BoundaryPoint(value, BoundaryCondition.EXCLUSIVE);
			} else if (key.endsWith(MAX_INCLUSIVE)) {
				path = path(key, path);
				end = new BoundaryPoint(value, BoundaryCondition.INCLUSIVE);
			} else if (key.endsWith(MAX_EXCLUSIVE)) {
				path = path(key, path);
				end = new BoundaryPoint(value, BoundaryCondition.EXCLUSIVE);
			}
		}
		if (start!=null && end!=null && path!=null) {
			FieldPath fieldPath = FieldPath.createFieldPath(path, struct);
			return new DataRange(fieldPath, start, end);
		}
		
		return null;
	}

	

	private String path(String paramName, String path) throws DaoException {
		
		String result = paramName.substring(0, paramName.length() - OFFSET);
		if (path != null && !path.equals(result)) {
			throw new DaoException("Multiple ranges not supported");
		}
		return result;
	}

	

}
