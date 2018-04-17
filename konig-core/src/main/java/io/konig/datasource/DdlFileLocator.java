package io.konig.datasource;

import java.io.File;

public class DdlFileLocator implements DatasourceFileLocator {

	private File baseDir;
	
	public DdlFileLocator(File baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public File locateFile(DataSource ds) {
		if (ds instanceof TableDataSource) {
			TableDataSource table = (TableDataSource) ds;
			String fileName = table.getUniqueIdentifier().replace(':', '_') + ".sql";
			return new File(baseDir, fileName);
		}
		return null;
	}

}
