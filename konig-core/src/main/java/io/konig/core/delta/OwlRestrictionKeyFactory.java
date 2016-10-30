package io.konig.core.delta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Vertex;
import io.konig.core.vocab.CS;

public class OwlRestrictionKeyFactory implements BNodeKeyFactory {

	@Override
	public BNodeKey createKey(URI predicate, Vertex object) {
		if (RDFS.SUBCLASSOF.equals(predicate)) {
			URI onProperty = object.getURI(OWL.ONPROPERTY);
			if (onProperty != null) {
				URI constraint = getConstraint(object);
				if (constraint != null) {
					StringBuilder builder = new StringBuilder();
					builder.append(onProperty.stringValue());
					builder.append('!');
					builder.append(constraint.stringValue());
					
					String text = builder.toString();
					String hash = ShaBNodeHasher.SHA1(text);
					Map<String, URI> map = new HashMap<>();
					map.put(OWL.ONPROPERTY.stringValue(), CS.KeyValue);
					map.put(constraint.stringValue(), CS.KeyTerm);
					
					return new BNodeKey(hash, map, this);
				}
			}
		}
		return null;
	}

	private URI getConstraint(Vertex object) {
		
		return 
			(object.getValue(OWL.MINCARDINALITY) != null) ? OWL.MINCARDINALITY :
			(object.getValue(OWL.MAXCARDINALITY) != null) ? OWL.MAXCARDINALITY :
			(object.getValue(OWL.CARDINALITY) != null)    ? OWL.CARDINALITY :
			(object.getValue(OWL.HASVALUE) != null)       ? OWL.HASVALUE :
			(object.getValue(OWL.ALLVALUESFROM) != null)  ? OWL.ALLVALUESFROM :
			(object.getValue(OWL.SOMEVALUESFROM) != null) ? OWL.SOMEVALUESFROM :
			null;
	}

	@Override
	public boolean isKeyPart(URI predicate) {
		
		return false;
	}

}
