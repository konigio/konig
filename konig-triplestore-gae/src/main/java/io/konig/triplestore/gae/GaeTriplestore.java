package io.konig.triplestore.gae;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.repackaged.com.google.api.client.util.Base64;

import io.konig.triplestore.core.Triplestore;
import io.konig.triplestore.core.TriplestoreException;

public class GaeTriplestore implements Triplestore {
	
	private static Random random = new Random();
	
	private static final String INDIVIDUAL = "Individual";
	private static final String PROPERTY = "Property";
	private static final String STATEMENT = "Statement";
	private static final String SUBJECT = "subject";
	private static final String PREDICATE = "predicate";
	private static final String OBJECT = "object";
	private static final String OBJECT_TYPE = "objectType";
	
	private static final String RESOURCE_OBJECT_TYPE = "@id";
	

	@Override
	public void save(URI resourceId, Collection<Statement> outEdges) throws TriplestoreException {

		Map<String,String> bnodeMap = new HashMap<>();
		
		Key rootKey = vertexKey(null, resourceId.stringValue());
		List<Entity> entityList = new ArrayList<>();
		for (Statement s : outEdges) {
			entityList.add(toEntity(rootKey, bnodeMap, s));
		}

		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		service.put(entityList);

	}

	private Entity toEntity(Key root, Map<String, String> bnodeMap, Statement s) throws TriplestoreException {
		
		String subject = stringValue(bnodeMap, s.getSubject());
		String predicate = s.getPredicate().stringValue();
		String objectValue = stringValue(bnodeMap, s.getObject());
		String objectType = objectType(s.getObject());
		
		Key key = statementKey(root, subject, predicate, objectValue, objectType);
		Entity e = new Entity(key);
		e.setProperty(SUBJECT, subject);
		e.setProperty(PREDICATE, predicate);
		e.setProperty(OBJECT, objectValue);
		e.setProperty(OBJECT_TYPE, objectType);
		
		
		return e;
	}

