package io.konig.spreadsheet;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.URIUtil;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.AS;
import io.konig.core.vocab.AWS;
import io.konig.core.vocab.DC;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.PROV;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.shacl.ShapeManager;

public class WorkbookProcessorImpl implements WorkbookProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(WorkbookProcessorImpl.class);

	
	
	private ServiceManager serviceManager = new ServiceManager();
	private DataFormatter dataFormatter = new DataFormatter(true);
	private List<Action> actionList = new ArrayList<>();
	
	private List<SheetProcessor> sheetProcessors = new ArrayList<>();
	private List<WorkbookListener> bookListeners = new ArrayList<>();


	private io.konig.spreadsheet.nextgen.Workbook activeBook;
	
	// Graph specific fields

	private Graph graph;
	private OwlReasoner owlReasoner;
	private ValueFactory vf = new ValueFactoryImpl();
	private NamespaceManager nsManager;
	private ShapeManager shapeManager;

	private boolean normalizeTerms;
	private Map<String,String> normalizedMap = new HashMap<>();
	private Set<String> normalizedTerms = new HashSet<>();
	private SettingsSheet settings;
	
	
	public WorkbookProcessorImpl(Graph graph, ShapeManager shapeManager, File templateDir) {

		this.graph = graph;
		this.nsManager = graph.getNamespaceManager();
		this.owlReasoner = new OwlReasoner(graph);
		this.shapeManager = shapeManager;

		serviceManager.setListener(new BaseServiceListener());
		addNamespaces();
		addServices(templateDir);
		sortBookListeners();
	}


	private void sortBookListeners() {
		DependencyManager<WorkbookListener> sorter = new DependencyManager<WorkbookListener>();
		sorter.sort(bookListeners);
	}


	private void addServices(File templateDir) {
		SettingsSheet settingSheet = settings =  new SettingsSheet(this);
		DataSourceGeneratorFactory dataSourceGeneratorFactory = new DataSourceGeneratorFactory(nsManager, templateDir, settings);
		addService(WorkbookProcessor.class, this);
		addService(dataSourceGeneratorFactory);
		addService(Graph.class, graph);
		addService(NamespaceManager.class, nsManager);
		addService(ValueFactory.class, vf);
		addService(ShapeManager.class, shapeManager);

		addSheetProcessor(new OntologySheet(this, nsManager));
		addSheetProcessor(settingSheet);
		addSheetProcessor(new ClassSheet(this, settingSheet));
		addSheetProcessor(new PropertySheet(this));
		addSheetProcessor(new IndividualSheet(this, settingSheet));
		addSheetProcessor(new ShapeSheet(this, dataSourceGeneratorFactory));
		addSheetProcessor(new PropertyConstraintSheet(this, owlReasoner));
		addSheetProcessor(new TripleSheet(this));
		
	}


	private void addNamespaces() {
		nsManager.add("vann", "http://purl.org/vocab/vann/");
		nsManager.add("owl", OWL.NAMESPACE);
		nsManager.add("sh", SH.NAMESPACE);
		nsManager.add("rdf", RDF.NAMESPACE);
		nsManager.add("rdfs", RDFS.NAMESPACE);
		nsManager.add("konig", Konig.NAMESPACE);
		nsManager.add("xsd", XMLSchema.NAMESPACE);
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("dc", DC.NAMESPACE);
		nsManager.add("prov", PROV.NAMESPACE);
		nsManager.add("as", AS.NAMESPACE);
		nsManager.add("gcp", GCP.NAMESPACE);
		nsManager.add("aws",AWS.NAMESPACE);
		nsManager.add("skos",SKOS.NAMESPACE);
		
	}


	public void addService(Object service) {
		serviceManager.addService(service);
	}
	
	public void addService(Class<?> type, Object service) {
		serviceManager.addService(type, service);
	}
	
	private String cellStringValue(Cell cell) {
		if (cell == null) {
			return null;
		}
		String text = dataFormatter.formatCellValue(cell);
		if (text != null && !text.startsWith("HYPERLINK(")) {
			text = text.trim();
			if (text.length() == 0) {
				text = null;
			}
		} else {

			Hyperlink link = cell.getHyperlink();
			if (link != null) {
				text = link.getLabel();
				if (text == null) {
					text = link.getAddress();
				}
				if (text != null) {
					text = text.trim();

				}
			}
		}
		return text;
	}


	@Override
	public String stringValue(SheetRow sheetRow, SheetColumn col) {
		if (!col.exists()) {
			return null;
		}
		Row row = sheetRow.getRow();
		if (row == null) {
			return null;
		}
		String text = null;
		int column = col.getIndex();
		if (column >= 0) {
			Cell cell = row.getCell(column);
			text = cellStringValue(cell);
		}

		return text == null || text.isEmpty() ? null : text.replaceAll("(^\\h*)|(\\h*$)", "");
	}

	private String locationMessage(SheetRow row, SheetColumn column, String message) {
		return locationMessage(location(row, column), message);
	}

	private String locationMessage(WorkbookLocation location, String message) {
		String ellipsis = "";
		StringBuilder builder = new StringBuilder();
		if (location.getWorkbookName() != null) {
			builder.append(location.getWorkbookName());
			builder.append(" - ");
		}
		
		if (location.getRowNum() != null) {
			builder.append("At row ");
			builder.append(location.getRowNum() + 1);
			ellipsis = " ... ";
		}
		
		if (location.getColumnName() != null) {
			builder.append(", column '");
			builder.append(location.getColumnName());
			builder.append("'");
			ellipsis = " ... ";
		}
		
		if (location.getSheetName() != null) {
			builder.append(" on sheet '");
			builder.append(location.getSheetName());
			builder.append("'");
			ellipsis = " ... ";
		}
		
		if (message != null) {
			builder.append(ellipsis);
			builder.append(message);
		}
		return builder.toString();
	}
	
	protected void error(Throwable cause, WorkbookLocation location, String message) throws SpreadsheetException {
		
		String text = locationMessage(location, message);
		
		throw new SpreadsheetException(text, cause);
		
	}


	protected void error(SheetRow row, SheetColumn col, String pattern, Object...args) throws SpreadsheetException {
		error((Throwable) null, row, col, pattern, args);
		
	}

	private void error(Throwable cause, SheetRow row, SheetColumn col, String pattern, Object...args) throws SpreadsheetException {
		String message = MessageFormat.format(pattern, args);
		String bookName = activeBook==null ? null : activeBook.getFile().getName();
		String sheetName = row==null ? null : row.getSheetName();
		Integer rowNum = row==null ? null : row.getRowNum();
		String columnName = col==null ? null : col.getName();
		
		WorkbookLocation location = new WorkbookLocation(bookName, sheetName, rowNum, columnName);
		error(cause, location, message);
	}

	protected void warn(SheetRow row, SheetColumn col, String pattern, Object...args) {
		if (logger.isWarnEnabled()) {
			String message = locationMessage(row, col, MessageFormat.format(pattern, args));
			logger.warn(message);
		}
		
		
	}
