package io.konig.shacl;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;

import io.konig.core.UidGenerator;

public class PropertyConstraint {

	private Resource id;
	private URI predicate;
	private List<Value> allowedValues;
	private Integer minCount;
	private Integer maxCount;
	private URI datatype;
	private URI type;
	private URI directType;
	private URI valueShapeId;
	private Shape valueShape;


	public PropertyConstraint(URI predicate) {
		this.id = new BNodeImpl(UidGenerator.INSTANCE.next());
		this.predicate = predicate;
	}
	public PropertyConstraint(Resource id, URI predicate) {
		this.id = id;
		this.predicate = predicate;
	}
	
	public Resource getId() {
		return id;
	}



	public URI getPredicate() {
		return predicate;
	}



	public void addAllowedValue(Value value) {
		if (allowedValues == null) {
			allowedValues = new ArrayList<Value>();
		}
		allowedValues.add(value);
	}
	
	/**
	 * Get read-only list of allowed-values for this property constraint.
	 * @return
	 */
	public List<Value> getAllowedValues() {
		return allowedValues;
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

	public URI getType() {
		return type;
	}

	public void setType(URI type) {
		this.type = type;
	}

	public URI getDirectType() {
		return directType;
	}

	public void setDirectType(URI directType) {
		this.directType = directType;
	}

	public URI getValueShapeId() {
		return valueShapeId;
	}

	public void setValueShapeId(URI valueShape) {
		this.valueShapeId = valueShape;
	}

	public Shape getValueShape() {
		return valueShape;
	}

	public void setValueShape(Shape valueShape) {
		this.valueShape = valueShape;
	}
	
	
	
}
