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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.cloud.bigquery.FieldValue;

public class FusionPieChartWriter implements ChartWriter {

	private JsonGenerator json;
	private Formatter dataFormatter;

	public FusionPieChartWriter(JsonGenerator json,  Formatter dataFormatter) {
		this.json = json;
		this.dataFormatter = dataFormatter;
	}

	@Override
	public void writeChart(Chart chart) throws IOException {

		json.writeStartObject();
		if (chart.getCaption() != null) {
			json.writeStringField("caption", chart.getCaption());
		}
		
		writeDataset(chart);

		json.writeEndObject();
		json.flush();
	}

	private void writeDataset(Chart chart) throws IOException {
		ChartDataset dataset = chart.getDataset();
		json.writeArrayFieldStart("data");
		for (ChartSeries series : dataset.getSeries()) {
			Iterator<OrderedPair> pairSequence = series.iterator();
			while (pairSequence.hasNext()) {
				OrderedPair pair = pairSequence.next();
				Object x = pair.getX();
				Object y = pair.getY();
				Object dimension = "";
				String dimensionId = "";
				if (x instanceof String) {
					dimension = x.toString();
				} else if (x instanceof ArrayList) {
					List<FieldValue> fieldValue = (ArrayList<FieldValue>) x;
					dimensionId = fieldValue.get(0).getStringValue();
					dimension = fieldValue.get(1).getStringValue();
				} else {
					dimension = x;
				}
				String value = dataFormatter.format(y);

				json.writeStartObject();
				json.writeStringField("value", value);
				json.writeStringField("label", dimension.toString());
				json.writeStringField("link", "j-drilldown-" + dimensionId + "|" + dimension.toString() + "|Grade");
				json.writeEndObject();

			}
			json.writeEndArray();
		}
	}

}
