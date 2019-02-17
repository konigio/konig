package io.konig.spreadsheet;

import java.io.IOException;
import java.util.ArrayList;
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

	private static final String USE_DEFAULT_NAME = "useDefaultName";
	
	private static final SheetColumn SETTING_NAME = new SheetColumn("Setting Name", true);
	private static final SheetColumn SETTING_VALUE =  new SheetColumn("Setting Value", true);
	private static final SheetColumn PATTERN =  new SheetColumn("Pattern");
	private static final SheetColumn REPLACEMENT =  new SheetColumn("Replacement");
	
	private static final SheetColumn[] COLUMNS = new SheetColumn[]{
		SETTING_NAME,
		SETTING_VALUE,
		PATTERN,
		REPLACEMENT
	};
	
	private Properties settings;
//	private DataSourceGenerator dataSourceGenerator;
	private Map<String,WorkbookLocation> locations = new HashMap<>();
	private Set<URI> defaultSubject;
	private List<RegexRule> ruleList = null;

	public SettingsSheet(WorkbookProcessor processor)  {
		super(processor);
	}

	private void loadDefaultProperties()  {
		try {
			settings.load(getClass().getClassLoader().getResourceAsStream("WorkbookLoader/settings.properties"));
		} catch (IOException e) {
			throw new KonigException(e);
		}
	}

	@Override
	public SheetColumn[] getColumns() {
		return COLUMNS;
	}
	
	@Override
	public void beginWorkbook(Workbook book) {
		settings = new Properties();
		ruleList = new ArrayList<>();
		loadDefaultProperties();
	}

	@Override
	public void endWorkbook(Workbook book) {
		settings = null;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
		String name = stringValue(row, SETTING_NAME);

		String value = stringValue(row, SETTING_VALUE);

			
		String pattern = stringValue(row, PATTERN);
		String replacement = stringValue(row, REPLACEMENT);
		
		
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

}
