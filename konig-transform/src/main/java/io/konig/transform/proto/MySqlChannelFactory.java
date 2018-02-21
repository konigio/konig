package io.konig.transform.proto;

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


import java.util.List;

import com.google.api.services.bigquery.model.ExternalDataConfiguration;

import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.shacl.Shape;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.rule.DataChannel;

public class MySqlChannelFactory implements DataChannelFactory {
	
	@Override
	public DataChannel createDataChannel(Shape shape) throws ShapeTransformException {
		
		DataSource table = selectDataSource(shape);
		return (table != null) ? new DataChannel(shape, table) : null;
	}
	
	@Override
	public DataSource selectDataSource(Shape shape) throws ShapeTransformException {
		if (shape.getShapeDataSource()==null) {
			return null;
		}
		GoogleCloudSqlTable sqlTable = null;
		
		for (DataSource datasource : shape.getShapeDataSource()) {
			if (datasource instanceof GoogleCloudSqlTable) {
				sqlTable = (GoogleCloudSqlTable) datasource;
			}
		}
		
		return sqlTable;
	}
}
