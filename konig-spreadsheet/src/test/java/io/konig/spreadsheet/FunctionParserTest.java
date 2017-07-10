package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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


import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.konig.core.util.ValueMap;

public class FunctionParserTest {

	private ListFunctionVisitor visitor = new ListFunctionVisitor();
	private FunctionParser parser = new FunctionParser(visitor);
	
	@Test
	public void test() throws Exception {
		
		String text =
		      " foo(alpha: \"one\", beta: \"two\" )\n"
			+ " bar( delta : \"three\") ";
		
		parser.parse(text);
		
		List<Function> list = visitor.getList();
		
		assertEquals(2, list.size());
		
		Function foo = list.get(0);
		assertEquals("foo", foo.getName());
		ValueMap params = foo.getParameters();
		assertEquals("one", params.get("alpha"));
		assertEquals("two", params.get("beta"));
		
		Function bar = list.get(1);
		assertEquals("bar", bar.getName());
		params = bar.getParameters();
		assertEquals("three", params.get("delta"));
		
		
	}

}
