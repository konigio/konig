package io.konig.schemagen.sql;

/*
 * #%L
 * Konig Schema Generator
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


import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;
import io.konig.core.vocab.SH;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;

public class SqlDatatypeMapper
{
	
	public OwlReasoner owlReasoner = new OwlReasoner(null);

	public FacetedSqlDatatype type(PropertyConstraint c) {
		
		URI datatype = c.getDatatype();
		
		if (datatype != null) {
			
			if (datatype.equals(XMLSchema.BOOLEAN)) {
				return FacetedSqlDatatype.BOOLEAN;
			}

			if (datatype.equals(XMLSchema.FLOAT)) {
				
				return isSigned(c) ? FacetedSqlDatatype.SIGNED_FLOAT : FacetedSqlDatatype.UNSIGNED_FLOAT;
			}
			
			if (owlReasoner.isRealNumber(datatype)) {
				return isSigned(c) ? FacetedSqlDatatype.SIGNED_DOUBLE : FacetedSqlDatatype.UNSIGNED_DOUBLE;
			}
			
			if (owlReasoner.isIntegerDatatype(datatype)) {
				Long min = integerMin(c);
				Long max = integerMax(c);
				
				
				boolean signed = min==null || min<0;
				
				return 
					(!signed && inRange(0, 64, c)) ? FacetedSqlDatatype.BIT :
					(signed && inRange(-128, 127, c)) ? FacetedSqlDatatype.SIGNED_TINYINT :
					(!signed && inRange(0, 255, c)) ? FacetedSqlDatatype.UNSIGNED_TINYINT :
					(signed && inRange(-32768, 32767, c)) ? FacetedSqlDatatype.SIGNED_SMALLINT :
					(!signed && inRange(0,65535, c)) ? FacetedSqlDatatype.UNSIGNED_SMALLINT :
					(signed && inRange(-8388608, 8388607, c)) ? FacetedSqlDatatype.SIGNED_MEDIUMINT :
					(!signed && inRange(0, 16777215, c)) ? FacetedSqlDatatype.UNSIGNED_MEDIUMINT :
					(signed && (max==null || inRange(-2147483648, 2147483647, c))) ? FacetedSqlDatatype.SIGNED_INT :
					(!signed && (max==null || inRange(0, 4294967295L, c))) ? FacetedSqlDatatype.UNSIGNED_INT :
					(signed) ? FacetedSqlDatatype.SIGNED_BIGINT : FacetedSqlDatatype.UNSIGNED_BIGINT;
			}
			
			if (XMLSchema.STRING.equals(datatype)) {
				Integer maxLength = c.getMaxLength();
				Integer minLength = c.getMinLength();
				if (maxLength == null) {
					return new StringSqlDatatype(SqlDatatype.VARCHAR, 65535);
				}
				if (minLength!=null && maxLength<256 && minLength.equals(maxLength)) {
					return new StringSqlDatatype(SqlDatatype.CHAR, maxLength);
				}
				if (maxLength!=null && maxLength<=65535) {
					return new StringSqlDatatype(SqlDatatype.VARCHAR, maxLength);
				}
				
				return new StringSqlDatatype(SqlDatatype.TEXT, maxLength);
			}
			
			if (XMLSchema.DATE.equals(datatype) ) {
				return FacetedSqlDatatype.DATE;
			}
			
			if (XMLSchema.DATETIME.equals(datatype)) {
				return FacetedSqlDatatype.DATETIME;
			}
		}
		
		if (c.getNodeKind() == NodeKind.IRI || SH.IRI.equals(datatype) || XMLSchema.ANYURI.equals(datatype)) {
			return FacetedSqlDatatype.IRI;
		}
		
		throw new SchemaGeneratorException("Unsupported datatype for predicate: " + c.getPredicate());
	}

	public boolean inRange(long min, long max, PropertyConstraint c) {
		Double minInclusive = c.getMinInclusive();
		Double maxInclusive = c.getMaxInclusive();
		Double minExclusive = c.getMinExclusive();
		Double maxExclusive = c.getMaxExclusive();
		
		return
			(
				(minInclusive!=null && minInclusive>=min) ||
				(minExclusive!=null && minExclusive>min)
			) && (
				(maxInclusive!=null && maxInclusive<=max) ||
				(maxExclusive!=null && maxExclusive<max)
			) ? true : false;
		
	}

	public Long integerMin(PropertyConstraint c) {
		Double doubleMin = minValue(c);
		return doubleMin==null ? null : new Long(doubleMin.longValue());
	}
	
	public Long integerMax(PropertyConstraint c) {
		Double doubleMax = maxValue(c);
		return doubleMax == null ? null : new Long(doubleMax.longValue());
	}

	public boolean isSigned(PropertyConstraint c) {
		Double minValue = minValue(c);
		return minValue==null || minValue<0;
	}

	public Double maxValue(PropertyConstraint c) {
		Double result = c.getMaxInclusive();
		if (result == null) {
			result = c.getMaxExclusive();
		}
		return result;
	}

	public Double minValue(PropertyConstraint c) {
		Double result = c.getMinInclusive();
		if (result == null) {
			result = c.getMinExclusive();
		}
		return result;
	}
}
