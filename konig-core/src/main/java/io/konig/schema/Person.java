package io.konig.schema;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

import org.openrdf.model.URI;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.Schema;

public class Person {

	private URI id;
	private String name;
	private Set<String> email;
	
	public Person() {
		
	}
	
	public Person(URI id) {
		this.id = id;
	}

	public URI getId() {
		return id;
	}

	public void setId(URI id) {
		this.id = id;
	}


	@RdfProperty(Schema.NAME)
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	@RdfProperty(Schema.EMAIL)
	public Set<String> getEmail() {
		return email;
	}


	public void setEmail(Set<String> email) {
		this.email = email;
	}
	
	

}
