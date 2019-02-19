package io.konig.spreadsheet;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.cadl.Attribute;
import io.konig.cadl.Cube;
import io.konig.cadl.CubeManager;
import io.konig.cadl.Dimension;
import io.konig.cadl.HasFormula;
import io.konig.cadl.Level;
import io.konig.cadl.CubeReasoner;
import io.konig.cadl.Measure;
import io.konig.cadl.Variable;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.rio.turtle.NamespaceMap;

public class CubeSheet extends BaseSheetProcessor {
	private static SheetColumn CUBE_ID = new SheetColumn("Cube Id", true);
	private static SheetColumn STEREOTYPE = new SheetColumn("Stereotype", true);
	private static SheetColumn ELEMENT_NAME = new SheetColumn("Element Name", true);
	private static SheetColumn ELEMENT_TYPE = new SheetColumn("Element Type");
	private static SheetColumn FORMULA = new SheetColumn("Formula");
	
	private static SheetColumn[] columns = new SheetColumn[] {
		CUBE_ID,
		STEREOTYPE,
		ELEMENT_NAME,
		ELEMENT_TYPE,
		FORMULA
	};
	
	private CubeManager cubeManager;
	private CubeReasoner levelReasoner;
	
	private Cube cube;
	private Dimension dimension;
	private Level level;
	
	public CubeSheet(WorkbookProcessor processor) {
		super(processor);
		ServiceManager sm = processor.getServiceManager();
		cubeManager = sm.service(CubeManager.class);
		levelReasoner = sm.service(CubeReasoner.class);
	}

	@Override
	public SheetColumn[] getColumns() {
		return columns;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
		if (cubeManager != null) {
			URI cubeId = iriValue(row, CUBE_ID);
			if (cube == null || !cube.getId().equals(cubeId)) {
				cube = cubeManager.produceCube(cubeId);
				processor.defer(new LevelReasoningAction(levelReasoner, cube));
			}
			
			
			String stereotypeName = stringValue(row, STEREOTYPE).toUpperCase();
			
			CadlStereotype stereotype = CadlStereotype.valueOf(stereotypeName);
			switch (stereotype) {
			
			case SOURCE :
				Variable source = new Variable();
				source.setId(sourceId(row, cubeId));
				source.setValueType(iriValue(row, ELEMENT_TYPE));
				cube.setSource(source);
				break;

			case DIMENSION :
				dimension = new Dimension();
				dimension.setId(dimensionId(row, cubeId));
				cube.addDimension(dimension);
				break;
				
			case LEVEL :
				level = new Level();
				level.setId(levelId(row));
				handleFormula(level, row);
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
				handleFormula(measure, row);
				cube.addMeasure(measure);
				break;
			}
		}

	}

	private URI measureId(SheetRow row, URI cubeId) {
		String localName = stringValue(row, ELEMENT_NAME);
		return new URIImpl(cubeId.stringValue() + "/measure/" + localName);
	}

	private URI sourceId(SheetRow row, URI cubeId) {
		String localName = stringValue(row, ELEMENT_NAME);
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

	private void handleFormula(HasFormula container, SheetRow row) {
		
		String formulaText = stringValue(row, FORMULA);
		if (formulaText != null) {
			ServiceManager sm = processor.getServiceManager();
			processor.defer(new CadlFormulaSetter(
				sm.service(SimpleLocalNameService.class),
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

	private enum CadlStereotype {
		SOURCE,
		DIMENSION,
		LEVEL,
		ATTRIBUTE, MEASURE
	}

	private static class LevelReasoningAction implements Action {
		private CubeReasoner reasoner;
		private Cube cube;
		
		public LevelReasoningAction(CubeReasoner reasoner, Cube cube) {
			this.reasoner = reasoner;
			this.cube = cube;
		}

		@Override
		public void execute() throws SpreadsheetException {
			reasoner.visit(cube);
		}
	}



}