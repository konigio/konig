package io.konig.rio.turtle;

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


import java.io.IOException;
import java.io.Reader;

import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Context;
import io.konig.core.util.IriTemplate;

public class IriTemplateParser  {
	public static final IriTemplateParser INSTANCE = new IriTemplateParser();

	public IriTemplate parse(Reader reader) throws IriTemplateParseException {
		Worker worker = new Worker(reader);
		try {
			return worker.parse();
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new IriTemplateParseException(e);
		}
	}
	
	private static class Worker extends SeaTurtleParser {
		
		public Worker(Reader reader) {
			initParse(reader, "");
		}

		public IriTemplate parse() throws IOException, RDFParseException, RDFHandlerException {
			int c = next();
			Context context = null;
			if (c == '@') {
				if (tryWord("context")) {
					contextTermList();
					context = getContext();
				} else {
					fail("Expected '@context'");
				}
			} else {
				unread(c);
			}
			
			String text = iriRef();
			
			return new IriTemplate(context, text);
			

		}
	}
	
}
