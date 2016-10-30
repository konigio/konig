package io.konig.core.delta;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Vertex;

public class SimpleKeyFactory implements BNodeKeyFactory {

	private URI accessor;
	private URI keyPredicate;
	private Set<String> part = new HashSet<>();

	

	public SimpleKeyFactory(URI accessor, URI keyPredicate) {
		this.accessor = accessor;
		this.keyPredicate = keyPredicate;
		part.add(keyPredicate.stringValue());
	}

	@Override
	public BNodeKey createKey(URI predicate, Vertex object) {
		
		if (this.accessor.equals(predicate)) {
			Value value = object.getValue(keyPredicate);
			StringBuilder builder = new StringBuilder();
			builder.append(predicate.stringValue());
			builder.append('!');
			if (value instanceof BNode) {
				builder.append("_:");
			}
			builder.append(value.stringValue());
			if (value instanceof Literal) {
				Literal literal = (Literal) value;
				String lang = literal.getLanguage();
				if (lang != null) {
					builder.append('@');
					builder.append(lang);
				}
				// If the literal has a declared datatype should we include it in the key?
				
			}
			
		}
		
		return null;
	}

	@Override
	public boolean isKeyPart(URI predicate) {
		// TODO Auto-generated method stub
		return false;
	}

}
