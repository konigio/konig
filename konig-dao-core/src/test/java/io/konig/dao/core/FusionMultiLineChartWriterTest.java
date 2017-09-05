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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class FusionMultiLineChartWriterTest {
	private StringWriter buffer;

	@Ignore
	public void test() throws Exception {
	
		Chart chart = createChart();
		FusionMultiLineChartWriter writer = multilineWriter();
		
		writer.writeChart(chart);
		String text = buffer.toString();
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = (ObjectNode) mapper.readTree(text);
		
		assertField(root, "caption", "Number of Logins");
		assertField(root, "xAxisName", "Time");

		ArrayNode categories = (ArrayNode) root.findValue("categories");
		assertTrue(categories != null);
		ObjectNode categoriesValue = (ObjectNode) categories.get(0);
		ArrayNode categoryArray = (ArrayNode) categoriesValue.findValue("category");
		
		assertEquals(12, categoryArray.size());
		String[] months = new String[] {
			"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
		};
		
		for (int i=0; i<months.length; i++) {
			ObjectNode object = (ObjectNode) categoryArray.get(i);
			assertField(object, "label", months[i]);
		}
		
		ArrayNode axisArray = (ArrayNode) root.findValue("axis");
		ObjectNode axis = (ObjectNode) axisArray.get(0);
		assertField(axis, "title", "Count");
		ArrayNode dataset = (ArrayNode) axis.findValue("dataset");
		assertEquals(12, dataset.size());
		for (int i=1; i<=12; i++) {
			ObjectNode object = (ObjectNode) dataset.get(i-1);
			String expected = Integer.toString(i);
			assertField(object, "value", expected);
			
		}
		
	}

	private void assertField(ObjectNode node, String fieldName, String fieldValue) {
		JsonNode value = node.findValue(fieldName);
		assertTrue(value != null);
		assertEquals(fieldValue, value.asText());
		
		
	}

	private FusionMultiLineChartWriter multilineWriter() throws IOException {
		buffer = new StringWriter();
		JsonFactory factory = new JsonFactory();
		
		JsonGenerator generator = factory.createGenerator(buffer);
		generator.useDefaultPrettyPrinter();
		Formatter categoryFormatter = new TemporalFormatter(DateTimeFormat.forPattern("MMM"));
		Formatter dataFormatter = new SimpleFormatter();
		return new FusionMultiLineChartWriter(generator, categoryFormatter, dataFormatter);
	}

	private Chart createChart() {
		DateTime start = new DateTime(2017, 1, 1, 0, 0);
		DateTime end = new DateTime(2018, 12, 31, 0, 0);
		Period interval = Period.months(1);
		
		DateTimeCategories categories = new DateTimeCategories(start, end, interval);
		
		List<OrderedPair> pairs = new ArrayList<>();
		Iterator<? extends Object> timeSequence = categories.iterator();
		for (int i=1; i<=12; i++) {
			pairs.add(new OrderedPair(timeSequence.next(), new Integer(i)));
		}
		ChartSeries series = new ListChartSeries("Count", pairs);
		List<ChartSeries> seriesList = new ArrayList<>();
		seriesList.add(series);
		ChartDataset dataset = new ChartDataset(seriesList);
		
		Chart chart = new Chart();
		chart.setxAxisLabel("Time");
		chart.setCategories(categories);
		chart.setCaption("Number of Logins");
		chart.setDataset(dataset);
		return chart;
	}

}
