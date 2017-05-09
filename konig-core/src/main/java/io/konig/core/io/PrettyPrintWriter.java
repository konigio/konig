package io.konig.core.io;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.turtle.TurtleUtil;

import io.konig.core.NamespaceManager;

public class PrettyPrintWriter extends PrintWriter {
	
	private boolean prettyPrint = true;
	private int indentLevel;
	private String indentText = "   ";
	
	private NamespaceManager nsManager;

	public PrettyPrintWriter(Writer arg0) {
		super(arg0);
	}

	public PrettyPrintWriter(OutputStream arg0) {
		super(arg0);
	}

	public PrettyPrintWriter(String arg0) throws FileNotFoundException {
		super(arg0);
	}

	public PrettyPrintWriter(File arg0) throws FileNotFoundException {
		super(arg0);
	}

	public PrettyPrintWriter(Writer arg0, boolean arg1) {
		super(arg0, arg1);
	}

	public PrettyPrintWriter(OutputStream arg0, boolean arg1) {
		super(arg0, arg1);
	}

	public PrettyPrintWriter(String arg0, String arg1) throws FileNotFoundException, UnsupportedEncodingException {
		super(arg0, arg1);
	}

	public PrettyPrintWriter(File arg0, String arg1) throws FileNotFoundException, UnsupportedEncodingException {
		super(arg0, arg1);
	}
	
	
	
	public NamespaceManager getNamespaceManager() {
		return nsManager;
	}

	public void setNamespaceManager(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}

	public void pushIndent() {
		indentLevel++;
	}
	
	public void popIndent() {
		indentLevel--;
	}
	
	public void indent() {
		if (prettyPrint) {
			for (int i=0; i<indentLevel; i++) {
				print(indentText);
			}
		} else {
			print(' ');
		}
	}

	public String getIndentText() {
		return indentText;
	}

	public void setIndentText(String indentText) {
		this.indentText = indentText;
	}

	public int getIndentLevel() {
		return indentLevel;
	}

	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}
	
	@Override
	public void println() {
		if (prettyPrint) {
			print('\n');
		} else {
			print(' ');
		}
	}
	
	public void print(PrettyPrintable object) {
		if (object == null) {
			print("null");
		} else {
			object.print(this);
		}
	}
	
	public void println(PrettyPrintable object) {
		print(object);
		println();
	}
	
	public void fieldName(String name) {
		indent();
		print(name);
		print(' ');
	}
	
	public void beginObject(Object pojo) {
		if (pojo == null) {
			println("null");
		} else {
			
			objectRef(pojo);
		}
		pushIndent();
	}
	
	public void objectRef(Object pojo) {
		if (pojo == null) {
			println("null");
			return;
		}
		if (pojo instanceof Value) {
			value((Value)pojo);
			println();
			return;
		} 
		
		Class<?> type = pojo.getClass();
		print(type.getSimpleName());
		print(':');
		println(pojo.hashCode());
	}
	
	private void value(Value value) {
		if (value instanceof Literal) {
			literal((Literal) value);
		} else if (value instanceof URI) {
			uri((URI)value);
		} else if (value instanceof BNode) {
			bnode((BNode)value);
		}
		
	}

	public boolean beginObjectField(String fieldName, Object pojo) {
		if (pojo != null) {
			fieldName(fieldName);
			beginObject(pojo);
			return true;
		}
		return false;
	}
	
	public void endObjectField(Object pojo) {
		if (pojo != null) {
			endObject();
		}
	}
	
	public void endObject() {
		popIndent();
	}

	public void field(String fieldName, Object object) {
		indent();
		print(fieldName);
		print(' ');
		if (object instanceof Value) {
			value((Value)object);
			println();
		} else if (object!=null && isPrimitiveOrWrapperType(object.getClass())) {
			print(object.toString());
			println();
		} else if (object instanceof String) {
			literalString((String)object);
			println();
		} else {
			objectRef(object);
		}
	}

	private static boolean isPrimitiveOrWrapperType(Class<?> type) {
		return type.isPrimitive() || isWrapperType(type);
	}
	
	private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

	private static boolean isWrapperType(Class<?> clazz) {
		return WRAPPER_TYPES.contains(clazz);
	}

	private static Set<Class<?>> getWrapperTypes() {
		Set<Class<?>> ret = new HashSet<Class<?>>();
		ret.add(Boolean.class);
		ret.add(Character.class);
		ret.add(Byte.class);
		ret.add(Short.class);
		ret.add(Integer.class);
		ret.add(Long.class);
		ret.add(Float.class);
		ret.add(Double.class);
		ret.add(Void.class);
		return ret;
	}
	
	public void field(String fieldName, PrettyPrintable object) {
		indent();
		print(fieldName);
		print(' ');
		println(object);
	}
	
	public void literal(Literal literal) {
		
		String label = literal.getLabel();
		URI datatype = literal.getDatatype();
		
		if (XMLSchema.INTEGER.equals(datatype) || XMLSchema.DECIMAL.equals(datatype)
				|| XMLSchema.DOUBLE.equals(datatype) || XMLSchema.BOOLEAN.equals(datatype))
		{
			try {
				write(XMLDatatypeUtil.normalize(label, datatype));
				return; // done
			}
			catch (IllegalArgumentException e) {
				// not a valid numeric typed literal. ignore error and write
				// as
				// quoted string instead.
			}
		}

		literalString(label);

		if (Literals.isLanguageLiteral(literal)) {
			// Append the literal's language
			write("@");
			write(literal.getLanguage());
		}
		else if (!XMLSchema.STRING.equals(datatype) ) {
			// Append the literal's datatype (possibly written as an abbreviated
			// URI)
			write("^^");
			uri(datatype);
		}
		
	}
	
	public void literalString(String label) {

		if (label.indexOf('\n') != -1 || label.indexOf('\r') != -1 || label.indexOf('\t') != -1) {
			// Write label as long string
			write("\"\"\"");
			write(TurtleUtil.encodeLongString(label));
			write("\"\"\"");
		}
		else {
			// Write label as normal string
			write("\"");
			write(TurtleUtil.encodeString(label));
			write("\"");
		}
		
	}

	public void bnode(BNode bnode) {
		print("_:");
		print(bnode.getID());
	}

	public void uri(URI uri) {
		if (nsManager != null) {
			Namespace ns = nsManager.findByName(uri.getNamespace());
			if (ns != null) {
				print(ns.getPrefix());
				print(':');
				print(uri.getLocalName());
				return;
			}
		}
		print('<');
		print(uri.stringValue());
		print('>');
	}
	
}
