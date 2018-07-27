package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
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


import java.util.NoSuchElementException;

import org.openrdf.model.URI;

import com.google.api.services.bigquery.model.ViewDefinition;

import io.konig.core.vocab.Konig;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleBigQueryView;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.AndExpression;
import io.konig.sql.query.BooleanTerm;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.FunctionExpression;
import io.konig.sql.query.JoinExpression;
import io.konig.sql.query.NullPredicate;
import io.konig.sql.query.OnExpression;
import io.konig.sql.query.OrExpression;
import io.konig.sql.query.SearchCondition;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SelectTableItemExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.TruthValue;

public class SimpleCurrentStateViewGenerator {

	public SimpleCurrentStateViewGenerator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create a view
	 * @param shape
	 * @param source
	 * @return
	 * @throws CreateViewException
	 */
	public ViewDefinition createViewDefinition(Shape shape, GoogleBigQueryView viewDataSource)
	throws CreateViewException {
		
		ViewDefinition result = null;
		
		try {
			GoogleBigQueryTable fullTable = shape.getShapeDataSource()
					.stream()
					.filter(ds -> (ds instanceof GoogleBigQueryTable) && !(ds instanceof GoogleBigQueryView))
					.map(ds -> (GoogleBigQueryTable) ds)
					.findFirst().get();
			
			result = createViewFromFullTable(shape, fullTable, viewDataSource);
		} catch (NoSuchElementException e) {
			
		}
		
		return result;
	}

	private ViewDefinition createViewFromFullTable(Shape shape, GoogleBigQueryTable fullTable,
			GoogleBigQueryView viewDataSource) {
		
		
		ViewDefinition view = new ViewDefinition();
		SelectExpression select = selectExpression(shape, fullTable);
		
		
		view.setQuery(select.toString());
		view.setUseLegacySql(false);
		return view;
	}

	SelectExpression selectExpression(Shape shape, GoogleBigQueryTable fullTable) {

		SelectExpression select = new SelectExpression();
		addFields(select, shape, fullTable);
		addFrom(select, shape, fullTable);
		addWhere(select, shape);
		return select;
	}

	private void addWhere(SelectExpression select, Shape shape) {
		
		AndExpression term = new AndExpression();
		ColumnExpression modifiedCol = new ColumnExpression("modified");
		ColumnExpression maxModified = new ColumnExpression("maxModified");
		
		PropertyConstraint deletedConstraint = shape.getPropertyConstraint(Konig.deleted);
		if (deletedConstraint != null) {
			ColumnExpression deleted = new ColumnExpression("deleted");
			
			BooleanTerm deletedTerm =  new ComparisonPredicate(ComparisonOperator.EQUALS, deleted, TruthValue.FALSE);
			if (!deletedConstraint.isRequired()) {

				BooleanTerm deletedIsNull = new NullPredicate(deleted, true);
				deletedTerm = new OrExpression(new AndExpression(deletedIsNull), new AndExpression(deletedTerm));
			}
			term.add(deletedTerm);
		}
		
		term.add(new ComparisonPredicate(ComparisonOperator.EQUALS, modifiedCol, maxModified));
		
		select.addWhereCondition(term);
		
	}

	private void addFields(SelectExpression select, Shape shape, GoogleBigQueryTable fullTable) {
		
		NodeKind k = shape.getNodeKind();
		if (k==NodeKind.IRI || k==NodeKind.BlankNodeOrIRI) {
			select.add(new ColumnExpression("A.id"));
		}
		for (PropertyConstraint p : shape.getProperty()) {
			addField(select, p);
		}
		
		
	}

	// TODO: Handle nested and repeated fields
	private void addField(SelectExpression select, PropertyConstraint p) {
		
		ColumnExpression col = column(p);
		if (col != null) {
			select.add(col);
		}
	}

	private ColumnExpression column(PropertyConstraint p) {
		URI predicate = p.getPredicate();
		if (predicate == null) {
			return null;
		}
		return new ColumnExpression("A." + predicate.getLocalName());
	}

	private void addFrom(SelectExpression select, Shape shape, GoogleBigQueryTable fullTable) {
		
		BigQueryTableReference ref = fullTable.getTableReference();
		StringBuilder builder = new StringBuilder();
		builder.append(ref.getDatasetId());
		builder.append('.');
		builder.append(ref.getTableId());
		
		String tableName = builder.toString();
		
		
		TableItemExpression left = new TableAliasExpression(new TableNameExpression(tableName), "A");
		
		SelectExpression s = new SelectExpression();
		
		s.add(new AliasExpression(new ColumnExpression("id"), "identifier"));
		s.add(new AliasExpression(new FunctionExpression("MAX", new ColumnExpression("modified")), "maxModified"));
		s.getFrom().add(new TableNameExpression(tableName));
		
		TableItemExpression right = new SelectTableItemExpression(s);
		
		ColumnExpression idCol = new ColumnExpression("id");
		ColumnExpression identifierCol = new ColumnExpression("identifier");
		SearchCondition searchCondition = new ComparisonPredicate(ComparisonOperator.EQUALS, idCol, identifierCol);
		OnExpression on = new OnExpression(searchCondition);
		
		JoinExpression join = new JoinExpression(left, right, on);
		
		select.getFrom().add(join);
	
	
		
	}
}
