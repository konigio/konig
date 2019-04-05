package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import io.konig.datasource.DataSource;

public class ShowlDataSource {
	
	private ShowlNodeShape dataSourceShape;
	private DataSource dataSource;
	
	public ShowlDataSource(ShowlNodeShape dataSourceShape, DataSource dataSource) {
		this.dataSourceShape = dataSourceShape;
		this.dataSource = dataSource;
		
		dataSourceShape.setShapeDataSource(this);
		
	}

	public ShowlNodeShape getDataSourceShape() {
		return dataSourceShape;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public String getPath() {
		StringBuilder builder = new StringBuilder();
		builder.append(dataSourceShape.getPath());
		builder.append(".shapeDataSource{");
		builder.append(dataSource.getIdentifier());
		builder.append("}");
		return builder.toString();
	}
	
	public String toString() {
		return getPath();
	}

}
