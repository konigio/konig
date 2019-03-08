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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.rio.RDFParseException;

import io.konig.annotation.RdfProperty;
import io.konig.core.KonigException;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.vocab.CADL;
import io.konig.formula.FormulaParser;
import io.konig.formula.QuantifiedExpression;

public class Level extends CadlEntity implements HasFormula {
	
	private Set<Level> rollUpTo;
	private QuantifiedExpression formula;
	private Set<Attribute> attribute;

	@Override
	public URI getType() {
		return CADL.Level;
	}
	

	public void addAttribute(Attribute a) {
		if (attribute == null) {
			attribute = new LinkedHashSet<>();
		}
		attribute.add(a);
	}

	public Attribute findAttributeByName(String localName) {
		for (Attribute a : attribute) {
			if (localName.equals(a.getId().getLocalName())) {
				return a;
			}
		}
		return null;
	}

	public Attribute findAttributeById(URI id) {
		for (Attribute a : attribute) {
			if (id.equals(a.getId())) {
				return a;
			}
		}
		return null;
	}
	
	@RdfProperty(CADL.Term.attribute)
	public Set<Attribute> getAttribute() {
		return attribute == null ? Collections.emptySet() : attribute;
	}
	
	
	@RdfProperty(CADL.Term.rollUpTo)
	public Set<Level> getRollUpTo() {
		return rollUpTo==null ? Collections.emptySet() : rollUpTo;
	}
	
	public void addRollUpTo(Level rollUpTo) {
		if (this.rollUpTo == null) {
			this.rollUpTo = new LinkedHashSet<>();
		}
		this.rollUpTo.add(rollUpTo);
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
		private Level level;
		
		private Builder() {
			level = new Level();
		}
		
		public Builder id(URI id) {
			level.setId(id);
			return this;
		}

		public Builder formula(String text, URI...term) {
			SimpleLocalNameService service = new SimpleLocalNameService();
			for (URI iri : term) {
				service.add(iri);
			}
			FormulaParser parser = new FormulaParser(null, service);
			try {
				QuantifiedExpression formula = parser.quantifiedExpression(text);
				level.setFormula(formula);
				
			} catch (RDFParseException | IOException e) {
				throw new KonigException(e);
			}
			return this;
		}
		
		public Builder rollUpTo(Level broader) {
			level.addRollUpTo(broader);
			return this;
		}
		
		public Level build() {
			return level;
		}
	}
	
	

}
