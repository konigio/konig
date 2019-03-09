package io.konig.core;

import java.io.IOException;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.openrdf.model.URI;

import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.io.PrettyPrintWriter;

public class Term implements Comparable<Term>{
	public static enum Kind {
		NAMESPACE,
		CLASS,
		PROPERTY,
		INDIVIDUAL,
		ANY
	}
	
	private String key;
	private String id;
	private String language;
	private String type;
	private String container;
	private URI expandedType;
	private URI expandedId;
	private int index=-1;
	private Kind kind = Kind.ANY;
	
	public Term(String key, String id) {
		this.key = key;
		this.id = id;
	}
	
	public Term(String key, String id, Kind kind) {
		this.key = key;
		this.id = id;
		this.kind = kind;
	}
	
	public Term(String key, String id, String language, String type) {
		this.key = key;
		this.id = id;
		this.language = language;
		this.type = type;
	}

	
	public Term(String key, String id, String language, String type, String container) {
		this.key = key;
		this.id = id;
		this.language = language;
		this.type = type;
		this.container = container;
	}

	public String getKey() {
		return key;
	}

	public String getId() {
		return id;
	}
	
	public Kind getKind() {
		return kind==null ? Kind.ANY : kind;
	}
	
	public String getContainer() {
		return container;
	}

	public void setKind(Kind kind) {
		this.kind = kind;
	}

	public String getLanguage() {
		return language;
	}

	public String getType() {
		return type;
	}

	public URI getExpandedType() {
		return expandedType;
	}

	public void setExpandedType(URI expandedType) {
		this.expandedType = expandedType;
	}

	public URI getExpandedId() {
		return expandedId;
	}

	public void setExpandedId(URI expandedId) {
		this.expandedId = expandedId;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(key);
		buffer.append('[');
		String comma = "";
		if (id != null) {
			buffer.append("@id: ");
			buffer.append(id);
			comma = ", ";
		}
		if (type != null) {
			buffer.append(comma);
			buffer.append("@type: ");
			buffer.append(type);
			comma = ", ";
		}
		if (language != null) {
			buffer.append("@language: ");
			buffer.append(language);
		}
		buffer.append(']');
		
		return buffer.toString();
	}
	

	public String getExpandedIdValue() {
		return expandedId!=null ? expandedId.stringValue() :
			id != null ? id :
			null;
	}

	public int compareTo(Term other) {
		Kind otherKind = other.kind;
		if (otherKind == null) {
			otherKind = Kind.ANY;
		}
		Kind thisKind = this.kind;
		if (thisKind == null) {
			thisKind = Kind.ANY;
		}
		int delta = thisKind.ordinal() - otherKind.ordinal();
		if (delta == 0) {
			delta = key.compareTo(other.key);
		}
		
		return delta;
	}
	
	public void toJson(JsonGenerator json) throws IOException {
		if (id!=null && type==null && language==null && container==null) {
			json.writeString(id);
		} else {
			json.writeStartObject();
			json.writeStringField("@id", id);
			if (type!=null) {
				json.writeStringField("@type", type);
			}
			if (language!=null) {
				json.writeStringField("@languge", language);
			}
			if (container!=null) {
				json.writeStringField("@container", container);
			}
			json.writeEndObject();
		}
	}
	
	public void print(PrettyPrintWriter out) {
		out.indent();
		out.print('"');
		out.print(key);
		out.print("\" : ");
		if (container==null && language==null && type==null) {
			out.print('"');
			out.print(id);
			out.print('"');
		} else {
			out.println('{');
			out.pushIndent();
			

			out.indent();
			out.print("\"@id\" : \"");
			out.print(id);
			out.print('"');
			
			if (type != null) {
				out.println(',');
				out.indent();
				out.print("\"@type\" : \"");
				out.print(type);
				out.print('"');
			}
			if (container != null) {
				out.println(',');
				out.indent();
				out.print("\"@container\" : \"");
				out.print(container);
				out.print('"');
			}
			if (language!=null) {
				out.println(',');
				out.indent();
				out.print("\"@language\" : \"");
				out.print(language);
				out.print('"');
			}
			out.println();
			out.popIndent();
			out.indent();
			out.print('}');
			
		}
	}

}
