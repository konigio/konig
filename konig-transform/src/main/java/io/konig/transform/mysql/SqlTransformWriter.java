package io.konig.transform.mysql;

/*
 * #%L
 * Konig Transform
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
import java.io.FileWriter;
import java.io.IOException;

import io.konig.core.project.ProjectFile;
import io.konig.core.project.ProjectFolder;
import io.konig.datasource.TableDataSource;
import io.konig.transform.model.ShapeTransformException;
import io.konig.transform.sql.SqlTransform;

public class SqlTransformWriter implements SqlTransformVisitor {
	
	private ProjectFolder folder;
	

	public SqlTransformWriter(ProjectFolder folder) {
		this.folder = folder;
	}


	@Override
	public void visit(SqlTransform transform) throws ShapeTransformException {
		
		TableDataSource ds = (TableDataSource) transform.getTargetShape().getTdatasource().getDatasource();
		
		ProjectFile file = folder.createFile(ds.getTransformFileName());
		ds.setTransformFile(file);
		
		File localFile = file.getLocalFile();
		localFile.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(localFile)) {
			writer.write(transform.getInsert().toString());
		} catch (IOException e) {
			throw new ShapeTransformException("Failed to write file: " + localFile.getAbsolutePath());
		}

	}



}
