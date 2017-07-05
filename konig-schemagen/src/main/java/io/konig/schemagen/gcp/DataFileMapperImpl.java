package io.konig.schemagen.gcp;

import java.io.File;

import io.konig.core.Vertex;

public class DataFileMapperImpl implements DataFileMapper {
	
	private File outDir;
	private DatasetMapper datasetMapper;
	private TableMapper tableMapper;
	
	
	
	public DataFileMapperImpl(File outDir, DatasetMapper datasetMapper, TableMapper tableMapper) {
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
