package io.konig.formula;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Context;
import io.konig.core.KonigException;
import io.konig.core.Term;

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


import io.konig.core.io.PrettyPrintWriter;

public class CurieTerm extends AbstractFormula implements IriValue, PrimaryExpression {
	
	private String namespacePrefix;
	private String localName;
	private Context context;

	public CurieTerm(Context context, String namespacePrefix, String localName) {
		this.namespacePrefix = namespacePrefix;
		this.localName = localName;
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	public String getNamespacePrefix() {
		return namespacePrefix;
	}

	public String getLocalName() {
		return localName;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(namespacePrefix);
		out.print(':');
		out.print(localName);
	}

	@Override
	public URI getIri() {
		
		Term term = context.getTerm(namespacePrefix);
		if (term == null) {
			throw new KonigException("Cannot resolve namespace prefix: " + namespacePrefix);
		}
		StringBuilder builder = new StringBuilder(term.getExpandedIdValue());
		builder.append(localName);
		
		return new URIImpl(builder.toString());
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		visitor.exit(this);
		
	}

}
