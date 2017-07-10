package io.konig.datagen;

/*
 * #%L
 * Konig Schema Generator
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


import java.util.ArrayList;
import java.util.List;

import io.konig.annotation.RdfProperty;

public class DataGeneratorConfig {
	private List<ShapeConstraints> shapeConfig = new ArrayList<>();
	private List<ClassConstraints> classConstraint = new ArrayList<>();
	
	@RdfProperty("http://www.konig.io/ns/core/generate")
	public void addShapeConfig(ShapeConstraints config) {
		shapeConfig.add(config);
	}
	
	public List<ShapeConstraints> getShapeConfigList() {
		return shapeConfig;
	}
	
	public void addClassConstraints(ClassConstraints constraint) {
		classConstraint.add(constraint);
	}
	
	public List<ClassConstraints> getClassConstraintsList() {
		return classConstraint;
	}
}
