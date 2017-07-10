package io.konig.transform.sql.query;

/*
 * #%L
 * Konig Transform
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

import org.junit.Ignore;
import org.junit.Test;

import io.konig.core.vocab.Schema;
import io.konig.formula.Expression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.sql.query.ValueExpression;

public class SqlFormulaFactoryTest {
	private SqlFormulaFactory sqlFactory = new SqlFormulaFactory();
	

	@Ignore
	public void testAddition() throws Exception {
		
		PropertyConstraint p = new PropertyConstraint(Schema.answerCount);
		QuantifiedExpression formula = new QuantifiedExpression("1 + 2 + 3");
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
		
		QuantifiedExpression formula = new QuantifiedExpression(text);
		PropertyConstraint p = new PropertyConstraint(Schema.answerCount);
		p.setFormula(formula);
		
		TableName sourceTable = new TableName("example.Question", null);
		
		ValueExpression value = sqlFactory.formula(sourceTable, p);
		
		assertEquals("IF(email=\"alice@example.com\" , 1 , 0)", value.toString());
	}



}
