package io.konig.transform.beam;

/*
 * #%L
 * Konig Transform Beam
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JExpr;

public class BeamLiteralFactory {
	
	JCodeModel model;
	

	public BeamLiteralFactory(JCodeModel model) {
		this.model = model;
	}


	public IJExpression javaExpression(Literal literal) throws BeamTransformGenerationException {
		URI datatype = literal.getDatatype();
		if (datatype == null || XMLSchema.STRING.equals(datatype)) {

      return JExpr.lit(literal.stringValue());
		}
		
		if (
				XMLSchema.LONG.equals(datatype) ||
				XMLSchema.INTEGER.equals(datatype) ||
				XMLSchema.INT.equals(datatype) ||
				XMLSchema.NEGATIVE_INTEGER.equals(datatype) ||
				XMLSchema.NON_NEGATIVE_INTEGER.equals(datatype) ||
				XMLSchema.NON_POSITIVE_INTEGER.equals(datatype) ||
				XMLSchema.POSITIVE_INTEGER.equals(datatype) ||
				XMLSchema.UNSIGNED_INT.equals(datatype)
		) {
			long longValue = Long.parseLong(literal.stringValue());
			return JExpr.lit(longValue);
		}
		
		if (XMLSchema.BOOLEAN.equals(datatype)) {
			if ("TRUE".equalsIgnoreCase(literal.stringValue())) {
				return JExpr.TRUE;
			} else {
				return JExpr.FALSE;
			}
		}
		
		throw new BeamTransformGenerationException("Literal not supported: " + literal.toString());
	}

}
