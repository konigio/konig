package io.konig.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * #%L
 * Konig SQL
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


import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.impl.RdfUtil;
import io.konig.core.path.OutStep;
import io.konig.core.path.PathFactory;
import io.konig.core.path.Step;
import io.konig.core.util.PathPattern;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SQLTableShapeGenerator {
	
	private SQLDatatypeMapper datatypeMapper;
	
	
	
	public SQLTableShapeGenerator(SQLDatatypeMapper datatypeMapper) {
		this.datatypeMapper = datatypeMapper;
	}


	public SQLTableShapeGenerator() {
	}
	
	public Shape toStructuredShape(SQLTableSchema tableSchema) {

		StructuredShapeGenerator delegate = new StructuredShapeGenerator();
		
		
		return delegate.toShape(tableSchema);
	}

	public Shape toShape(SQLTableSchema tableSchema) {
		
		if (datatypeMapper == null) {
			datatypeMapper = new XsdSQLDatatypeMapper();
		}
		
		URI tableId = tableSchema.getTableShapeId();
		if (tableId == null) {
			throw new KonigException("Shape Id is not defined for table " + tableSchema.getFullName());
		}
		Shape shape = new Shape(tableId);
		shape.setTargetClass(tableSchema.getTargetClass());
		
		for (SQLColumnSchema column : tableSchema.listColumns()) {
			addProperty(shape, column);
		}
		
		
		
		return shape;
	}
	



	private void addProperty(Shape shape, SQLColumnSchema column) {
		
		URI predicate = column.getColumnPredicate();
		if (predicate == null) {
			throw new KonigException("predicate is not defined for column: " + column.getFullName());
		}
		URI datatype = datatypeMapper.rdfDatatype(column.getColumnType().getDatatype());
		
		PropertyConstraint p = new PropertyConstraint(predicate);
		p.setDatatype(datatype);
		
		int minCount = column.isNotNull() ? 1 : 0;
		p.setMinCount(minCount);
		p.setMaxCount(1);
		p.setEquivalentPath(column.getEquivalentPath());
		
		shape.add(p);
		
		
	}
	
	private class StructuredShapeGenerator {
		
		private Map<String,Shape> shapeMap = new HashMap<>();
		private Map<String, URI> targetClassMap = new HashMap<>();
		private PathFactory pathFactory;
		private NamespaceManager nsManager;
	
		
		public Shape toShape(SQLTableSchema tableSchema) {
			nsManager = tableSchema.getNamespaceManager();
			pathFactory = new PathFactory(nsManager);
			
			if (datatypeMapper == null) {
				datatypeMapper = new XsdSQLDatatypeMapper();
			}
			
			buildTargetClassMap(tableSchema);
			
			Shape shape = new Shape();
			shape.setTargetClass(tableSchema.getTargetClass());
			
			for (SQLColumnSchema column : tableSchema.listColumns()) {
				addStructuredProperty(shape, column);
			}
			
			return shape;
		}

		private void buildTargetClassMap(SQLTableSchema tableSchema) {
			
			List<PathPattern> list = tableSchema.getPathPatternList();
			for (PathPattern p : list) {
				Path path = p.getPath();
				String key = path.toString(nsManager);
				URI targetClass = p.getTargetClass();
				targetClassMap.put(key, targetClass);
			}
			
		}

		private void addStructuredProperty(Shape shape, SQLColumnSchema column) {
			
			Path path = null;
			Shape targetShape = shape;

			SQLTableSchema table = column.getColumnTable();
			ForeignKeyConstraint fk = table.getForeignKeyConstraint(column);
			String equivalentPath = column.getEquivalentPath();
			
			
			URI predicate = column.getColumnPredicate();
			
			if (equivalentPath != null) {
				
				path = pathFactory.createPath(equivalentPath);
				if (fk == null) {
					predicate = lastPredicate(path);
					targetShape = shapeForPath(shape, path);
				} else {
					predicate = firstPredicate(path);
				}
				
			}
			
			if (predicate == null) {
				throw new KonigException("predicate not defined for column: " + column.getFullName());
			}
			URI datatype = datatypeMapper.rdfDatatype(column.getColumnType().getDatatype());
			
			PropertyConstraint p = new PropertyConstraint(predicate);
			
			int minCount = column.isNotNull() ? 1 : 0;
			p.setMinCount(minCount);
			p.setMaxCount(1);
			
			if (fk == null || equivalentPath == null) {
				p.setDatatype(datatype);
			} else {
				p.setNodeKind(NodeKind.IRI);
				List<SQLColumnSchema> target = fk.getTarget();
				if (target.size()==1) {
					SQLColumnSchema targetColumn = target.get(0);
					SQLTableSchema targetTable = targetColumn.getColumnTable();
					URI targetClass = targetTable.getTargetClass();
					if (targetClass != null) {
						p.setValueClass(targetClass);
					}
				}
			}
			
			
			
			targetShape.add(p);
			
		}

		private URI firstPredicate(Path path) {

			List<Step> list = path.asList();
			OutStep last = (OutStep)list.get(0);
			return last.getPredicate();
		}

		private URI lastPredicate(Path path) {
			
			List<Step> list = path.asList();
			OutStep last = (OutStep)list.get(list.size()-1);
			return last.getPredicate();
		}

		private Shape shapeForPath(Shape root, Path path) {
			List<Step> stepList = path.asList();
			int last = stepList.size()-1;
			
			Shape parent = root;
			Shape shape = null;
			StringBuilder key = new StringBuilder();
			for (int i=0; i<last; i++) {
				Step step = stepList.get(i);
				if (step instanceof OutStep) {
					OutStep out = (OutStep) step;
					URI predicate = out.getPredicate();
					String curie = RdfUtil.curie(nsManager, predicate);
					
					PropertyConstraint p = new PropertyConstraint(predicate);
					parent.add(p);
					
					
					key.append('/');
					key.append(curie);
					String keyValue = key.toString();
					
					shape = shapeMap.get(keyValue);
					
					parent = shape;
					
					if (shape == null) {
						shape = new Shape();
						shapeMap.put(keyValue, shape);
						URI targetClass = targetClassMap.get(keyValue);
						shape.setTargetClass(targetClass);
					}
					p.setShape(shape);
					
				} else {
					throw new KonigException("Step type not supported: " + step.getClass().getSimpleName());
				}
				
				
			}
			if (shape == null) {
				shape = root;
			}
			
			return shape;
		}
		
		
	}
	

}
