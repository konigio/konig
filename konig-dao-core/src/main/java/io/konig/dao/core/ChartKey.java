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


import java.util.StringTokenizer;

/**
 * A unique key that identifies a specific kind of chart.
 * The key consists of two parts: 
 * <ol>
 *   <li> The name of the Javascript library that renders the chart.
 *   <li> The name of the chart type within that library.
 * </ol>
 * 
 * @author Greg McFall
 *
 */
public class ChartKey {
	
	private static final String[] LIBRARIES = new String[] {
		FusionCharts.LIBRARY_NAME	
	};
	
	private String libraryName;
	private String chartType;
	
	private ChartKey(String libraryName, String chartType) {
		this.libraryName = libraryName;
		this.chartType = chartType;
	}
	
	public static boolean isLibrary(String name) {
		for (String value : LIBRARIES) {
			if (value.equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	public static ChartKey fromMediaType(String mediaType) {
		int slash = mediaType.indexOf('/');
		if (slash > 0) {
			mediaType = mediaType.substring(slash+1);
		}
		StringTokenizer tokenizer = new StringTokenizer(mediaType, ".");
		while (tokenizer.hasMoreTokens()) {
			String library = tokenizer.nextToken();
			if (isLibrary(library) && tokenizer.hasMoreTokens()) {
				String chartType = tokenizer.nextToken();
				return new ChartKey(library, chartType);
			}
			
		}
		return null;
	}
	
	public String getLibraryName() {
		return libraryName;
	}
	public String getChartType() {
		return chartType;
	}
	
	public String toString() {
		return libraryName + '.' + chartType;
	}
	
	
}
