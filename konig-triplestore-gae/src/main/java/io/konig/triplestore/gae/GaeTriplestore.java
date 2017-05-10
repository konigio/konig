package io.konig.triplestore.gae;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import io.konig.triplestore.core.Triplestore;
import io.konig.triplestore.core.TriplestoreException;

public class GaeTriplestore implements Triplestore {
	
	private static final String VERTEX = "Vertex";
	private static final String EDGE = "Edge";
	
	private static final String VALUE_KIND = "valueKind";
	private static final String IRI = "IRI";
	private static final String BNODE = "BNode";
	private static final String LITERAL = "Literal";
	private static final String SUBJECT = "subject";
	private static final String PREDICATE = "predicate";
	private static final String STRING_VALUE = "stringValue";
	private static final String STATEMENTS = "statements";
	private static final String DATATYPE = "datatype";
	private static final String LANG = "lang";
	private static final String LINKS = "links";
	
	

	@Override
	public void save(URI resourceId, Collection<Statement> outEdges) throws TriplestoreException {
		
		Entity vertex = vertex(resourceId);
		List<EmbeddedEntity> statements = new ArrayList<>();
		for (Statement s : outEdges) {
			EmbeddedEntity edge = edge(resourceId, s);
			statements.add(edge);
		}
		vertex.setProperty(STATEMENTS, statements);
		setLinks(vertex, outEdges);
		
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		service.put(vertex);
		
	}

	/**
	 * Creates a synthetic index used during search over inward edges.
	 */
	private void setLinks(Entity vertex, Collection<Statement> outEdges) {
		Set<String> set = new HashSet<>();
		for (Statement s : outEdges) {
			Value object = s.getObject();
			if (object instanceof URI) {
				set.add(object.stringValue());
			}
		}
		
		if (!set.isEmpty()) {
			vertex.setProperty(LINKS, new ArrayList<>(set));
		}
		
		
	}

	private EmbeddedEntity edge(URI vertexId, Statement s) {
		
		EmbeddedEntity e = new EmbeddedEntity();
		
		Resource subject = s.getSubject();
		if (!vertexId.equals(subject)) {
			e.setProperty(SUBJECT, stringValue(subject));
		}
		e.setProperty(PREDICATE, s.getPredicate().stringValue());
		
		Value object = s.getObject();
		if (object instanceof URI) {
			e.setProperty(VALUE_KIND, IRI);
			e.setProperty(STRING_VALUE, object.stringValue());
		} else if (object instanceof BNode) {
			e.setProperty(VALUE_KIND, BNODE);
			e.setProperty(STRING_VALUE, object.stringValue());
		} else if (object instanceof Literal) {
			setLiteralValue(e, (Literal)object);
		}
		
		
		return e;
	}

	private String stringValue(Resource resource) {
		if (resource instanceof BNode) {
			BNode bnode = (BNode) resource;
			return "_:" + bnode.getID();
		}
		return resource.stringValue();
	}

	private void setLiteralValue(EmbeddedEntity e, Literal object) {
		e.setProperty(VALUE_KIND, LITERAL);
		e.setProperty(STRING_VALUE, object.getLabel());
		
		String language = object.getLanguage();
		if (language != null) {
			e.setProperty(LANG, language);
		} else {
			URI datatype = object.getDatatype();
			if (datatype == null) {
				datatype = XMLSchema.STRING;
			}
			e.setProperty(DATATYPE, datatype.getLocalName());
		}
		
		
	}

	

	private Entity vertex(URI resourceId) {
		Key key = vertexKey(resourceId);
		return new Entity(key);
	}

	private Key vertexKey(URI resourceId) {
		return KeyFactory.createKey(VERTEX, resourceId.stringValue());
	}

	@Override
	public Collection<Statement> getOutEdges(URI resourceId) throws TriplestoreException {
		
		Collection<Statement> result = new ArrayList<>();
		
		Key key = vertexKey(resourceId);
		
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		try {
			Entity e = service.get(key);
			@SuppressWarnings("unchecked")
			List<EmbeddedEntity> statementList = (List<EmbeddedEntity>) e.getProperty(STATEMENTS);
			for (EmbeddedEntity edge : statementList) {
				result.add(toStatement(resourceId, edge));
			}
			
		} catch (EntityNotFoundException e) {
			// Silently ignore
		}
		
		return result;
	}

	private Statement toStatement(URI resourceId, EmbeddedEntity e) throws TriplestoreException {
		
		String subjectValue = (String) e.getProperty(SUBJECT);
		String predicateValue = (String) e.getProperty(PREDICATE);
		
		Resource subject = (subjectValue==null) ? resourceId : resource(subjectValue);
		URI predicate = new URIImpl(predicateValue);
		Value object = toValue(e);
		
		return new StatementImpl(subject, predicate, object);
	}

	private Value toValue(EmbeddedEntity e) throws TriplestoreException {
		String valueKind = (String) e.getProperty(VALUE_KIND);
		String stringValue = (String) e.getProperty(STRING_VALUE);
		
		switch (valueKind) {
		case IRI : return new URIImpl(stringValue);
		case BNODE : return new BNodeImpl(stringValue);
		case LITERAL : 
			String language = (String) e.getProperty(LANG);
			if (language == null) {
				URI datatype = getDatatype(e);
				return new LiteralImpl(stringValue, datatype);
			} else {
				return new LiteralImpl(stringValue, language);
			}
			
		}
		throw new TriplestoreException("Invalid value");
	}

	private URI getDatatype(EmbeddedEntity e) throws TriplestoreException {
		
		String localName = (String) e.getProperty(DATATYPE);
		if (localName == null) {
			throw new TriplestoreException("Datatype not defined");
		}
		String stringValue = XMLSchema.NAMESPACE + localName;
		return new URIImpl(stringValue);
	}

	private Resource resource(String stringValue) {
		if (stringValue.startsWith("_:")) {
			return new BNodeImpl(stringValue.substring(2));
		}
		return new URIImpl(stringValue);
	}

	@Override
	public Collection<Statement> getInEdges(URI resourceId) throws TriplestoreException {
		ArrayList<Statement> inEdges = new ArrayList<>();
		Filter filter = new FilterPredicate(LINKS, FilterOperator.EQUAL, resourceId.stringValue());
		Query q = new Query(VERTEX).setFilter(filter);
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		Iterable<Entity> results = service.prepare(q).asIterable();
		for (Entity e : results) {
			URI subjectId = new URIImpl(e.getKey().getName());
			
			@SuppressWarnings("unchecked")
			List<EmbeddedEntity> statementList = (List<EmbeddedEntity>)e.getProperty(STATEMENTS);
			if (statementList != null) {
				for (EmbeddedEntity s : statementList) {
					Statement statement = toStatement(subjectId, s);
					if (resourceId.equals(statement.getObject())) {
						inEdges.add(statement);
					}
				}
			}
		}
		return inEdges;
	}

	

	@Override
	public void remove(URI resourceId) throws TriplestoreException {
		throw new TriplestoreException("Not implemented");

	}

}
