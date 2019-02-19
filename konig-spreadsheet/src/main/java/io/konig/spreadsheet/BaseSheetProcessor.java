package io.konig.spreadsheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

abstract public class BaseSheetProcessor implements SheetProcessor {
	
	private static final SheetProcessor[] EMPTY = new SheetProcessor[0];

	protected WorkbookProcessor processor;
	private SheetProcessor[] dependsOn = EMPTY;
	private Map<String,SheetColumn> columnMap;
	
	protected BaseSheetProcessor(WorkbookProcessor processor) {
		this.processor = processor;
	}
	
	

	public WorkbookProcessor getWorkbookProcessor() {
		return processor;
	}

	protected String compactName(Resource id) {
		return RdfUtil.compactName(processor.getGraph().getNamespaceManager(), id);
	}

	protected void addNamespace(String prefix, String namespace) {
		NamespaceManager nsManager = processor.getGraph().getNamespaceManager();
		if (nsManager.findByName(namespace) == null) {
			nsManager.add(prefix, namespace);
		}
		
	}

	@Override
	public SheetColumn findColumnByName(String name) {
		if (columnMap == null) {
			columnMap = new HashMap<>();
			for (SheetColumn c : getColumns()) {
				columnMap.put(c.getName().toLowerCase(), c);
			}
		}
		return columnMap.get(name.trim().toLowerCase());
	}
	
	@SuppressWarnings("unchecked")
	protected void dependsOn(Class<? extends SheetProcessor>...sheet) {
		ServiceManager sm = processor.getServiceManager();
		
		dependsOn = new SheetProcessor[sheet.length];
		for (int i=0; i<dependsOn.length; i++) {
			dependsOn[i] = sm.service(sheet[i]);
		}
		
	}
	
	protected URI uri(String value) {
		return new URIImpl(value);
	}
	protected void edge(Resource subject, URI predicate, Value object) {
		if (subject!=null && predicate!=null && object!=null) {
			processor.getGraph().edge(subject, predicate, object);
		}
	}
	
	protected <T> T service(Class<T> javaClass) {
		return processor.getServiceManager().service(javaClass);
	}
	
	protected void fail(SheetRow row, SheetColumn col, Throwable cause, String pattern, Object...arg) throws SpreadsheetException {
		processor.fail(cause, row, col, pattern, arg);
	}

	protected void fail(SheetRow row, SheetColumn col, String pattern, Object...arg) throws SpreadsheetException {
		processor.fail(null, row, col, pattern, arg);
	}

	protected void termStatus(URI subject, URI termStatus) {
		if (subject!=null && termStatus!=null) {
			TermStatusProcessor tsp = service(TermStatusProcessor.class);
			tsp.assertStatus(subject, termStatus);
		}
	}
	
	protected void declareStatus(URI termStatus) {
		if (termStatus != null) {
			service(TermStatusProcessor.class).declareStatus(termStatus);
		}
	}

