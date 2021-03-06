package io.konig.transform.showl.sql;

/*
 * #%L
 * Konig Transform
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


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.konig.core.project.ProjectFile;
import io.konig.core.project.ProjectFolder;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlProcessingException;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.sql.query.InsertStatement;

public class BigQueryTransformConsumer implements ShowlSqlTransformConsumer {
	
	private ProjectFolder folder;
	

	public BigQueryTransformConsumer(ProjectFolder folder) {
		this.folder = folder;
	}

	@Override
	public void consume(InsertStatement insert, ShowlNodeShape targetShape, DataSource targetDataSource) throws ShowlProcessingException {
		
		File outFile = createFile(targetDataSource);
		if (outFile != null) {
			outFile.getParentFile().mkdirs();
			try (FileWriter writer = new FileWriter(outFile)) {
				writer.write(insert.toString());
			} catch (IOException e) {
				throw new ShowlProcessingException(e);
			}
		}

	}
	
	private File createFile(DataSource targetDataSource) {
		if (targetDataSource instanceof GoogleBigQueryTable) {
			return createFile((GoogleBigQueryTable) targetDataSource);
		}
		return null;
	}

	private File createFile(TableDataSource datasource) {
		ProjectFile file = folder.createFile(datasource.getTransformFileName());
		datasource.setTransformFile(file);
		return file.getLocalFile();
	}

}
