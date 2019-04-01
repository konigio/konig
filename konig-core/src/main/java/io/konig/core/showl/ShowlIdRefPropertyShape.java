package io.konig.core.showl;

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

/**
 * A PropertyShape for the konig:id pseudo-property that appears in a virtual NodeShape
 * induced by a KQL formula.
 * 
 * Consider the following example:
 * <pre>
 * shape:SourcePerson a sh:NodeShape;
 *   sh:property [
 *     sh:path ex:mother ;
 *     sh:nodeKind sh:IRI ;
 *     konig:formula "$.parent[gender Female]"
 *   ]
 * .
 * </pre>
 * <p>
 * In this example, the 'parent' property has a virtual sh:shape which contains the 'gender' property.
 * For the sake of argument, let's call the virtual shape 'shape:SourcePerson_parent'.
 * </p>
 * <p>
 * Clearly, shape:SourcePerson_parent describes the same individual as {shape:SourcePerson}.mother, and
 * since {shape:SourcePerson}.mother has nodeKind sh:IRI.  
 * 
 * The ShowlIdRefPropertyShape describes the 'konig:id' property of 'shape:SourcePerson_parent'.
 * The {@link getIdRef()} method returns the {shape:SourcePerson}.mother PropertyShape.
 * </p>
 * 
 * The ShowlIdRefPropertyShape 
 * 
 * @author Greg McFall
 *
 */
public class ShowlIdRefPropertyShape extends ShowlDerivedPropertyShape {
	
	private ShowlPropertyShape idref;

	/**
	 * Create a new ShowlIdRefPropertyShape.
	 * @param declaringShape The 'virtual' shape within the formula.
	 * @param property The ShowlProperty for 'konig:id'.
	 * @param idref  The source property that references the virtual shape.
	 */
	public ShowlIdRefPropertyShape(ShowlNodeShape declaringShape, ShowlProperty property, ShowlPropertyShape idref) {
		super(declaringShape, property);
		this.idref = idref;
	}

	public ShowlPropertyShape getIdref() {
		return idref;
	}
	
	


}
