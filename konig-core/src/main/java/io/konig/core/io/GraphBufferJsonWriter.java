package io.konig.core.io;

import java.io.IOException;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Context;
import io.konig.core.ContextManager;
import io.konig.core.Term;

public class GraphBufferJsonWriter extends BaseGraphReader {
	private JsonGenerator json;
	private boolean isIdField;
	private String predicateType;
	
	private String idKey;
	private String typeKey;
	private String valueKey;
	private String languageKey;
	
	
	/**
	 * Write binary graph data to a JSON document. 
	 * @param data The binary graph data.
	 * @param json The JsonGenerator to use.
	 */
	public void write(byte[] data, ContextManager manager, JsonGenerator json) {
		this.json = json;
		
		read(data, manager);
		

		
	}

	@Override
	protected void startRDF() {
		try {
			json.writeStartObject();
		} catch (IOException e) {
			throw new KonigReadException(e);
		}
	}


	@Override
	protected void handleContext(Context context) {
		idKey = context.alias("@id");
		typeKey = context.alias("@type");
		valueKey = context.alias("@value");
		languageKey = context.alias("@language");
		try {
			json.writeStringField("@context", context.getContextIRI());
			json.writeArrayFieldStart("@graph");
		} catch (IOException e) {
			throw new KonigReadException(e);
		}
	}


	protected void beginSubject(Resource subject, Term qnameTerm, Term idTerm) {
		try {

			String idValue =
				(idTerm != null) ? idTerm.getKey() :
				(qnameTerm != null && subject instanceof URI) ? qnameTerm.getKey() + ":" + ((URI)subject).getLocalName() :
				(subject instanceof BNode) ? "_:" + subject.stringValue() :
				subject.stringValue();
				
			json.writeStartObject();
			json.writeStringField(idKey, idValue);
			
			
		} catch (IOException e) {
			throw new KonigReadException(e);
		}
		
		
	}

	protected void endSubject() {
		try {
			json.writeEndObject();
		} catch (IOException e) {
			throw new KonigReadException(e);
		}
		
	}
	

	
	protected void startPredicate(URI predicate, Term term, Term qnameTerm, int objectCount)  {
		
		isIdField = (term != null && term.getType().equals("@id"));
		predicateType = null;
		if (term != null) {
			predicateType = term.getType();
		}
				
		String fieldName = predicate.stringValue();
		
		if (predicate.equals(RDF.TYPE)) {
			fieldName = typeKey;
		} else if (term != null) {
			fieldName = term.getKey();
		} else if (qnameTerm != null) {
			fieldName = qnameTerm.getKey() + ":" + predicate.getLocalName();
		}
		
		try {
			json.writeFieldName(fieldName);
			if (objectCount > 1) {
				json.writeStartArray();
			}
		} catch (IOException e) {
			throw new KonigReadException(e);
		}
		
		
	}
	

	protected void endPredicate(URI predicate, int objectCount) {
		if (objectCount > 1) {
			try {
				json.writeEndArray();
			} catch (IOException e) {
				throw new KonigReadException(e);
			}
		}
	}

	protected void handleObject(Value value, Term term, Term qnameTerm) {
		
		try {

			if (value instanceof URI) {
				
				URI iriValue = (URI) value;
				
				
				String text =
					(term != null) ? term.getKey() :
					(qnameTerm != null) ? qnameTerm.getKey() + ":" + iriValue.getLocalName() :
					iriValue.stringValue();
				
				if (isIdField) {
					json.writeString(text);
				} else {
					json.writeStartObject();
					json.writeStringField(idKey, text);
					json.writeEndObject();
				}
			} else if (value instanceof BNode) {

				json.writeStartObject();
				json.writeStringField(idKey, "_:" + value.stringValue());
				json.writeEndObject();
			} else {
				// Literal
				
				Literal literal = (Literal) value;
				
				if (predicateType != null) {
					json.writeString(value.stringValue());
				} else {
					String language = literal.getLanguage();
					URI type = literal.getDatatype();
					
					json.writeStartObject();
					json.writeStringField(valueKey, literal.stringValue());
					
					if (language != null) {
						json.writeStringField(languageKey, literal.getLanguage());
					} else if (type != null) {
						// TODO: pass typeQNameTerm and typeTerm as parameters
						json.writeStringField(typeKey, type.stringValue());
					}
					
					json.writeEndObject();
					
				}
				
			}
		} catch (IOException e) {
			throw new KonigReadException(e);
		}

	}
	
	

	@Override
	protected void endRDF() {
		try {
			json.writeEndArray();
			json.writeEndObject();
		} catch (IOException e) {
			throw new KonigReadException(e);
		}
	}
	
	

}
