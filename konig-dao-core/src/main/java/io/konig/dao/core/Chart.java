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


public class Chart {
	private ChartKey key;
	private String caption;
	private String xAxisLabel;
	private String yAxisLabel;
	
	private ChartDataset dataset;
	private ChartCategories categories;
	
	public ChartDataset getDataset() {
		return dataset;
	}
	public void setDataset(ChartDataset dataset) {
		this.dataset = dataset;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String getxAxisLabel() {
		return xAxisLabel;
	}
	public void setxAxisLabel(String xAxisLabel) {
		this.xAxisLabel = xAxisLabel;
	}
	public String getyAxisLabel() {
		return yAxisLabel;
	}
	public void setyAxisLabel(String yAxisLabel) {
		this.yAxisLabel = yAxisLabel;
	}
	public ChartCategories getCategories() {
		return categories;
	}
	public void setCategories(ChartCategories categories) {
		this.categories = categories;
	}
	public ChartKey getKey() {
		return key;
	}
	public void setKey(ChartKey key) {
		this.key = key;
	}

}
