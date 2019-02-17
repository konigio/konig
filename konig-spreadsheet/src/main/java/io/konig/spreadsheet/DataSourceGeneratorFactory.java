package io.konig.spreadsheet;

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
		generator = new DataSourceGenerator(nsManager, templateDir, settings.getProperties());
		List<RegexRule> ruleList = settings.getRuleList();
		for (RegexRule rule : ruleList) {
			generator.addRegexRule(rule);
		}
	}

	@Override
	public void endWorkbook(Workbook workbook) {
		generator = null;
	}
	
	public DataSourceGenerator getDataSourceGenerator() {
		return generator;
	}


}
