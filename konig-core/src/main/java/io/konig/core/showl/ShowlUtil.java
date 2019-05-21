package io.konig.core.showl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

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

public class ShowlUtil {

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
}
