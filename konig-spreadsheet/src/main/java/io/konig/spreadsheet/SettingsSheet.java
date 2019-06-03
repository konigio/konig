package io.konig.spreadsheet;

import java.io.File;
import java.io.FileOutputStream;

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


import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;

import io.konig.core.KonigException;
import io.konig.spreadsheet.nextgen.Workbook;

public class SettingsSheet extends BaseSheetProcessor implements WorkbookListener {

	public static final String DEFAULT_SUBJECT = "defaultSubject";
	private static final String GCP_DATASET_ID = "gcpDatasetId";
	private static final String IGNORE_SHEETS = "ignoreSheets"; 
	private static final String  INDIVIVIDUAL_LOCAL_NAME_ENCODING = "individual.localname.encoding"; 

	private static final String USE_DEFAULT_NAME = "useDefaultName";
	public static final String SHAPE_URL_TEMPLATE = "shapeURLTemplate";
	private static final String EXCLUDE_SECURITY_CLASSIFICATIONS = "dictionary.excludeSecurityClassifications";
	private static final String PROPERTY_BASE_URL = "propertyBaseURL";
	private static final String DEFAULT_DATA_SOURCE = "defaultDataSource";
	private static final String DICTIONARY_DEFAULT_MAX_LENGTH = "dictionary.defaultMaxLength";
	public static final String ABBREVIATION_SCHEME_IRI = "abbreviationSchemeIri";
	
	private static final SheetColumn SETTING_NAME = new SheetColumn("Setting Name", true);
	private static final SheetColumn SETTING_VALUE =  new SheetColumn("Setting Value", true);
	private static final SheetColumn PATTERN =  new SheetColumn("Pattern");
	private static final SheetColumn REPLACEMENT =  new SheetColumn("Replacement");
	private static final SheetColumn PROJECT = new SheetColumn("Project");
	
	private static final String URLENCODING= "urlencoding";
	
	private static final SheetColumn[] COLUMNS = new SheetColumn[]{
		SETTING_NAME,
		SETTING_VALUE,
		PATTERN,
		REPLACEMENT,
		PROJECT
	};
	
	private File outDir;
	private Properties settings;
//	private DataSourceGenerator dataSourceGenerator;
	private Map<String,WorkbookLocation> locations = new HashMap<>();
	private Set<URI> defaultSubject;
	private List<RegexRule> ruleList = null;
	private Set<URI> excludeSecurityClassification;
	private List<Function> defaultDataSource;
	private Set<String> ignoreSheets = null;

	public SettingsSheet(WorkbookProcessor processor)  {
		super(processor);
	}

	public File getOutDir() {
		return outDir;
	}

	public void setOutDir(File outDir) {
		this.outDir = outDir;
	}
	
	public Set<String> getIgnoreSheets() {
		
		return ignoreSheets==null ? Collections.emptySet() : ignoreSheets;
	}
	

	public IndividualLocalNameEncoding getIndividualLocalNameEncoding() {
		String value = settings.getProperty(
				INDIVIVIDUAL_LOCAL_NAME_ENCODING, 
				IndividualLocalNameEncoding.NONE.name());
				
		return URLENCODING.equalsIgnoreCase(value) ? IndividualLocalNameEncoding.URL_ENCODING : 
			IndividualLocalNameEncoding.NONE;
	}



	private void loadDefaultProperties()  {
		try {
			settings.load(getClass().getClassLoader().getResourceAsStream("WorkbookLoader/settings.properties"));
		} catch (IOException e) {
			throw new KonigException(e);
		}
	}
	
	public String getAbbreviationSchemeIri() {
		return settings.getProperty(ABBREVIATION_SCHEME_IRI);
	}
	
	public Set<URI> getExcludeSecurityClassification() throws SpreadsheetException {
		if (excludeSecurityClassification == null) {
			excludeSecurityClassification = iriSet(EXCLUDE_SECURITY_CLASSIFICATIONS);
		}
		return excludeSecurityClassification;
	}
	
	public Integer getDictionaryDefaultMaxLength() throws SpreadsheetException {
		return intValue(DICTIONARY_DEFAULT_MAX_LENGTH);
	}
	
	public Integer intValue(String propertyName) throws SpreadsheetException {

		String value = settings.getProperty(propertyName);
	
		if (value != null) {
			try {
				return new Integer(value);
			} catch (Throwable e) {
				WorkbookLocation location = locations.get(propertyName);
				processor.fail(e, location, "Setting {0} must be an integer but found {1}", propertyName, value);
				
			}
		}
		
		return null;
	}

	@Override
	public SheetColumn[] getColumns() {
		return COLUMNS;
	}
	
