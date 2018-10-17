package io.konig.schemagen.sql;

/*
 * #%L
 * Konig Schema Generator
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


import java.text.MessageFormat;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;
import io.konig.formula.FormulaBuilder.BasicPathBuilder;
import io.konig.formula.FormulaBuilder.PathBuilder;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SyntheticKeyLinkingStrategy implements TabularLinkingStrategy {

	private String tabularFieldNamespace;
	
	public SyntheticKeyLinkingStrategy(String tabularPropertyNamespace) {
		this.tabularFieldNamespace = tabularPropertyNamespace;
	}
	
	private URI primaryKey(URI targetClass) {
		StringBuilder id = new StringBuilder();
		id.append(tabularFieldNamespace);
		id.append(StringUtil.SNAKE_CASE(targetClass.getLocalName()));
		id.append("_PK");
		
		URI predicate = new URIImpl(id.toString());
		return predicate;
	}

	@Override
	public void addPrimaryKey(Shape tabularShape) throws TabularShapeException {
		
		PropertyConstraint p = originKeyClone(tabularShape);
		if (p == null) {
			URI targetClass = tabularShape.getTargetClass();
			if (targetClass == null) {
				throw new TabularShapeException("targetClass must be defined on shape " + tabularShape.getId());
			}
			
			URI predicate = primaryKey(targetClass);
			p = new PropertyConstraint(predicate);
			
			p.setMinCount(1);
			p.setMaxCount(1);
			p.setDatatype(XMLSchema.LONG);
			p.setStereotype(Konig.syntheticKey);
		}
		
		tabularShape.add(p);
		
	}

	private PropertyConstraint originKeyClone(Shape tabularShape) {
		Shape origin = tabularShape.getTabularOriginShape();
		if (origin != null)  {
			for (PropertyConstraint p : origin.getProperty()) {
				if (p.getStereotype() == Konig.syntheticKey || p.getStereotype() == Konig.primaryKey) {
					PropertyConstraint pc = p.deepClone();
					String localName = p.getPredicate().getLocalName();
					String snakeCase = StringUtil.SNAKE_CASE(localName);
					if (!snakeCase.equals(localName)) {
						URI predicate = new URIImpl(tabularFieldNamespace + snakeCase);
						pc.setPredicate(predicate);
						BasicPathBuilder path = new BasicPathBuilder();
						path.out(p.getPredicate());
						pc.setFormula(path.build());
					}
					pc.setMinCount(1);
					pc.setMaxCount(1);
					return pc;
				}
			}
		}
		return null;
	}

	@Override
	public void addSingleIriReference(Shape tabularShape, PropertyConstraint originProperty)
			throws TabularShapeException {
		
		URI predicate = originProperty.getPredicate();
		String snakeCaseName = StringUtil.SNAKE_CASE(predicate.getLocalName()) + "_FK";
		URI aliasPredicate = new URIImpl(tabularFieldNamespace + snakeCaseName);
		PropertyConstraint p = new PropertyConstraint(aliasPredicate);
		p.setMinCount(originProperty.getMinCount());
		p.setMaxCount(originProperty.getMaxCount());
		p.setDatatype(XMLSchema.LONG);
		StringBuilder description = new StringBuilder();
		description.append("A foreign key reference to the entity referenced by the '");
		description.append(predicate.getLocalName());
		description.append("'");
		if (originProperty.getComment() != null) {
			description.append(" which is described as follows... ");
			description.append(originProperty.getComment());
		}
		p.setComment(description.toString());
		
		Resource valueClass = originProperty.getValueClass();
		if (!(valueClass instanceof URI)) {
			String msg = MessageFormat.format(
					"An IRI value must be defined for the sh:class of the {0} property in shape {1}", 
					predicate.getLocalName(), tabularShape.getTabularOriginShape().getId());
			throw new TabularShapeException(msg);
		}
		
		URI pk = primaryKey((URI)valueClass);
		BasicPathBuilder formula = new BasicPathBuilder();
		p.setFormula(formula.out(RDF.SUBJECT).out(originProperty.getPredicate()).out(pk).build());
		
		tabularShape.add(p);
	}

	@Override
	public void addAssociationSubject(Shape associationShape, PropertyConstraint originProperty)
			throws TabularShapeException {
		
		Resource subjectClass = associationShape.getDerivedPropertyByPredicate(RDF.SUBJECT).getValueClass();
		if (!(subjectClass instanceof URI)) {
			throw new TabularShapeException("sh:class of rdf:subject must be defined");
		}
		URI subjectClassId = (URI) subjectClass;
		URI subjectPk = primaryKey(subjectClassId);
		
		URI subjectFk = foreignKey(subjectClassId);
		
		PropertyConstraint p = new PropertyConstraint(subjectFk);
		p.setMinCount(1);
		p.setMaxCount(1);
		p.setDatatype(XMLSchema.LONG);
		
		StringBuilder description = new StringBuilder();
		description.append("A foreign key reference to the subject of the '");
		description.append(originProperty.getPredicate().getLocalName());
		description.append("' property");
		
		p.setComment(description.toString());
		
		p.setFormula(new BasicPathBuilder().out(RDF.SUBJECT).out(subjectPk).build());
		
		associationShape.add(p);
		
		
		
	}

	private URI foreignKey(URI subjectClassId) {
		StringBuilder builder = new StringBuilder(tabularFieldNamespace);
		builder.append(StringUtil.SNAKE_CASE(subjectClassId.getLocalName()));
		builder.append("_FK");
		return new URIImpl(builder.toString());
	}

	@Override
	public void addAssociationObject(Shape associationShape, PropertyConstraint originProperty)
			throws TabularShapeException {
		
		Resource objectClass = associationShape.getDerivedPropertyByPredicate(RDF.OBJECT).getValueClass();
		if (!(objectClass instanceof URI)) {
			throw new TabularShapeException("sh:class of rdf:object must be defined");
		}
		URI objectClassId = (URI) objectClass;
		URI objectPk = primaryKey(objectClassId);
		
		URI objectFk = foreignKey(objectClassId);
		
		PropertyConstraint p = new PropertyConstraint(objectFk);
		p.setMinCount(1);
		p.setMaxCount(1);
		p.setDatatype(XMLSchema.LONG);
		
		StringBuilder description = new StringBuilder();
		description.append("A foreign key reference to the object of the '");
		description.append(originProperty.getPredicate().getLocalName());
		description.append("' property");
		
		p.setComment(description.toString());
		
		p.setFormula(new BasicPathBuilder().out(RDF.OBJECT).out(objectPk).build());
		
		associationShape.add(p);
		
	}

}
