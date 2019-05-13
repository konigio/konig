package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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


import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.util.SimpleValueMap;
import io.konig.core.vocab.Konig;
import io.konig.shacl.AndConstraint;
import io.konig.shacl.OrConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.XoneConstraint;

public class ShapeSheet extends BaseSheetProcessor {

	private static final SheetColumn SHAPE_ID = new SheetColumn("Shape Id", true);
	private static final SheetColumn COMMENT = new SheetColumn("Comment");
	private static final SheetColumn TARGET_CLASS = new SheetColumn("Target Class");
	private static final SheetColumn DATASOURCE = new SheetColumn("Datasource");
	private static final SheetColumn IRI_TEMPLATE = new SheetColumn("IRI Template");
	private static final SheetColumn DERIVED_FROM = new SheetColumn("Derived From");
	private static final SheetColumn SHAPE_TYPE = new SheetColumn("Shape Type");
	private static final SheetColumn ONE_OF = new SheetColumn("One Of");
	private static final SheetColumn XONE_OF = new SheetColumn("Exactly One Of");
	private static final SheetColumn AND = new SheetColumn("All Of");
	private static final SheetColumn TABULAR_ORIGIN_SHAPE = new SheetColumn("Tabular Origin Shape");
	private static final SheetColumn STATUS = new SheetColumn("Status");
	private static final SheetColumn MEDIA_TYPE = new SheetColumn("Media Type");
	private static final SheetColumn SUBJECT = new SheetColumn("Subject");
	private static final SheetColumn PROJECT = new SheetColumn("Project");
	
	private static final SheetColumn[] COLUMNS = new SheetColumn[] {
		SHAPE_ID,
		COMMENT,
		TARGET_CLASS,
		DATASOURCE,
		IRI_TEMPLATE,
		DERIVED_FROM,
		SHAPE_TYPE,
		ONE_OF,
		XONE_OF,
		AND,
		TABULAR_ORIGIN_SHAPE,
		STATUS,
		MEDIA_TYPE,
		SUBJECT,
		PROJECT
	};

	private DataSourceGeneratorFactory dataSourceGeneratorFactory;
	private boolean generatedBatchFileBucket=false;
	
	@SuppressWarnings("unchecked")
	public ShapeSheet(WorkbookProcessor processor, DataSourceGeneratorFactory dataSourceGeneratorFactory) {
		super(processor);
		this.dataSourceGeneratorFactory = dataSourceGeneratorFactory;
		dependsOn(OntologySheet.class, SettingsSheet.class);
	}

	@Override
	public SheetColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
		URI shapeId = iriValue(row, SHAPE_ID);

		String iriTemplate = stringValue(row, IRI_TEMPLATE);
		URI targetClass = iriValue(row, TARGET_CLASS);
		String mediaType = stringValue(row, MEDIA_TYPE);
		String orList = stringValue(row, ONE_OF);
		String xoneList = stringValue(row, XONE_OF);
		String andList = stringValue(row, AND);
		
		List<Function> dataSourceList = dataSourceList(row, DATASOURCE);

		String shapeComment = stringValue(row, COMMENT);
        List<URI> shapeType=iriList(row, SHAPE_TYPE);
        URI tabularOriginShape = iriValue(row, TABULAR_ORIGIN_SHAPE);
        List<URI> derivedFromUriList = iriList(row, DERIVED_FROM);
        Set<URI> subject = subjectSet(row, SUBJECT);
        URI termStatus = iriValue(row, STATUS);

		
		Shape shape = produceShape(shapeId);

		
		if (shapeType != null) {
			for (URI type : shapeType) {
				shape.addType(type);
			}
		}
		shape.setComment(shapeComment);
		shape.setTargetClass(targetClass);
		for (URI s : subject) {
			shape.addBroader(s);
		}
		
		for (URI derivedFromId : derivedFromUriList) {
			shape.addExplicitDerivedFrom(produceShape(derivedFromId));
		}
		
		edge(targetClass, RDF.TYPE, OWL.CLASS);
		
		shape.setMediaTypeBaseName(mediaType);
		
		if (tabularOriginShape != null) {
			shape.setTabularOriginShape(produceShape(tabularOriginShape));
		}
		
		shape.setOr(orConstraint(row, ONE_OF, orList));
		shape.setXone(xoneConstraint(row, XONE_OF, xoneList));
		shape.setAnd(andConstraint(row, AND, andList));

		if (iriTemplate != null) {
			processor.defer(
				new BuildShapeTemplateAction(
					location(row, IRI_TEMPLATE),
					processor, 
					processor.getGraph().getNamespaceManager(), 
					service(SimpleLocalNameService.class), 
					shape, 
					iriTemplate
			));
		}
		

		if (dataSourceList != null) {
			processor.defer(
				new BuildDataSourceAction(
					location(row, DATASOURCE),
					processor,
					dataSourceGeneratorFactory.getDataSourceGenerator(),
					shape,
					processor.getShapeManager(),
					dataSourceList
					
			));
		}
		
		shape.setTermStatus(termStatus);
		edge(termStatus, RDF.TYPE, Konig.TermStatus);

	}
	
	protected  List<Function> dataSourceList(SheetRow row, SheetColumn column) throws SpreadsheetException {
		List<Function> list = super.dataSourceList(row, column);
		addBatchFileBucket(list);
		return list;
	}
	
	private void addBatchFileBucket(List<Function> list) {
		if (!generatedBatchFileBucket && list!=null) {
			for (Function func : list) {
				String name = func.getName();
				if ("BigQueryCsvBucket".equals(name) || "GoogleCloudStorageBucket".equals(name)) {
					generatedBatchFileBucket = true;
					list.add(new Function("BatchEtlBucket",  new SimpleValueMap()));
					break;
				}
			}
		}
	}

	private AndConstraint andConstraint(SheetRow row, SheetColumn col, String andList) throws SpreadsheetException {

		AndConstraint and = null;
		if (andList != null) {
			StringTokenizer tokenizer = new StringTokenizer(andList, "& \n\r\t");
			if (tokenizer.hasMoreTokens()) {
				and = new AndConstraint();
				while (tokenizer.hasMoreTokens()) {
					String iri = tokenizer.nextToken();
					URI shapeId = processor.expandCurie(iri, row, col);
					Shape shape = produceShape(shapeId);
					and.add(shape);
				}
			}
		}
		return and;
	}

	private XoneConstraint xoneConstraint(SheetRow row, SheetColumn col, String xoneList) throws SpreadsheetException {

		XoneConstraint xone = null;
		if (xoneList != null) {
			StringTokenizer tokenizer = new StringTokenizer(xoneList, "| \n\r\t");
			if (tokenizer.hasMoreTokens()) {
				xone = new XoneConstraint();
				while (tokenizer.hasMoreTokens()) {
					String iri = tokenizer.nextToken();
					URI shapeId = processor.expandCurie(iri, row, col);
					Shape shape = produceShape(shapeId);
					xone.add(shape);
				}
			}
		}
		return xone;
	}
	private OrConstraint orConstraint(SheetRow row, SheetColumn column, String valueTypeText) throws SpreadsheetException {
		OrConstraint or = null;
		if (valueTypeText != null) {
			StringTokenizer tokenizer = new StringTokenizer(valueTypeText, "| \n\r\t");
			if (tokenizer.hasMoreTokens()) {
				or = new OrConstraint();
				while (tokenizer.hasMoreTokens()) {
					String iri = tokenizer.nextToken();
					URI shapeId = processor.expandCurie(iri, row, column);
					Shape shape = produceShape(shapeId);
					or.add(shape);
				}
			}
		}
		return or;
	}




}
