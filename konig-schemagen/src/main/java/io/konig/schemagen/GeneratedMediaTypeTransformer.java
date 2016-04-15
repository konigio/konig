package io.konig.schemagen;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.impl.KonigLiteral;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class GeneratedMediaTypeTransformer implements ShapeTransformer {
	private static final URI GENERATED_MEDIATYPE = new URIImpl("http://schema.pearson.com/ns/activity/generatedMediaType");
	
	private String suffix;

	public GeneratedMediaTypeTransformer(String suffix) {
		this.suffix = suffix;
	}

	@Override
	public PropertyConstraint transform(Shape shape, PropertyConstraint constraint) {
		
		if (GENERATED_MEDIATYPE.equals(constraint.getPredicate())) {
			Set<Value> valueSet = constraint.getHasValue();
			if (valueSet != null && !valueSet.isEmpty()) {
				PropertyConstraint clone = constraint.clone();
				clone.setHasValue(new HashSet<Value>());
				for (Value value : valueSet) {
					String text = value.stringValue();
					if (text.lastIndexOf('+') < 0) {
						StringBuilder builder = new StringBuilder(text);
						builder.append(suffix);
						value = new KonigLiteral(builder.toString());
					}
					clone.addHasValue(value);
				}
				constraint = clone;
			}
		
		}
		return constraint;
	}

}
