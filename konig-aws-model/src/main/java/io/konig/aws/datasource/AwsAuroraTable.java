package io.konig.aws.datasource;

import java.text.MessageFormat;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/*
 * #%L
 * Konig AWS Model
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


import io.konig.core.vocab.Konig;
import io.konig.datasource.TableDataSource;
import io.konig.shacl.Shape;

public class AwsAuroraTable extends AwsAurora {
	
	public AwsAuroraTable() {
		addType(Konig.AwsAuroraTable);
	}
	

	@Override
	public TableDataSource generateAssociationTable(Shape subjectShape, URI predicate) {
		
		AwsAuroraTable table = new AwsAuroraTable();
		AwsAuroraTableReference tableRef = getTableReference().clone();
	
		String tableName = associationTableName(subjectShape, predicate);
		tableRef.setAwsTableName(tableName);
		table.setAwsTableName(tableName);
		table.setTableReference(tableRef);
		
		String pattern =
			"http://www.konig.io/ns/aws/host/{0}/databases/{1}/tables/{2}";
		
		String awsAuroraHost = tableRef.getAwsAuroraHost();
		String awsAuroraSchema = tableRef.getAwsSchema();
		String idValue = MessageFormat.format(pattern, awsAuroraHost, awsAuroraSchema, tableName);
		table.setId(new URIImpl(idValue));
		
		return table;
	}
}
