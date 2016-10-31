package io.konig.datagen;

import java.util.ArrayList;
import java.util.List;

import io.konig.annotation.RdfProperty;

public class DataGeneratorConfig {
	private List<ShapeConfig> shapeConfig = new ArrayList<>();
	private List<ClassConstraint> classConstraint = new ArrayList<>();
	
	@RdfProperty("http://www.konig.io/ns/core/generate")
	public void addShapeConfig(ShapeConfig config) {
		shapeConfig.add(config);
	}
	
	public List<ShapeConfig> getShapeConfigList() {
		return shapeConfig;
	}
	
	public void addClassConstraint(ClassConstraint constraint) {
		classConstraint.add(constraint);
	}
	
	public List<ClassConstraint> getClassConstraintList() {
		return classConstraint;
	}
}
