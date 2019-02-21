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


import java.io.IOException;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFParseException;

import io.konig.annotation.RdfProperty;
import io.konig.core.KonigException;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.vocab.CADL;
import io.konig.formula.FormulaParser;
import io.konig.formula.QuantifiedExpression;

public class Measure extends CadlEntity implements HasFormula {

	private QuantifiedExpression formula;
	
	@Override
	@RdfProperty(RDF.NAMESPACE + "type")
	public URI getType() {
		return CADL.Measure;
	}


	@RdfProperty(CADL.Term.formula)
	public QuantifiedExpression getFormula() {
		return formula;
	}

	public void setFormula(QuantifiedExpression formula) {
		this.formula = formula;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private URI id;
		private QuantifiedExpression formula;
		
		private Builder() {
			
		}
		
		public Builder id(URI id) {
			this.id = id;
			return this;
		}
		
		public Builder formula(String text, URI...term) {
			SimpleLocalNameService service = new SimpleLocalNameService();
			for (URI iri : term) {
				service.add(iri);
			}
			FormulaParser parser = new FormulaParser(null, service);
			try {
				formula = parser.quantifiedExpression(text);
				
			} catch (RDFParseException | IOException e) {
				throw new KonigException(e);
			}
			return this;
		}
		
		public Measure build() {
			Measure m = new Measure();
			m.setId(id);
			m.setFormula(formula);
			return m;
		}
	}
}