	@Override
	public boolean transitiveDependsOn(SheetProcessor other) {
		for (SheetProcessor s : dependsOn) {
			if (s == other || s.transitiveDependsOn(other)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public SheetProcessor[] dependsOn() {
		return dependsOn;
	}
	

	@Override
	public int compareTo(SheetProcessor o) {
		return 
			this.transitiveDependsOn(o) ? 1 :
			o.transitiveDependsOn(this) ? -1 :
			0;
	}
	

	protected String stringValue(SheetRow row, SheetColumn column) {
		return processor.stringValue(row, column);
	}


	protected Double doubleValue(SheetRow row, SheetColumn column) throws SpreadsheetException {
		String text = stringValue(row, column);
		if (text != null) {
			try {
				return new Double(text);
			} catch (Throwable e) {
				processor.fail(e, row, column, "Expected a numeric expression but found: {0}", text);
			}
		}
		return null;
	}

	protected Boolean booleanValue(SheetRow row, SheetColumn column) throws SpreadsheetException {
		String text = stringValue(row, column);
		if (text != null) {
			
			if (!text.equalsIgnoreCase("true") && !text.equalsIgnoreCase("false")) {
			
				processor.fail(null, row,  column, "Expected 'true' or 'false' but value was {0}", text);
			}
			try {
				return new Boolean(text);
			} catch (Throwable e) {
				processor.fail(e, row, column, "Failed to parse boolean: {0}", text);
			}
		}
		return null;
	}


	protected Integer intValue(SheetRow row, SheetColumn column) throws SpreadsheetException {
		String text = stringValue(row, column);
		if (text != null) {
			try {
				return new Integer(text);
			} catch (Throwable e) {
				fail(row, column, e, "Failed to parse Integer value");
			}
		}
		return null;
	}
	
	protected URI iriValue(SheetRow row, SheetColumn column) throws SpreadsheetException {
		return processor.iriValue(row, column);
	}


	protected Resource resourceValue(SheetRow row, SheetColumn column) throws SpreadsheetException {
		String text = stringValue(row, column);
		if (text != null) {
			if (text.startsWith("_:")) {
				String bnodeId = text.substring(2);
				return new BNodeImpl(bnodeId);
			}
			return processor.expandCurie(text, row, column);
		}
		return null;
	}

	protected Literal stringLiteral(SheetRow row, SheetColumn column) {
		String text = stringValue(row, column);
		return text==null ? null : new LiteralImpl(text);
	}
	
	protected List<URI> nullableIriList(SheetRow row, SheetColumn column) throws SpreadsheetException {

		List<URI> result = null;

		String text = stringValue(row, column);
		if (text != null) {
			StringTokenizer tokens = new StringTokenizer(text, " \n\t\r");
			if (tokens.hasMoreTokens()) {
				result = new ArrayList<>();
				while (tokens.hasMoreTokens()) {
					String value = tokens.nextToken();
					result.add(processor.expandCurie(value, row, column));
				}
			}
		}
		return result;
	}
	
	protected List<URI> iriList(SheetRow row, SheetColumn column) throws SpreadsheetException {
		List<URI> result = nullableIriList(row, column);
		return result==null ? Collections.emptyList() : result;
	}

	protected List<String> stringList(SheetRow row, SheetColumn column) {
		
		String text = stringValue(row, column);
		if (text == null) {
			return Collections.emptyList();
		}
		
		List<String> list = new ArrayList<>();
		StringTokenizer tokens = new StringTokenizer(text, " \t\r\n");
		while (tokens.hasMoreTokens()) {
			list.add(tokens.nextToken());
		}
		
		return list;
	}

	protected WorkbookLocation location(SheetRow row, SheetColumn column) {
		return processor.location(row, column);
	}
	
	protected Vertex vertex(Resource id) {
		return processor.getGraph().vertex(id);
	}

	protected Vertex vertex() {
		return processor.getGraph().vertex();
	}
	
	protected String optionalCurie(URI uri) {
		return RdfUtil.optionalCurie(processor.getGraph().getNamespaceManager(), uri);
	}
	
	protected void warn(WorkbookLocation location, String pattern, Object... args) {
		processor.warn(location,pattern, args);
	}

	protected  List<Function> dataSourceList(SheetRow row, SheetColumn column) throws SpreadsheetException {
		String text = stringValue(row, column);
		return text==null ? null : dataSourceList(location(row, column), text);
	}
	
	protected  List<Function> dataSourceList(WorkbookLocation location, String text) throws SpreadsheetException {

		if (text == null) {
			return null;
		}

		ListFunctionVisitor visitor = new ListFunctionVisitor();
		FunctionParser parser = new FunctionParser(visitor);
		try {
			parser.parse(text);
		} catch (FunctionParseException e) {
			processor.fail(e, location, "Failed to parse Datasource definition: {}", text);
		}
		List<Function> list = visitor.getList();

		return list.isEmpty() ? null : list;
	}

	protected Shape produceShape(URI shapeId) {
		ShapeManager shapeManager = processor.getShapeManager();

		Shape shape = shapeManager.getShapeById(shapeId);
		if (shape == null) {
			shape = new Shape(shapeId);
			shapeManager.addShape(shape);
			shape.addType(SH.Shape);
			shape.addType(SH.NodeShape);
		}
		return shape;
	}
	
	protected void assignSubject(URI term, Collection<URI> subjects) throws SpreadsheetException {
		if (term != null) {
			if (subjects==null || subjects.isEmpty()) {
				subjects = service(SettingsSheet.class).getDefaultSubject();
			}
			Graph graph = processor.getGraph();
			for (URI value : subjects) {
				edge(term, DCTERMS.SUBJECT, value);
			}
		}
		
	}
	
	protected void setDefaultSubject(URI term) throws SpreadsheetException {
		if (term != null) {
			Set<URI> subjectSet = service(SettingsSheet.class).getDefaultSubject();
			if (!subjectSet.isEmpty()) {
				for (URI value : subjectSet) {
					edge(term, Konig.termStatus, value);
				}
			}
		}
	}

	protected Set<URI> subjectSet(SheetRow row, SheetColumn column) throws SpreadsheetException {
		List<URI> list = iriList(row, column);
		if (list.isEmpty()) {
			return service(SettingsSheet.class).getDefaultSubject();
		}
		return new LinkedHashSet<>(list);
	}

}
