package io.konig.transform.factory;

import java.util.HashSet;
import java.util.Set;

import io.konig.core.KonigException;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class VariableTargetProperty extends TargetProperty {
	private Set<SourceShape> candidateSourceShape = new HashSet<>();
	private SourceShape preferredSourceShape;

	public VariableTargetProperty(PropertyConstraint propertyConstraint) {
		super(propertyConstraint);
	}

	@Override
	public SourceProperty getPreferredMatch() {
		return null;
	}

	@Override
	public void setPreferredMatch(SourceProperty preferredMatch) {
		throw new KonigException("Cannot set a SourceProperty as the preferredMatch of a VariableTargetProperty");
	}
	
	public void addCandidateSourceShape(SourceShape source) {
		candidateSourceShape.add(source);
	}

	public Set<SourceShape> getCandidateSourceShape() {
		return candidateSourceShape;
	}

	
	public boolean isDirectProperty() {
		return false;
	}
	

	public SourceShape getPreferredSourceShape() {
		return preferredSourceShape;
	}

	public void setPreferredSourceShape(SourceShape preferredSourceShape) {
		this.preferredSourceShape = preferredSourceShape;
	}

	@Override
	public int getPathIndex() {
		return -1;
	}
	
	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		super.printLocalFields(out);
		if (!candidateSourceShape.isEmpty()) {
			out.beginArray("candidateSourceShape");
			for (SourceShape source : candidateSourceShape) {
				out.beginObject(source);
				Shape shape = source.getShape();
				out.beginObjectField("shape", shape);
				out.field("id", source.getShape().getId());
				out.endObjectField(shape);
				out.endObject();
			}
			out.endArray("candidateSourceShape");
		}
		
		if (preferredSourceShape != null) {
			out.field("preferredSourceShape", preferredSourceShape);
		}
	}

	@Override
	public int totalPropertyCount() {
		return 1;
	}

	@Override
	public int mappedPropertyCount() {
		return preferredSourceShape==null ? 0 : 1;
	}

}
