package io.konig.spreadsheet;

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
