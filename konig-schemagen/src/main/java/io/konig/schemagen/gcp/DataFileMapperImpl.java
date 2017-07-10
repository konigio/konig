package io.konig.schemagen.gcp;

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


import java.io.File;

import io.konig.core.Vertex;

public class DataFileMapperImpl implements DataFileMapper {
	
	private File outDir;
	private DatasetMapper datasetMapper;
	private BigQueryTableMapper tableMapper;
	
	
	
	public DataFileMapperImpl(File outDir, DatasetMapper datasetMapper, BigQueryTableMapper tableMapper) {
		this.outDir = outDir;
		this.datasetMapper = datasetMapper;
		this.tableMapper = tableMapper;
	}



	@Override
	public File fileForEnumRecords(Vertex owlClass) {
		File file = null;
		String datasetId = datasetMapper.datasetForClass(owlClass);
		String tableId = tableMapper.tableForClass(owlClass);
		if (datasetId != null && tableId!=null) {
			StringBuilder builder = new StringBuilder();
			builder.append(datasetId);
			builder.append('.');
			builder.append(tableId);
			builder.append(".jsonl");
			
			file = new File(outDir, builder.toString());
			
		}
		return file;
	}


}
