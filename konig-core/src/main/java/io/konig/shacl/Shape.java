package io.konig.shacl;

import java.io.IOException;
import java.io.StringWriter;

/*
 * #%L
 * konig-shacl
 * %%
 * Copyright (C) 2015 Gregory McFall
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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.vocabulary.SKOS;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.activity.Activity;
import io.konig.annotation.RdfProperty;
import io.konig.core.Context;
import io.konig.core.UidGenerator;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.datasource.DataSource;
import io.konig.formula.Expression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.impl.EmptyList;

public class Shape implements Cloneable {
	private static final List<PropertyConstraint> EMPTY_PROPERTY_LIST = new EmptyList<PropertyConstraint>();
	
	private Resource id;
	private URI targetClass;
	private List<PropertyConstraint> property;
	private List<PropertyConstraint> derivedProperty;
	private List<PropertyConstraint> variable;
	private Context jsonldContext;
	private URI equivalentRelationalShape;
	private NodeKind nodeKind;
	private String comment;
	private URI termStatus;
	
	
	private AndConstraint and;
	private OrConstraint or;
	private XoneConstraint xone;
	private NotConstraint not;
	
	private Activity wasGeneratedBy;
	private String bigQueryTableId;
	private Set<URI> type;

	
	private IriTemplate iriTemplate;
	private QuantifiedExpression iriFormula;
	private URI idFormat;
	
	private List<DataSource> shapeDataSource;
	private Expression updateWhen;
	private int ordinal;
	private URI preferredJsonldContext;
	private URI preferredJsonSchema;
	private List<URI> defaultShapeFor;
	private List<Expression> constraint;

	private URI aggregationOf;
	private URI rollUpBy;
	private String mediaTypeBaseName;
	private List<URI> inputShapeOf;
	private Shape tabularOriginShape;
	private URI usesAbbreviationScheme;
	private List<ShapeMaxRowLength> shapeMaxRowLengthList;
	private Set<URI> shapeProcessing;
	private Set<Shape> explicitDerivedFrom;
	private Set<URI> broader;
	 
	 public List<URI> getInputShapeOf() {
	 	return inputShapeOf;
	 }

	 @RdfProperty(Konig.INPUT_SHAPE_OF)
	 public void setInputShapeOf(List<URI> inputShapeOf) {
	 	this.inputShapeOf = inputShapeOf;
	 }
	 public void addInputShapeOf(URI uri) {
			if (inputShapeOf == null) {
				inputShapeOf = new ArrayList<>();
			}
			inputShapeOf.add(uri);
		}
	 
	public Shape() {
		String bnodeId = UidGenerator.INSTANCE.next();
		id = new BNodeImpl(bnodeId);
		addType(SH.Shape);
	}
	
	public Shape(Resource id) {
		this.id = id;
		addType(SH.Shape);
	}
	
	/**
	 * Create a shallow clone of this Shape.
	 */
	@Override
	public Shape clone() {
		try {
			return (Shape) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	

	/**
	 * Recursively clone this shape, all of its properties, and the shapes referenced by
	 * SHACL logical constraints (sh:and, sh:or).
	 */
	public Shape deepClone() {
		Shape clone = clone();

		List<PropertyConstraint> propertyList = new ArrayList<>();
		clone.setProperty(propertyList);
		for (PropertyConstraint p : getProperty()) {
			propertyList.add(p.deepClone());
		}
		if (and != null) {
			AndConstraint cloneAnd = new AndConstraint();
			clone.setAnd(cloneAnd);
			for (Shape shape : and.getShapes()) {
				cloneAnd.add(shape.deepClone());
			}
		}
		if (or != null) {
			OrConstraint cloneOr = new OrConstraint();
			clone.setOr(cloneOr);
			for (Shape shape : or.getShapes()) {
				cloneOr.add(shape.deepClone());
			}
		}
		
		if (xone != null) {
			XoneConstraint cloneXone = new XoneConstraint();
			clone.setXone(cloneXone);
			for (Shape s : xone.getShapes()) {
				cloneXone.add(s.deepClone());
			}
		}
		if (not != null) {
			NotConstraint cloneNot = new NotConstraint();
			clone.setNot(cloneNot);
			cloneNot.setShape(not.getShape().deepClone());
		}
		return clone;
	}

	public List<PropertyConstraint> getProperty() {
		return property==null ? EMPTY_PROPERTY_LIST : property;
	}
	
	
	public void setProperty(List<PropertyConstraint> list) {
		property = list;
	}
	
	
	public AndConstraint getAnd() {
		return and;
	}

	public void setAnd(AndConstraint and) {
		this.and = and;
		if (and != null) {
			and.setDeclaringShape(this);
		}
	}
	
	

	@RdfProperty(SH.OR)
	public OrConstraint getOr() {
		return or;
	}

	public void setOr(OrConstraint or) {
		this.or = or;
		if (or != null) {
			or.setDeclaringShape(this);
		}
	}
	
	public PropertyConstraint getTimeParam() {
		if (property != null) {
			for (PropertyConstraint c : property) {
				if (c.isTimeParam()) {
					return c;
				}
			}
		}
		return null;
	}
	
	public PropertyConstraint getDimensionConstraint(URI dimension) {
		if (property != null) {
			for (PropertyConstraint c : property) {
				URI d = c.getDimensionTerm();
				if (dimension.equals(d)) {
					return c;
				}
			}
		}
		return null;
	}
	
	public PropertyConstraint getPropertyConstraint(URI predicate) {
		
		if (property!=null) {
			for (PropertyConstraint c : property) {
				if (predicate.equals(c.getPredicate())) {
					return c;
				}
			}
		}
		
		return null;
	}
	
	public boolean hasPropertyConstraint(URI predicate) {
		
		if (getPropertyConstraint(predicate) != null) {
			return true;
		}
		
		if (and!=null && and.hasPropertyConstraint(predicate)) {
			return true;
		}
		
		if (or!=null && or.hasPropertyConstraint(predicate)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Get the PropertyConstraint with the specified predicate.
	 * @param predicate The predicate of the requested PropertyConstraint
	 * @return The PropertyConstraint with the specified predicate, or null if this Shape does not contain such a property constraint.
	 */
	public PropertyConstraint property(URI predicate) {
		if (property != null) {
			for (PropertyConstraint p : property) {
				if (p.getPredicate().equals(predicate)) {
					return p;
				}
			}
		}
		return null;
	}
	
	@RdfProperty(SH.PROPERTY)
	public Shape add(PropertyConstraint c) {
		if (property == null) {
			property  = new ArrayList<PropertyConstraint>();
		}
		URI id = c.getPredicate();
		for (PropertyConstraint p : property) {
			URI predicate = p.getPredicate();
			if (predicate!=null && predicate.equals(id)) {
				return this;
			}
		}
		property.add(c);
		return this;
	}
	
	public Shape updatePropertyConstraint(PropertyConstraint c) {
		if (property == null) {
			return this;
		}
		URI id = c.getPredicate();
		for (int i=0;i<property.size();i++) {
			PropertyConstraint p=property.get(i);
			URI predicate = p.getPredicate();
			if (predicate!=null && predicate.equals(id)) {
				p=c;
				property.set(i, p);
			}
		}
		return this;
	}

	public URI getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(URI targetClass) {
		this.targetClass = targetClass;
	}
	
	public URI getIri() {
		return id instanceof URI ? (URI) id : null;
	}

	public Resource getId() {
		return id;
	}
	
	public void setId(Resource id) {
		this.id = id;
	}

	public String toString() {
		
		StringWriter out = new StringWriter();
		JsonFactory factory = new JsonFactory();
		
		try {
			JsonGenerator json = factory.createGenerator(out);
			json.useDefaultPrettyPrinter();
			Set<Shape> memory = new HashSet<>();
			toJson(memory, json);
			json.flush();
			
		} catch (Throwable e) {
			return "ERROR: " + e.getMessage();
		}
		
		return out.toString();
	}
	
	public void toJson(Set<Shape> memory, JsonGenerator json) throws IOException {
		json.writeStartObject();
		json.writeStringField("id", id.toString());
		if (targetClass != null) {
			json.writeStringField("targetClass", targetClass.stringValue());
		}
		
		if (nodeKind!=null) {
			json.writeStringField("nodeKind", nodeKind.getURI().getLocalName());
		}
		
		if (!memory.contains(this)) {
			memory.add(this);
			if (property!=null && !property.isEmpty()) {
				json.writeFieldName("property");
				json.writeStartArray();
				for (PropertyConstraint p : property) {
					p.toJson(memory, json);
				}
				json.writeEndArray();
			}
			
		}
		json.writeEndObject();
		
		
	}
	
	

	/**
	 * Get the default JSON-LD context for this shape.
	 * @return The default JSON-LD context for this shape.
	 */
	public Context getJsonldContext() {
		return jsonldContext;
	}

	/**
	 * Set the default JSON-LD context for this shape
	 * @param jsonldContext The default JSON-LD context for this shape
	 */
	public void setJsonldContext(Context jsonldContext) {
		this.jsonldContext = jsonldContext;
	}

	public URI getEquivalentRelationalShape() {
		return equivalentRelationalShape;
	}

	public void setEquivalentRelationalShape(URI equivalentRelationalShape) {
		this.equivalentRelationalShape = equivalentRelationalShape;
	}

	public NodeKind getNodeKind() {
		return nodeKind;
	}

	public void setNodeKind(NodeKind nodeKind) {
		this.nodeKind = nodeKind;
	}

	public URI getAggregationOf() {
		return aggregationOf;
	}

	public void setAggregationOf(URI aggregationOf) {
		this.aggregationOf = aggregationOf;
	}

	public URI getRollUpBy() {
		return rollUpBy;
	}

	public void setRollUpBy(URI rollUpBy) {
		this.rollUpBy = rollUpBy;
	}

	public Activity getWasGeneratedBy() {
		return wasGeneratedBy;
	}

	public void setWasGeneratedBy(Activity wasGeneratedBy) {
		this.wasGeneratedBy = wasGeneratedBy;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@RdfProperty("http://www.konig.io/ns/core/bigQueryTableId")
	public String getBigQueryTableId() {
		return bigQueryTableId;
	}

	@RdfProperty("http://www.konig.io/ns/core/bigQueryTableId")
	public void setBigQueryTableId(String bigQueryTableId) {
		this.bigQueryTableId = bigQueryTableId;
	}

	public void addType(URI type) {
		if (this.type == null) {
			this.type = new LinkedHashSet<>();
		}
		if (!this.type.contains(type)) {
			this.type.add(type);
		}
		
	}
	

	@RdfProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
	public Set<URI> getType() {
		return type==null ? Collections.emptySet() : type;
	}

	public void setType(Set<URI> type) {
		this.type = type;
	}

	@RdfProperty(Konig.NAMESPACE + "iriTemplate")
	public IriTemplate getIriTemplate() {
		return iriTemplate;
	}

	public void setIriTemplate(IriTemplate iriTemplate) {
		this.iriTemplate = iriTemplate;
	}

	@RdfProperty(Konig.SHAPE_DATA_SOURCE)
	public List<DataSource> getShapeDataSource() {
		return shapeDataSource==null ? Collections.emptyList() : shapeDataSource;
	}
	
	public void addShapeDataSource(DataSource dataSource) {
		if (shapeDataSource == null) {
			shapeDataSource = new ArrayList<>();
		}
		shapeDataSource.add(dataSource);
	}

	@RdfProperty(Konig.SHAPE_DATA_SOURCE)
	public void setShapeDataSource(List<DataSource> shapeDataSource) {
		this.shapeDataSource = shapeDataSource;
	}
	
	public boolean hasDataSourceType(URI type) {
		if (shapeDataSource != null) {
			for (DataSource s : shapeDataSource) {
				if (s.isA(type)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public String bigQueryTableId() {

		String result = null;
		if (shapeDataSource != null) {
			for (DataSource s : shapeDataSource) {
				if (s.isA(Konig.GoogleBigQueryTable)) {
					result = s.getIdentifier();
					if (s.getId() instanceof URI) {
						result = ((URI)s.getId()).getLocalName();
					}
					break;
				}
			}
		}
		return result;
	}

	public URI getIdFormat() {
		return idFormat;
	}

	public void setIdFormat(URI idFormat) {
		this.idFormat = idFormat;
	}

	public Expression getUpdateWhen() {
		return updateWhen;
	}

	public void setUpdateWhen(Expression updateWhen) {
		this.updateWhen = updateWhen;
	}
	
	public PropertyConstraint getDerivedPropertyByPredicate(URI predicate) {
		if (derivedProperty!=null) {
			for (PropertyConstraint p : derivedProperty) {
				if (predicate.equals(p.getPredicate())) {
					return p;
				}
			}
		}
		return null;
	}

	public List<PropertyConstraint> getDerivedProperty() {
		return derivedProperty==null ? Collections.emptyList() : derivedProperty;
	}

	public void setDerivedProperty(List<PropertyConstraint> derivedProperty) {
		this.derivedProperty = derivedProperty;
	}

	public void addDerivedProperty(PropertyConstraint p) {
		if (derivedProperty == null) {
			derivedProperty = new ArrayList<>();
		}
		derivedProperty.add(p);
	}
	

	public void addVariable(PropertyConstraint p) {
		if (variable == null) {
			variable = new ArrayList<>();
		}
		variable.add(p);
	}
	
	public PropertyConstraint getVariableByName(String name) {
		if (variable != null) {
			for (PropertyConstraint p : variable) {
				URI predicate = p.getPredicate();
				if (predicate!=null && name.equals(predicate.getLocalName())) {
					return p;
				}
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<PropertyConstraint> getVariable() {
		return variable==null ? Collections.EMPTY_LIST : variable;
	}

	public void setVariable(List<PropertyConstraint> variable) {
		this.variable = variable;
	}

	/**
	 * A integer used to define the position of this shape within some ordering.
	 */
	public int getOrdinal() {
		return ordinal;
	}
	
	/**
	 * Set integer used to define the position of this shape within some ordering.
	 */
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	public URI getPreferredJsonldContext() {
		return preferredJsonldContext;
	}

	public void setPreferredJsonldContext(URI preferredJsonldContext) {
		this.preferredJsonldContext = preferredJsonldContext;
	}

	public URI getPreferredJsonSchema() {
		return preferredJsonSchema;
	}

	public void setPreferredJsonSchema(URI preferredJsonSchema) {
		this.preferredJsonSchema = preferredJsonSchema;
	}

	public List<Expression> getConstraint() {
		return constraint;
	}
	
	public void addConstraint(Expression expr) {
		if (constraint == null) {
			constraint = new ArrayList<>();
		}
		constraint.add(expr);
	}

	public void setConstraint(List<Expression> constraint) {
		this.constraint = constraint;
	}
	
	public void addDefaultShapeFor(URI app) {
		if (defaultShapeFor == null) {
			defaultShapeFor = new ArrayList<>();
		}
		defaultShapeFor.add(app);
	}

	public List<URI> getDefaultShapeFor() {
		return defaultShapeFor;
	}

	public void setDefaultShapeFor(List<URI> defaultShapeFor) {
		this.defaultShapeFor = defaultShapeFor;
	}

	public String getMediaTypeBaseName() {
		return mediaTypeBaseName;
	}

	public void setMediaTypeBaseName(String mediaTypeBaseName) {
		this.mediaTypeBaseName = mediaTypeBaseName;
	}
	

	@RdfProperty(Konig.IRI_FORMULA)
	public QuantifiedExpression getIriFormula() {
		return iriFormula;
	}

	public void setIriFormula(QuantifiedExpression iriFormula) {
		this.iriFormula = iriFormula;
	}
	
	public String summaryText() {
		StringWriter buffer = new StringWriter();
		PrettyPrintWriter out = new PrettyPrintWriter(buffer);
		out.print("SHAPE: <");
		if (id != null) {
			out.print(id.stringValue());
		}
		out.println(">");
		printSummaryProperties(this, out);
		return buffer.toString();
	}

	private void printSummaryProperties(Shape shape, PrettyPrintWriter out) {
		out.pushIndent();
		for (PropertyConstraint p : shape.getProperty()) {
			PropertyPath path = p.getPath();
			if (path instanceof PredicatePath) {
				PredicatePath pp = (PredicatePath) path;
				URI predicate = pp.getPredicate();
				out.indent();
				out.println(predicate.getLocalName());
				Shape nested = p.getShape();
				if (nested != null) {
					printSummaryProperties(nested, out);
				}
			}
		}
		
		out.popIndent();
		
	}
	
	@SuppressWarnings("unchecked")
	public <T> T findDataSource(Class<T> type) {
		if (shapeDataSource != null) {
			for (DataSource ds : shapeDataSource) {
				if (type.isAssignableFrom(ds.getClass())) {
					return (T) ds;
				}
			}
		}
		return null;
	}

	public Shape getTabularOriginShape() {
		return tabularOriginShape;
	}

	public void setTabularOriginShape(Shape tabularOriginShape) {
		this.tabularOriginShape = tabularOriginShape;
		addType(Konig.TabularNodeShape);
	}

	public URI getUsesAbbreviationScheme() {
		return usesAbbreviationScheme;
	}

	public void setUsesAbbreviationScheme(URI usesAbbreviationScheme) {
		this.usesAbbreviationScheme = usesAbbreviationScheme;
	}
	 @RdfProperty(Konig.ShapeMaxRowLength)
	public List<ShapeMaxRowLength> getShapeMaxRowLengthList() {
		return shapeMaxRowLengthList;
	}

	
	public void setShapeMaxRowLengthList(List<ShapeMaxRowLength> shapeMaxRowLengthList) {
		this.shapeMaxRowLengthList = shapeMaxRowLengthList;
	}

	@RdfProperty(Konig.TERM_STATUS)
	public URI getTermStatus() {
		return termStatus;
	}

	public void setTermStatus(URI termStatus) {
		this.termStatus = termStatus;
	}

	public Set<URI> getShapeProcessing() {
		return shapeProcessing==null ? Collections.emptySet() : shapeProcessing;
	}
	
	public void addShapeProcessing(URI shapeProcessing) {
		if (shapeProcessing != null) {
			if (this.shapeProcessing == null) {
				this.shapeProcessing = new LinkedHashSet<>();
			}
			this.shapeProcessing.add(shapeProcessing);
		}
	}

	public void setShapeProcessing(Set<URI> shapeProcessing) {
		this.shapeProcessing = shapeProcessing;
	}

	@RdfProperty(SH.XONE)
	public XoneConstraint getXone() {
		return xone;
	}

	public void setXone(XoneConstraint xone) {
		this.xone = xone;
		if (xone!=null) {
			xone.setDeclaringShape(this);
		}
	}

	public NotConstraint getNot() {
		return not;
	}

	public void setNot(NotConstraint not) {
		this.not = not;
		if (not != null) {
			not.setDeclaringShape(this);
		}
	}
	
	public void addExplicitDerivedFrom(Shape sourceShape) {
		if (explicitDerivedFrom == null) {
			explicitDerivedFrom = new HashSet<>();
		}
		explicitDerivedFrom.add(sourceShape);
	}

	@RdfProperty(Konig.EXPLICIT_DERIVED_FROM)
	public Set<Shape> getExplicitDerivedFrom() {
		return explicitDerivedFrom==null ? Collections.emptySet() : explicitDerivedFrom;
	}

	public Set<URI> getBroader() {
		return broader == null ? Collections.emptySet() : broader;
	}

	@RdfProperty(SKOS.NAMESPACE + "broader")
	public void addBroader(URI skosConcept) {
		if (broader == null) {
			broader = new LinkedHashSet<>();
		}
		broader.add(skosConcept);
	}
	
	
}
