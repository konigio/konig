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


import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.RelationshipDegree;
import io.konig.shacl.Shape;

public class PropertyConstraintSheet extends BaseSheetProcessor {
	private static final Logger logger = LoggerFactory.getLogger(PropertyConstraintSheet.class);
	
	private static final SheetColumn SHAPE_ID = new SheetColumn("Shape Id", true);
	private static final SheetColumn PROPERTY_ID = new SheetColumn("Property Id", true);
	private static final SheetColumn COMMENT = new SheetColumn("Comment");
	private static final SheetColumn VALUE_TYPE = new SheetColumn("Value Type", true);
	private static final SheetColumn MIN_COUNT = new SheetColumn("Min Count", true);
	private static final SheetColumn MAX_COUNT = new SheetColumn("Max Count", true);
	private static final SheetColumn VALUE_CLASS = new SheetColumn("Value Class");
	private static final SheetColumn FORMULA = new SheetColumn("Formula");
	private static final SheetColumn VALUE_IN = new SheetColumn("Value In");
	private static final SheetColumn STEREOTYPE = new SheetColumn("Stereotype");
	private static final SheetColumn SECURITY_CLASSIFICATION = new SheetColumn("Security Classification");
	private static final SheetColumn STATUS = new SheetColumn("Status");
	private static final SheetColumn SUBJECT = new SheetColumn("Subject");
	private static final SheetColumn UNIQUE_LANG = new SheetColumn("Unique Lang");
	private static final SheetColumn RELATIONSHIP_DEGREE = new SheetColumn("Relationship Degree");
	private static final SheetColumn MIN_INCLUSIVE = new SheetColumn("Min Inclusive");
	private static final SheetColumn MAX_INCLUSIVE = new SheetColumn("Max Inclusive");
	private static final SheetColumn MIN_EXCLUSIVE = new SheetColumn("Min Exclusive");
	private static final SheetColumn MAX_EXCLUSIVE = new SheetColumn("Max Exclusive");
	private static final SheetColumn MIN_LENGTH = new SheetColumn("Min Length");
	private static final SheetColumn MAX_LENGTH = new SheetColumn("Max Length");
	private static final SheetColumn DECIMAL_PRECISION = new SheetColumn("Decimal Precision");
	private static final SheetColumn DECIMAL_SCALE = new SheetColumn("Decimal Scale");
	private static final SheetColumn PREFERRED_TABULAR_SHAPE = new SheetColumn("References Shape");
	
	private static final SheetColumn[] COLUMNS = new SheetColumn[]{

			SHAPE_ID,
			PROPERTY_ID,
			COMMENT,
			VALUE_TYPE,
			MIN_COUNT,
			MAX_COUNT,
			VALUE_CLASS,
			FORMULA,
			VALUE_IN,
			STEREOTYPE,
			SECURITY_CLASSIFICATION,
			STATUS,
			SUBJECT,
			UNIQUE_LANG,
			RELATIONSHIP_DEGREE,
			MIN_INCLUSIVE,
			MAX_INCLUSIVE,
			MIN_EXCLUSIVE,
			MAX_EXCLUSIVE,
			MIN_LENGTH,
			MAX_LENGTH,
			DECIMAL_PRECISION,
			DECIMAL_SCALE,
			PREFERRED_TABULAR_SHAPE
	};
	
	private OwlReasoner reasoner;

	@SuppressWarnings("unchecked")
	protected PropertyConstraintSheet(WorkbookProcessor processor, OwlReasoner reasoner) {
		super(processor);
		dependsOn(OntologySheet.class, SettingsSheet.class, PropertySheet.class);
		this.reasoner = reasoner;
	}

	@Override
	public SheetColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
		
		URI shapeId = iriValue(row, SHAPE_ID);
		String propertyPathText = stringValue(row, PROPERTY_ID);
		String comment = stringValue(row, COMMENT);
		URI valueType = iriValue(row, VALUE_TYPE);
		Integer minCount = intValue(row, MIN_COUNT);
		Integer maxCount = maxCount(row, MAX_COUNT);
		URI valueClass = iriValue(row, VALUE_CLASS);
		String formulaText = stringValue(row, FORMULA);
		
