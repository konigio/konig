package io.konig.data.app.generator;

/*
 * #%L
 * Konig Data App Generator
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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudStorageBucket;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.runtime.EntityStructure;
import io.konig.sql.runtime.FieldInfo;

public class EntityStructureGenerator {
	
	
	public EntityStructure toEntityStructure(Shape shape) {
		TableDataSource datasource = getTableDataSource(shape);
		
		if (datasource != null) {
			String tableName = datasource.getTableIdentifier();
			EntityStructure e = new EntityStructure(tableName);
			Map<Shape, EntityStructure> map = new HashMap<>();
			map.put(shape, e);
			addFields(e, shape, map);
			return e;
		}
		return null;
	}
	

	private void addFields(EntityStructure e, Shape shape, Map<Shape, EntityStructure> map) {
		
		for (PropertyConstraint p : shape.getProperty()) {
			addField(e, p, map);
		}
		
	}

	private void addField(EntityStructure e, PropertyConstraint p, Map<Shape, EntityStructure> map) {
		URI predicate = p.getPredicate();
		if (predicate != null) {
			String fieldName = predicate.getLocalName();
			FieldInfo field = new FieldInfo(fieldName);
			if (p.getDatatype() != null) {
				field.setDataType(p.getDatatype().getLocalName());
			}
			e.addField(field);
			
			Shape shape = p.getShape();
			if (shape != null) {
				EntityStructure nested = map.get(shape);
				if (nested == null) {
					nested = new EntityStructure();		
					map.put(shape, nested);
					TableDataSource ds = getTableDataSource(shape);
					if (ds != null) {
						nested.setName(ds.getTableIdentifier());						
					}
					addFields(nested, shape, map);
				}
				field.setStruct(nested);
			}
			
		}
		
	}

	private TableDataSource getTableDataSource(Shape shape) {
		GoogleBigQueryTable bigquery = null;
		GoogleCloudStorageBucket bucket = null;
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			for (DataSource ds : list) {
				if (ds instanceof GoogleBigQueryTable) {
					bigquery = (GoogleBigQueryTable) ds;
				}
				if (ds instanceof GoogleCloudStorageBucket) {
					bucket = (GoogleCloudStorageBucket) ds;
				}
				
			}
		}
		return bucket==null ? bigquery : null;
	}

}
