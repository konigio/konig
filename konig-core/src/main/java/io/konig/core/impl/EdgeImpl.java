package io.konig.core.impl;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;

import io.konig.core.Edge;

public class EdgeImpl extends StatementImpl implements Edge  {
	private static final long serialVersionUID = 1L;
	public EdgeImpl(Resource subject, URI predicate, Value object) {
		super(subject, predicate, object);
	}


}
