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
import java.util.List;

import io.konig.core.NamespaceManager;
import io.konig.spreadsheet.nextgen.Workbook;

public class DataSourceGeneratorFactory implements WorkbookListener {
	
	private SettingsSheet settings;
	private NamespaceManager nsManager;
	private File templateDir;
	private DataSourceGenerator generator;
	
	

	public DataSourceGeneratorFactory(NamespaceManager nsManager, File templateDir, SettingsSheet settings) {
		this.nsManager = nsManager;
		this.templateDir = templateDir;
		this.settings = settings;
	}

	@Override
	public void beginWorkbook(Workbook workbook) {
		generator = null;
	}

	@Override
	public void endWorkbook(Workbook workbook) {
		DataSourceGenerator engine =  getDataSourceGenerator();
		List<RegexRule> ruleList = settings.getRuleList();
		for (RegexRule rule : ruleList) {
			engine.addRegexRule(rule);
		}
		generator = null;
	}
	
	public DataSourceGenerator getDataSourceGenerator() {
		if (generator == null) {
			generator = new DataSourceGenerator(nsManager, templateDir, settings.getProperties());
		}
		return generator;
	}


}
