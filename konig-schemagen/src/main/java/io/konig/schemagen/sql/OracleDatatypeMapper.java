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


import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.vocab.SH;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;

public class OracleDatatypeMapper extends SqlDatatypeMapper {
	
	public FacetedSqlDatatype type(PropertyConstraint c) {
		
		URI datatype = c.getDatatype();
		
		if (datatype != null) {
			
			if (owlReasoner.isIntegerDatatype(datatype)) {
				
				int precision = precision(c);
				if (precision>0) {
					// (min, max) range is declared so use the corresponding precision.
					return new NumericSqlDatatype(SqlDatatype.NUMBER, precision, 0);
				}
				
				if( datatype.equals(XMLSchema.BYTE) || datatype.equals(XMLSchema.UNSIGNED_BYTE)) {
					return new NumericSqlDatatype(SqlDatatype.NUMBER, 3, 0);
				} else if(datatype.equals(XMLSchema.SHORT) || datatype.equals(XMLSchema.UNSIGNED_SHORT)) {
					return new NumericSqlDatatype(SqlDatatype.NUMBER, 5, 0);
				}  else if(datatype.equals(XMLSchema.INT) || datatype.equals(XMLSchema.UNSIGNED_INT) ) {
					return new NumericSqlDatatype(SqlDatatype.NUMBER, 10, 0);
				} else {
					return new NumericSqlDatatype(SqlDatatype.NUMBER, 19, 0);
				}
			}
			
			if (datatype.equals(XMLSchema.FLOAT)) {
				return new NumericSqlDatatype(SqlDatatype.FLOAT, 126);
			} 
			if (datatype.equals(XMLSchema.DOUBLE)) {
				return new NumericSqlDatatype(SqlDatatype.FLOAT, 126);
			} 
			if (owlReasoner.isRealNumber(datatype)) {
				return new NumericSqlDatatype(SqlDatatype.FLOAT, 63);
			}
			if (XMLSchema.STRING.equals(datatype)) {
				Integer maxLength = c.getMaxLength();
				Integer minLength = c.getMinLength();
				if (maxLength == null) {
					return new StringSqlDatatype(SqlDatatype.VARCHAR2, 4000);
				}
				if (minLength!=null && maxLength<=2000 && minLength.equals(maxLength)) {
					return new StringSqlDatatype(SqlDatatype.CHAR, maxLength);
				}
				if (maxLength!=null && maxLength<=4000) {
					return new StringSqlDatatype(SqlDatatype.VARCHAR2, maxLength);
				}
				
				return new StringSqlDatatype(SqlDatatype.CLOB);
			}
			
			if (XMLSchema.DATE.equals(datatype) ) {
				return FacetedSqlDatatype.DATE;
			}
			
			if (XMLSchema.DATETIME.equals(datatype)) {
				return FacetedSqlDatatype.TIMESTAMP;
			}
		}
		
		if (c.getNodeKind() == NodeKind.IRI || SH.IRI.equals(datatype) || XMLSchema.ANYURI.equals(datatype)) {
			return FacetedSqlDatatype.IRI;
		}
		
		throw new SchemaGeneratorException("Unsupported datatype for predicate: " + c.getPredicate());
	}

	private int precision(PropertyConstraint c) {
		Double min = c.getMinInclusive();
		if (min==null) {
			min = c.getMinExclusive();
		}
		
		Double max = c.getMaxInclusive();
		if (max == null) {
			max = c.getMaxExclusive();
		}
		
		return Math.max(precision(min), precision(max));
	}

	private int precision(Double value) {
		if (value ==null) {
			return 0;
		}
		long v = value.longValue();
		if (v<0) {
			v = -1*v;
		}
		
		String text = Long.toString(v);
		
		return text.length();
	}

}
