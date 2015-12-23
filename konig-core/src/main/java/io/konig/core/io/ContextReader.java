package io.konig.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.Context;
import io.konig.core.Term;

public class ContextReader {

	public ContextReader() {
	}
	
	public Context read(InputStream input) throws KonigReadException {
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node;
		try {
			node = mapper.readTree(input);

			if (node instanceof ObjectNode) {
				return parse((ObjectNode)node);
			}
		} catch (IOException e) {
			throw new KonigReadException(e);
		}
		
		throw new KonigReadException("Root node in stream is not a JSON object");
		
	}

	private Context parse(ObjectNode node) throws KonigReadException {
		JsonNode id = node.get("@id");
		if (id == null) {
			throw new KonigReadException("@id of context is not defined");
		}
		String contextIRI = id.asText();
		Context context = new Context(contextIRI);
		JsonNode set = node.get("@context");
		if (set instanceof ObjectNode) {
			ObjectNode terms = (ObjectNode) set;
			Iterator<String> sequence = terms.fieldNames();
			while (sequence.hasNext()) {
				String key = sequence.next();
				JsonNode termNode = terms.get(key);
				String idValue=null;
				String language=null;
				String type=null;
				
				Term term = null;
				if (termNode.isTextual()) {
					idValue = termNode.asText();
				} else if (termNode.isObject()) {
					ObjectNode termObject = (ObjectNode) termNode;
					idValue = stringValue(termObject, "@id");
					language = stringValue(termObject, "@language");
					type = stringValue(termObject, "@type");
				}
				
				term = new Term(key, idValue, language, type);
				context.add(term);
			}
		}
		
		return context;
	}

	private String stringValue(ObjectNode node, String key) {
		JsonNode value = node.get(key);
		if (value != null && value.isTextual()) {
			return value.asText();
		}
		return null;
	}

}
