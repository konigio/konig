package io.konig.lineage;

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


import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.RdfVocab;

public class PropertyGenerator {

	private URI id;
	private DatasourceProperty generatorOutput;	
	private Set<DatasourceProperty> generatorInput;
	
	@RdfProperty(RdfVocab.Terms.type)
	public URI getType() {
		return Konig.PropertyGenerator;
	}

	public URI getId() {
		return id;
	}

	public void setId(URI id) {
		this.id = id;
	}

	@RdfProperty(Konig.Terms.generatorOutput)
	public DatasourceProperty getGeneratorOutput() {
		return generatorOutput;
	}

	public void setGeneratorOutput(DatasourceProperty generatorOutput) {
		this.generatorOutput = generatorOutput;
	}

	public void addGeneratorInput(DatasourceProperty p) {
		if (generatorInput == null) {
			generatorInput = new LinkedHashSet<>();
		}
		generatorInput.add(p);
	}
	
	@RdfProperty(Konig.Terms.generatorInput)
	public Set<DatasourceProperty> getGeneratorInput() {
		return generatorInput==null ? Collections.emptySet() : generatorInput;
	}

}
