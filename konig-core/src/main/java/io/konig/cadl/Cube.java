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


import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.CADL;
import io.konig.datasource.DataSource;

public class Cube extends CadlEntity {
	private Variable source;
	private Set<Dimension> dimension = new LinkedHashSet<>();
	private Set<Measure> measure = new LinkedHashSet<>();
	private Set<DataSource> storage = new LinkedHashSet<>();
	
	@Override
	public URI getType() {
		return CADL.Cube;
	}
	
	public void addStorage(DataSource ds) {
		storage.add(ds);
	}

	@RdfProperty(CADL.Term.storage)
	public Set<DataSource> getStorage() {
		return storage;
	}

	@RdfProperty(CADL.Term.source)
	public Variable getSource() {
		return source;
	}
	
	public void setSource(Variable source) {
		this.source = source;
	}
	
	public Dimension findDimensionByName(String localName) {

		for (Dimension d : dimension) {
			if (localName.equals(d.getId().getLocalName())) {
				return d;
			}
		}
		return null;
	}
	
	public Dimension findDimensionById(URI id) {
		for (Dimension d : dimension) {
			if (id.equals(d.getId())) {
				return d;
			}
		}
		return null;
	}

	public void addDimension(Dimension dimension) {
		this.dimension.add(dimension);
	}

	@RdfProperty(CADL.Term.dimension)
	public Set<Dimension> getDimension() {
		return dimension;
	}
	
	public void addMeasure(Measure measure) {
		this.measure.add(measure);
	}

	@RdfProperty(CADL.Term.measure)
	public Set<Measure> getMeasure() {
		return measure;
	}

	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private Cube cube;
		private Builder() {
			cube = new Cube();
		}
		public Builder id(URI id) {
			cube.setId(id);
			return this;
		}
		public Builder dimension(Dimension dim) {
			cube.addDimension(dim);
			return this;
		}
		public Builder measure(Measure measure) {
			cube.addMeasure(measure);
			return this;
		}
		
		public Builder source(Variable source) {
			cube.setSource(source);
			return this;
		}
		
		public Cube build() {
			return cube;
		}
	}

}