package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
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


import java.util.List;

import org.openrdf.model.URI;

import com.google.api.services.bigquery.model.ViewDefinition;

import io.konig.core.KonigException;
import io.konig.core.Path;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.query.SelectExpression;
import io.konig.transform.proto.ShapeModel;
import io.konig.transform.proto.ShapeModelFactory;
import io.konig.transform.proto.ShapeModelToShapeRule;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.sql.factory.SqlFactory;

public class CurrentStateViewGenerator {
	
	private ShapeModelFactory shapeModelFactory;
	private ShapeModelToShapeRule shapeRuleFactory;
	private SqlFactory sqlFactory = new SqlFactory();
	
	public CurrentStateViewGenerator(ShapeModelFactory shapeModelFactory){
		this.shapeModelFactory = shapeModelFactory;
		this.shapeRuleFactory = new ShapeModelToShapeRule();
	}
	
	public ViewDefinition createViewDefinition(Shape shape, DataSource source) {
		SelectExpression select = null;
		ViewDefinition result = new ViewDefinition();
		try {
			ShapeModel shapeModel = shapeModelFactory.createShapeModel(shape);
			ShapeRule shapeRule = shapeRuleFactory.toShapeRule(shapeModel);
			select = sqlFactory.selectExpression(shapeRule);
			result.setQuery("#standardSQL\n" + select.toString());
			result.setUseLegacySql(false);
		} catch(Exception ex) {
			throw new KonigException("Unable to create the view for the shape " + shape);
		}
		return result;
	}

	private GoogleBigQueryTable sourceTable(Shape shape) {

		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			for (DataSource ds : list) {
				if (
					(ds instanceof GoogleBigQueryTable) && 
					!ds.isA(Konig.CurrentState)
				) {
					return (GoogleBigQueryTable) ds;
				}
			}
		}
		return null;
	}

	private void appendProperties(StringBuilder builder, Shape shape) {
		
		for (PropertyConstraint p : shape.getProperty()) {
			URI predicate = p.getPredicate();
			String fieldName = predicate.getLocalName();
			builder.append(", a.");
			builder.append(fieldName);
		}
		
	}

	private GoogleBigQueryTable currentStateDataSource(Shape shape) {
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			for (DataSource ds : list) {
				if (ds instanceof GoogleBigQueryTable && ds.isA(Konig.CurrentState)) {
					return (GoogleBigQueryTable) ds;
				}
			}
		}
		return null;
	}

	private URI predicate(Shape shape, URI predicate) {
		PropertyConstraint p = shape.getPropertyConstraint(predicate);
		if (p == null) {
			List<PropertyConstraint> derivedList = shape.getDerivedProperty();
			if (derivedList != null) {

				for (PropertyConstraint q : derivedList) {
					Path path = q.getEquivalentPath();
					if (path != null) {
						List<Step> stepList = path.asList();
						if (stepList.size()==1) {
							Step step = stepList.get(0);
							if (step instanceof OutStep) {
								OutStep out = (OutStep) step;
								if (predicate.equals(out.getPredicate())) {
									return predicate;
								}
							}
						}
					}
				}
			}
		} else {
			return predicate;
		}
		return null;
	}

}
