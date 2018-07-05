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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.annotation.RdfProperty;
import io.konig.core.Path;
import io.konig.core.Term;
import io.konig.core.UidGenerator;
import io.konig.core.impl.KonigLiteral;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.formula.Formula2PathTranslator;
import io.konig.formula.Path2FormulaTranslator;
import io.konig.formula.QuantifiedExpression;
import io.konig.schema.Person;

public class PropertyConstraint implements Cloneable {

	private Resource id;
	private PropertyPath path;
	private List<Value> in;
	private Integer minCount;
	private Integer maxCount;
	private Integer minLength;
	private Integer maxLength;
	private Integer decimalPrecision;
	private Integer decimalScale;
	private Number minExclusive;
	private Number maxExclusive;
	private Number minInclusive;
	private Number maxInclusive;
	private Boolean uniqueLang;
	private URI datatype;
	private URI directType;
	private Shape shape;
	private NodeKind nodeKind;
	private Set<Value> hasValue;
	private String pattern;
	private Resource valueClass;
	private String documentation;
	private List<Value> knownValue;
	private boolean uniqueCountKey;
	private URI dimensionTerm;
	private boolean timeParam;
	private URI stereotype;
	private String fromAggregationSource;
	private Path sourcePath;
	private Path partitionOf;
	private QuantifiedExpression formula;
	private URI idFormat;
	private List<URI> qualifiedSecurityClassification ;
	private Person dataSteward;
	private String name;
	
	private Term term;
	private URI termStatus;
	
	public PropertyConstraint(URI predicate) {
		this.id = new BNodeImpl(UidGenerator.INSTANCE.next());
		this.path = new PredicatePath(predicate);
	}
	
	public PropertyConstraint(PropertyPath path) {
		this.id = new BNodeImpl(UidGenerator.INSTANCE.next());
		this.path = path;
	}

	
	public PropertyConstraint(Resource id, PropertyPath path) {
		this.id = id;
		this.path = path;
	}
	
	public PropertyConstraint(Resource id, URI predicate) {
		this.id = id;
		this.path = new PredicatePath(predicate);
	}
	
	public PropertyConstraint() {
		
	}
	
	
	@RdfProperty(Schema.NAME)
	public String getName() {
		return name;
	}

	@RdfProperty(Schema.NAME)
	public void setName(String name) {
		this.name = name;
	}

	public void setId(Resource id) {
		this.id = id;
	}

