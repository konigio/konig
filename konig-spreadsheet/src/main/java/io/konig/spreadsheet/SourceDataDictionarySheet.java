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


import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.jsonpath.JsonPathParseException;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SourceDataDictionarySheet extends BaseSheetProcessor {
	private static final SheetColumn SOURCE_TYPE = new SheetColumn("Source Type", true);
	private static final SheetColumn SOURCE_SYSTEM = new SheetColumn("Source System", true);
	private static final SheetColumn SOURCE_OBJECT_NAME = new SheetColumn("Source Object Name", true);
	private static final SheetColumn FIELD = new SheetColumn("Field", true);
	private static final SheetColumn DATA_TYPE = new SheetColumn("Data Type", true);
	private static final SheetColumn MAX_LENGTH = new SheetColumn("Max Length");
	private static final SheetColumn DECIMAL_PRECISION = new SheetColumn("Decimal Precision");
	private static final SheetColumn DECIMAL_SCALE = new SheetColumn("Decimal Scale");
	private static final SheetColumn CONSTRAINTS = new SheetColumn("Constraints");
	private static final SheetColumn FORMULA = new SheetColumn("Formula");
	private static final SheetColumn BUSINESS_NAME = new SheetColumn("Business Name");
	private static final SheetColumn BUSINESS_DEFINITION = new SheetColumn("Business Definition");
	private static final SheetColumn DATA_STEWARD = new SheetColumn("Data Steward");
	private static final SheetColumn SECURITY_CLASSIFICATION = new SheetColumn("Security Classification");
	private static final SheetColumn PII_CLASSIFICATION = new SheetColumn("PII Classification");
	private static final SheetColumn PROJECT = new SheetColumn("Project");
	private static final SheetColumn DERIVED_PROPERTY = new SheetColumn("Derived");
	
	private static final SheetColumn[] COLUMNS = new SheetColumn[]{
		SOURCE_TYPE,
		SOURCE_SYSTEM,
		SOURCE_OBJECT_NAME,
		FIELD,
		DATA_TYPE,
		MAX_LENGTH,
		DECIMAL_PRECISION,
		DECIMAL_SCALE,
		CONSTRAINTS,
		FORMULA,
		BUSINESS_NAME,
		BUSINESS_DEFINITION,
		DATA_STEWARD,
		SECURITY_CLASSIFICATION,
		PII_CLASSIFICATION,
		PROJECT,
		DERIVED_PROPERTY
	};

	private SettingsSheet settings;
	private IndividualSheet individuals;
	private Set<URI> shapeIdSet = new HashSet<>();
	
	@SuppressWarnings("unchecked")
	public SourceDataDictionarySheet(WorkbookProcessor processor, SettingsSheet settings, IndividualSheet individuals) {
		super(processor);
		this.settings = settings;
		this.individuals = individuals;
		dependsOn(OntologySheet.class, SettingsSheet.class, IndividualSheet.class, PropertySheet.class);
	}

	@Override
	public SheetColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {

//		String sourceTypeValue = stringValue(row, SOURCE_TYPE);
		String sourceSystemValue = stringValue(row, SOURCE_SYSTEM);
		String sourceObjectNameValue = stringValue(row, SOURCE_OBJECT_NAME);
		String fieldValue = stringValue(row, FIELD);
		String dataTypeValue = stringValue(row, DATA_TYPE);
		Integer maxLength = intValue(row, MAX_LENGTH);
		Integer decimalPrecision = intValue(row, DECIMAL_PRECISION);
		Integer decimalScale = intValue(row, DECIMAL_SCALE);
		String constraintsValue = stringValue(row, CONSTRAINTS);
		String formulaValue = stringValue(row, FORMULA);
		String businessNameValue = stringValue(row, BUSINESS_NAME);
		String businessDefinitionValue = stringValue(row, BUSINESS_DEFINITION);
		String dataStewardValue = stringValue(row, DATA_STEWARD);
		URI securityClassification = namedIndividual(row, SECURITY_CLASSIFICATION);
		URI piiClassification = namedIndividual(row, PII_CLASSIFICATION);
		
		List<URI> securityClassificationList = listOf(securityClassification, piiClassification);

		URI shapeId = shapeId(sourceSystemValue, sourceObjectNameValue);
		
		Set<URI> filter = settings.getExcludeSecurityClassification();
		if (filter.contains(securityClassification) || filter.contains(piiClassification)) {
			warn(location(row, null), 
				"Excluding property {0} on Shape {1} because of its security classification",
				fieldValue, compactName(shapeId));
			return;
		}
		if(decimalScale!=null &&(decimalScale<0 || decimalScale>30)){
			fail(row, DECIMAL_SCALE, "Decimal Scale must be in the range [0, 30]");
		}

		if (decimalScale!=null && decimalPrecision!=null && decimalScale>decimalPrecision) {
			fail(row, DECIMAL_SCALE, "Decimal Scale must be less than or equal to Decimal Precision");
		}
		
		Shape shape = produceShape(shapeId);
		setMediaType(row, shape, sourceSystemValue, sourceObjectNameValue);
		shape.addType(Konig.TabularNodeShape);
		
		PropertyConstraint p = dataDictionaryPropertyConstraint(row, FIELD, shape, fieldValue);
		p.setName(businessNameValue);
		p.setComment(businessDefinitionValue);
		

		if(constraintsValue!=null && constraintsValue.contains("Primary Key")){
			p.setStereotype(Konig.primaryKey);
		} else if(constraintsValue!=null && constraintsValue.contains("NOT NULL")){
			p.setMinCount(1);
		} else if(constraintsValue!=null && constraintsValue.contains("Unique Key")){
			p.setStereotype(Konig.uniqueKey);
			p.setMinCount(0);
		} else {
			p.setMinCount(0);
		}
		
		URI rdfDatatype = getRdfDatatype(dataTypeValue, p);
		if (rdfDatatype == null) {
			fail(row, DATA_TYPE, "Invalid datatype: {0}", dataTypeValue);
		}
		p.setDatatype(rdfDatatype);
		
		if (XMLSchema.STRING.equals(rdfDatatype) || XMLSchema.BASE64BINARY.equals(rdfDatatype)) {
			if (maxLength == null) {
				maxLength = settings.getDictionaryDefaultMaxLength();
			}
			p.setMaxLength(maxLength);
		}
		p.setQualifiedSecurityClassification(securityClassificationList);
		p.setDecimalPrecision(decimalPrecision);
		p.setDecimalScale(decimalScale);
		if (dataStewardValue != null) {
			p.dataSteward().setName(dataStewardValue);
		}
		
		if (formulaValue != null) {

			processor.service(SimpleLocalNameService.class);
			ShapeFormulaAction action = processor.service(ShapeFormulaAction.class);
			action.addShapeFormulaBuilder(shape, false,
				new PropertyConstraintFormulaBuilder(
					location(row, FORMULA), 
					p, 
					formulaValue), processor);
		}
		
		if (!shapeIdSet.contains(shapeId)) {
			shapeIdSet.add(shapeId);
			List<Function> dataSourceList = settings.getDefaultDataSource();
			if (dataSourceList !=null) {
	
				DataSourceGeneratorFactory dataSourceGeneratorFactory = service(DataSourceGeneratorFactory.class);
				processor.defer(
					new BuildDataSourceAction(
						location(row, null),
						processor,
						dataSourceGeneratorFactory.getDataSourceGenerator(),
						shape,
						processor.getShapeManager(),
						dataSourceList
						
				));
			}
		}
		

	}


	private void setMediaType(SheetRow row, Shape shape, String sourceSystemValue, String sourceObjectNameValue) throws SpreadsheetException {
		if (shape.getMediaTypeBaseName() == null) {
			String vendorName = settings.getMediaTypeVendorName();
			if (vendorName == null) {
				fail(row, SOURCE_OBJECT_NAME, "{} property must be defined", SettingsSheet.MEDIA_TYPE_VENDOR_NAME);
			}
			sourceSystemValue = StringUtil.mediaTypePart(sourceSystemValue);
			sourceObjectNameValue = StringUtil.mediaTypePart(sourceObjectNameValue);
			StringBuilder builder = new StringBuilder();
			builder.append("application/vnd.");
			builder.append(vendorName);
			builder.append('.');
			builder.append(sourceSystemValue.toLowerCase());
			builder.append('.');
			builder.append(sourceObjectNameValue.toLowerCase());
			
			shape.setMediaTypeBaseName(builder.toString());
		}
		
	}

	private URI getRdfDatatype(String typeName, PropertyConstraint constraint) {
		if (typeName==null) {
			return null;
		}
		typeName = typeName.trim().toUpperCase();
		switch(typeName){
			case "CHAR":
			case "VARCHAR":
			case "UUID":
			case "TEXT":
			case "STRING":
			case "VARCHAR2":
			case "NVARCHAR":
			case "NVARCHAR2":
				return XMLSchema.STRING;
			case "DATE":
				return XMLSchema.DATE;
				
			case "BIT" :
				constraint.setMinInclusive(0);
				constraint.setMaxInclusive(1);
				return XMLSchema.INTEGER;
				
			case "TIMESTAMP":
			case "DATETIME2":
			case "DATETIME":
				return XMLSchema.DATETIME;
			case "NUMBER":
				return XMLSchema.DECIMAL;
			case "SMALLINT":
				constraint.setMinInclusive(-32768);
				constraint.setMaxInclusive(32767);
				return XMLSchema.INTEGER;
			case "UNSIGNED SMALLINT":
				constraint.setMinInclusive(0);
				constraint.setMaxInclusive(65535);
				return XMLSchema.INTEGER;
				
			case "INTEGER":
			case "INT":
				constraint.setMinInclusive(-2147483648);
				constraint.setMaxInclusive(2147483647);
				return XMLSchema.INTEGER;
				
			case "INT64":
				return XMLSchema.LONG;
				
			case "UNSIGNED INT":
				constraint.setMinInclusive(0);
				constraint.setMaxInclusive(4294967295L);
				return XMLSchema.INTEGER;
			case "BIGINT":
				constraint.setMinInclusive(-9223372036854775808L);
				constraint.setMaxInclusive(9223372036854775807L);
				return XMLSchema.INTEGER;
			case "UNSIGNED BIGINT":
				constraint.setMinInclusive(0);
				constraint.setMaxInclusive(new BigInteger("18446744073709551615"));
				return XMLSchema.INTEGER;
				
			case "MONEY":
			case "FLOAT":
				return XMLSchema.FLOAT;
				
			case "FLOAT64":
			case "DOUBLE":
				return XMLSchema.DOUBLE;
			case "DECIMAL":
				return XMLSchema.DECIMAL;
			case "BINARY":
				return XMLSchema.BASE64BINARY;
			case "BOOLEAN":
				return XMLSchema.BOOLEAN;
		}
		return null;
	}

	private PropertyConstraint dataDictionaryPropertyConstraint(SheetRow row, SheetColumn col, Shape shape,
			String fieldName) throws SpreadsheetException {
		if (fieldName.startsWith("$")) {
			JsonPathPropertyConstraintBuilder builder = service(JsonPathPropertyConstraintBuilder.class);
			try {
				return builder.parse(shape, fieldName);
			} catch (JsonPathParseException | IOException e) {
				fail(row, col, e, "Failed to parse JSON path");
			}
			
		}

//		String snake_case_name = StringUtil.LABEL_TO_SNAKE_CASE(fieldName);
		String snake_case_name = fieldName;
		String baseURL = settings.getPropertyBaseURL();
		
		URI predicate = new URIImpl(concatPath(baseURL, snake_case_name));
		Boolean derivedProperty = booleanValue(row, DERIVED_PROPERTY);
		
		PropertyConstraint p = shape.getPropertyConstraint(predicate);
		if (p != null) {
			warn(location(row, col), "Duplicate definition of property {0} on {1}", fieldName, compactName(shape.getId()));
		} else {
			p = new PropertyConstraint(predicate);
			if (derivedProperty != null && derivedProperty) {
				NamespaceManager nsManager = processor.getGraph().getNamespaceManager();
				Namespace ns = nsManager.findByName(predicate.getNamespace());
				if (ns != null) {
					StringBuilder builder = new StringBuilder();
					builder.append(ns.getPrefix());
					builder.append(':');
					builder.append(predicate.getLocalName());
					p.setPredicate(new URIImpl(builder.toString()));
				}	
				shape.addDerivedProperty(p);
			} else {
				shape.add(p);
			}
		}
		p.setMaxCount(1);
		return p;
	}

	private List<URI> listOf(URI...id) {
		List<URI> list = new ArrayList<>();
		for (URI value : id) {
			if (value != null) {
				list.add(value);
			}
		}
		
		return list.isEmpty() ? null : list;
	}

	private URI namedIndividual(SheetRow row, SheetColumn col) throws SpreadsheetException {
		String name = stringValue(row, col);
		if (name == null) {
			return null;
		}
		Set<URI> set = individuals.getIndividualManager().getIndividualsByName(name);
		if (set.isEmpty()) {
			fail(row, col, "No individual found with name ''{0}'' ", name);
		}
		if (set.size()>1) {
			StringBuilder text = new StringBuilder();
			text.append("The name ''{0}'' is ambiguous.  Possible values include:");
			for (URI id : set) {
				text.append("\n   ");
				text.append(compactName(id));
			}
			fail(row, col, text.toString());
		}
		return set.iterator().next();
	}

	private URI shapeId(String sourceSystemName, String shapeIdLocalName) throws SpreadsheetException {
		URI shapeURI=null;
		String shapeURLTemplate=settings.getShapeUrlTemplate();
		if(shapeURLTemplate == null){
			fail(null, null, SettingsSheet.SHAPE_URL_TEMPLATE + " must be defined on the Settings sheet");
		}
		else{
//			String shapeLocalName = StringUtil.LABEL_TO_SNAKE_CASE(sourceSystemName);
			String shapeLocalName = sourceSystemName;
			String shapeURL = sourceSystemName==null ? shapeURLTemplate : shapeURLTemplate.replace("{SOURCE_SYSTEM}", shapeLocalName);
			shapeURL=shapeURL.replace("{SOURCE_OBJECT_NAME}", StringUtil.LABEL_TO_SNAKE_CASE(shapeIdLocalName));		
			if(settings!=null){
				for(Object key:settings.getProperties().keySet()){
					String propertyKey=(String)key;
					String propertyValue=settings.getProperties().getProperty(propertyKey);	
					shapeURL=(propertyValue==null)?shapeURL:shapeURL.replace("{"+propertyKey+"}", propertyValue);
				}
			}
			shapeURI=new URIImpl(shapeURL);
			NamespaceManager nsManager = processor.getGraph().getNamespaceManager();
			nsManager.add("shape", shapeURI.getNamespace());
		}
		return shapeURI;
	}

}
