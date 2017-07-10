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
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class CurrentStateViewGenerator {

	public ViewDefinition createViewDefinition(Shape shape, DataSource source) {
		
		if (!source.isA(Konig.CurrentState)) {
			return null;
		}
		
		GoogleBigQueryTable bigQuery = sourceTable(shape);
		
		if (bigQuery == null) {
			return null;
		}
		
		
		if (shape.getNodeKind() != NodeKind.IRI) {
			throw new KonigException("Cannot generate current state view.  Shape <" + shape.getId() + 
					"> does not have sh:nodeKind equal to sh:IRI.");
		}
		
		URI modifiedPredicate = predicate(shape, Konig.modified);
		URI deletedPredicate = predicate(shape, Konig.deleted);
		
		if (modifiedPredicate == null) {
			throw new KonigException("Last modified timestamp must be defined on Shape: " + shape.getId());
		}
		

		ViewDefinition result = new ViewDefinition();
		StringBuilder builder = new StringBuilder(); 
		
		builder.append("SELECT a.id");
		appendProperties(builder, shape);
		builder.append(" FROM ");
		builder.append(bigQuery.getTableIdentifier());
		builder.append(" AS a ");
		builder.append("JOIN (");
		builder.append("SELECT id as identifier, MAX(modified) AS maxModified ");
		builder.append("FROM ");
		builder.append(bigQuery.getTableIdentifier());
		builder.append(" GROUP BY identifier");
		builder.append(") AS b");
		builder.append(" ON a.id = b.identifier ");
		builder.append("WHERE ");
		builder.append("a.modified = b.maxModified");
		if (deletedPredicate != null) {
			builder.append(" AND deleted IS NULL");
		}
		
		result.setQuery(builder.toString());
		result.setUseLegacySql(false);
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
