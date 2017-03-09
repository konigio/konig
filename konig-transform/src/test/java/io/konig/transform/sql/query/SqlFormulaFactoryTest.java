package io.konig.transform.sql.query;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import io.konig.core.vocab.Schema;
import io.konig.formula.Expression;
import io.konig.shacl.PropertyConstraint;
import io.konig.sql.query.ValueExpression;

public class SqlFormulaFactoryTest {
	private SqlFormulaFactory sqlFactory = new SqlFormulaFactory();
	

	@Ignore
	public void testAddition() throws Exception {
		
		PropertyConstraint p = new PropertyConstraint(Schema.answerCount);
		Expression formula = new Expression("1 + 2 + 3");
		p.setFormula(formula);
		
		ValueExpression value = sqlFactory.formula(null, p);
		
		assertEquals("1 + 2 + 3", value.toString());
		
	}
	
	@Test
	public void testIf() throws Exception {
		String text = 
			"@context {\n" + 
			"  \"email\" : \"http://schema.org/email\"\n" + 
			"}\n" + 
			"IF(email=\"alice@example.com\" , 1 , 0)";
		
		Expression formula = new Expression(text);
		PropertyConstraint p = new PropertyConstraint(Schema.answerCount);
		p.setFormula(formula);
		
		TableName sourceTable = new TableName("example.Question", null);
		
		ValueExpression value = sqlFactory.formula(sourceTable, p);
		
		assertEquals("IF(email=\"alice@example.com\" , 1 , 0)", value.toString());
	}



}
