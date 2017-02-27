package io.konig.core.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.openrdf.model.Literal;

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

import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Context;
import io.konig.core.KonigException;
import io.konig.core.Term;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.rio.turtle.IriTemplateParseException;
import io.konig.rio.turtle.IriTemplateParser;

public class IriTemplate extends SimpleValueFormat {
	
	private Context context;
	
	public IriTemplate(Context context, String text) {
		super(text);
		this.context = context;
	}
	
	public IriTemplate() {
		
	}
	
	public IriTemplate(String text) {
		super();
		parse(text);
	}

	private void parse(String input) {
		input = input.trim();
		int c = input.charAt(0);
		if (c == '@' || c=='<') {
			StringReader reader = new StringReader(input);
			try {
				IriTemplate self = IriTemplateParser.INSTANCE.parse(reader);
				context = self.getContext();
				text = self.getText();
				compile();
			} catch (IriTemplateParseException e) {
				throw new KonigException(e);
			}
		} else {
			text = input;
			compile();
		}
		
	}


	public URI expand(ValueMap map) {
		
		return new URIImpl(format(map));
		
	}
	
	public String getText() {
		return text;
	}
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public Literal toValue() {
		return new LiteralImpl(toString(), XMLSchema.STRING);
	}
	
	public String toString() {
		StringWriter buffer = new StringWriter();
		PrettyPrintWriter out = new PrettyPrintWriter(buffer);
		print(out);
		out.close();
		return buffer.toString();
	}
	
	public void print(PrettyPrintWriter out) {
		if (context != null) {
			List<Term> list = context.asList();
			
			out.indent();
			out.print("@context {");
			out.pushIndent();
			String comma = "";
			for (Term term : list) {
				out.println(comma);
				term.print(out);
				comma = ",";
			}
			out.popIndent();
			out.println();
			out.indent();
			out.println('}');
			out.println();
		}
		out.indent();
		out.print('<');
		out.print(text);
		out.print('>');
	}
	
}
