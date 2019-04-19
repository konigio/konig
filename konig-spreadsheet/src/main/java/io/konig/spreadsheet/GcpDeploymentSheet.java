package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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

public class GcpDeploymentSheet extends BaseSheetProcessor {
	private static final SheetColumn TEMPLATE_TEXT = new SheetColumn("Template Text", true);
	private static final SheetColumn PROJECT = new SheetColumn("Project");
	
	private static final SheetColumn[] COLUMNS = new SheetColumn[]{
		TEMPLATE_TEXT,
		PROJECT
	};

	private File globalConfig;
	public GcpDeploymentSheet(WorkbookProcessor processor, File globalConfig) {
		super(processor);
		this.globalConfig = globalConfig;
	}

	@Override
	public SheetColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
			
		String text = stringValue(row, TEMPLATE_TEXT);
		
		globalConfig.getParentFile().mkdirs();
		try (FileWriter out = new FileWriter(globalConfig)) {
			out.write(text);
		} catch (IOException e) {
			fail(row, TEMPLATE_TEXT, e, "Failed to save file: {0}", globalConfig.getAbsolutePath());
		}

	}

}