	private Key statementKey(Key root, String subject, String predicate, String objectValue, String objectType) throws TriplestoreException {
		
		Key subjectKey = vertexKey(root, subject);
		Key predicateKey = KeyFactory.createKey(subjectKey, PROPERTY, predicate);
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(objectType.getBytes());
			digest.update("|".getBytes());
			digest.update(objectValue.getBytes());
			byte[] data = digest.digest();
			
			String objectId = Base64.encodeBase64String(data);
			return KeyFactory.createKey(predicateKey, STATEMENT, objectId);
			
		} catch (NoSuchAlgorithmException e) {
			throw new TriplestoreException(e);
		}
	}

	private Key vertexKey(Key root, String subject) {
		if (root != null && !root.getName().equals(subject)) {
			return KeyFactory.createKey(root, INDIVIDUAL, subject);
		}
		return KeyFactory.createKey(INDIVIDUAL, subject);
	}

	private String objectType(Value object) throws TriplestoreException {
		if (object instanceof Resource) {
			return RESOURCE_OBJECT_TYPE;
		} 
		
		Literal literal = (Literal) object;
		String language = literal.getLanguage();
		if (language!=null) {
			return language;
		}
		URI datatype = literal.getDatatype();
		if (datatype == null) {
			datatype = XMLSchema.STRING;
		}
		if (!XMLSchema.NAMESPACE.equals(datatype.getNamespace())) {
			throw new TriplestoreException("Invalid datatype.  Only datatypes from XMLSchema are permitted.");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("xsd:");
		builder.append(datatype.getLocalName());
		return builder.toString();
	}

	private String stringValue(Map<String, String> bnodeMap, Value object) {
		if (object instanceof BNode) {
			BNode bnode = (BNode) object;
			String sourceId = bnode.getID();
			String targetId = bnodeMap.get(sourceId);
			if (targetId == null) {
				byte[] array = new byte[16];
				random.nextBytes(array);
				StringBuilder builder = new StringBuilder();
				builder.append("_:");
				builder.append(Base64.encodeBase64URLSafeString(array));
				targetId = builder.toString();
				bnodeMap.put(sourceId, targetId);
			}
			return targetId;
		}
		return object.stringValue();
	}

	

	@Override
	public Collection<Statement> getOutEdges(URI resourceId) throws TriplestoreException {
		Key vertexKey = vertexKey(null, resourceId.stringValue());
		Query query = new Query(STATEMENT);
		query.setAncestor(vertexKey);
		query.addProjection(new PropertyProjection(PREDICATE, String.class));
		query.addProjection(new PropertyProjection(OBJECT, String.class));
		query.addProjection(new PropertyProjection(OBJECT_TYPE, String.class));
		
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		Collection<Statement> result = new ArrayList<>();
		Iterable<Entity> sequence = service.prepare(query).asIterable();
		for (Entity e : sequence) {
			URI predicate = new URIImpl((String) e.getProperty(PREDICATE));
			String objectValue = (String) e.getProperty(OBJECT);
			String objectType = (String) e.getProperty(OBJECT_TYPE);
			Value object = toValue(objectValue, objectType);
			result.add(new StatementImpl(resourceId, predicate, object));
		}
		
		return result;
	}

	private Value toValue(String objectValue, String objectType) {
		
		if (objectType.equals(RESOURCE_OBJECT_TYPE)) {
			return toResource(objectValue);
		}
		// Must be a Literal
		if (objectType.startsWith("xsd:")) {
			String localName = objectType.substring(4);
			URI datatype = new URIImpl(XMLSchema.NAMESPACE + localName);
			return new LiteralImpl(objectValue, datatype);
		}
		// Must be a language Literal
		return new LiteralImpl(objectValue, objectType);
	}

	private Resource toResource(String objectValue) {
		if (objectValue.startsWith("_:")) {
			String id = objectValue.substring(2);
			return new BNodeImpl(id);
		}
		return new URIImpl(objectValue);
	}

	@Override
	public Collection<Statement> getInEdges(URI resourceId) throws TriplestoreException {

		Query query = new Query(STATEMENT);
		query.addProjection(new PropertyProjection(SUBJECT, String.class));
		query.addProjection(new PropertyProjection(PREDICATE, String.class));
		
	
		Filter valueFilter = new FilterPredicate(OBJECT, FilterOperator.EQUAL, resourceId.stringValue());
		Filter valueTypeFilter = new FilterPredicate(OBJECT_TYPE, FilterOperator.EQUAL, RESOURCE_OBJECT_TYPE);
		
		Filter andFilter = CompositeFilterOperator.and(valueFilter, valueTypeFilter);
		query.setFilter(andFilter);
		
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		Collection<Statement> result = new ArrayList<>();
		Iterable<Entity> sequence = service.prepare(query).asIterable();
		for (Entity e : sequence) {
			Resource subject = toResource((String) e.getProperty(SUBJECT));
			URI predicate = new URIImpl((String) e.getProperty(PREDICATE));
			result.add(new StatementImpl(subject, predicate, resourceId));
		}
		
		return result;
	}

	@Override
	public void remove(URI resourceId) throws TriplestoreException {
		List<Key> doomed = new ArrayList<>();
		
		addOutEdges(resourceId, doomed);
		addInEdges(resourceId, doomed);
	
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		service.delete(doomed);

	}

	private void addInEdges(URI resourceId, List<Key> list) {
		Query query = new Query(STATEMENT);
		query.setKeysOnly();
		Filter valueFilter = new FilterPredicate(OBJECT, FilterOperator.EQUAL, resourceId.stringValue());
		Filter valueTypeFilter = new FilterPredicate(OBJECT_TYPE, FilterOperator.EQUAL, RESOURCE_OBJECT_TYPE);
		Filter andFilter = CompositeFilterOperator.and(valueFilter, valueTypeFilter);
		query.setFilter(andFilter);
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		Iterable<Entity> sequence = service.prepare(query).asIterable();
		for (Entity e : sequence) {
			list.add(e.getKey());
		}
	}

	private void addOutEdges(URI resourceId, List<Key> list) {
		Key vertexKey = vertexKey(null, resourceId.stringValue());
		Query query = new Query(STATEMENT);
		query.setKeysOnly();
		query.setAncestor(vertexKey);

		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		Iterable<Entity> sequence = service.prepare(query).asIterable();
		for (Entity e : sequence) {
			list.add(e.getKey());
		}
	}
}
