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
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonGenerator;

public class FusionMultiLineChartWriter implements ChartWriter {
	
	private JsonGenerator json;
	private Formatter categoryFormatter;
	private Formatter dataFormatter;
	
	

	public FusionMultiLineChartWriter(JsonGenerator json, Formatter categoryFormatter, Formatter dataFormatter) {
		this.json = json;
		this.categoryFormatter = categoryFormatter;
		this.dataFormatter = dataFormatter;
	}

	@Override
	public void writeChart(Chart chart) throws IOException {
		
		json.writeStartObject();
		if (chart.getCaption() != null) {
			json.writeStringField("caption", chart.getCaption());
		}
		if (chart.getxAxisLabel() != null) {
			json.writeStringField("xAxisName", chart.getxAxisLabel());
		}
		writeCategories(chart);
		writeDataset(chart);
		
		json.writeEndObject();
		json.flush();		
	}

	private void writeDataset(Chart chart) throws IOException {
		
		ChartDataset dataset = chart.getDataset();
		
	
		
		json.writeArrayFieldStart("dataset");
		for (ChartSeries series : dataset.getSeries()) {
			json.writeStartObject();
		
			if (series.getTitle() != null) {
				json.writeStringField("title", series.getTitle());
			}
			json.writeArrayFieldStart("data");
			Iterator<OrderedPair> pairSequence = series.iterator();
			Iterator<? extends Object> categorySequence = chart.getCategories().iterator();
			
			
			while (pairSequence.hasNext()) {
				OrderedPair pair = pairSequence.next();
				Object x = pair.getX();

				if (!categorySequence.hasNext()) {
					break;
				}
				
				while (categorySequence.hasNext()) {
					Object category = categorySequence.next();
					json.writeStartObject();
					if (category.equals(x)) {
						Object y = pair.getY();
						String value = dataFormatter.format(y);
						json.writeStringField("value", value);			
					} else {
						String value = dataFormatter.format("0");
						json.writeStringField("value", value);					
					}
					json.writeEndObject();
				}
			}
			json.writeEndArray();
			json.writeEndObject();
		}
		json.writeEndArray();
		
	}

	private void writeCategories(Chart chart) throws IOException {
		Iterator<? extends Object> sequence = chart.getCategories().iterator();
		
		json.writeArrayFieldStart("categories");
		json.writeStartObject();
		json.writeArrayFieldStart("category");
		
		while (sequence.hasNext()) {
			json.writeStartObject();
			String value = categoryFormatter.format(sequence.next());
			json.writeStringField("label", value);
			json.writeEndObject();
		}
		json.writeEndArray();
		json.writeEndObject();
		json.writeEndArray();
		
	}

	
}
