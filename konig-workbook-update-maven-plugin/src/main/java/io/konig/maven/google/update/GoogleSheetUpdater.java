package io.konig.maven.google.update;

/*
 * #%L
 * konig-workbook-update-maven-plugin Maven Plugin
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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.maven.model.FileSet;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.AddSheetResponse;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Response;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import io.konig.core.Context;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.VANN;
import io.konig.formula.FormulaParser;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.rio.turtle.IriTemplateParseException;
import io.konig.rio.turtle.IriTemplateParser;
import io.konig.spreadsheet.SpreadsheetException;
import io.konig.spreadsheet.Worksheet;

public class GoogleSheetUpdater extends Worksheet{
	
	private static Logger LOG = Logger.getLogger(GoogleSheetUpdater.class.getName());
	
	private static final int HEADER_ROW = 1;
	private Map<URI,String> prefixList = new HashMap<>();
	private Map<String,String> targetClassList = new HashMap<>();
	private ValueFactory vf = new ValueFactoryImpl();
	
	private boolean isOverrideSheetValues = false;
	private boolean isOverrideRDFValues = false;
	private boolean isRaiseError = false;
	
	private  Sheets sheets = null;
	private  String sheetId = null;
	
	public GoogleSheetUpdater(String option) {
		sheets = getSheetService();
		if(option.equals("OPTION 1")) {
			isOverrideSheetValues = true;
		} else if (option.equals("OPTION 2")) {
			isOverrideRDFValues = true;
		} else {
			isRaiseError = true;
		} 
	}
	
	public Sheet getSheet(List<SheetInfo> list, int sheetType) {
		for (SheetInfo info : list) {
			if(info.sheetType == sheetType){
				return info.sheet;
			}
		}
		return null;
	}
	
	public void execute(FileSet[] owlFileSet ,FileSet[] shaclFileSet, String documentId) throws RDFParseException, RDFHandlerException, IOException, IriTemplateParseException, SpreadsheetException {
		this.sheetId = documentId;
		
		List<SheetInfo> sheetlist = collectSheetInfo();
		Collections.sort(sheetlist);

		for(FileSet fileSet : owlFileSet) {
			if(fileSet.getDirectory() != null){
				File viewDir = new File(fileSet.getDirectory());
				File[] files = viewDir.listFiles();
				for(File file : files) {
					try (InputStream inputStream = new FileInputStream(file)) {
						Graph graph = new MemoryGraph();
						RdfUtil.loadTurtle(graph, inputStream, "");
						updateOntology(graph, getSheet(sheetlist, SHEET_ONTOLOGY));
						updateClasses(graph , getSheet(sheetlist, SHEET_CLASS));
						updateProperties(graph , getSheet(sheetlist, SHEET_PROPERTY));
					}
				}
			}
		}
		
		for(FileSet fileSet : shaclFileSet) {
			if(fileSet.getDirectory() != null){
				File viewDir = new File(fileSet.getDirectory());
				File[] files = viewDir.listFiles();
				for(File file : files) {
					try (InputStream inputStream = new FileInputStream(file)) {
						Graph graph = new MemoryGraph();
						RdfUtil.loadTurtle(graph, inputStream, "");
						updateShapes(graph, getSheet(sheetlist, SHEET_SHAPE));
						updatePropertyConstraints(graph, getSheet(sheetlist, SHEET_PROPERTY_CONSTRAINT));
					}
				}
			}
		}
	}
	
	
	private Sheets getSheetService() {
		String credentialFile = System.getProperty("io.konig.maven.google.update");
		if(credentialFile == null || credentialFile.equals("")) {
			throw new KonigException("Credential not found. Please define the 'io.konig.maven.google.update' system property ");
		}
		InputStream in = null;
		try {
			in = new FileInputStream(new File(credentialFile));
			GoogleCredential credential = GoogleCredential.fromStream(in).createScoped(DriveScopes.all());
			sheets = new Sheets.Builder(Utils.getDefaultTransport(), Utils.getDefaultJsonFactory(), credential)
					.setApplicationName("Google Sheet API")
					.build();
		}catch(Exception ex){
			throw new KonigException(ex);
		}
		return sheets;
	}
	
	private void updateOntology(Graph graph, Sheet sheet) throws IOException {
		List<Vertex> ontologyList = graph.v(OWL.ONTOLOGY).in(RDF.TYPE).toVertexList();
		List<List<Object>> ontologies = null;
		List<Object> headers = new ArrayList<>();
		int columns = 0;
		
		if(sheet != null) {
			ValueRange response = sheets.spreadsheets().values()
					.get(sheetId, sheet.getProperties().getTitle())
					.execute();
			ontologies = response.getValues();
			if(ontologies != null) {
				headers = ontologies.get(0);
				columns = headers.size();
				addPrefix(ontologies);
			}
		} else {
			addNewSheet("Ontologies");
		}
		
		addOntologyHeaders(headers, ontologyList);
		
		if(columns < headers.size()) {
			updateValues(headers, sheet.getProperties().getTitle(), HEADER_ROW);
		}
		
		List<List<Object>> newOntologies = new ArrayList<>();
		List<Object> ontology =  new ArrayList<>();
		Resource nameSpaceURI = null;
		for(Vertex v : ontologyList) {
			nameSpaceURI = v.getId();
			String prefix = v.getValue(VANN.preferredNamespacePrefix).stringValue();
			prefixList.put(new URIImpl(v.getId().toString()), prefix);
			for (Object header : headers) {
				if(ONTOLOGY_NAME.equals(header)){
					ontology.add(v.getValue(RDFS.LABEL).stringValue());
				} else if(COMMENT.equals(header)) {
					ontology.add(v.getValue(RDFS.COMMENT).stringValue());
				} else if(NAMESPACE_URI.equals(header)) {
					ontology.add(v.getId().toString());
				} else if(PREFIX.equals(header)) {
					ontology.add(prefix);
				} else {
					ontology.add("");
				}
			}
		}
		
		int rowNumber = ontologyExistRow(ontologies, nameSpaceURI);
		if( rowNumber == 0) {
			newOntologies.add(ontology);
			appendValues(newOntologies, sheet.getProperties().getTitle());
		} else if(rowNumber != 0 && isOverrideRDFValues) {
			updateValues(ontology, sheet.getProperties().getTitle(), rowNumber);
		} else if(isRaiseError) {
			throw new KonigException("Conflict in the NamespaceURI["+nameSpaceURI.toString()+"]");
		} else if(isOverrideSheetValues) {
			LOG.warning("Skipped updating Namespace URI ["+nameSpaceURI.toString()+"]" );
		}
	}
	
	private int ontologyExistRow(List<List<Object>> values, Resource id) {
		int rowNumber = 0;
		int matchingRow = 0;
		if(values != null) {
			int columnIndex = 0;
			for (List<Object> valueList : values) {
				rowNumber ++;
				for (int i =0; i< valueList.size(); i++) { 
					Object value = valueList.get(i);
					if(value.toString().equals(NAMESPACE_URI)) {
						columnIndex = i;
					}
					if(value.toString().equals(id.toString()) && i == columnIndex) {
						matchingRow = rowNumber;
						return matchingRow;
					}
				}
			}
		}
		return matchingRow;
	}
	
	private void addOntologyHeaders(List<Object> headers, List<Vertex> ontologyList) {
		for(Vertex v : ontologyList) {
			if(v.getId() != null && !headers.contains(NAMESPACE_URI)){
				headers.add(NAMESPACE_URI);
			} 
			if(v.getValue(RDFS.LABEL) != null && !headers.contains(ONTOLOGY_NAME)) {
				headers.add(ONTOLOGY_NAME);
			} 
			if(v.getValue(RDFS.COMMENT) != null && !headers.contains(COMMENT)) {
				headers.add(COMMENT);
			} 
			if(v.getValue(VANN.preferredNamespacePrefix) != null && !headers.contains(PREFIX)) {
				headers.add(PREFIX);
			}
		}
	}
	
	private void updatePropertyConstraints(Graph graph, Sheet sheet) throws IOException, RDFParseException {
		List<Vertex> shapeList = graph.v(SH.Shape).in(RDF.TYPE).toVertexList();
		List<List<Object>> propertyContraints = null;
		List<Object> headers = new ArrayList<>();
		int columns = 0;
		
		if(sheet != null) {
			ValueRange response = sheets.spreadsheets().values()
					.get(sheetId, sheet.getProperties().getTitle())
					.execute();
			propertyContraints = response.getValues();
			if(propertyContraints != null) {
				headers = propertyContraints.get(0);
				columns = headers.size();
			}
		} else if(!shapeList.isEmpty()){
			addNewSheet("Property Constraints");
		}
		addPropertyConstraintHeaders(headers, shapeList);
		if(columns < headers.size()) {
			updateValues(headers, sheet.getProperties().getTitle(), HEADER_ROW);
		}
		
		List<Object> newPropertyConstraint = null;
		
		for(Vertex v : shapeList) {
			List<Vertex> propertyList = v.asTraversal().out(SH.property).toVertexList();
			for(Vertex property : propertyList) {
				List<List<Object>> newPropertyConstraints = new ArrayList<>();
				newPropertyConstraint = new ArrayList<>();
				String shapeId = stringLiteral(v.getId());
				String propertyId = stringLiteral(property.getValue(SH.path));
				for (Object header : headers) {
					if(SHAPE_ID.equals(header)){
						newPropertyConstraint.add(stringLiteral(v.getId()));
					} else if(COMMENT.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(RDFS.COMMENT)));
					} else if(PROPERTY_ID.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(SH.path)));
					} else if(VALUE_TYPE.equals(header)) {
						if(property.getValue(SH.datatype) != null) {
							newPropertyConstraint.add(stringLiteral(property.getValue(SH.datatype)));
						} else if(property.getValue(SH.nodeKind) != null) {
							newPropertyConstraint.add(stringLiteral(XMLSchema.ANYURI));
						} else if(property.getValue(SH.shape) != null) {
							newPropertyConstraint.add(stringLiteral(property.getValue(SH.shape)));
						} else {
							newPropertyConstraint.add("");
						}
					} else if(MIN_COUNT.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(SH.minCount)));
					} else if(MAX_COUNT.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(SH.maxCount)));
					} else if(UNIQUE_LANG.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(SH.uniqueLang)));
					} else if(VALUE_CLASS.equals(header)) {
						if(property.getValue(SH.valueClass) != null) {
							newPropertyConstraint.add(stringLiteral(property.getValue(SH.valueClass)));	
						} else if(property.getValue(SH.shape) != null) {
							newPropertyConstraint.add(getTargetClass(stringLiteral(property.getValue(SH.shape))));
						}  else if(Konig.id.equals(v.getId())) { 
							newPropertyConstraint.add(stringLiteral(OWL.CLASS));	
						} else {
							newPropertyConstraint.add("");
						}
					} else if(STEREOTYPE.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(Konig.stereotype)));
					} else if(VALUE_IN.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(SH.in)));
					} else if(EQUALS.equals(header)) {
						newPropertyConstraint.add("");
					} else if(EQUIVALENT_PATH.equals(header)) {
						newPropertyConstraint.add("");
					} else if(SOURCE_PATH.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(Konig.sourcePath)));
					} else if(PARTITION_OF.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(Konig.partitionOf)));
					} else if(FORMULA.equals(header)) {
						if(property.getValue(Konig.formula) != null){
							FormulaParser parser = new FormulaParser();
							QuantifiedExpression e = parser.quantifiedExpression(property.getValue(Konig.formula).stringValue());
							Context context = e.getContext();
							context.compile();
							PrimaryExpression primary = e.asPrimaryExpression();
							newPropertyConstraint.add(primary.toString());
						} else {
							newPropertyConstraint.add("");
						}
					} else if(MIN_INCLUSIVE.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(SH.minInclusive)));
					} else if(MAX_INCLUSIVE.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(SH.maxInclusive)));
					} else if(MIN_EXCLUSIVE.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(SH.minExclusive)));
					} else if(MAX_EXCLUSIVE.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(SH.maxExclusive)));
					} else if(MIN_LENGTH.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(SH.minLength)));
					} else if(MAX_LENGTH.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(SH.maxLength)));
					} else if(DECIMAL_PRECISION.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(Konig.decimalPrecision)));
					} else if(DECIMAL_SCALE.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(Konig.decimalScale)));
					} else if(SECURITY_CLASSIFICATION.equals(header)) {
						newPropertyConstraint.add(stringLiteral(property.getValue(Konig.qualifiedSecurityClassification)));
					} else {
						newPropertyConstraint.add("");
					}
				}
				int rowNumber = propertyConstraintsMatchingRow(propertyContraints, shapeId, propertyId);
				if( rowNumber == 0) {
					newPropertyConstraints.add(newPropertyConstraint);
					appendValues(newPropertyConstraints, sheet.getProperties().getTitle());
				} else if(rowNumber != 0 && isOverrideRDFValues) {
					updateValues(newPropertyConstraint, sheet.getProperties().getTitle(), rowNumber);
				} else if(isRaiseError) {
					throw new KonigException("Conflict in the Shape ["+shapeId+"], Property ["+propertyId+"] ");
				} else if(isOverrideSheetValues) {
					LOG.warning("Skipped updating Shape ["+shapeId+"], Property ["+propertyId+"]" );
				}
			}	
		}
	}
	
	private String getTargetClass(String shape) {
		return targetClassList.get(shape) == null ? "" : targetClassList.get(shape);
	}
	
	private int propertyConstraintsMatchingRow(List<List<Object>> values, String shapeId, String propertyId ) {
		int rowNumber = 0;
		int matchingRow = 0;
		if(values != null) {
			int propertyIndex = 0;
			int shapeIndex = 0;
			for (List<Object> valueList : values) {
				rowNumber ++;
				for (int i =0; i< valueList.size(); i++) { 
					Object value = valueList.get(i);
					if(value.toString().equals(PROPERTY_ID)) {
						propertyIndex = i;
					}
					if(value.toString().equals(SHAPE_ID)) {
						shapeIndex = i;
					}
					String shape = (String)valueList.get(shapeIndex);
					String property = (String)valueList.get(propertyIndex);
					if(shape != null && shape.equals(shapeId) && property != null && property.equals(propertyId)) {
						matchingRow = rowNumber;
						return matchingRow;
					}
				}
			}
		}
		return matchingRow;
	}
	
	private void addPropertyConstraintHeaders(List<Object> headers, List<Vertex> shapeList) {
		for(Vertex v : shapeList) {
			List<Vertex> propertyList = v.asTraversal().out(SH.property).toVertexList();
			for(Vertex property : propertyList) {
				if(v.getId() != null && !headers.contains(SHAPE_ID)){
					headers.add(SHAPE_ID);
				}  
				if(property.getValue(RDFS.COMMENT) != null && !headers.contains(COMMENT)) {
					headers.add(COMMENT);
				}  
				if(property.getValue(SH.path) != null && !headers.contains(PROPERTY_ID)) {
					headers.add(PROPERTY_ID);
				}  
				if( (property.getValue(SH.datatype) != null  
						|| property.getValue(SH.valueClass) != null 
						|| property.getValue(SH.shape) != null) && !headers.contains(VALUE_TYPE)) {
					headers.add(VALUE_TYPE);
				} 
				if(property.getValue(SH.minCount) != null && !headers.contains(MIN_COUNT)) {
					headers.add(MIN_COUNT);
				} 
				if(property.getValue(SH.maxCount) != null && !headers.contains(MAX_COUNT)) {
					headers.add(MAX_COUNT);
				} 
				if(property.getValue(SH.uniqueLang) != null && !headers.contains(UNIQUE_LANG)) {
					headers.add(UNIQUE_LANG);
				} 
				if(property.getValue(SH.valueClass) != null && !headers.contains(VALUE_CLASS)) {
					headers.add(VALUE_CLASS);
				} 
				if(property.getValue(Konig.stereotype) != null && !headers.contains(STEREOTYPE)) {
					headers.add(STEREOTYPE);
				} 
				if(property.getValue(SH.in) != null && !headers.contains(VALUE_IN)) {
					headers.add(VALUE_IN);
				}
				if(property.getValue(Konig.sourcePath) != null && !headers.contains(SOURCE_PATH)) {
					headers.add(SOURCE_PATH);
				} 
				if(property.getValue(Konig.partitionOf) != null && !headers.contains(PARTITION_OF)) {
					headers.add(PARTITION_OF);
				} 
				if(property.getValue(Konig.formula) != null && !headers.contains(FORMULA)) {
					headers.add(FORMULA);
				}
				if(property.getValue(SH.minInclusive) != null && !headers.contains(MIN_INCLUSIVE)) {
					headers.add(MIN_INCLUSIVE);
				} 
				if(property.getValue(SH.maxInclusive) != null && !headers.contains(MAX_INCLUSIVE)) {
					headers.add(MAX_INCLUSIVE);
				} 
				if(property.getValue(SH.minExclusive) != null && !headers.contains(MIN_EXCLUSIVE)) {
					headers.add(MIN_EXCLUSIVE);
				} 
				if(property.getValue(SH.maxExclusive) != null && !headers.contains(MAX_EXCLUSIVE)) {
					headers.add(MAX_EXCLUSIVE);
				} 
				if(property.getValue(SH.minLength) != null && !headers.contains(MIN_LENGTH)) {
					headers.add(MIN_LENGTH);
				} 
				if(property.getValue(SH.maxLength) != null && !headers.contains(MAX_LENGTH)) {
					headers.add(MAX_LENGTH);
				} 
				if(property.getValue(Konig.decimalPrecision) != null && !headers.contains(DECIMAL_PRECISION)) {
					headers.add(DECIMAL_PRECISION);
				} 
				if(property.getValue(Konig.decimalScale) != null && !headers.contains(DECIMAL_SCALE)) {
					headers.add(DECIMAL_SCALE);
				} 
				if(property.getValue(Konig.qualifiedSecurityClassification) != null && !headers.contains(SECURITY_CLASSIFICATION)) {
					headers.add(SECURITY_CLASSIFICATION);
				}
			}
		}
	}
	
	private void updateProperties(Graph graph, Sheet sheet) throws IOException {
		List<Vertex> propertyList = graph.v(RDF.PROPERTY).in(RDF.TYPE).toVertexList();
		int columns = 0;
		
		List<List<Object>> properties = null;
		List<Object> headers = new ArrayList<>();
		if(sheet != null) {
			ValueRange response = sheets.spreadsheets().values()
					.get(sheetId, sheet.getProperties().getTitle())
					.execute();
			properties = response.getValues();
			if(properties != null) {
				headers = properties.get(0);
				columns = headers.size();
			}
		} else if(!propertyList.isEmpty()){
			addNewSheet("Properties");
		}
		
		addPropertyHeaders(headers, propertyList);
		if(columns < headers.size()) {
			updateValues(headers, sheet.getProperties().getTitle(), HEADER_ROW);
		}
		
		List<List<Object>> newProperties = new ArrayList<>();
		List<Object> property = null;
		
		for(Vertex v : propertyList) {
			property = new ArrayList<>();
			String propertyId = stringLiteral(v.getId());
			for (Object header : headers) {
				if(PROPERTY_NAME.equals(header)){
					property.add(stringLiteral(v.getValue(RDFS.LABEL)));
				} else if(COMMENT.equals(header)) {
					property.add(stringLiteral(v.getValue(RDFS.COMMENT)));
				} else if(PROPERTY_ID.equals(header)) {
					property.add(stringLiteral(v.getId()));
				} else if(DOMAIN.equals(header)) {
					String domain = "";
					if(v.getValue(RDFS.DOMAIN) != null){
						property.add(stringLiteral(v.getValue(RDFS.DOMAIN)));
					}else if(v.getValueSet(Schema.domainIncludes) != null) {
						for(Value domainIncludes : v.getValueSet(Schema.domainIncludes)) {
							domain = stringLiteral(domainIncludes) + "\n" +  domain;
						}
						property.add(domain);
					}else {
						property.add(domain);
					}
				} else if(RANGE.equals(header)) {
					String range = "";
					if(v.getValue(RDFS.RANGE) != null){
						range = stringLiteral(v.getValue(RDFS.RANGE));
					}else if(v.getValueSet(Schema.rangeIncludes) != null) {
						for(Value rangeIncludes : v.getValueSet(Schema.rangeIncludes)) {
							range = stringLiteral(rangeIncludes) + "\n" +  range;
						}
					}
					property.add(range);						
				} else if(INVERSE_OF.equals(header)) {
					property.add(stringLiteral(v.getValue(OWL.INVERSEOF)));
				} else if(PROPERTY_TYPE.equals(header)) {
					if(v.getValueSet(RDF.TYPE) != null) {
						Set<Value> values = v.getValueSet(RDF.TYPE);
						for(Value value: values) {
							if(isDatatype(new URIImpl(value.stringValue()))) {
								property.add(stringLiteral(value));
							}
						}
					}
				} else if(SECURITY_CLASSIFICATION.equals(header)) {
					String securityClassification = "";
					if(v.getValue(Konig.securityClassification) != null){
						securityClassification = stringLiteral(v.getValue(Konig.securityClassification));
					}else if(v.getValueSet(Konig.securityClassification) != null) {
						for(Value rangeIncludes : v.getValueSet(Konig.securityClassification)) {
							securityClassification = stringLiteral(rangeIncludes) + "\n" +  securityClassification;
						}
					}
					property.add(securityClassification);
				} else if(SUBPROPERTY_OF.equals(header)) {
					property.add(stringLiteral(v.getValue(RDFS.SUBPROPERTYOF)));
				}
			}
			
			int rowNumber = propertyExistRow(properties, propertyId);
			if( rowNumber == 0) {
				newProperties.add(property);
				appendValues(newProperties, sheet.getProperties().getTitle());
			} else if(rowNumber != 0 && isOverrideRDFValues) {
				updateValues(property, sheet.getProperties().getTitle(), rowNumber);
			} else if(isRaiseError) {
				throw new KonigException("Conflict in the Property["+propertyId+"]");
			} else if(isOverrideSheetValues) {
				LOG.warning("Skipped updating Property ["+propertyId+"]" );
			}
			
		}
	}
	
	private int propertyExistRow(List<List<Object>> values, String propertyId) {
		int rowNumber = 0;
		int matchingRow = 0;
		if(values != null) {
			int columnIndex = 0;
			for (List<Object> valueList : values) {
				rowNumber ++;
				for (int i =0; i< valueList.size(); i++) { 
					Object value = valueList.get(i);
					if(value.toString().equals(PROPERTY_ID)) {
						columnIndex = i;
					}
					String property = (String)valueList.get(columnIndex);
					if(property != null && property.equals(propertyId)) {
						matchingRow = rowNumber;
						return matchingRow;
					}
				}
			}
		}
		return matchingRow;
	}
	
	private boolean isDatatype(Resource subject) {

		if (subject instanceof URI) {

			if (XMLSchema.NAMESPACE.equals(((URI) subject).getNamespace())) {
				return true;
			}

			if (subject.equals(Schema.Boolean) || subject.equals(Schema.Date) || subject.equals(Schema.DateTime)
					|| subject.equals(Schema.Number) || subject.equals(Schema.Float)
					|| subject.equals(Schema.Integer) || subject.equals(Schema.Text) || subject.equals(Schema.Time)
					|| subject.equals(RDFS.LITERAL) || subject.equals(RDFS.DATATYPE)
					|| subject.equals(RDF.XMLLITERAL)

			) {
				return true;
			}
		}

		return false;
	}
	
	private void addPropertyHeaders(List<Object> headers, List<Vertex> propertyList) {
		for(Vertex v : propertyList) {
			if(v.getId() != null && !headers.contains(PROPERTY_ID)){
				headers.add(PROPERTY_ID);
			} 
			if(v.getValue(RDFS.LABEL) != null && !headers.contains(PROPERTY_NAME)) {
				headers.add(PROPERTY_NAME);
			} 
			if(v.getValue(RDFS.COMMENT) != null && !headers.contains(COMMENT)) {
				headers.add(COMMENT);
			} 
			if(v.getValue(RDFS.DOMAIN) != null && !headers.contains(DOMAIN)) {
				headers.add(DOMAIN);
			} 
			if(v.getValue(RDFS.RANGE) != null && !headers.contains(RANGE)) {
				headers.add(RANGE);
			} 
			if(v.getValue(OWL.INVERSEOF) != null && !headers.contains(INVERSE_OF)) {
				headers.add(INVERSE_OF);
			} 
			if(!headers.contains(PROPERTY_TYPE)) {
				headers.add(PROPERTY_TYPE);
			}
			if(v.getValue(Konig.securityClassification) != null && !headers.contains(SECURITY_CLASSIFICATION)) {
				headers.add(SECURITY_CLASSIFICATION);
			} 
			if(v.getValue(RDFS.SUBPROPERTYOF) != null && !headers.contains(SUBPROPERTY_OF)) {
				headers.add(SUBPROPERTY_OF);
			} 
		}
	}
	
	
	private void updateClasses(Graph graph, Sheet sheet) throws IOException {
		List<Vertex> classList = graph.v(OWL.CLASS).in(RDF.TYPE).toVertexList();
		List<List<Object>> classes = null;
		List<Object> headers = new ArrayList<>();
		int columns = 0;
		if(sheet != null) {
			ValueRange response = sheets.spreadsheets().values()
					.get(sheetId, sheet.getProperties().getTitle())
					.execute();
			classes = response.getValues();
			if(classes != null) {
				headers = classes.get(0);
				columns = headers.size();
			}
		} else if(!classList.isEmpty()){
			addNewSheet("Classes");
		}
		addClassHeaders(headers, classList);
		if(columns < headers.size()) {
			updateValues(headers, sheet.getProperties().getTitle(), HEADER_ROW);
		}
		List<List<Object>> newClasses = new ArrayList<>();
		List<Object> newclass = null;
		
		for(Vertex v : classList) {
			newclass = new ArrayList<>();
			String classId = stringLiteral(v.getId());
			String subClassOf = stringLiteral(v.getValue(RDFS.SUBCLASSOF));
			if(!classId.equals("") && !subClassOf.equals("")) {
				for (Object header : headers) {
					if(CLASS_NAME.equals(header)){
						newclass.add(stringLiteral(v.getValue(RDFS.LABEL)));
					} else if(COMMENT.equals(header)) {
						newclass.add(stringLiteral(v.getValue(RDFS.COMMENT)));
					} else if(CLASS_ID.equals(header)) {
						newclass.add(classId);
					} else if(CLASS_SUBCLASS_OF.equals(header)) {
						newclass.add(subClassOf);
					}else {
						newclass.add("");
					}
				}
				
				int rowNumber = classExistRow(classes, classId);
				if( rowNumber == 0) {
					newClasses = new ArrayList<>();
					newClasses.add(newclass);
					appendValues(newClasses, sheet.getProperties().getTitle());
				} else if(rowNumber != 0 && isOverrideRDFValues) {
					updateValues(newclass, sheet.getProperties().getTitle(), rowNumber);
				} else if(isRaiseError) {
					throw new KonigException("Conflict in the class["+classId+"]");
				} else if(isOverrideSheetValues) {
					LOG.warning("Skipped updating class ["+classId+"]" );
				}
			}
		}
	}
	
	private int classExistRow(List<List<Object>> values, String classId) {
		int rowNumber = 0;
		int matchingRow = 0;
		if(values != null) {
			int columnIndex = 0;
			for (List<Object> valueList : values) {
				rowNumber ++;
				for (int i =0; i< valueList.size(); i++) { 
					Object value = valueList.get(i);
					if(value.toString().equals(CLASS_ID)) {
						columnIndex = i;
					}
					String clazz = (String) valueList.get(columnIndex);
					if(clazz.equals(classId)) {
						matchingRow = rowNumber;
						return matchingRow;
					}
				}
			}
		}
		return matchingRow;
	}
	private void addClassHeaders(List<Object> headers, List<Vertex> classList) {
		for(Vertex v : classList) {
			if(v.getId() != null && !headers.contains(CLASS_ID)){
				headers.add(CLASS_ID);
			}  
			if(v.getValue(RDFS.LABEL) != null && !headers.contains(CLASS_NAME)) {
				headers.add(CLASS_NAME);
			} 
			if(v.getValue(RDFS.COMMENT) != null && !headers.contains(COMMENT)) {
				headers.add(COMMENT);
			} 
			if(v.getValue(RDFS.SUBCLASSOF) != null && !headers.contains(CLASS_SUBCLASS_OF)) {
				headers.add(CLASS_SUBCLASS_OF);
			} 
		}
	}
	
	private void updateShapes(Graph graph, Sheet sheet) throws IOException, IriTemplateParseException {
		List<Vertex> shapeList = graph.v(SH.Shape).in(RDF.TYPE).toVertexList();
		List<List<Object>> shapes = null;
		List<Object> headers = new ArrayList<>();
		int columns = 0;
		
		if(sheet != null) {
			ValueRange response = sheets.spreadsheets().values()
					.get(sheetId, sheet.getProperties().getTitle())
					.execute();
			shapes = response.getValues();
			if(shapes != null) {
				headers = shapes.get(0);
				columns = headers.size();
			}
		}  else if(!shapeList.isEmpty()){
			addNewSheet("Shapes");
		}
		addShapeHeaders(headers, shapeList);
		if(columns < headers.size()) {
			updateValues(headers, sheet.getProperties().getTitle(), HEADER_ROW);
		}
		
		List<List<Object>> newshapes = new ArrayList<>();
		List<Object> shape = null;
		
		for(Vertex v : shapeList) {
			targetClassList.put(stringLiteral(v.getId()), stringLiteral(v.getValue(SH.targetClass)));
			shape = new ArrayList<>();
			String shapeId = stringLiteral(v.getId());
			for (Object header : headers) {
				if(SHAPE_ID.equals(header) && v.getId() != null) {
					shape.add(stringLiteral(v.getId()));
				} else if(TARGET_CLASS.equals(header)) {
					shape.add(stringLiteral(v.getValue(SH.targetClass)));
				} else if(SCOPE_CLASS.equals(header)) {
					shape.add(stringLiteral(v.getValue(SH.targetClass)));
				} else if(MEDIA_TYPE.equals(header)) {
					shape.add(stringLiteral(v.getValue(Konig.mediaTypeBaseName)));
				} else if(AGGREGATION_OF.equals(header)) {
					shape.add(stringLiteral(v.getValue(Konig.aggregationOf)));
				} else if(ROLL_UP_BY.equals(header)) {
					shape.add(stringLiteral(v.getValue(Konig.rollUpBy)));
				} else if(BIGQUERY_TABLE.equals(header)) {
					shape.add(stringLiteral(v.getValue(Konig.bigQueryTableId)));
				} else if(DATASOURCE.equals(header)) {
					String dataSourceValue = "";
					for(Value dataSource : v.getValueSet(Konig.shapeDataSource)) {
						if(graph.getVertex(dataSource.toString()) != null) {
							Value datasourceValue = graph.getVertex(dataSource.toString()).getValue(RDF.TYPE);
							if(datasourceValue != null) {
								dataSourceValue = new URIImpl(datasourceValue.toString()).getLocalName() + "\n" + dataSourceValue;
							} else {
								dataSourceValue = new URIImpl(dataSource.toString()).getLocalName() + "\n" + dataSourceValue;
							}
						}
					}
					shape.add(dataSourceValue);
				} else if(IRI_TEMPLATE.equals(header)) {
					if(v.getValue(Konig.iriTemplate) != null) {
						IriTemplateParser parser = new IriTemplateParser();
						IriTemplate t =	parser.parse(new StringReader(v.getValue(Konig.iriTemplate).stringValue()));
						shape.add(t.getText());
					} else {
						shape.add("");
					}
				} else if(DEFAULT_FOR.equals(header)) {
					String defaultShapeValue = "";
					 if(v.getValueSet(Konig.defaultShapeFor) != null) {
						for(Value defaultShapeFor : v.getValueSet(Konig.defaultShapeFor)) {
							defaultShapeValue = stringLiteral(defaultShapeFor) + "\n" +  defaultShapeValue;
						}
					} else if(v.getValue(Konig.defaultShapeFor) != null){
						defaultShapeValue = stringLiteral(v.getValue(Konig.defaultShapeFor));
					}
					shape.add(defaultShapeValue);
				} else if(TERM_STATUS.equals(header)) {
					shape.add(stringLiteral(v.getValue(Konig.KeyTerm)));
				} else if(TABULAR_ORIGIN_SHAPE.equals(header)) {
					shape.add(stringLiteral(v.getValue(Konig.tabularOriginShape)));
				} else if(ONE_OF.equals(header)) {
					String orValue = "";
					if(v.getValueSet(SH.or) != null) {
						for(Value oneOf : v.getValueSet(SH.or)) {
							orValue = stringLiteral(oneOf) + "\n" +  orValue;
						}
					} else if(v.getValue(SH.or) != null){
						orValue = stringLiteral(v.getValue(SH.or));
					}
					shape.add(orValue);
				} else if(COMMENT.equals(header)) {
					shape.add(stringLiteral(v.getValue(RDFS.COMMENT)));
				} else if(SHAPE_OF.equals(header)) {
					String inputShapeValue = "";
					 if(v.getValueSet(Konig.inputShapeOf) != null) {
						for(Value inputShapeFor : v.getValueSet(Konig.inputShapeOf)) {
							inputShapeValue = stringLiteral(inputShapeFor) + "\n" +  inputShapeValue;
						}
					} else if(v.getValue(Konig.inputShapeOf) != null){
						inputShapeValue = stringLiteral(v.getValue(Konig.inputShapeOf));
					}
					shape.add(inputShapeValue);
				}else {
					shape.add("");
				}
			}
			
			int rowNumber = shapeExistRow(shapes, shapeId);
			if( rowNumber == 0) {
				newshapes.add(shape);
				appendValues(newshapes, sheet.getProperties().getTitle());
			} else if(rowNumber != 0 && isOverrideRDFValues) {
				updateValues(shape, sheet.getProperties().getTitle(), rowNumber);
			} else if(isRaiseError) {
				throw new KonigException("Conflict in the Shape["+shapeId+"]");
			} else if(isOverrideSheetValues) {
				LOG.warning("Skipped updating Shape["+shapeId+"]" );
			}
			
		}
	}
	
	private int shapeExistRow(List<List<Object>> values, String shapeId) {
		int rowNumber = 0;
		int matchingRow = 0;
		if(values != null) {
			int columnIndex = 0;
			int targetClassIndex = 0;
			for (List<Object> valueList : values) {
				rowNumber ++;
				for (int i =0; i< valueList.size(); i++) { 
					Object value = valueList.get(i);
					if(value.toString().equals(SHAPE_ID)) {
						columnIndex = i;
					}
					if(value.toString().equals(TARGET_CLASS) || value.toString().equals(SCOPE_CLASS)) {
						targetClassIndex = i;
					}
					if(columnIndex != 0 && targetClassIndex !=0) {
						String shape = (String)valueList.get(columnIndex);
						String targetClass = (String)valueList.get(targetClassIndex);
						targetClassList.put(shape, targetClass);
						if(shape != null && shape.equals(shapeId)) {
							matchingRow = rowNumber;
							return matchingRow;
						}
					}
				}
			}
		}
		return matchingRow;
	}
	
	private void addShapeHeaders(List<Object> headers, List<Vertex> shapeList) {
		for(Vertex v : shapeList) {
			if(v.getId() != null && !headers.contains(SHAPE_ID)){
				headers.add(SHAPE_ID);
			} 
			if(v.getValue(SH.targetClass) != null && !headers.contains(TARGET_CLASS)) {
				headers.add(TARGET_CLASS);
			} 
			if(v.getValue(Konig.mediaTypeBaseName) != null && !headers.contains(MEDIA_TYPE)) {
				headers.add(MEDIA_TYPE);
			} 
			if(v.getValue(Konig.aggregationOf) != null && !headers.contains(AGGREGATION_OF)) {
				headers.add(AGGREGATION_OF);
			} 
			if(v.getValue(Konig.rollUpBy) != null && !headers.contains(ROLL_UP_BY)) {
				headers.add(ROLL_UP_BY);
			} 
			if(v.getValue(Konig.shapeDataSource) != null && !headers.contains(DATASOURCE)) {
				headers.add(DATASOURCE);
			} 
			if(v.getValue(Konig.bigQueryTableId) != null && !headers.contains(BIGQUERY_TABLE)) {
				headers.add(BIGQUERY_TABLE);
			} 
			if(v.getValue(Konig.iriTemplate) != null && !headers.contains(IRI_TEMPLATE)) {
				headers.add(IRI_TEMPLATE);
			} 
			if(v.getValue(Konig.defaultShapeFor) != null && !headers.contains(DEFAULT_FOR)) {
				headers.add(DEFAULT_FOR);
			} 
			if(v.getValue(Konig.KeyTerm) != null && !headers.contains(TERM_STATUS)) {
				headers.add(TERM_STATUS);
			} 
			if(v.getValue(Konig.tabularOriginShape) != null && !headers.contains(TABULAR_ORIGIN_SHAPE)) {
				headers.add(TABULAR_ORIGIN_SHAPE);
			} 
 			if(v.getValue(SH.or) != null && !headers.contains(ONE_OF)) {
				headers.add(ONE_OF);
			} 
 			if(v.getValue(RDFS.COMMENT) != null && !headers.contains(COMMENT)) {
				headers.add(COMMENT);
			} 
 			if(v.getValue(Konig.inputShapeOf) != null && !headers.contains(SHAPE_OF)) {
				headers.add(SHAPE_OF);
			} 
		}
	}
	
	private boolean appendValues(List<List<Object>> value, String sheetTitle) throws IOException {
		if(!value.isEmpty()){
			ValueRange body = new ValueRange()
			        .setValues(value);
			AppendValuesResponse result =
					sheets.spreadsheets().values().append(sheetId, sheetTitle, body)
					.setValueInputOption("RAW")
					.setInsertDataOption("INSERT_ROWS")
			        .execute();
			while(result == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return true;
			}
			LOG.info("Total number of cells added ["+result.toPrettyString()+ "]");	
		}
		return false;
	}
	
	private boolean updateValues(List<Object> value, String sheetTitle, int rowNumber) throws IOException {
		if(!value.isEmpty()){
			String defaultRange = sheetTitle+"!A"+rowNumber;
			List<List<Object>> values = new ArrayList<>();
			values.add(value);
			ValueRange body = new ValueRange()
					.setMajorDimension("ROWS")
			        .setValues(values);
			UpdateValuesResponse result =
					sheets.spreadsheets().values().update(sheetId, defaultRange, body)
					.setValueInputOption("RAW")
			        .execute();
			while(result == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return true;
			}
			LOG.info("Total number of cells updated ["+result.toPrettyString()+ "]");
		} 
		return false;
	}
	
	private void addNewSheet(String title) throws IOException {
		List<Request> requests = new ArrayList<>();
		AddSheetRequest addSheetReq = new AddSheetRequest();
		addSheetReq.setProperties(new SheetProperties().setTitle(title));
		requests.add(new Request().setAddSheet(addSheetReq));
		BatchUpdateSpreadsheetRequest req = new BatchUpdateSpreadsheetRequest();
		req.setRequests(requests);
		req.setIncludeSpreadsheetInResponse(false);
		BatchUpdateSpreadsheetResponse rsp = sheets.spreadsheets().batchUpdate(sheetId, req).execute();
		List<Response> responses = rsp.getReplies();
		AddSheetResponse addSheetResponse = responses.get(0).getAddSheet();
		LOG.info("New Sheet Added [" + addSheetResponse.toPrettyString()+ "] ");
	}
	
	private int sheetType(Sheet sheet) throws IOException {
		ValueRange responseValue = sheets.spreadsheets().values()
				.get(sheetId, sheet.getProperties().getTitle())
				.execute();
		List<List<Object>> values = responseValue.getValues();
		List<Object> header = values.get(0);
		if (header == null) {
			return 0;
		}
		int colSize = header.size();
		int bits = 0;
		for (int i = 0; i < colSize; i++) {
			String cell = (String)header.get(i);
			if (cell == null)
				continue;
			switch (cell) {
				case NAMESPACE_URI:
					bits = bits | COL_NAMESPACE_URI;
					break;
				case CLASS_ID:
					bits = bits | COL_CLASS_ID;
					break;
				case PROPERTY_ID:
					bits = bits | COL_PROPERTY_PATH;
					break;
				case SHAPE_ID:
					bits = bits | COL_SHAPE_ID;
					break;
			}
		}
		return bits;
	}
	
	static class SheetInfo implements Comparable<SheetInfo> {
		int sheetType;
		int sheetIndex;
		Sheet sheet;
		
		public SheetInfo(int sheetType, int sheetIndex) {
			this.sheetType = sheetType;
			this.sheetIndex = sheetIndex;
			
		}

		public SheetInfo(int sheetType, Sheet sheet) {
			this.sheetType = sheetType;
			this.sheetIndex = sheet.getProperties().getIndex();
			this.sheet = sheet;
		}
		
		@Override
		public int compareTo(SheetInfo other) {
			int result = sheetType - other.sheetType;
			if (result == 0) {
				result = sheetIndex - other.sheetIndex;
			}
			return result;
		}
	}
	
	private List<SheetInfo> collectSheetInfo() throws IOException {
		List<SheetInfo> list = new ArrayList<>();
		Spreadsheet response= sheets.spreadsheets().get(sheetId).setIncludeGridData(false)
				.execute ();
		List<Sheet> sheetList = response.getSheets();
		for (int i = 0; i < sheetList.size(); i++) {
			Sheet s = sheetList.get(i);
			int sheetType = sheetType(s);
			list.add(new SheetInfo(sheetType, s));
		}
		return list;
	}
	
	private String stringLiteral(Value v) {
		String text = v != null? v.stringValue() : "" ;
		if (text.startsWith("http://") || text.startsWith("https://")) {
			String prefix = prefixList.get(new URIImpl(new URIImpl(text).getNamespace()));
			if(prefix != null) {
				text = prefix + ":" + new URIImpl(text).getLocalName();
			} else {
				prefix = new URIImpl(text).getNamespace();
				text = prefix + "/" + new URIImpl(text).getLocalName();
			}
		}
		return text;
	}
	
	private void addPrefix(List<List<Object>> values) {
		List<Object> headers = values.get(0);
		int uriIndex = 0;
		int prefixIndex = 0;
		for (int i =0; i < headers.size() ; i++) {
			if(NAMESPACE_URI.equals(headers.get(i).toString())) {
				uriIndex = i;
			}else if(PREFIX.equals(headers.get(i).toString())) {
				prefixIndex = i;
			}
		}
		for (List<Object> valueList : values) {
			String namespaceURI = valueList.get(uriIndex).toString();
			if (namespaceURI.startsWith("http://") || namespaceURI.startsWith("https://")) {
				prefixList.put(vf.createURI(namespaceURI), valueList.get(prefixIndex).toString());
			}
		}
	}
}
