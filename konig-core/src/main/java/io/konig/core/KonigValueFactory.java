package io.konig.core;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.impl.KonigLiteral;

public class KonigValueFactory implements ValueFactory {
	
	private DatatypeFactory datatypeFactory;
	
	public KonigValueFactory() {
		
	}

	@Override
	public URI createURI(String uri) {
		return new URIImpl(uri);
	}

	@Override
	public URI createURI(String namespace, String localName) {
		return new URIImpl(namespace + localName);
	}

	@Override
	public BNode createBNode() {
		return new BNodeImpl(UidGenerator.INSTANCE.next());
	}

	@Override
	public BNode createBNode(String nodeID) {
		return new BNodeImpl(nodeID);
	}

	@Override
	public Literal createLiteral(String label) {
		return new KonigLiteral(label);
	}

	@Override
	public Literal createLiteral(String label, String language) {
		
		return new KonigLiteral(label, language);
	}

	@Override
	public Literal createLiteral(String label, URI datatype) {
		return new KonigLiteral(label, datatype);
	}

	@Override
	public Literal createLiteral(boolean value) {
		return new KonigLiteral(Boolean.toString(value), XMLSchema.BOOLEAN);
	}

	@Override
	public Literal createLiteral(byte value) {
		return new KonigLiteral(Byte.toString(value), XMLSchema.BYTE);
	}

	@Override
	public Literal createLiteral(short value) {
		return new KonigLiteral(Short.toString(value), XMLSchema.SHORT);
	}

	@Override
	public Literal createLiteral(int value) {
		return new KonigLiteral(Integer.toString(value), XMLSchema.INT);
	}

	@Override
	public Literal createLiteral(long value) {
		return new KonigLiteral(Long.toString(value), XMLSchema.LONG);
	}

	@Override
	public Literal createLiteral(float value) {
		return new KonigLiteral(Float.toString(value), XMLSchema.FLOAT);
	}

	@Override
	public Literal createLiteral(double value) {
		return new KonigLiteral(Double.toString(value), XMLSchema.DOUBLE);
	}

	@Override
	public Literal createLiteral(XMLGregorianCalendar calendar) {
		return new KonigLiteral(calendar.toXMLFormat(), XMLSchema.DATETIME);
	}

	@Override
	public Literal createLiteral(Date date) {
	
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(date);
		XMLGregorianCalendar calendar = datatypeFactory().newXMLGregorianCalendar(c);
		return createLiteral(calendar);
	}

	@Override
	public Statement createStatement(Resource subject, URI predicate, Value object) {
		return new StatementImpl(subject, predicate, object);
	}

	@Override
	public Statement createStatement(Resource subject, URI predicate, Value object, Resource context) {
		return new ContextStatementImpl(subject, predicate, object, context);
	}
	
	private DatatypeFactory datatypeFactory() {
		if (datatypeFactory == null) {
			try {
				datatypeFactory = DatatypeFactory.newInstance();
			} catch (DatatypeConfigurationException e) {
				throw new RuntimeException(e);
			}
		}
		return datatypeFactory;
	}


}
