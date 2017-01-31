package io.konig.datasource;

/*
 * #%L
 * Konig Core
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


import java.util.LinkedHashSet;
import java.util.Set;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.Konig;

public class GoogleBigQueryTable extends DataSource {
	
	private Set<DataSource> bigQuerySource;

	public GoogleBigQueryTable() {
		addType(Konig.GoogleBigQueryTable);
	}

	public void addBigQuerySource(DataSource source) {
		if (bigQuerySource == null) {
			bigQuerySource = new LinkedHashSet<>();
		}
		bigQuerySource.add(source);
	}

	@RdfProperty(Konig.BIG_QUERY_SOURCE)
	public Set<DataSource> getBigQuerySource() {
		return bigQuerySource;
	}

	public void setBigQuerySource(Set<DataSource> bigQuerySource) {
		this.bigQuerySource = bigQuerySource;
	}


}
