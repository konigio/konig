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


import io.konig.aws.datasource.AwsAurora;
import io.konig.core.vocab.Konig;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SqlTableGeneratorUtil {
	public static boolean isValidRdbmsShape(Shape rdbmsShape) {
		AwsAurora auroraTable = rdbmsShape.findDataSource(AwsAurora.class);
		GoogleCloudSqlTable gcpSqlTable = rdbmsShape.findDataSource(GoogleCloudSqlTable.class);
		if (auroraTable !=null || gcpSqlTable != null){
			return true;
		}
		return false;
	}
	public static SqlKeyType getKeyType(PropertyConstraint p) throws SchemaGeneratorException {
		SqlKeyType keyType = null;

		if (p.getStereotype() != null) {

			if (p.getStereotype().equals(Konig.primaryKey)) {
				
				keyType = SqlKeyType.PRIMARY_KEY;	
			}
			else if (p.getStereotype().equals(Konig.syntheticKey)) {
				
				keyType = SqlKeyType.SYNTHETIC_KEY;	
			}
			else if(p.getStereotype().equals(Konig.uniqueKey)){
				
				keyType = SqlKeyType.UNIQUE_KEY;
				
			}
		}

		return keyType;
	}
}
