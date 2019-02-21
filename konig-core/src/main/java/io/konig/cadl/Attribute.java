package io.konig.cadl;

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


import org.openrdf.model.URI;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.CADL;
import io.konig.formula.QuantifiedExpression;

public class Attribute extends CadlEntity  implements HasFormula {
	
	private QuantifiedExpression formula;

	@Override
	public URI getType() {
		return CADL.Attribute;
	}

	@Override
	public void setFormula(QuantifiedExpression e) {
		formula = e;
	}

	@Override
	@RdfProperty(CADL.Term.formula)
	public QuantifiedExpression getFormula() {
		return formula;
	}

}
