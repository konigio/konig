package io.konig.spreadsheet;

import java.util.List;

/*
 * #%L
 * Konig Spreadsheet
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


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.cadl.Attribute;
import io.konig.cadl.Cube;
import io.konig.cadl.CubeManager;
import io.konig.cadl.Dimension;
import io.konig.cadl.HasFormula;
import io.konig.cadl.Level;
import io.konig.cadl.Measure;
import io.konig.cadl.Variable;
import io.konig.core.LocalNameService;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.rio.turtle.NamespaceMap;

public class CubeSheet extends BaseSheetProcessor {
	private static SheetColumn CUBE_ID = new SheetColumn("Cube Id", true);
	private static SheetColumn STEREOTYPE = new SheetColumn("Stereotype", true);
	private static SheetColumn ELEMENT_NAME = new SheetColumn("Element Name");
	private static SheetColumn ELEMENT_TYPE = new SheetColumn("Element Type");
	private static SheetColumn FORMULA = new SheetColumn("Formula");
	private static SheetColumn ROLL_UP_FROM = new SheetColumn("Roll-up From");
	
	private static SheetColumn[] columns = new SheetColumn[] {
		CUBE_ID,
		STEREOTYPE,
		ELEMENT_NAME,
		ELEMENT_TYPE,
		FORMULA,
		ROLL_UP_FROM
	};
	
	private CubeManager cubeManager;
	
	private Cube cube;
	private Dimension dimension;
	private Level level;

	private DataSourceGeneratorFactory dataSourceGeneratorFactory;
	
	@SuppressWarnings("unchecked")
	public CubeSheet(WorkbookProcessor processor, DataSourceGeneratorFactory factory) {
		super(processor);
		dataSourceGeneratorFactory = factory;
		dependsOn(OntologySheet.class);
	}

	@Override
	public SheetColumn[] getColumns() {
		return columns;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
		init();
		URI cubeId = iriValue(row, CUBE_ID);
		if (cube == null || !cube.getId().equals(cubeId)) {
			cube = cubeManager.produceCube(cubeId);
		}
		
		
		String stereotypeName = stringValue(row, STEREOTYPE).toUpperCase();
		List<Function> dataSourceList = null;
		
		CadlKind kind = CadlKind.valueOf(stereotypeName);
		switch (kind) {
		
		case SOURCE :
			Variable source = new Variable();
			source.setId(sourceId(row, cubeId));
			source.setValueType(iriValue(row, ELEMENT_TYPE));
			cube.setSource(source);
			break;
			
		case STORAGE :
			dataSourceList = dataSourceList(row, ELEMENT_TYPE);
			break;

		case DIMENSION :
			dimension = new Dimension();
			dimension.setId(dimensionId(row, cubeId));
			cube.addDimension(dimension);
			level = null;
			break;
			
		case LEVEL :
			level = new Level();
			level.setId(levelId(row));
			handleFormula(cube, level, row);
			dimension.addLevel(level);
			break;
			
		case ATTRIBUTE :
			Attribute attr = new Attribute();
			attr.setId(attributeId(row));
			level.addAttribute(attr);
			break;
			
		case MEASURE :
			Measure measure = new Measure();
			measure.setId(measureId(row, cubeId));
			handleFormula(cube, measure, row);
			cube.addMeasure(measure);
			break;
		}
		
		String rollUpFrom = stringValue(row, ROLL_UP_FROM);
		if (rollUpFrom != null) {
			LevelRollUpAction action = processor.service(LevelRollUpAction.class);
			action.register(
				location(row, ROLL_UP_FROM), dimension, level, rollUpFrom);
		}
		
		if (dataSourceList != null) {
			processor.defer(
				new BuildCubeDataSourceAction(
					location(row, ELEMENT_TYPE),
					processor,
					dataSourceGeneratorFactory.getDataSourceGenerator(),
					cube,
					dataSourceList					
			));
		}

	}

	private void init() {
		if (cubeManager == null) {
			cubeManager = processor.service(CubeManager.class);
		}
		
	}

	private URI measureId(SheetRow row, URI cubeId) {
		String localName = stringValue(row, ELEMENT_NAME);
		return new URIImpl(cubeId.stringValue() + "/measure/" + localName);
	}

	private URI sourceId(SheetRow row, URI cubeId) {
		String localName = stringValue(row, ELEMENT_NAME);
		if (localName.startsWith("?")) {
			localName = localName.substring(1);
		}
		return new URIImpl(cubeId.stringValue() + "/source/" + localName);
	}

	private URI dimensionId(SheetRow row, URI cubeId) {
		String localName = stringValue(row, ELEMENT_NAME);
		return new URIImpl(cubeId.stringValue() + "/dimension/" + localName);
	}

	private URI attributeId(SheetRow row) {
		
		String localName = stringValue(row, ELEMENT_NAME);
		String baseIri = level.getId().stringValue();
		
		return new URIImpl(baseIri + "/attribute/" + localName);
	}

	private void handleFormula(Cube cube, HasFormula container, SheetRow row) {
		
		String formulaText = stringValue(row, FORMULA);
		if (formulaText != null) {
			ServiceManager sm = processor.getServiceManager();
			LocalNameService defaultService = sm.service(SimpleLocalNameService.class);
			CubeLocalNameService cubeNameService = new CubeLocalNameService(defaultService, cube);
			processor.defer(new CadlFormulaSetter(
				cubeNameService,
				sm.service(NamespaceMap.class),
				container,
				formulaText));
		}
		
	}

	private URI levelId(SheetRow row) {
		String localName = stringValue(row, ELEMENT_NAME);
		String baseIri = dimension.getId().stringValue();
		
		return new URIImpl( baseIri + "/level/" + localName);
	}

	private enum CadlKind {
		SOURCE,
		STORAGE,
		DIMENSION,
		LEVEL,
		ATTRIBUTE, 
		MEASURE
	}




}
