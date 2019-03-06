package io.konig.cadl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Context;
import io.konig.core.Term;
import io.konig.core.impl.BasicContext;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class CubeShapeBuilder {

	private ShapeManager shapeManager;
	private String shapeNamespace;
	private URI datasourceType;
	
	public void buildShape(Cube cube) {
		URI shapeId = uri(shapeNamespace + cube.getId().getLocalName() + "RawShape");
		Shape shape = produceShape(shapeId);
		
		addVariable(shape, cube.getSource());
		
		for (Dimension dim : cube.getDimension()) {
			for (Level level : dim.getLevel()) {
				addLevel(shape, cube, dim, level);
			}
		}
	}

	private void addVariable(Shape shape, Variable source) {
		
		PropertyConstraint p = shape.getVariableById(source.getId());
		if (p == null) {
			p = new PropertyConstraint(source.getId());
			shape.addVariable(p);
		}
		
		p.setValueClass(source.getValueType());
		
		
		
	}

	private void addLevel(Shape shape, Cube cube, Dimension dim, Level level) {
		
		PropertyConstraint p = shape.getPropertyConstraint(level.getId());
		if (p == null) {
			p = new PropertyConstraint(level.getId());
			shape.add(p);
		}
		
		setLevelFormula(dim, level, p);
		
		if (!level.getAttribute().isEmpty()) {
			
			URI levelShapeId = uri(shapeNamespace + cube.getId().getLocalName() + "RawShape/level/" + level.getId().getLocalName() + "Shape");
			Shape levelShape = produceShape(levelShapeId);
			addAttributes(levelShape, level, p.getFormula());
			
		}
		
	}


	private void addAttributes(Shape shape, Level level, QuantifiedExpression levelFormula) {
		for (Attribute attr : level.getAttribute()) {
			PropertyConstraint p = shape.getPropertyConstraint(attr.getId());
			if (p == null) {
				p = new PropertyConstraint(attr.getId());
				shape.add(p);
			}
			
			QuantifiedExpression formula = attr.getFormula();
			// TODO: Compute default formula if necessary
			p.setFormula(formula);
			
		}
		
	}

	private Shape produceShape(URI shapeId) {
		Shape shape = shapeManager.getShapeById(shapeId);
		if (shape == null) {
			shape = new Shape(shapeId);
			shapeManager.addShape(shape);
		}
		return shape;
	}

	private void setLevelFormula(Dimension dim, Level level, PropertyConstraint p) {
		
		QuantifiedExpression levelFormula = level.getFormula();
		if (dim.getFormula() != null) {
			String levelFormulaText = levelFormula.toSimpleString();
			
			if (levelFormulaText.contains("$.") || levelFormulaText.contains("$^")) {
				
				String dimFormulaText = dim.getFormula().toSimpleString();
				levelFormulaText = levelFormulaText.replaceAll(
						Pattern.quote("$"), Matcher.quoteReplacement(dimFormulaText));
				
				Context context = new BasicContext("");
				copyContext(dim.getFormula().getContext(), context);
				copyContext(levelFormula.getContext(), context);
				
				StringBuilder builder = new StringBuilder();
				appendContext(builder, context);
				
				builder.append(levelFormulaText);
				String text = builder.toString();
				levelFormula = QuantifiedExpression.fromString(text);
			}
		}
		
		p.setFormula(levelFormula);
		
	}

	private void appendContext(StringBuilder builder, Context context) {
		context.sort();
		for (Term term : context.asList()) {
			switch (term.getKind()) {
			case NAMESPACE :
				builder.append("@prefix ");
				builder.append(term.getKey());
				builder.append(": <");
				builder.append(term.getId());
				builder.append("> .\n");
				break;
			default:
				builder.append("@term ");
				builder.append(term.getKey());
				builder.append(" : ");
				if (term.getExpandedId().stringValue().equals(term.getId())) {
					builder.append('<');
					builder.append(term.getId());
					builder.append(">\n");
				} else {
					builder.append(term.getId());
					builder.append("\n");
				}
			}
		}
		builder.append("\n");
		
		
	}

	private void copyContext(Context source, Context target) {
		for (Term term : source.asList()) {
			target.add(term);
		}
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
