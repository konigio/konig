package io.konig.validation;

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


public class ModelStatistics {
	private int numberOfClasses;
	private int numberOfProperties;
	private int numberOfNamedIndividuals;
	private int numberOfShapes;
	private int numberOfErrors;
	private RationalNumber classesWithDescription = RationalNumber.UNDEFINED;
	private RationalNumber propertiesWithDescription = RationalNumber.UNDEFINED;
	private RationalNumber propertyShapesWithDescription = RationalNumber.UNDEFINED;
	private RationalNumber namedIndividualsWithDescription= RationalNumber.UNDEFINED;
	
	public int getNumberOfClasses() {
		return numberOfClasses;
	}
	public void setNumberOfClasses(int numberOfClasses) {
		this.numberOfClasses = numberOfClasses;
	}
	public int getNumberOfProperties() {
		return numberOfProperties;
	}
	public void setNumberOfProperties(int numberOfProperties) {
		this.numberOfProperties = numberOfProperties;
	}
	public int getNumberOfNamedIndividuals() {
		return numberOfNamedIndividuals;
	}
	public void setNumberOfNamedIndividuals(int numberOfNamedIndividuals) {
		this.numberOfNamedIndividuals = numberOfNamedIndividuals;
	}
	public int getNumberOfShapes() {
		return numberOfShapes;
	}
	public void setNumberOfShapes(int numberOfShapes) {
		this.numberOfShapes = numberOfShapes;
	}
	
	public RationalNumber getClassesWithDescription() {
		return classesWithDescription;
	}

	public void setClassesWithDescription(RationalNumber classesWithDescription) {
		this.classesWithDescription = classesWithDescription;
	}


	public RationalNumber getPropertyShapesWithDescription() {
		return propertyShapesWithDescription;
	}

	public void setPropertyShapesWithDescription(RationalNumber propertyShapesWithDescription) {
		this.propertyShapesWithDescription = propertyShapesWithDescription;
	}

	public RationalNumber getNamedIndividualsWithDescription() {
		return namedIndividualsWithDescription;
	}

	public void setNamedIndividualsWithDescription(RationalNumber namedIndividualsWithDescription) {
		this.namedIndividualsWithDescription = namedIndividualsWithDescription;
	}
	public RationalNumber getPropertiesWithDescription() {
		return propertiesWithDescription;
	}
	public void setPropertiesWithDescription(RationalNumber propertiesWithDescription) {
		this.propertiesWithDescription = propertiesWithDescription;
	}
	public int getNumberOfErrors() {
		return numberOfErrors;
	}
	public void setNumberOfErrors(int numberOfErrors) {
		this.numberOfErrors = numberOfErrors;
	}
	
	public int getNumberOfTerms() {
		return numberOfClasses + numberOfProperties + numberOfNamedIndividuals;
	}
	
	public RationalNumber getTermsWithDescription() {
		return RationalNumber.combine(classesWithDescription, propertiesWithDescription, namedIndividualsWithDescription);
	}
	
}
