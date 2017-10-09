package io.konig.formula;

import static org.junit.Assert.*;

import org.junit.Test;

import io.konig.core.Path;
import io.konig.core.PathFactory;

public class Path2FormulaTranslatorTest {
	private PathFactory pathFactory = new PathFactory();

	@Test
	public void test() {
		String text =
			"@context {\r\n" + 
			"  \"schema\" : \"http://schema.org/\",\r\n" + 
			"  \"offers\" : \"schema:offers\",\r\n" + 
			"  \"priceCurrency\" : \"schema:priceCurrency\",\r\n" + 
			"  \"price\" : \"schema:price\"\r\n" + 
			"}\r\n" + 
			"/offers[priceCurrency \"USD\"]/price";
		
		Path path = pathFactory.createPath(text);
		
		QuantifiedExpression formula = Path2FormulaTranslator.getInstance().toQuantifiedExpression(path);
		
		Path path2 = Formula2PathTranslator.getInstance().toPath(formula);
		
		String actual = path2.toSimpleString();
		
		assertEquals("/<http://schema.org/offers>[<http://schema.org/priceCurrency> \"USD\"]/<http://schema.org/price>", actual);
	}

}
