package io.konig.datagen;

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