		// TODO: Support value types other than IRI values
		@SuppressWarnings("unchecked")
		List<Value> valueIn = (List<Value>)(Object) nullableIriList(row, VALUE_IN);
		URI stereotype = iriValue(row, STEREOTYPE);
		List<URI> securityClassification = nullableIriList(row, SECURITY_CLASSIFICATION);
		URI status = iriValue(row, STATUS);
		Boolean uniqueLang = booleanValue(row, UNIQUE_LANG);
		URI relationshipDegree = iriValue(row, RELATIONSHIP_DEGREE);
		Integer minLength = intValue(row, MIN_LENGTH);
		Integer maxLength = intValue(row, MAX_LENGTH);
		Double minInclusive = doubleValue(row, MIN_INCLUSIVE);
		Double maxInclusive = doubleValue(row, MAX_INCLUSIVE);
		Double minExclusive = doubleValue(row, MIN_EXCLUSIVE);
		Double maxExclusive = doubleValue(row, MAX_EXCLUSIVE);
		Integer decimalPrecision = intValue(row, DECIMAL_PRECISION);
		Integer decimalScale = intValue(row, DECIMAL_SCALE);
		URI preferredTabularShapeId = iriValue(row, PREFERRED_TABULAR_SHAPE);
		Collection<URI> subject = iriList(row, SUBJECT);
		
		logger.trace("visit - shapeId: {}, propertyId: {}", compactName(shapeId), propertyPathText);
		
		if(decimalPrecision!=null && (decimalPrecision.intValue()<1 || decimalPrecision.intValue()>65)){
			fail(row, DECIMAL_PRECISION, "Decimal Precison must be in the range [1, 65]");
		}
		if (XMLSchema.DECIMAL.equals(valueType)) {
			if (decimalScale==null) {
				fail(row, DECIMAL_SCALE, "Decimal Scale must be defined for Property {0}", propertyPathText);
			}
			if (decimalPrecision==null) {
				fail(row, DECIMAL_PRECISION, "Decimal Precision must be defined for Property {0}", propertyPathText);
			}
		}

		if (decimalScale !=null && (decimalScale<0 || decimalScale>30 | decimalScale>decimalPrecision)) {
			fail(row, DECIMAL_SCALE, "Decimal Scale must be less than or equal to Decimal Precision and in the range [0, 30]");
		}

		if (formulaText != null) {
			// Check that the user terminates the where clause with a dot.

			Pattern pattern = Pattern.compile("\\sWHERE\\s");
			Matcher matcher = pattern.matcher(formulaText);
			if (matcher.find() && !formulaText.endsWith(".")) {
				formulaText = formulaText + " .";
			}

		}
		
		if (valueClass != null) {
			edge(valueClass, RDF.TYPE, OWL.CLASS);
			setDefaultSubject(valueClass);
		}
		
		PropertyConstraint p = new PropertyConstraint();
		
		Shape shape = produceShape(shapeId);
		
		PathType pathType = pathType(propertyPathText);
		
		switch (pathType) {
		
		case PREDICATE :
			URI predicate = processor.expandCurie(propertyPathText, row, PROPERTY_ID);
			
			// Special processing for konig:id
			if (Konig.id.equals(predicate)) {
				if (maxCount==null || maxCount>1) {
					fail(row, MAX_COUNT, "Max Count of konig:id must be 0 or 1");
				}
				if (!SH.IRI.equals(valueType) && !SH.BlankNodeOrIRI.equals(valueType)) {
					fail(row, VALUE_TYPE, "On Shape {0}, Value Type of konig:id must be sh:IRI or sh:BlankNodeOrIRI", compactName(shapeId));
				}
				shape.setNodeKind(minCount==0 ? NodeKind.BlankNodeOrIRI : NodeKind.IRI);
				if (valueClass != null) {
					shape.setTargetClass(valueClass);
				}
				
				// TODO: consider supporting Formula as an alternative for Shape IRI template.
				return;
			}
			
			// Special Processing for rdf:type
			if (RDF.TYPE.equals(predicate)) {
				if (valueClass == null) {
					valueClass = OWL.CLASS;
				} else if (!valueClass.equals(OWL.CLASS)) {
					fail(row, VALUE_CLASS, "In Shape {0}, the Value Class of rdf:type must be owl:Class", compactName(shapeId));
				}
				
				if (valueIn != null && valueIn.size()==1) {
					Value value = valueIn.get(0);
					if(value instanceof URI) {
						shape.setTargetClass((URI) value);
					} else {
						fail(row, VALUE_CLASS, "In Shape {0}, Value In must specify a URI");
					}
				}
			}
			
			
			PropertyConstraint prior = shape.getPropertyConstraint(predicate);
			if (prior != null) {
				p = prior;
				processor.warn(
					location(row, PROPERTY_ID), 
					"Duplicate definition of property {0} on {0}", 
					compactName(predicate), 
					compactName(shapeId)
				);
			}
			p.setPath(predicate);
			if (Konig.derivedProperty.equals(stereotype)) {
				shape.addDerivedProperty(p);
			} else {
				shape.add(p);
			}
			break;
			
		case VARIABLE :
			URI variableId = variableId(shapeId, propertyPathText);
			p.setPath(variableId);
			shape.addVariable(p);
			break;
			
		case SEQUENCE :
			// Ensure that the BuildLocalNameServiceAction gets executed
			processor.service(SimpleLocalNameService.class);
			ShapeFormulaAction action = processor.service(ShapeFormulaAction.class);
			action.addShapeFormulaBuilder(
				shape, true, new SequencePathBuilder(
						location(row, PROPERTY_ID), 
						p, 
						propertyPathText));
			break;
		}
		
