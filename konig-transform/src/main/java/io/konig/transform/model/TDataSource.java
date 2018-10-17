package io.konig.transform.model;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import io.konig.core.impl.RdfUtil;
import io.konig.datasource.DataSource;

/**
 * A DataSource decorated with information useful for generating transforms.
 * @author Greg McFall
 *
 */
public class TDataSource {

	private DataSource datasource;
	private TNodeShape tshape;

	public TDataSource(DataSource datasource, TNodeShape tshape) {
		this.datasource = datasource;
		this.tshape = tshape;
	}

	public DataSource getDatasource() {
		return datasource;
	}

	public TNodeShape getTshape() {
		return tshape;
	}
	
	public String toString() {
		String shapeId = RdfUtil.localName(tshape.getShape().getId());
		String sourceType = datasource.getClass().getSimpleName();
		return "TDataSource[shape: " + shapeId + ", dataSource.type: " + sourceType + "]";
	}

}
