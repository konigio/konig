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

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public class CompositeRdfHandler extends ArrayList<RDFHandler> implements RDFHandler {
	private static final long serialVersionUID = 1L;
	
	public CompositeRdfHandler() {
		
	}
	
	public CompositeRdfHandler(RDFHandler...handlers) {
		for (RDFHandler h : handlers) {
			add(h);
		}
	}

	@Override
	public void startRDF() throws RDFHandlerException {
		for (RDFHandler child : this) {
			child.startRDF();
		}

	}

	@Override
	public void endRDF() throws RDFHandlerException {
		for (RDFHandler child : this) {
			child.endRDF();
		}

	}

	@Override
	public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
		for (RDFHandler child : this) {
			child.handleNamespace(prefix,uri);
		}
	}

	@Override
	public void handleStatement(Statement st) throws RDFHandlerException {
		for (RDFHandler child : this) {
			child.handleStatement(st);
		}
	}

	@Override
	public void handleComment(String comment) throws RDFHandlerException {
		for (RDFHandler child : this) {
			child.handleComment(comment);
		}
	}

}
