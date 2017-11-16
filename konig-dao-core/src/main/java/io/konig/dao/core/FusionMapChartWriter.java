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

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.cloud.bigquery.FieldValue;

public class FusionMapChartWriter implements ChartWriter {

	private JsonGenerator json;
	private Formatter dataFormatter;
	private ChartGeoLocationMapping mapping = null;
	
	public FusionMapChartWriter(JsonGenerator json,  Formatter dataFormatter) {
		this.json = json;
		this.dataFormatter = dataFormatter;
		mapping = (ChartGeoLocationMapping)MemcacheServiceFactory
				.getMemcacheService()
				.get("FusionIdMapping");
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

		writeDataset(chart);

		json.writeEndObject();
		json.flush();
	}

	private void writeDataset(Chart chart) throws IOException {
		ChartDataset dataset = chart.getDataset();		
		json.writeArrayFieldStart("data");
		String containedInPlace = "";
		for (ChartSeries series : dataset.getSeries()) {
			Iterator<OrderedPair> pairSequence = series.iterator();
			while (pairSequence.hasNext()) {
				OrderedPair pair = pairSequence.next();
				Object x = pair.getX();
				Object y = pair.getY();
				
				String value = dataFormatter.format(y);
				String mapId = "";
				if (x instanceof String) {
					mapId = x.toString();
				} else if (x instanceof ArrayList) {
					List<FieldValue> fieldValue = ((ArrayList<FieldValue>) x);
					mapId = fieldValue.get(0).getStringValue();			 
				}
				try {
					containedInPlace = mapping.getContainedInPlace(mapId);
					json.writeStartObject();
					json.writeStringField("id", mapping.getFusionId(mapId));
					json.writeStringField("value", value);
					json.writeStringField("showLabel", "1");
					json.writeStringField("fontBold", "1");
					json.writeStringField("useHoverColor", "1");
					json.writeStringField("showToolTip", "1");
					json.writeStringField("FontColor", "#000000");
					json.writeStringField("link", "j-drilldown-"+mapId+"|"+mapping.getName(mapId)+"|"+mapping.getType(mapId));
					json.writeEndObject();
				}catch(Exception ex){
					//TODO: exception to be logged
				}
			}
		}
		json.writeEndArray();
		if(containedInPlace != null &&!containedInPlace.equals("")) {
			json.writeStringField("type", mapping.getName(containedInPlace).replaceAll(" ", "").toLowerCase());
		}
	}
}
