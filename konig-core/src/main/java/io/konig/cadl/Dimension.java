package io.konig.cadl;

import java.io.IOException;

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


import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.rio.RDFParseException;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.CADL;
import io.konig.formula.FormulaParser;
import io.konig.formula.QuantifiedExpression;

public class Dimension extends CadlEntity {
	
	public Set<Level> level = new LinkedHashSet<>();
	private QuantifiedExpression formula;

	@Override
	public URI getType() {
		return CADL.Dimension;
	}
	
	public QuantifiedExpression getFormula() {
		return formula;
	}

	public void setFormula(QuantifiedExpression formula) {
		this.formula = formula;
	}

	public void addLevel(Level level) {
		this.level.add(level);
	}
	
	public Level findLevelByName(String localName) {
		for (Level e : level) {
			if (localName.equals(e.getId().getLocalName())) {
				return e;
			}
		}
		return null;
	}
	
	public Level findLevelById(URI id) {
		for (Level e : level) {
			if (id.equals(e.getId())) {
				return e;
			}
		}
		return null;
	}

	@RdfProperty(CADL.Term.level)
	public Set<Level> getLevel() {
		return level;
	}

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private Dimension dimension;
		private Builder() {
			dimension = new Dimension();
		}
		
		public Builder formula(String text, URI...term) throws RDFParseException, IOException {
			FormulaParser parser = new FormulaParser();
			dimension.setFormula(parser.quantifiedExpression(text, term));
			return this;
		}
		
		public Builder id(URI id) {
			dimension.setId(id);
			return this;
		}
		
		public Builder level(Level level) {
			dimension.addLevel(level);
			return this;
		}
		
		public Dimension build() {
			return dimension;
		}
	}

}