		p.setComment(comment);
		
		switch (valueKind(valueType)) {
		
		case DATATYPE :
			p.setDatatype(valueType);
			break;
			
		case IRI_REFERENCE :
			p.setValueClass(valueClass);
			p.setNodeKind(NodeKind.IRI);
			break;
			
		case SHAPE:
			p.setValueClass(valueClass);
			p.setShape(produceShape(valueType));
			break;
		}
		
		p.setMinCount(minCount);
		p.setMaxCount(maxCount);
		p.setStereotype(stereotype);
		p.setQualifiedSecurityClassification(securityClassification);
		p.setIn(valueIn);
		
		if (formulaText != null) {
			// Ensure that the BuildLocalNameServiceAction gets executed
			processor.service(SimpleLocalNameService.class);
			ShapeFormulaAction action = processor.service(ShapeFormulaAction.class);
			action.addShapeFormulaBuilder(shape, true,
				new PropertyConstraintFormulaBuilder(
					location(row, FORMULA), 
					p, 
					formulaText));
		}
		
		declareStatus(status);
		p.setTermStatus(status);
		p.setUniqueLang(uniqueLang);
		p.setRelationshipDegree(RelationshipDegree.fromURI(relationshipDegree));
		p.setMinLength(minLength);
		p.setMaxLength(maxLength);
		p.setMinInclusive(minInclusive);
		p.setMaxInclusive(maxInclusive);
		p.setMinExclusive(minExclusive);
		p.setMaxExclusive(maxExclusive);
		p.setDecimalPrecision(decimalPrecision);
		p.setDecimalScale(decimalScale);
		p.setPreferredTabularShape(preferredTabularShapeId);
		
	}
	
	

	private Integer maxCount(SheetRow row, SheetColumn maxCount) throws SpreadsheetException {
		String text = stringValue(row, maxCount);
		if ("unbounded".equalsIgnoreCase(text)) {
			return null;
		}
		try {
			return new Integer(text);
		} catch (Throwable e) {
			fail(row, maxCount, "Max Count value must be 'unbounded' or an integer, but was: {0}", text);
		}
		
		return null;
	}

	private ValueKind valueKind(URI valueType) {
		return 
			reasoner.isDatatype(valueType) ? ValueKind.DATATYPE :
			SH.IRI.equals(valueType) ? ValueKind.IRI_REFERENCE :
			ValueKind.SHAPE;
	}



	private URI variableId(URI shapeId, String propertyPathText) {
		String localName = propertyPathText.substring(1);
		String iri = shapeId + "/variable/" + localName;
		
		return new URIImpl(iri);
	}

	private PathType pathType(String text) {
		if (text.startsWith("$")) {
			return PathType.SEQUENCE;
		}
		if (text.startsWith("?")) {
			return PathType.VARIABLE;
		}
		return PathType.PREDICATE;
	}

	private static enum PathType {
		PREDICATE,
		SEQUENCE,
		VARIABLE
	}
	
	private static enum ValueKind {
		DATATYPE,
		IRI_REFERENCE,
		SHAPE
	}


}