	public PropertyConstraint clone() {
		try {
			return (PropertyConstraint) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public PropertyConstraint deepClone() {
		PropertyConstraint clone = clone();
		if (shape != null) {
			clone.setShape(shape.deepClone());
		}
		return clone;
	}
	
	
	public Resource getId() {
		return id;
	}
	
	public String getComment() {
		return documentation;
	}
	
	public void setComment(String comment) {
		this.documentation = comment;
	}
	public Integer getMinLength() {
		return minLength;
	}
	public void setMinLength(Integer minLength) {
		this.minLength = minLength;
	}
	public Integer getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}
	public Number getMinExclusive() {
		return minExclusive;
	}
	public void setMinExclusive(Number minExclusive) {
		this.minExclusive = minExclusive;
	}
	public Number getMaxExclusive() {
		return maxExclusive;
	}
	public void setMaxExclusive(Number maxExclusive) {
		this.maxExclusive = maxExclusive;
	}
	/**
	 * Get the default JSON-LD term for the predicate
	 * @return The default JSON-LD term for the predicate
	 */
	public Term getTerm() {
		return term;
	}

	/**
	 * Set the default JSON-LD term for the predicate
	 * @param term The default JSON-LD term for the predicate
	 */
	public void setTerm(Term term) {
		this.term = term;
	}
	public Number getMinInclusive() {
		return minInclusive;
	}
	public void setMinInclusive(Number minInclusive) {
		this.minInclusive = minInclusive;
	}
	public Number getMaxInclusive() {
		return maxInclusive;
	}
	public void setMaxInclusive(Number maxInclusive) {
		this.maxInclusive = maxInclusive;
	}
	
	@RdfProperty("http://www.w3.org/ns/shacl#predicate")
	public URI getPredicate() {
		return path instanceof PredicatePath ? ((PredicatePath)path).getPredicate() : null;
	}
	
	public void setPredicate(URI predicate) {
		path = new PredicatePath(predicate);
	}

	public void setPath(PropertyPath path) {
		this.path = path;
	}
	
	public void setPath(URI predicate) {
		path = new PredicatePath(predicate);
	}

	public NodeKind getNodeKind() {
		return nodeKind;
	}
	public void setNodeKind(NodeKind nodeKind) {
		this.nodeKind = nodeKind;
	}
	
	public void addIn(Value value) {
		if (in == null) {
			in = new ArrayList<Value>();
		}
		in.add(value);
	}
	
	public void setIn(List<Value> list) {
		in = list;
	}
	
	public void addKnownValue(Value value) {
		if (knownValue == null) {
			knownValue = new ArrayList<>();
		}
		knownValue.add(value);
	}
	
	public List<Value> getKnownValue() {
		return knownValue;
	}
	
	public void addHasValue(Value value) {
		if (hasValue == null) {
			hasValue = new HashSet<>();
		}
		
		// Coerce String literals into untyped literal.
		// This ensures that we don't have multiple values of the same literal.
		if (value instanceof Literal) {
			Literal literal = (Literal) value;
			URI datatype = literal.getDatatype();
			if (datatype != null && XMLSchema.STRING.equals(datatype)) {
				value = new KonigLiteral(literal.stringValue());
			}
		}
		
		
		hasValue.add(value);
	}
	
	public Set<Value> getHasValue() {
		return hasValue;
	}
	
	public void setHasValue(Set<Value> set) {
		this.hasValue = set;
	}
	
	/**
	 * Get read-only list of allowed-values for this property constraint.
	 * @return
	 */
	public List<Value> getIn() {
		return in;
	}

	public Integer getMinCount() {
		return minCount;
	}

	public void setMinCount(Integer minCount) {
		this.minCount = minCount;
	}

	public Integer getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(Integer maxCount) {
		this.maxCount = maxCount;
	}

	public URI getDatatype() {
		return datatype;
	}

	public void setDatatype(URI datatype) {
		this.datatype = datatype;
	}


	public URI getDirectValueType() {
		return directType;
	}

	public void setDirectValueType(URI directType) {
		this.directType = directType;
	}

	public Resource getShapeId() {
		return shape==null ? null : shape.getId();
	}

	public Shape getShape() {
		return shape;
	}
	
	public Shape getShape(ShapeManager manager) {
		return shape;
	}

	public void setShape(Shape valueShape) {
		this.shape = valueShape;
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
			
		} catch (IOException e) {
			return "ERROR:" + e.getMessage();
		}
		
		return out.toString();
	}
	
	public Person dataSteward() {
		if (dataSteward == null) {
			dataSteward = new Person();
		}
		return dataSteward;
	}
	
