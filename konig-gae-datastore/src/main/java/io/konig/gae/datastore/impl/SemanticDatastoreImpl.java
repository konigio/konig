package io.konig.gae.datastore.impl;

import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.gae.datastore.DatastoreDatatype;
import io.konig.gae.datastore.DatastoreDatatypeMapper;
import io.konig.gae.datastore.EntityNamer;
import io.konig.gae.datastore.SemanticDatastore;
import io.konig.shacl.Shape;

public class SemanticDatastoreImpl implements SemanticDatastore {
	
	private EntityNamer entityNamer;
	private DatastoreDatatypeMapper datatypeMapper;
	private DateTimeFormatter timeFormat;

	public SemanticDatastoreImpl(EntityNamer entityNamer) {
		this.entityNamer = entityNamer;
		timeFormat = ISODateTimeFormat.dateTime();
	}
	

	@Override
	public void put(Vertex vertex) {

		 URI owlClass = getType(vertex);
		 String entityKind = entityNamer.entityName(owlClass);
		 if (entityKind == null) {
			 throw new KonigException("Entity type not defined for OWL Class: " + owlClass);
		 }
		 
		 Entity entity = new Entity(entityKind);
		 addProperties(vertex, entity);
		 

		 DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		 ds.put(entity);
		 
	}

	private void addProperties(Vertex vertex, Entity entity) {
		
		Set<Entry<URI,Set<Edge>>> out = vertex.outEdges();
		
		for (Entry<URI,Set<Edge>> entry : out) {
			URI predicate = entry.getKey();
			Set<Edge> edgeSet = entry.getValue();
			if (edgeSet.size() == 1) {
				addSingleValue(predicate, edgeSet.iterator().next().getObject());
			}
		}
		
	}


	private void addSingleValue(URI predicate, Value value) {
		
		Object object = datastoreValue(value);
	
		
		
	}


	private Object datastoreValue(Value value) {
		if (value instanceof Literal) {
			Literal literal = (Literal) value;
			return datastoreLiteral(literal);
		}
		return null;
	}


	private Object datastoreLiteral(Literal literal) {
		DatastoreDatatype datastoreType = datatypeMapper.getDatastoreDatatype(literal);
		if (datastoreType == null) {
			throw new KonigException("DatastoreType not support for literal: " + literal.toString());
		}
		String label = literal.getLabel();
		
		switch (datastoreType) {
		case BLOB :
			return new Blob(label.getBytes());
			
		case BOOLEAN :
			return Boolean.parseBoolean(label);
			
		case DATE :
			DateTime dt = timeFormat.parseDateTime(label);
			long millis = dt.getMillis();
			return new Date(millis);
			
		case DOUBLE :
			return Double.parseDouble(label);
			
		case LONG :
			return Long.parseLong(label);
			
		case SHORT_BLOB :
			return new ShortBlob(label.getBytes());
			
		case STRING :
			return label;
			
		case TEXT :
			return new Text(label);
			
		}
		
		return null;
	}


	private URI getType(Vertex vertex) {
		Set<Edge> set = vertex.outProperty(RDF.TYPE);
		if (set.size()==0) {
			throw new KonigException("Entity type is not defined");
		}
		if (set.size()>1) {
			// TODO:  Develop a solution to disambiguate the entity type.
			// For now, we just throw an exception.
			throw new KonigException("Entity type is ambiguous");
		}
		Value value = set.iterator().next().getObject();
		if (value instanceof URI) {
			return (URI) value;
		}
		
		return null;
	}


	private Entity toEntity(Vertex vertex, Shape shape) {
		
		
		return null;
	}

	@Override
	public Vertex getById(URI entityId, Graph graph) {
		// TODO Auto-generated method stub
		return null;
	}

}
