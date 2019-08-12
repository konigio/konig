package io.konig.core.showl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;

/*
 * #%L
 * Konig Core
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


import io.konig.core.vocab.Konig;
import io.konig.formula.KqlType;
import io.konig.shacl.PropertyConstraint;

public class ShowlUtil {
	public static final String ENUM_SHAPE_BASE_IRI = "urn:konig:enumShape/";

	public static boolean isUndefinedClass(ShowlClass owlClass) {
		
		return owlClass == null || Konig.Undefined.equals(owlClass.getId());
	}
	
	public static KqlType kqlType(URI rdfType) {
		if (
			XMLSchema.INT.equals(rdfType) ||
			XMLSchema.INTEGER.equals(rdfType) ||
			XMLSchema.LONG.equals(rdfType) ||
			XMLSchema.SHORT.equals(rdfType) ||
			XMLSchema.BYTE.equals(rdfType) ||
			XMLSchema.NON_POSITIVE_INTEGER.equals(rdfType) ||
			XMLSchema.NON_NEGATIVE_INTEGER.equals(rdfType) ||
			XMLSchema.NEGATIVE_INTEGER.equals(rdfType) ||
			XMLSchema.UNSIGNED_BYTE.equals(rdfType) ||
			XMLSchema.UNSIGNED_BYTE.equals(rdfType) ||
			XMLSchema.UNSIGNED_LONG.equals(rdfType) ||
			XMLSchema.UNSIGNED_SHORT.equals(rdfType) ||
			XMLSchema.UNSIGNED_BYTE.equals(rdfType)
		) {
			return KqlType.INTEGER;
		}
			
		if (
			XMLSchema.DECIMAL.equals(rdfType) ||
			XMLSchema.FLOAT.equals(rdfType) ||
			XMLSchema.DOUBLE.equals(rdfType)
		) {
			return KqlType.NUMBER;
		}
		
		if (
			XMLSchema.DATETIME.equals(rdfType) ||
			XMLSchema.DATE.equals(rdfType) ||
			XMLSchema.GYEAR.equals(rdfType) ||
			XMLSchema.GYEARMONTH.equals(rdfType)
		) {
			return KqlType.INSTANT;
		}

		if (
			XMLSchema.BASE64BINARY.equals(rdfType) ||
			XMLSchema.HEXBINARY.equals(rdfType) ||
			XMLSchema.ANYURI.equals(rdfType) ||
			XMLSchema.NOTATION.equals(rdfType) ||
			XMLSchema.STRING.equals(rdfType) ||
			XMLSchema.NORMALIZEDSTRING.equals(rdfType) ||
			XMLSchema.TOKEN.equals(rdfType) ||
			XMLSchema.LANGUAGE.equals(rdfType) ||
			XMLSchema.NAME.equals(rdfType) ||
			XMLSchema.NMTOKEN.equals(rdfType) ||
			XMLSchema.NCNAME.equals(rdfType) ||
			XMLSchema.NMTOKENS.equals(rdfType) ||
			XMLSchema.ID.equals(rdfType) ||
			XMLSchema.IDREF.equals(rdfType) ||
			XMLSchema.ENTITY.equals(rdfType) ||
			XMLSchema.QNAME.equals(rdfType)
		) {
			return KqlType.STRING;
		}
		return null;
	}
	
	/**
	 * Determine whether a given property has a well-defined value.
	 * A value is well-defined if one of the following conditions is satisfied:
	 * <ol>
	 *   <li> The property is direct and has no nested shape.
	 *   <li> The property has a direct synonym without a nested shape
	 *   <li> The property is derived from a formula where all the parameters are well-defined.
	 *   <li> The property has a selected formula where all the parameters are well-defined.
	 * </ol>
	 * @return
	 */
	public static boolean isWellDefined(ShowlPropertyShape p) {
		if (p.isDirect() && p.getValueShape()==null) {
			return true;
		}
		
		ShowlPropertyShape synonym = p.getSynonym();
		if (synonym!=null && synonym.isDirect() && synonym.getValueShape()==null) {
			return true;
		}
		
		if (isWellDefined(p.getFormula()) || isWellDefined(p.getSelectedExpression())) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Determine whether a given expression is well defined.
	 * An expression is well defined if all of its parameters are well defined.
	 */
	public static boolean isWellDefined(ShowlExpression e) {
		if (e == null) {
			return false;
		}
		for (ShowlPropertyShape p : ShowlExpression.parameters(e)) {
			if (!isWellDefined(p)) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Compute the path of a relative to b.
	 * @param a
	 * @param b
	 * @return
	 */
	public static List<URI> relativePath(ShowlNodeShape a, ShowlNodeShape b) {
		List<URI> result = new ArrayList<>();
		if (a == b) {
			return result;
		}
		ShowlPropertyShape p = a.getAccessor();
		while (p!=null) {
			result.add(p.getPredicate());
			a = p.getDeclaringShape();
			if (a == b) {
				Collections.reverse(result);
				return result;
			}
			p = a.getAccessor();
			
		}
		
		return null;
	}

	/**
	 * Determine whether a given node is well-defined.
	 * A node is well-defined if each of it's direct properties is well defined, recursively.
	 */
	public static boolean isWellDefined(ShowlNodeShape node) {
		
		for (ShowlDirectPropertyShape direct : node.getProperties()) {
			if (direct.getValueShape() != null) {
				if (!isWellDefined(direct.getValueShape())) {
					return false;
				}
			} else if (!isWellDefined(direct)) {
				return false;
			}
		}
		
		return true;
	}

	public static ShowlPropertyExpression propertyExpression(ShowlPropertyShape p) {
		
		return p instanceof ShowlDirectPropertyShape ?
				new ShowlDirectPropertyExpression((ShowlDirectPropertyShape)p) :
				new ShowlDerivedPropertyExpression((ShowlDerivedPropertyShape)p);
	}

	public static ShowlChannel channelFor(ShowlNodeShape enumNode, List<ShowlChannel> channelList) {
		for (ShowlChannel channel : channelList) {
			if (channel.getSourceNode() == enumNode) {
				return channel;
			}
		}
		return null;
	}
	
	
//	public static ShowlEnumJoinInfo enumJoinInfo(ShowlNodeShape enumNode)
	

	public static ShowlPropertyShape otherProperty(ShowlEqualStatement equal, ShowlNodeShape node) {
		ShowlPropertyShape left = propertyShape(equal.getLeft());
		ShowlPropertyShape right = propertyShape(equal.getRight());
		
		if (left != null && left.getDeclaringShape()==node) {
			return right;
		}
		
		if (right!=null && right.getDeclaringShape()!=node) {
			return left;
		}
		
		return null;
	}
	
	public static ShowlPropertyShape propertyOf(ShowlEqualStatement equal, ShowlNodeShape node) {

		ShowlPropertyShape left = propertyShape(equal.getLeft());
		
		if (left != null && left.getDeclaringShape()==node) {
			return left;
		}

		ShowlPropertyShape right = propertyShape(equal.getRight());
		if (right!=null && right.getDeclaringShape()!=node) {
			return right;
		}
		
		return null;
	}

	private static ShowlPropertyShape propertyShape(ShowlExpression e) {
		if (e instanceof ShowlPropertyExpression) {
			return ((ShowlPropertyExpression) e).getSourceProperty();
		}
		return null;
	}

	public static ShowlPropertyShape propertyOf(ShowlExpression e, ShowlNodeShape node) {
		if (e instanceof ShowlPropertyExpression) {
			ShowlPropertyShape p = ((ShowlPropertyExpression) e).getSourceProperty();
			if (p.getDeclaringShape() == node) {
				return p;
			}
		}
		return null;
	}
	
	public static ShowlDirectPropertyShape propertyMappedTo(ShowlNodeShape targetNode, ShowlPropertyShape sourceProperty) {
		for (ShowlDirectPropertyShape direct : targetNode.getProperties()) {
			ShowlExpression e = direct.getSelectedExpression();
			ShowlPropertyShape p = propertyShape(e);
			if (p == sourceProperty) {
				return direct;
			}
		}
		return null;
	}

	public static ShowlPropertyShape asPropertyShape(ShowlExpression e) {
		if (e instanceof ShowlPropertyExpression) {
			ShowlPropertyExpression p = (ShowlPropertyExpression) e;
			return p.getSourceProperty();
		}
		return null;
	}

	public static boolean isEnumSourceNode(ShowlNodeShape sourceNode, OwlReasoner reasoner) {
		return !sourceNode.isTargetNode() && reasoner.isEnumerationClass(sourceNode.getOwlClass().getId());
	}
	
	public static String shortShapeName(URI shapeId) {
		String localName = shapeId.getLocalName();
		if (localName.endsWith("_Shape")) {
			return localName.substring(0, localName.length()-6);
		}
		if (localName.endsWith("Shape")) {
			return localName.substring(0, localName.length()-5);
		}
		return localName;
	}
	
	public static String shortShapeName(ShowlNodeShape node) {
		if (node == null) {
			System.err.println("Null value detected");
		}
		return shortShapeName(RdfUtil.uri(node.getId()));
	}

	public static ShowlExpression enumExpression(ShowlEqualStatement equal) {
		ShowlExpression left = equal.getLeft();
		if (isEnumExpression(left)) {
			return left;
		}
		ShowlExpression right = equal.getRight();
		if (isEnumExpression(right)) {
			return right;
		}
		return null;
	}

	private static boolean isEnumExpression(ShowlExpression e) {
		
		return e instanceof ShowlEnumPropertyExpression || e instanceof ShowlEnumIndividualReference;
	}
	
	/**
	 * Get the list of all enum properties referenced by a join condition within a given target NodeShape.
	 * @param enumClass The enum class of interest
	 * @param targetShape The target NodeShape to be scanned
	 * @return
	 */
	public static Set<ShowlPredicatePath> uniqueKeys(URI enumClass, ShowlNodeShape targetShape) {
		// Scan targetShape for join statements that involve the given enumClass.
		Set<ShowlPropertyShape> properties = new HashSet<>();
		for (ShowlChannel channel : targetShape.getChannels()) {
			
			ShowlStatement statement = channel.getJoinStatement();
			if (statement != null) {
				statement.addProperties(properties);
			}
		}
		
		Iterator<ShowlPropertyShape> sequence = properties.iterator();
		while (sequence.hasNext()) {
			ShowlPropertyShape p = sequence.next();
			ShowlNodeShape node = p.getDeclaringShape();
			if (node.getOwlClass().getId().equals(enumClass)) {
				if (!Konig.id.equals(p.getPredicate())) {
					continue;
				}
			}
			sequence.remove();
		}

		Set<ShowlPredicatePath> result = new HashSet<>();
		for (ShowlPropertyShape p : properties) {
			result.add(ShowlPredicatePath.forProperty(p));
		}
		
		return result;
	}

	public static boolean isEnumNode(ShowlExpression e) {
		
		return  e instanceof ShowlEnumNodeExpression || e instanceof ShowlEnumStructExpression ;
	}
	
	public static ShowlNodeShape parentEnumNode(ShowlPropertyShape p) {
		while (p!=null) {
			ShowlNodeShape node = p.getDeclaringShape();
			if (RdfUtil.uri(node.getId()).getNamespace().startsWith(ENUM_SHAPE_BASE_IRI)) {
				return node;
			}
			p = node.getAccessor();
		}
		return null;
	}

	public static boolean isEnumProperty(ShowlPropertyShape p) {
		while (p!=null) {
			ShowlNodeShape node = p.getDeclaringShape();
			if (RdfUtil.uri(node.getId()).getNamespace().startsWith(ENUM_SHAPE_BASE_IRI)) {
				return true;
			}
			p = node.getAccessor();
		}
		return false;
	}

	public static boolean isEnumField(ShowlExpression e) {
		return e instanceof ShowlEnumPropertyExpression;
	}

	public static boolean isEnumNode(ShowlNodeShape node) {
		return node.getShape().getId().stringValue().startsWith(ENUM_SHAPE_BASE_IRI);
	}

	public static ShowlNodeShape containingEnumNode(ShowlPropertyShape p, OwlReasoner owlReasoner) {
		while (p != null) {
			ShowlNodeShape node = p.getDeclaringShape();
			if (owlReasoner.isEnumerationClass(node.getOwlClass().getId())) {
				return node;
			}
			p = node.getAccessor();
		}
		return null;
	}

	public static boolean isUniqueKey(ShowlPropertyShape p, OwlReasoner reasoner) {
		if (Konig.id.equals(p.getPredicate())) {
			return true;
		}
		PropertyConstraint constraint = p.getPropertyConstraint();
		if (constraint != null) {
			URI stereotype = constraint.getStereotype();
			if (Konig.uniqueKey.equals(stereotype) || Konig.primaryKey.equals(stereotype)) {
				return true;
			}
		}
		
		return reasoner.isInverseFunctionalProperty(p.getPredicate());
	}

	public static List<ShowlExpression> transform(List<ShowlExpression> memberList) {
		List<ShowlExpression> result = new ArrayList<>();
		for (ShowlExpression e : memberList) {
			result.add(e.transform());
		}
		return result;
	}

	public static ShowlExpression transform(ShowlExpression e) {
		
		return e==null ? null : e.transform();
	}

	public static ShowlNodeShape parentEnumNode(ShowlPropertyShape p, OwlReasoner reasoner) {
		ShowlNodeShape result = parentEnumNode(p);
		if (result == null) {
			while (p != null) {
				ShowlNodeShape node = p.getDeclaringShape();
				URI owlClass = node.getOwlClass().getId();
				if (reasoner.isEnumerationClass(owlClass)) {
					return node;
				}
				p = node.getAccessor();
			}
		}
		return result;
	}

	public static ShowlNodeShape enumClassNode(ShowlNodeShape enumNode) {
		ShowlPropertyShape accessor = enumNode.getAccessor();
		if (accessor != null) {
			ShowlExpression s = accessor.getSelectedExpression();
			if (s instanceof ShowlEnumNodeExpression) {
				ShowlEnumNodeExpression e = (ShowlEnumNodeExpression) s;
				return e.getEnumNode();
			}
		}
		return null;
	}

	


	
}
