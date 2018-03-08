package io.konig.schemagen.aws;

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


import java.io.File;

import org.openrdf.model.URI;

import io.konig.aws.datasource.AwsAurora;
import io.konig.core.io.ShapeFileFactory;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public class AWSAuroraShapeFileCreator implements ShapeFileFactory {
	private File baseDir;	
	
	public AWSAuroraShapeFileCreator(File baseDir){
		this.baseDir = baseDir;		
	}
	
	@Override
	public File createFile(Shape shape) {
		baseDir.mkdir();
		//URI shapeURI = (URI) shape.getId();
		//String localName = shapeURI.getLocalName();
		String awsAuroraSchema="${awsAuroraSchema}";
		String awsAuroraTable="${awsTableName}";
		for(DataSource ds:shape.getShapeDataSource()){
			if(ds instanceof AwsAurora){
				AwsAurora awsAurora=(AwsAurora) ds;
				if(awsAurora.getTableReference()!=null){
					awsAuroraSchema=awsAurora.getTableReference().getAwsSchema();
					awsAuroraTable=awsAurora.getTableReference().getAwsTableName();
				}
				break;	
			}
		}
		StringBuilder builder = new StringBuilder();
		builder.append(awsAuroraSchema);
		builder.append("_");
		builder.append(awsAuroraTable);
		builder.append(".sql");			
		return new File(baseDir, builder.toString());
	}
}
