package io.konig.schemagen.sql;

/*
 * #%L
 * Konig Schema Generator
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


import java.io.IOException;
import java.text.MessageFormat;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFParseException;

import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;
import io.konig.formula.FormulaParser;
import io.konig.formula.FormulaBuilder.BasicPathBuilder;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class KonigIdLinkingStrategy implements TabularLinkingStrategy {
	
	private URI idPredicate;
	private String tabularFieldNamespace;

	public KonigIdLinkingStrategy(URI idPredicate, String tabularFieldNamespace) {
		this.idPredicate = idPredicate;
		this.tabularFieldNamespace = tabularFieldNamespace;
	}

	@Override
	public void addPrimaryKey(Shape tabularShape) throws TabularShapeException {
		if (tabularShape.getPropertyConstraint(idPredicate) == null) {

			PropertyConstraint p = new PropertyConstraint(idPredicate);
			p.setMinCount(1);
			p.setMaxCount(1);
			p.setDatatype(XMLSchema.STRING);
			p.setStereotype(Konig.primaryKey);
			BasicPathBuilder formula = new BasicPathBuilder();
			p.setFormula(formula.out(Konig.id).build());		
			tabularShape.add(p);
			tabularShape.setNodeKind(null);	
		}
		

	}

	@Override
	public void addSingleIriReference(Shape tabularShape, PropertyConstraint originProperty)
			throws TabularShapeException {
		URI predicate = originProperty.getPredicate();
		String snakeCaseName = StringUtil.SNAKE_CASE(predicate.getLocalName()) + "_ID";
		URI aliasPredicate = new URIImpl(tabularFieldNamespace + snakeCaseName);
		PropertyConstraint p = new PropertyConstraint(aliasPredicate);
		p.setMinCount(originProperty.getMinCount());
		p.setMaxCount(originProperty.getMaxCount());
		p.setDatatype(XMLSchema.STRING);
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
	
	private URI primaryKey(URI targetClass) {
		return idPredicate;
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
	

	private URI foreignKey(URI subjectClassId) {
		StringBuilder builder = new StringBuilder(tabularFieldNamespace);
		builder.append(StringUtil.SNAKE_CASE(subjectClassId.getLocalName()));
		builder.append("_ID");
		return new URIImpl(builder.toString());
	}

}
