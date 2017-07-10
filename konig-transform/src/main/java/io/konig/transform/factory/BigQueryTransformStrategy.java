package io.konig.transform.factory;

/*
 * #%L
 * Konig Transform
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
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudStorageBucket;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class BigQueryTransformStrategy extends AbstractTransformStrategy {

	@Override
	public List<SourceShape> findCandidateSourceShapes(TargetShape target) throws TransformBuildException {

		List<SourceShape> result = new ArrayList<>();
		URI targetClass = target.getShape().getTargetClass();
		
		if (targetClass != null) {
			List<Shape> list = factory.getShapeManager().getShapesByTargetClass(targetClass);
			for (Shape shape : list) {
				DataSource datasource = findDatasource(shape);
				if (datasource != null) {
					SourceShape source = SourceShape.create(shape);
					source.setDataSource(datasource);
					target.match(source);
					result.add(source);
				}
			}
		} 
		scanVariables(target, result);
		return result;
	}
	
	private void scanVariables(TargetShape target, List<SourceShape> result) {
		
		List<VariableTargetProperty> variableList = target.getVariableList();
		if (variableList != null) {
			for (VariableTargetProperty vtp : variableList) {
				PropertyConstraint p = vtp.getPropertyConstraint();
				Shape sourceShape = p.getShape();
				if (sourceShape != null) {
					DataSource datasource = findDatasource(sourceShape);
					if (datasource != null) {
						SourceShape source = SourceShape.create(sourceShape);
						source.setDataSource(datasource);
						vtp.addCandidateSourceShape(source);
					}
				} else {
					Resource valueClass = p.getValueClass();
					if (valueClass instanceof URI) {
						URI sourceClass = (URI) valueClass;
						List<Shape> shapeList = factory.getShapeManager().getShapesByTargetClass(sourceClass);
						for (Shape shape : shapeList) {
							DataSource ds = findDatasource(shape);
							if (ds != null) {
								SourceShape source = SourceShape.create(shape);
								source.setDataSource(ds);
								vtp.addCandidateSourceShape(source);
							}
						}
					}
				}
			}
		}
		
	}

	private DataSource findDatasource(Shape shape) {
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			GoogleBigQueryTable bigQueryTable = null;
			GoogleCloudStorageBucket bucket = null;
			for (DataSource source : list) {
				if (source instanceof GoogleBigQueryTable) {
					bigQueryTable = (GoogleBigQueryTable) source;
				} else if (source instanceof GoogleCloudStorageBucket) {
					bucket = (GoogleCloudStorageBucket) source;
				}
			}
			
			if (bucket !=null && bigQueryTable != null) {
				return bigQueryTable;
			}
			
		}
		return null;
	}

}
