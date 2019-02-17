package io.konig.spreadsheet;

import java.util.ArrayList;
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
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
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
		processor.fail(row, col, cause, pattern, arg);
	}

	protected void fail(SheetRow row, SheetColumn col, String pattern, Object...arg) throws SpreadsheetException {
		processor.fail(row, col, null, pattern, arg);
	}

	protected void termStatus(URI subject, URI termStatus) {
		if (subject!=null && termStatus!=null) {
			TermStatusProcessor tsp = service(TermStatusProcessor.class);
			tsp.assertStatus(subject, termStatus);
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
	
	protected URI iriValue(SheetRow row, SheetColumn column) throws SpreadsheetException {
		return processor.iriValue(row, column);
	}

	protected Literal stringLiteral(SheetRow row, SheetColumn column) {
		String text = stringValue(row, column);
		return text==null ? null : new LiteralImpl(text);
	}
	
	protected List<URI> iriList(SheetRow row, SheetColumn column) throws SpreadsheetException {
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

	protected Set<URI> subjectSet(SheetRow row, SheetColumn column) throws SpreadsheetException {
		List<URI> list = iriList(row, column);
		if (list.isEmpty()) {
			return service(SettingsSheet.class).getDefaultSubject();
		}
		return new LinkedHashSet<>(list);
	}

}