	@Override
	public void beginWorkbook(Workbook book) {
		settings = new Properties();
		ruleList = new ArrayList<>();
		excludeSecurityClassification = null;
		defaultDataSource = null;
		ignoreSheets = null;
		loadDefaultProperties();
	}

	@Override
	public void endWorkbook(Workbook book) {
		persist();
		settings = null;
		ignoreSheets = null;
	}
	
	private void persist() {
		if (outDir != null && settings!=null) {
			outDir.mkdirs();
			String fileName = processor.getActiveWorkbook().getFile().getName();
			int dot = fileName.lastIndexOf('.');
			if (dot > 0) {
				fileName = fileName.substring(0, dot);
			}
			fileName = fileName + ".properties";
			File outFile = new File(outDir, fileName);
			
			try (OutputStream out = new FileOutputStream(outFile)) {
				settings.store(out, "");
			} catch (Throwable e) {
				throw new KonigException(e);
			}
		}
		
	}

	public String getShapeUrlTemplate() {
		return settings.getProperty(SHAPE_URL_TEMPLATE);
	}
	
	public List<Function> getDefaultDataSource() throws SpreadsheetException {
		if (defaultDataSource == null) {
			String value = settings.getProperty(DEFAULT_DATA_SOURCE);
			defaultDataSource = dataSourceList(value);
		}
		return defaultDataSource;
	}
	
	private  List<Function> dataSourceList(String text) throws SpreadsheetException {

		if (text == null) {
			return null;
		}

		ListFunctionVisitor visitor = new ListFunctionVisitor();
		FunctionParser parser = new FunctionParser(visitor);
		try {
			parser.parse(text);
		} catch (FunctionParseException e) {
			WorkbookLocation location = locations.get(DEFAULT_DATA_SOURCE);
			processor.fail(location, "Failed to parse Datasource definition: {0}", text);
		}
		List<Function> list = visitor.getList();

		return list.isEmpty() ? null : list;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
		String name = stringValue(row, SETTING_NAME);

		String value = stringValue(row, SETTING_VALUE);

			
		String pattern = stringValue(row, PATTERN);
		String replacement = stringValue(row, REPLACEMENT);
		
		if (name.equals(IGNORE_SHEETS)) {
			buildIgnoreSheets(value);
		}
		if (pattern!=null && replacement==null) {
			fail(
				row, REPLACEMENT, 
				"For the setting ''{0}'' a regular expression pattern is defined, but no replacement string is defined", 
				name);
		}
		
		if (replacement != null && pattern==null) {

			fail(
				row, PATTERN,
				"For the setting ''{0}'' a replacement string is defined, but no regular expression pattern is defined", 
				name);
		}
		

		
		WorkbookLocation location = processor.location(row, SETTING_VALUE);
		locations.put(name, location);
		
		if (replacement!=null && pattern!=null) {
			RegexRule rule = new RegexRule(name, value, pattern, replacement);
			ruleList.add(rule);
			
		} else {
			settings.setProperty(name, value);
		}	
		
		
	}
	
	private void buildIgnoreSheets(String value) {

		ignoreSheets = new HashSet<>();
		if (value != null) {
			StringTokenizer tokens = new StringTokenizer(value, "\r\n");
			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken().trim();
				ignoreSheets.add(token);
			}
		}
		
	}

	public List<RegexRule> getRuleList() {
		return ruleList;
	}
	
	public WorkbookLocation getLocation(String settingName) {
		return locations.get(settingName);
	}
	
	public Properties getProperties() {
		return settings;
	}
	
	public Set<URI> iriSet(String propertyName) throws SpreadsheetException {
		Set<URI> set = new HashSet<>();
		String value = settings.getProperty(propertyName);
		if (value != null) {
			WorkbookLocation location = locations.get(propertyName);
			StringTokenizer tokens = new StringTokenizer(value, " \t\r\n");
			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				URI id = processor.expandCurie(token, location);
				set.add(id);
			}
		}
		
		return set;
	}

	public Literal gcpDatasetId() {

		String value = settings.getProperty(GCP_DATASET_ID);
		return value==null ? null : new LiteralImpl(value);
	}

	public boolean useDefaultName() {
		return "true".equalsIgnoreCase(settings.getProperty(USE_DEFAULT_NAME, "true"));
	}
	
	public Set<URI> getDefaultSubject() throws SpreadsheetException  {
		if (defaultSubject == null) {
			try {
				defaultSubject = iriSet(SettingsSheet.DEFAULT_SUBJECT);
			} catch (SpreadsheetException e) {
				// Only throw the error once
				defaultSubject = new HashSet<>();
				throw e;
			}
			
			
		}
		
		return defaultSubject;
	}

	public String getPropertyBaseURL() {
		return settings.getProperty(PROPERTY_BASE_URL);
	}

}
