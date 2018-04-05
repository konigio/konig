package io.konig.transform.proto;

/*
 * #%L
 * Konig Transform
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


/**
 * A record that stores the relationship between a some target property and the first step in an inverse path.
 * 
 * Consider the following example.  
 * 
 * Suppose that PersonShape has a an "owns" property whose value is a Product owned by the Person.
 * Suppose further that ProductShape has an "ownerName" property which gives the name 
 * of the person who owns the product.
 * 
 * The formula for the "ownerName" property would be given by <code>^owns.name</code>
 * 
 * We want to describe a relationship from the PropertyModel for "ownerName" in ProductShape (the subject)
 * to the PropertyModel for "owns" in Person (the object).
 * 
 * That relationship is captured in an instance of InversePropertyLink.
 * 
 * 
 * @author Greg McFall
 *
 */
public class InversePropertyLink {
	
	private PropertyModel subject;
	private PropertyModel object;
	
	public InversePropertyLink(PropertyModel subject, PropertyModel object) {
		this.subject = subject;
		this.object = object;
	}

	public PropertyModel getSubject() {
		return subject;
	}

	public PropertyModel getObject() {
		return object;
	}
	
	
}
