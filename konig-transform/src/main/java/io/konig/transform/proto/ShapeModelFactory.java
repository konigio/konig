package io.konig.transform.proto;

/*
 * #%L
 * Konig Transform
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


import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.Path;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.shacl.PredicatePath;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeFilter;
import io.konig.shacl.ShapeManager;
import io.konig.transform.ShapeTransformException;

public class ShapeModelFactory {

	private ShapeManager shapeManager;
	private ShapeFilter shapeFilter;
	
	
	public ShapeModelFactory(ShapeManager shapeManager, ShapeFilter shapeFilter) {
		this.shapeManager = shapeManager;
		this.shapeFilter = shapeFilter;
	}

	public ShapeModel createShapeModel(Shape targetShape) throws ShapeTransformException {
		
		URI targetClass = targetShape.getTargetClass();
		
		if (targetClass == null) {
			throw new ShapeTransformException("Target class must be defined for Shape " + targetShape.getId());
		}
		ShapeModel shapeModel = new ShapeModel();
		ClassModel classModel = new ClassModel(targetClass);
		classModel.setTargetShapeModel(shapeModel);
		
		shapeModel.setShape(targetShape);
		shapeModel.setClassModel(classModel);
		
		addProperties(shapeModel);
		
		
		
		
		return shapeModel;
	}

	private void addProperties(ShapeModel targetShapeModel) throws ShapeTransformException {
		Shape targetShape = targetShapeModel.getShape();
		ClassModel classModel = targetShapeModel.getClassModel();
		
		for (PropertyConstraint p : targetShape.getProperty()) {
			if (p.getPath() instanceof PredicatePath) {
				PredicatePath path = (PredicatePath) p.getPath();
				URI predicate = path.getPredicate();
				PropertyGroup group = classModel.producePropertyGroup(predicate);
				
				PropertyModel propertyModel = new BasicPropertyModel(predicate, p, group);
				propertyModel.setDeclaringShape(targetShapeModel);
				group.setTargetProperty(propertyModel);
				group.add(propertyModel);
				
				targetShapeModel.add(propertyModel);
				
				Shape valueShape = p.getShape();
				if (valueShape != null) {
					ShapeModel valueShapeModel = createShapeModel(valueShape);
					group.setValueClassModel(valueShapeModel.getClassModel());
					valueShapeModel.setAccessor(propertyModel);
					propertyModel.setValueModel(valueShapeModel);
				}
				handleEquivalentPath(propertyModel);
			}
		}
		
		addSourceShapes(targetShapeModel);
		
	}

	private void handleEquivalentPath(PropertyModel propertyModel) throws ShapeTransformException {

		PropertyConstraint p = propertyModel.getPropertyConstraint();
		Path path = p.getEquivalentPath();
		
		
		if (path != null) {
			ShapeModel declaringShape = propertyModel.getDeclaringShape();
			ClassModel classModel = declaringShape.getClassModel();
			List<Step> stepList = path.asList();
			int last = stepList.size()-1;
			PropertyGroup group = null;
			for (int index=0; index<=last; index++) {
				Step step = stepList.get(index);
				if (step instanceof OutStep) {
					if (group != null) {
						classModel = group.produceValueClassModel(null);
					}
					OutStep out = (OutStep) step;
					URI predicate = out.getPredicate();
					group = classModel.getPropertyGroupByPredicate(predicate);
					if (group == null) {
						group = (index==last) ? propertyModel.getGroup() : classModel.producePropertyGroup(predicate);
					} else if (index==last) {
						group = merge(group, propertyModel.getGroup());
					}
					StepPropertyModel stepModel = new StepPropertyModel(predicate, p, group, index);
					stepModel.setDeclaringShape(declaringShape);
					group.add(stepModel);
					
				} else {
					throw new ShapeTransformException("Step type not supported: " + step.getClass().getSimpleName());
				}
			}
		}
		
	}


	private PropertyGroup merge(PropertyGroup a, PropertyGroup b) throws ShapeTransformException {
		
		// We are going to keep the PropertyGroup that has a well-defined target property.
		// Assuming that 'b' is the one to keep, we'll copy PropertyModel instances from 'a' to 'b'.
		
		// If it turns out that 'a' has a well-defined target property, then we need to swap 'a' and 'b'.
		
		if (a.getTargetProperty()!=null && b.getTargetProperty()==null) {
			PropertyGroup c = a;
			a = b;
			b = c;
		} else if (a.getTargetProperty()!=null && b.getTargetProperty()!=null) {
			throw new ShapeTransformException("Cannot merge PropertyGroups with conflicting target property");
		}
		
		for (PropertyModel p : a) {
			p.setGroup(b);
			b.add(p);
			URI predicate = p.getPredicate();
			ClassModel classModel = p.getDeclaringShape().getClassModel();
			classModel.put(predicate, b);
		}
		
		return b;
	}

	private void addSourceShapes(ShapeModel targetShapeModel) throws ShapeTransformException {
		
		Shape targetShape = targetShapeModel.getShape();
		ClassModel classModel = targetShapeModel.getClassModel();

		URI targetClass = targetShapeModel.getShape().getTargetClass();
		List<Shape> shapeList = shapeManager.getShapesByTargetClass(targetClass);
		
		for (Shape shape : shapeList) {
			if (shape != targetShape && (shapeFilter==null || shapeFilter.accept(shape))) {
				addSourceShape(classModel, shape);
			}
			
		}
		
		
	}

	private ShapeModel addSourceShape(ClassModel classModel, Shape sourceShape) throws ShapeTransformException {
		
		ShapeModel sourceShapeModel = new ShapeModel();
		classModel.getCandidateSourceShapeModel().add(sourceShapeModel);
		sourceShapeModel.setShape(sourceShape);
		sourceShapeModel.setClassModel(classModel);
		
		for (PropertyConstraint propertyConstraint : sourceShape.getProperty()) {
			
			if (propertyConstraint.getPath() instanceof PredicatePath) {
				PredicatePath path = (PredicatePath) propertyConstraint.getPath();
				URI predicate = path.getPredicate();
				PropertyGroup group = classModel.producePropertyGroup(predicate);
				
				PropertyModel propertyModel = new BasicPropertyModel(predicate, propertyConstraint, group);
				propertyModel.setDeclaringShape(sourceShapeModel);
				propertyModel.setGroup(group);
				group.add(propertyModel);
				
				handleEquivalentPath(propertyModel);
				
				Shape valueShape = propertyConstraint.getShape();
				if (valueShape != null) {
					URI valueClass = propertyConstraint.getValueClass() instanceof URI ? (URI) propertyConstraint.getValueClass() : null;
					ClassModel valueClassModel = group.produceValueClassModel(valueClass);
					ShapeModel valueShapeModel = addSourceShape(valueClassModel, valueShape);
					propertyModel.setValueModel(valueShapeModel);
				}
			}
		}
		
		return sourceShapeModel;
		
	}
}
