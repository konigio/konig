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
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.annotation.RdfProperty;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Path;
import io.konig.core.Term;
import io.konig.core.UidGenerator;
import io.konig.core.impl.KonigLiteral;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.formula.QuantifiedExpression;

public class PropertyConstraint {

	private Resource id;
	private PropertyPath path;
	private List<Value> in;
	private Integer minCount;
	private Integer maxCount;
	private Integer minLength;
	private Integer maxLength;
	private Double minExclusive;
	private Double maxExclusive;
	private Double minInclusive;
	private Double maxInclusive;
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
	private Path equivalentPath;
	private String fromAggregationSource;
	private Path sourcePath;
	private Path partitionOf;
	private QuantifiedExpression formula;
	private URI idFormat;
	
	private Term term;
	
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
	
	public void save(Graph graph) {
		if (id == null) {
			id = graph.vertex().getId();
		}
		
		if (path instanceof PredicatePath) {
			edge(graph, SH.path, getPredicate());
		} else {
			if (path == null) {
				throw new KonigException("Path must not be null");
			}
			throw new KonigException("Path type not supported: " + path.getClass().getSimpleName());
		}
		
		if (in != null) {
			graph.edge(id, SH.in, in);
		}
		edge(graph, SH.minCount, minCount);
		edge(graph, SH.maxCount, maxCount);
		edge(graph, SH.minLength, minLength);
		edge(graph, SH.minExclusive, minExclusive);
		edge(graph, SH.maxExclusive, maxExclusive);
		edge(graph, SH.minInclusive, minInclusive);
		edge(graph, SH.maxInclusive, maxInclusive);
		edge(graph, SH.datatype, datatype);
		edge(graph, SH.shape, getShapeId());
		edge(graph, SH.nodeKind, nodeKind==null ? null : nodeKind.getURI());
		
		if (hasValue != null) {
			for (Value value : hasValue) {
				graph.edge(id, SH.hasValue, value);
			}
		}
		
		edge(graph, SH.nodeKind, literal(pattern));
		edge(graph, SH.valueClass, valueClass);
		edge(graph, RDFS.COMMENT, literal(documentation));
		edge(graph, Konig.stereotype, stereotype);
		edge(graph, Konig.equivalentPath, literal(equivalentPath));
		edge(graph, Konig.fromAggregationSource, literal(fromAggregationSource));
		edge(graph, Konig.sourcePath, literal(sourcePath));
		edge(graph, Konig.partitionOf, literal(partitionOf));
		
	}
	
	private Literal literal(String value) {
		return value == null ? null : new LiteralImpl(value);
	}
	
	private Literal literal(Object value) {
		return value==null ? null : new LiteralImpl(value.toString());
	}
	
	private void edge(Graph graph, URI predicate, Double value) {
		if (value != null) {
			Value object = ValueFactoryImpl.getInstance().createLiteral(value);
			graph.edge(id, predicate, object);
		}
	}

	private void edge(Graph graph, URI predicate, Integer value) {
		if (value != null) {
			Value object = ValueFactoryImpl.getInstance().createLiteral(value);
			graph.edge(id, predicate, object);
		}
		
	}

	private void edge(Graph graph, URI predicate, Value value) {
		if (value != null) {
			graph.edge(id, predicate, value);
		}
	}

	public void setId(Resource id) {
		this.id = id;
	}

	public PropertyConstraint clone() {
		PropertyConstraint other = new PropertyConstraint(id, path);
		other.in = in;
		other.datatype = datatype;
		other.directType = directType;
		other.documentation = documentation;
		other.hasValue = hasValue;
		other.knownValue = knownValue;
		other.maxCount = maxCount;
		other.maxExclusive = maxExclusive;
		other.maxInclusive = maxInclusive;
		other.maxLength = maxLength;
		other.minCount = minCount;
		other.minExclusive = minExclusive;
		other.minInclusive = minInclusive;
		other.minLength = minLength;
		other.nodeKind = nodeKind;
		other.pattern = pattern;
		other.term = term;
		other.valueClass = valueClass;
		other.shape = shape;
		other.equivalentPath = equivalentPath;
		other.stereotype = stereotype;
		
		return other;
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
	public Double getMinExclusive() {
		return minExclusive;
	}
	public void setMinExclusive(Double minExclusive) {
		this.minExclusive = minExclusive;
	}
	public Double getMaxExclusive() {
		return maxExclusive;
	}
	public void setMaxExclusive(Double maxExclusive) {
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
	public Double getMinInclusive() {
		return minInclusive;
	}
	public void setMinInclusive(Double minInclusive) {
		this.minInclusive = minInclusive;
	}
	public Double getMaxInclusive() {
		return maxInclusive;
	}
	public void setMaxInclusive(Double maxInclusive) {
		this.maxInclusive = maxInclusive;
	}
	
	@RdfProperty("http://www.w3.org/ns/shacl#predicate")
	public URI getPredicate() {
		return path instanceof PredicatePath ? ((PredicatePath)path).getPredicate() : null;
	}
	
	public void setPredicate(URI predicate) {
		path = new PredicatePath(predicate);
	}
	
//	public void setPath(PropertyPath path) {
//		this.path = path;
//	}
	
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
	
	public void toJson(Set<Shape> memory, JsonGenerator json) throws IOException {
		json.writeStartObject();
		if (path instanceof PredicatePath) {
			json.writeStringField("predicate", getPredicate().toString());
		} else {
			throw new KonigException("Unsupported path type: " + path==null ? "null" : path.getClass().getSimpleName());
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
			json.writeNumberField("minInclusive", minInclusive);
		}
		if (maxInclusive != null) {
			json.writeNumberField("maxInclusive", maxInclusive);
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
		return equivalentPath;
	}

	public void setEquivalentPath(Path equivalentPath) {
		this.equivalentPath = equivalentPath;
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

	public Path getPartitionOf() {
		return partitionOf;
	}

	public void setPartitionOf(Path partitionOf) {
		this.partitionOf = partitionOf;
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
	
}