	public void toJson(Set<Shape> memory, JsonGenerator json) throws IOException {
		json.writeStartObject();
		if (path instanceof PredicatePath) {
			json.writeStringField("predicate", getPredicate().toString());
		} else if (path != null) {
			json.writeStringField("path", path.toString());
		}
		if (minCount!=null) {
			json.writeNumberField("minCount", minCount);
		}
		if (maxCount!=null) {
			json.writeNumberField("maxCount", maxCount);
		}
		if (datatype != null) {
			json.writeStringField("datatype", datatype.stringValue());
		}
		if (directType != null) {
			json.writeStringField("directType", directType.stringValue());
		}
		if (minInclusive != null) {
			json.writeNumberField("minInclusive", minInclusive.doubleValue());
		}
		if (maxInclusive != null) {
			json.writeNumberField("maxInclusive", maxInclusive.doubleValue());
		}
		if (nodeKind != null) {
			json.writeStringField("nodeKind", "sh:" + nodeKind.getURI().getLocalName());
		}
		if (valueClass != null) {
			json.writeStringField("class", valueClass.stringValue());
		}
		if (shape != null) {
			json.writeFieldName("valueShape");
			if (memory.contains(shape)) {
				json.writeString( shape.getId().toString());
			} else {
				shape.toJson(memory, json);
			}
			
		} 
		json.writeEndObject();
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	@RdfProperty("http://www.w3.org/ns/shacl#class")
	public Resource getValueClass() {
		return valueClass;
	}

	@RdfProperty("http://www.w3.org/ns/shacl#class")
	public void setValueClass(Resource valueClass) {
		this.valueClass = valueClass;
	}

	public boolean isUniqueCountKey() {
		return uniqueCountKey;
	}

	public void setUniqueCountKey(boolean uniqueCountKey) {
		this.uniqueCountKey = uniqueCountKey;
	}

	public URI getDimensionTerm() {
		return dimensionTerm;
	}

	public void setDimensionTerm(URI dimensionTerm) {
		this.dimensionTerm = dimensionTerm;
	}

	public boolean isTimeParam() {
		return timeParam;
	}

	public void setTimeParam(boolean timeParam) {
		this.timeParam = timeParam;
	}
	
	public URI getStereotype() {
		return stereotype;
	}

	public void setStereotype(URI stereotype) {
		this.stereotype = stereotype;
	}

	public void setMeasure(URI measure) {
		path = new PredicatePath(measure);
		stereotype = Konig.measure;
	}
	
	public void setDimension(URI dimension) {
		path = new PredicatePath(dimension);
		stereotype = Konig.dimension;
	}
	
	public void setAttribute(URI attribute) {
		path = new PredicatePath(attribute);
		stereotype = Konig.attribute;
	}

	
	@RdfProperty(Konig.EQUIVALENT_PATH)
	public Path getEquivalentPath() {
		return Formula2PathTranslator.getInstance().toPath(formula);
	}
	
	/**
	 * @deprecated
	 * @param path  Use {@link #setFormula(QuantifiedExpression)}
	 */
	public void setEquivalentPath(Path path) {
		formula = Path2FormulaTranslator.getInstance().toQuantifiedExpression(path);
	}
	
	


	@RdfProperty(Konig.SOURCE_PATH)
	public Path getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(Path sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getFromAggregationSource() {
		return fromAggregationSource;
	}

	public void setFromAggregationSource(String fromAggregationSource) {
		this.fromAggregationSource = fromAggregationSource;
	}
	
	public PropertyPath getPath() {
		return path;
	}


	public QuantifiedExpression getFormula() {
		return formula;
	}

	public void setFormula(QuantifiedExpression formula) {
		this.formula = formula;
	}

	public URI getIdFormat() {
		return idFormat;
	}

	public void setIdFormat(URI idFormat) {
		this.idFormat = idFormat;
	}
	
	public URI getTermStatus() {
		return termStatus;
	}

	public void setTermStatus(URI termStatus) {
		this.termStatus  = termStatus ;
	}

	public boolean isRequiredSingleValue() {
		return minCount!=null && minCount==1 && maxCount!=null && maxCount==1;
	}

	public Integer getDecimalPrecision() {
		return decimalPrecision;
	}

	public void setDecimalPrecision(Integer decimalPrecision) {
		this.decimalPrecision = decimalPrecision;
	}

	public Integer getDecimalScale() {
		return decimalScale;
	}

	public void setDecimalScale(Integer decimalScale) {
		this.decimalScale = decimalScale;
	}

	public List<URI> getQualifiedSecurityClassification() {
		return qualifiedSecurityClassification;
	}

	public void setQualifiedSecurityClassification(List<URI> qualifiedSecurityClassification) {
		this.qualifiedSecurityClassification = qualifiedSecurityClassification;
	}

	public Boolean getUniqueLang() {
		return uniqueLang;
	}

	public void setUniqueLang(Boolean uniqueLang) {
		this.uniqueLang = uniqueLang;
	}
	
	
	public Person getDataSteward() {
		return dataSteward;
	}

	public void setDataSteward(Person dataSteward) {
		this.dataSteward = dataSteward;
	}
}
