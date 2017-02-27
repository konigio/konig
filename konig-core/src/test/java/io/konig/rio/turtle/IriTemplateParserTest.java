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


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Test;

import io.konig.core.Context;
import io.konig.core.Term;
import io.konig.core.util.IOUtil;
import io.konig.core.util.IriTemplate;

public class IriTemplateParserTest {
	private IriTemplateParser parser = new IriTemplateParser();

	@Test
	public void testContext() throws Exception {
		
		Reader input = reader("IriTemplateParserTest/testContext.txt");
		try {
			IriTemplate template = parser.parse(input);
			Context context = template.getContext();
			assertTrue(context != null);
			Term term = context.getTerm("schema");
			assertTrue(term != null);
			assertEquals("http://schema.org/", term.getId());
			
			assertEquals("http://example.com/product/{name}", template.getText());
			
		} finally {
			IOUtil.close(input, "IriTemplateParserTest/testContext.txt");
		}
		
	}

	private Reader reader(String resource) {
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		
		return new InputStreamReader(input);
	}

}
