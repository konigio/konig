package io.konig.core.showl.expression;

/*
 * #%L
 * Konig Core
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


import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlProcessingException;
import io.konig.core.showl.ShowlPropertyShape;

public class ShowlLiteralExpression implements ShowlExpression {
	private Literal literal;

	public ShowlLiteralExpression(Literal literal) {
		this.literal = literal;
	}

	public Literal getLiteral() {
		return literal;
	}

	@Override
	public String displayValue() {
		return RdfUtil.toTurtle(literal);
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		// Do nothing
		
	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		// Do nothing
		
	}

	public String toString() {
		return "ShowlLiteralExpression(\"" + literal.stringValue() + "\")";
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		URI type = literal.getDatatype();
		if (type == null) {
			type = XMLSchema.STRING;
		}
		return type;
	}

}
