package io.konig.dao.sql.generator;

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


import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.shacl.Shape;

public class BasicSqlDataSourceProcessor implements SqlDataSourceProcessor {
	private  String packageName;
	
	

	public BasicSqlDataSourceProcessor(String packageName) {
		this.packageName = packageName;
	}
	@Override
	public List<DataSource> findDataSources(Shape shape) {
		List<DataSource> list = new ArrayList<>();
		List<DataSource> sourceList = shape.getShapeDataSource();
		if (sourceList != null) {
				
			for (DataSource ds : sourceList) {
				if (ds instanceof TableDataSource) {
					list.add(ds);
				}
			}
		}
		
		return list;
	}

	@Override
	public String shapeReaderClassName(Shape shape, DataSource dataSource) throws SqlDaoGeneratorException {
		
		String shapeSlug = shapeSlug(shape);
		
		StringBuilder builder = new StringBuilder();
		builder.append(packageName);
		builder.append('.');
		builder.append(shapeSlug);
		builder.append("SqlReadService");
		
		return builder.toString();
	}

	private String shapeSlug(Shape shape) throws SqlDaoGeneratorException {
		Resource shapeId = shape.getId();
		if (shapeId instanceof URI) {
			URI uri = (URI) shapeId;
			String localName = uri.getLocalName();
			if (localName.endsWith("Shape")) {
				localName = localName.substring(0, localName.length() - 5);
			}
			return localName;
		} 
		throw new SqlDaoGeneratorException("Shape id must be a URI but was: " + shape.getId().stringValue());
	}

}