//
//	private String format(SheetRow row, SheetColumn col, String pattern, Object...args) {
//		if (col == null) {
//			return format(row, pattern, args);
//		}
//		String prefix = format("At row {0}, column ''{1}'' on sheet ''{2}''...", 
//				row.getRowNum(), col.getName(), row.getSheetName());
//		String suffix = format(pattern, args);
//		return prefix + suffix;
//	}
//
//	private String format(SheetRow row, String pattern, Object...args) {
//		if (row == null) {
//			return format(pattern, args);
//		}
//
//		String prefix = format("At row {0} on sheet ''{1}''...", 
//				row.getRowNum(),  row.getSheetName());
//		String suffix = format(pattern, args);
//		return prefix + suffix;
//	}
//
//	protected String format(String pattern, Object...args) {
//		
//		return MessageFormat.format(pattern, args);
//	}

	@Override
	public void defer(Action action) {
		actionList.add(action);
	}
	

	@Override
	public void executeDeferredActions() throws SpreadsheetException {
		
		// It is possible that some actions may add new deferred actions.
		// To support that scenario, we run two loops.
		
		// The outer loop moves the current actionList to an 'oldList' variable,
		// and replaces actionList with a new ArrayList that is ready to 
		// accept new deferred actions.
		
		// The inner loop executes actions in the oldList.
		// The entire process ends when there are no more actions to execute.
		
		while (!actionList.isEmpty()) {
			List<Action> oldList = actionList;
			actionList = new ArrayList<>();
			for (Action action : oldList) {
				action.execute();
			}
		}
		
	}

	@Override
	public void process(File workbookFile) throws SpreadsheetException {
		activeBook = new io.konig.spreadsheet.nextgen.Workbook(workbookFile);
		Collections.sort(sheetProcessors);
		Workbook workbook = createWorkbook(workbookFile);
		beginWorkbook();
		List<WorkbookSheet> sheetList = listSheets(workbook);
		for (WorkbookSheet sheet : sheetList) {
			visitSheet(sheet);
		}
		endWorkbook();
		activeBook = null;
	}

	private void endWorkbook() {
		for (WorkbookListener listener : bookListeners) {
			listener.endWorkbook(activeBook);
		}
		
	}


	private void beginWorkbook() {
		logger.debug("beginWorkbook({})", activeBook.getFile().getName());
		for (WorkbookListener listener : bookListeners) {
			listener.beginWorkbook(activeBook);
		}
		
	}


	private void visitSheet(WorkbookSheet bookSheet) throws SpreadsheetException {
		logger.debug("visitSheet({})", bookSheet.getSheet().getSheetName());
		List<SheetColumn> undeclaredColumns = new ArrayList<>();
		assignColumnIndexes(bookSheet, undeclaredColumns);
		SheetProcessor processor = bookSheet.getProcessor();
		Sheet sheet = bookSheet.getSheet();

		int rowSize = sheet.getLastRowNum() + 1;

		// Skip the first row since it is the column header row
		for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
			Row row = sheet.getRow(i);
			if (row != null) {
				SheetRow sheetRow = new SheetRow(bookSheet, row);
				sheetRow.setUndeclaredColumns(undeclaredColumns);
				if (accept(sheetRow, processor)) {
					processor.visit(sheetRow);
				}
			}
		}
		
		
	}

	private boolean accept(SheetRow sheetRow, SheetProcessor processor) throws SpreadsheetException {
		Row row = sheetRow.getRow();
		boolean foundAny = false;
		List<String> missingRequired = null;
		for (SheetColumn column : processor.getColumns()) {
			
			if (column.isRequired()) {
				if (column.getIndex()<0) {
					return false;
				}
				Cell cell = row.getCell(column.getIndex());
				
				if (cell == null) {
					missingRequired = append(missingRequired, column.getName());
				} else {
//					if (sheetRow.getSheetName().equals("Property Constraints")) {
//						String value = cellStringValue(cell);
//						logger.debug("accept - sheet: {}, column: {}, value: {}", sheetRow.getSheetName(), column.getName(), value);
//					}
					@SuppressWarnings("deprecation")
					CellType cellType = cell.getCellTypeEnum();
					switch (cellType) {
					case _NONE :
					case BLANK:
						missingRequired = append(missingRequired, column.getName());
						break;
						
					default:
						foundAny=true;
					}
				}
				
			} else if (!foundAny && column.getIndex()>=0) {
				Cell cell = row.getCell(column.getIndex());
				foundAny = cell!=null && cellStringValue(cell)!=null;
			}
		}
		if (foundAny && missingRequired != null) {
			StringBuilder text = new StringBuilder();
			text.append("Values are required for: ");
			String comma = "";
			for (String name : missingRequired) {
				text.append(comma);
				comma = ", ";
				text.append('[');
				text.append(name);
				text.append(']');
			}
			
			warn(sheetRow, null, text.toString());
		}
		return missingRequired == null;
	}


	


	private List<String> append(List<String> list, String value) {
		if (list == null) {
			list = new ArrayList<>();
		}
		list.add(value);
		return list;
	}


	private List<WorkbookSheet> listSheets(Workbook workbook) throws SpreadsheetException {
		
		List<WorkbookSheet> list = new ArrayList<>();
		for (int i=0; i<workbook.getNumberOfSheets(); i++) {
			Sheet sheet = workbook.getSheetAt(i);
			int bestRank = 0;
			SheetProcessor best = null;
			for (SheetProcessor p : sheetProcessors) {
				int rank = rank(sheet, p);
				if (rank > bestRank) {
					bestRank = rank;
					best = p;
				}
			}
			if (best != null) {
				list.add(new WorkbookSheet(sheet, best));
			}
		}
		Collections.sort(list);
		return list;
	}
	
	private void assignColumnIndexes(WorkbookSheet s, List<SheetColumn> undeclaredColumns) {
		logger.debug("assignColumnIndexes({})", s.getSheet().getSheetName());
		undeclaredColumns.clear();
		Sheet sheet = s.getSheet();
		SheetProcessor p = s.getProcessor();

		for (SheetColumn c : p.getColumns()) {
			c.setIndex(-1);
		}

		int firstRow = sheet.getFirstRowNum();
		Row row = sheet.getRow(firstRow);

		int colSize = row.getLastCellNum() + 1;
		for (int i = row.getFirstCellNum(); i < colSize; i++) {

			Cell cell = row.getCell(i);
			if (cell != null) {
				
				String columnName = cellStringValue(cell);
				if (columnName != null) {
					SheetColumn column = p.findColumnByName(columnName);
					if (column != null) {
						column.setIndex(i);
						logger.debug("assignColumnIndexes - {} index = {}", column, i);
						
					} else {
						SheetColumn c = new SheetColumn(columnName);
						c.setIndex(i);
						undeclaredColumns.add(c);
					}
				}
			}
		}
		
		
	}


	private int rank(Sheet sheet, SheetProcessor p) throws SpreadsheetException {
		
		
		int count = 0;
		
		int firstRow = sheet.getFirstRowNum();
		Row row = sheet.getRow(firstRow);

		int colSize = row.getLastCellNum() + 1;
		for (int i = row.getFirstCellNum(); i < colSize; i++) {

			Cell cell = row.getCell(i);
			if (cell != null) {
				String text = cell.getStringCellValue();
				if (text != null) {
					SheetColumn column = p.findColumnByName(text);
					if (column != null) {
						count++;
					}
				}
			}
		}
		
		return count;
	}

	private Workbook createWorkbook(File workbookFile) throws SpreadsheetException {

		try {
			return WorkbookFactory.create(workbookFile);
		} catch (Throwable e) {
			throw new SpreadsheetException("Failed to create workbookboo: " + workbookFile.getName(), e);
		}
	}

	@Override
	public ServiceManager getServiceManager() {
		return serviceManager;
	}

	@Override
	public void addSheetProcessor(SheetProcessor processor) {
		sheetProcessors.add(processor);
		serviceManager.addService(processor);
	}
	
	protected class BaseServiceListener implements ServiceListener {

		@Override
		public void onCreateService(Object service) {
			
			if (service instanceof SheetProcessor) {
				addSheetProcessor((SheetProcessor) service);
			}
			if (service instanceof SimpleLocalNameService) {
				defer(new BuildLocalNameServiceAction(
					(SimpleLocalNameService)service, graph, shapeManager));
			}
			
		}

		@Override
		public void onRegister(Object service) {
			if (service instanceof WorkbookListener) {
				WorkbookListener listener =(WorkbookListener)service; 
				bookListeners.add(listener);
				if (activeBook != null) {
					listener.beginWorkbook(activeBook);
				}
			}
			
		}
		
	}

	@Override
	public void fail(Throwable cause, SheetRow row, SheetColumn column, String pattern, Object... arg) throws SpreadsheetException {
		String message = MessageFormat.format(pattern, arg);
		error(cause, row, column, message);
		
	}


	public boolean isNormalizeTerms() {
		return normalizeTerms;
	}

	public void setNormalizeTerms(boolean normalizeTerms) {
		this.normalizeTerms = normalizeTerms;
	}

	@Override
	public URI iriValue(SheetRow sheetRow, SheetColumn col) throws SpreadsheetException {

		if (!col.exists()) {
			return null;
		}
		String text = stringValue(sheetRow, col);
		return expandCurie(text, sheetRow, col);
	}
	
	@Override
	public URI expandCurie(String text, WorkbookLocation location) throws SpreadsheetException {
		if (text == null) {
			return null;
		}
		if (text.startsWith("http://") || text.startsWith("https://") || text.startsWith("urn:")) {
			return vf.createURI(text);
		}
		int colon = text.indexOf(':');
		if (colon < 1) {
			fail(location, "Invalid URI: {0} ", text);
		}
		String prefix = text.substring(0, colon);
		Namespace ns = nsManager.findByPrefix(prefix);
		if (ns == null) {
			error(location, MessageFormat.format("Namespace not found for prefix ''{0}''", prefix));
		}
		StringBuilder builder = new StringBuilder();
		builder.append(ns.getName());

		String localName = text.substring(colon + 1);
		if (normalizeTerms) {
			
			String normalized = StringUtil.normalizedLocalName(localName);
			if (!localName.equals(normalized)) {
				String prior = normalizedMap.get(localName);
				if (prior != null) {
					normalized = prior;
				} else if (normalizedTerms.contains(normalized)){
					
					for (int count=2; count<102; count++) {
						String candidate = normalized + count;
						if (!normalizedTerms.contains(candidate)) {
							normalized = candidate;
							break;
						}
					}
					normalizedMap.put(localName, normalized);
					normalizedTerms.add(normalized);
				}
				localName = normalized;
				warn(location, "{0} has been normalized as {1}:{2}", text, prefix, normalized);
			}
		}

		builder.append(localName);

		if (localName.indexOf('/') >= 0) {
			StringBuilder err = new StringBuilder();
			err.append("The localname of a CURIE should not contain a slash, but found '");
			err.append(text);
			err.append("'");
		}
		
		String iriValue = builder.toString();
		if (!URIUtil.isValidURIReference(iriValue)) {
			error(location, "Invalid IRI <{0}>", text);
		}

		return vf.createURI(builder.toString());
	}



	@Override
	public void warn(WorkbookLocation location, String pattern, Object...args) {
		if (logger.isWarnEnabled()) {
			String message = locationMessage(location, MessageFormat.format(pattern, args));
			logger.warn(message);
		}
		
	}


	private void error(WorkbookLocation location, String pattern, Object...args) throws SpreadsheetException {
		String message = MessageFormat.format(pattern, args);
		error(null, location, message);
	}


	@Override
	public URI expandCurie(String text, SheetRow row, SheetColumn col) throws SpreadsheetException {
		if (text == null || !col.exists()) {
			return null;
		}
		if (text.startsWith("http://") || text.startsWith("https://") || text.startsWith("urn:")) {
			return vf.createURI(text);
		}
		int colon = text.indexOf(':');
		if (colon < 1) {
			fail(location(row, col), "Invalid URI: {0} ", text);
		}
		String prefix = text.substring(0, colon);
		Namespace ns = nsManager.findByPrefix(prefix);
		if (ns == null) {
			error(row, col, "Namespace not found for prefix ''{0}''", prefix);
		}
		StringBuilder builder = new StringBuilder();
		builder.append(ns.getName());

		String localName = text.substring(colon + 1);
		if (normalizeTerms) {
			
			String normalized = StringUtil.normalizedLocalName(localName);
			if (!localName.equals(normalized)) {
				String prior = normalizedMap.get(localName);
				if (prior != null) {
					normalized = prior;
				} else if (normalizedTerms.contains(normalized)){
					
					for (int count=2; count<102; count++) {
						String candidate = normalized + count;
						if (!normalizedTerms.contains(candidate)) {
							normalized = candidate;
							break;
						}
					}
					normalizedMap.put(localName, normalized);
					normalizedTerms.add(normalized);
				}
				localName = normalized;
				warn(row, col, "{0} has been normalized as {1}:{2}", text, prefix, normalized);
			}
		}

		builder.append(localName);

		if (localName.indexOf('/') >= 0) {
			StringBuilder err = new StringBuilder();
			err.append("The localname of a CURIE should not contain a slash, but found '");
			err.append(text);
			err.append("'");
			warn(row, col, err.toString());
		}
		
		String iriValue = builder.toString();
		if (!URIUtil.isValidURIReference(iriValue)) {
			error(row, col, "Invalid IRI <{0}>", text);
		}

		return vf.createURI(builder.toString());
	}


	@Override
	public Graph getGraph() {
		return graph;
	}



	@Override
	public void fail(Throwable cause, WorkbookLocation location, String pattern, Object... arg)
			throws SpreadsheetException {

		error(cause, location, MessageFormat.format(pattern, arg));
		
	}
	
	@Override
	public void fail(WorkbookLocation location, String pattern, Object... arg) throws SpreadsheetException {
		fail(null, location, pattern, arg);
	}


	@Override
	public URI uri(String stringValue) {
		return stringValue==null ? null : new URIImpl(stringValue);
	}


	@Override
	public WorkbookLocation location(SheetRow row, SheetColumn column) {
		String workbookName = activeBook==null ? null : activeBook.getFile().getName();
		String sheetName = row==null ? null : row.getSheetName();
		Integer rowNum = row==null ? null : row.getRowNum();
		String colName = column==null ? null : column.getName();
		
		return new WorkbookLocation(workbookName, sheetName, rowNum, colName);
	}


	@Override
	public OwlReasoner getOwlReasoner() {
		return owlReasoner;
	}


	@Override
	public String getSetting(String name) {
		return settings.getProperties().getProperty(name);
	}


	@Override
	public ShapeManager getShapeManager() {
		return shapeManager;
	}


	@Override
	public io.konig.spreadsheet.nextgen.Workbook getActiveWorkbook() {
		return activeBook;
	}


}