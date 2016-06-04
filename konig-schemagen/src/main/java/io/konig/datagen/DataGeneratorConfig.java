package io.konig.datagen;

import java.util.ArrayList;
import java.util.List;

import io.konig.annotation.RdfProperty;

public class DataGeneratorConfig {
	private List<ShapeConfig> shapeConfig = new ArrayList<>();
	
	@RdfProperty("http://www.konig.io/ns/datagen/generate")
	public void addShapeConfig(ShapeConfig config) {
		shapeConfig.add(config);
	}
	
	public List<ShapeConfig> getShapeConfigList() {
		return shapeConfig;
	}
	

}
