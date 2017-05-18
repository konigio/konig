package io.konig.transform.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.util.TurtleElements;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.transform.rule.AlphabeticVariableNamer;
import io.konig.transform.rule.ContainerPropertyRule;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.ExactMatchPropertyRule;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RenamePropertyRule;
import io.konig.transform.rule.ShapeRule;

public class ShapeRuleFactory {
	
	private ShapeManager shapeManager;
	
	public ShapeRuleFactory(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}


	public ShapeRule createShapeRule(Shape targetShape) throws TransformBuildException {
		
		Worker worker = new Worker();
		return worker.build(targetShape);
	}

	
	private class Worker {

		public ShapeRule build(Shape targetShape) throws TransformBuildException {
			
			URI targetClass = targetShape.getTargetClass();
			if (targetClass == null) {
				throw new TransformBuildException("Target class must be defined for shape " + TurtleElements.resource(targetShape.getId()));
			}
			
			TargetShape target = TargetShape.create(targetShape);
			
			List<SourceShape> sourceList = createSourceShapes(target, targetClass);
			
			int expectedPropertyCount = target.totalPropertyCount();
			int currentPropertyCount = 0;
			int priorPropertyCount = -1;
			
			while (
				currentPropertyCount<expectedPropertyCount && 
				currentPropertyCount!=priorPropertyCount &&
				!sourceList.isEmpty()
			) {
				priorPropertyCount = currentPropertyCount;
				
				SourceShape bestSource = selectBest(sourceList);
				target.commit(bestSource);
				
				currentPropertyCount = target.mappedPropertyCount();
			}
			
			if (currentPropertyCount != expectedPropertyCount) {
				throw new TransformBuildException( unmappedPropertyMessage(target) );
			}

			createDataChannels(target);
			return assemble(target);
			
		}

		private ShapeRule assemble(TargetShape target) {
		
			ShapeRule shapeRule = new ShapeRule(target.getShape());
			
			for (TargetProperty tp : target.getProperties()) {
				if (tp.isDirectProperty()) {
					PropertyRule propertyRule = createPropertyRule(tp);
					shapeRule.addPropertyRule(propertyRule);
				}
			}
			
			return shapeRule;
		}

		private void createDataChannels(TargetShape target) {
			
			AlphabeticVariableNamer namer = new AlphabeticVariableNamer();
			for (SourceShape ss : target.getSourceList()) {
				String name = namer.next();
				DataChannel channel = new DataChannel(name, ss.getShape());
				ss.setDataChannel(channel);
			}
			
		}

		private PropertyRule createPropertyRule(TargetProperty tp) {
			SourceProperty sp = tp.getPreferredMatch();
			DataChannel channel = sp.getParent().getDataChannel();
			
			
			if (tp.getNestedShape() != null) {
				ContainerPropertyRule rule = new ContainerPropertyRule(tp.getPredicate(), channel);
				ShapeRule nestedRule = assemble(tp.getNestedShape());
				rule.setNestedRule(nestedRule);
				
				return rule;
				
			}
			
			URI predicate = tp.getPredicate();
			
			int pathIndex = sp.getPathIndex();
			if (pathIndex<0) {
				return new ExactMatchPropertyRule(channel, predicate);
			}
			
			return new RenamePropertyRule(predicate, channel, sp.getPropertyConstraint(), sp.getPathIndex());
		}

		private String unmappedPropertyMessage(TargetShape target) {
			List<TargetProperty> unmapped = target.getUnmappedProperties();
			StringBuilder builder = new StringBuilder();
			
			builder.append("Failed to produce transform for Shape ");
			builder.append(TurtleElements.resource(target.getShape().getId()));
			builder.append("\n   Could not find a mapping for the following properties: ");
			for (TargetProperty tp : unmapped) {
				builder.append("\n   ");
				TransformBuildUtil.appendSimplePath(builder, tp);
			}
			return builder.toString();
		}

		private SourceShape selectBest(List<SourceShape> sourceList) {
			
			if (sourceList.size()==1) {
				return sourceList.remove(0);
			}
			
			int bestCount = 0;
			SourceShape bestShape = null;
			Iterator<SourceShape> sequence = sourceList.iterator();
			while (sequence.hasNext()) {
				SourceShape candidate = sequence.next();
				int count = candidate.potentialMatchCount();
				if (count > bestCount) {
					bestCount = count;
					bestShape = candidate;
					sequence.remove();
				}
			}
			
			return bestShape;
		}

		private List<SourceShape> createSourceShapes(TargetShape target, URI targetClass) {
			Shape targetShape = target.getShape();
			List<Shape> sourceList = shapeManager.getShapesByTargetClass(targetClass);
			
			List<SourceShape> result = new ArrayList<>();
			for (Shape sourceShape : sourceList) {
				if (sourceShape == targetShape) {
					continue;
				}
				SourceShape source = SourceShape.create(sourceShape);
				target.match(source);
				
				result.add(source);
			}
			return result;
		}
	}

}
