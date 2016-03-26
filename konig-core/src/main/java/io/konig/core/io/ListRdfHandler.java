package io.konig.core.io;

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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.ListHandler;

public class ListRdfHandler implements RDFHandler {

	private ListHandler listHandler;
	private RDFHandler delegate;
	
	private Map<String, RdfList> listMap;
	private Map<String, StatementList> objectMap;
	
	
	
	public ListRdfHandler(ListHandler listHandler, RDFHandler delegate) {
		this.listHandler = listHandler;
		this.delegate = delegate;
	}




	private static class StatementList {
		private Statement statement;
		private StatementList rest;
		public StatementList(Statement s) {
			this.statement = s;
		}
		
		
	}
	
	private static class RdfList {
		private Resource subject;
		private Value first;
		private RdfList rest;
		private RdfList prev;
		
		public RdfList(Resource subject) {
			this.subject = subject;
		}

		public List<Value> toJavaList() {
			List<Value> result = new ArrayList<>();
			RdfList current = this;
			while (current != null) {
				result.add(current.first);
				current = current.rest;
			}
			
			return result;
		}
	}




	@Override
	public void startRDF() throws RDFHandlerException {
		listMap = new HashMap<>();
		objectMap = new HashMap<>();
		delegate.startRDF();
	}




	@Override
	public void endRDF() throws RDFHandlerException {
		
		for (RdfList list : listMap.values()) {
			if (list.prev == null) {
				List<Value> javaList = list.toJavaList();
				
				StatementList sequence = objectMap.get(list.subject.stringValue());
				while (sequence != null) {
					Statement s = sequence.statement;

					listHandler.handleList(s.getSubject(), s.getPredicate(), javaList);
					sequence = sequence.rest;
				}
			}
		}		
		delegate.endRDF();
	}

	@Override
	public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
		delegate.handleNamespace(prefix, uri);
	}




	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		Resource subject = st.getSubject();
		URI predicate = st.getPredicate();
		Value object = st.getObject();
		
		if (predicate.equals(RDF.FIRST)) {
			list(subject).first = object;
		} else if (predicate.equals(RDF.REST)) {
			
			if (!object.equals(RDF.NIL)) {
				RdfList target = list(subject);
				
				target.rest = list((Resource)object);
				target.rest.prev = target;
				
			}
		} else {
			if (object instanceof Resource) {
			
				StatementList slist = objectMap.get(object.stringValue());
				if (slist == null) {
					objectMap.put(object.stringValue(), new StatementList(st));
				} else {
					slist.rest = new StatementList(st);
				}
			}
			delegate.handleStatement(st);
		}
	}


	private RdfList list(Resource subject) {
		RdfList list = listMap.get(subject.stringValue());
		if (list == null) {
			list = new RdfList(subject);
			listMap.put(subject.stringValue(), list);
		}
		return list;
	}




	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		delegate.handleComment(comment);
	}
	
}
