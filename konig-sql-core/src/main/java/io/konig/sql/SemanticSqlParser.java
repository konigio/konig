package io.konig.sql;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.io.CompositeRdfHandler;
import io.konig.core.io.GraphLoadHandler;
import io.konig.core.path.PathParser;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.SH;
import io.konig.rio.turtle.NamespaceMap;
import io.konig.rio.turtle.SeaTurtleParser;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class SemanticSqlParser extends SeaTurtleParser {

	private static final char[] BUFFER = new char[20];

	private SQLSchemaManager sqlSchemaManager;
	private ShapeManager shapeManager;
	private ShapeLoader shapeLoader;
	private NamespaceManager namespaceManager;

	private String columnNamespace;
	private IriTemplate originShapeId;
	private IriTemplate targetShapeId;
	private IriTemplate classId;
	private PathParser pathParser;

	public SemanticSqlParser(SQLSchemaManager sqlSchemaManager, ShapeManager shapeManager) {
		this.sqlSchemaManager = sqlSchemaManager;
		this.shapeManager = shapeManager;
		
		namespaceManager = new MemoryNamespaceManager();
		
		shapeLoader = new ShapeLoader(shapeManager);
	}

	public IriTemplate getOriginShapeId() {
		return originShapeId;
	}

	public void setOriginShapeId(IriTemplate originShapeId) {
		this.originShapeId = originShapeId;
	}

	public IriTemplate getTargetShapeId() {
		return targetShapeId;
	}

	public IriTemplate getClassId() {
		return classId;
	}

	public void setClassId(IriTemplate classId) {
		this.classId = classId;
	}

	public void setTargetShapeId(IriTemplate targetShapeId) {
		this.targetShapeId = targetShapeId;
	}

	public String getColumnNamespace() {
		return columnNamespace;
	}

	public void setColumnNamespace(String columnNamespace) {
		this.columnNamespace = columnNamespace;
	}

	@Override
	protected void initParse(Reader reader, String baseURI) {
		super.initParse(reader, baseURI);
		pathParser = new MyPathParser(getNamespaceMap(), this.reader);
	}

	public void parse(Reader reader) throws IOException, RDFParseException, RDFHandlerException {
		initParse(reader, "");

		sqlDoc();
	}

	protected void namespace(String prefix, String name) throws RDFHandlerException {

		super.namespace(prefix, name);
		
		if (namespaceManager.findByPrefix(prefix) != null) {
			namespaceManager = new MemoryNamespaceManager(namespaceManager);
		}
		namespaceManager.add(prefix, name);
	}

	/**
	 * <pre>
	 * sqlDoc ::= sqlElement+
	 * </pre>
	 * 
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 */
	private void sqlDoc() throws IOException, RDFParseException, RDFHandlerException {

		int c = next();
		while (c != -1) {
			sqlElement(c);
			c = next();
		}

	}

	/**
	 * <pre>
	 * sqlElement ::= semanticDirective | createTable
	 * </pre>
	 * 
	 * @throws IOException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 */
	private void sqlElement(int c) throws RDFParseException, RDFHandlerException, IOException {
		if (c == '@') {
			semanticDirective(c);
		} else {
			unread(c);
			createTable();
		}

	}

	/**
	 * <pre>
	 * createTable ::= "CREATE" "GLOBAL"? "TEMPORARY"? "TABLE" tableId tableParts tableSemantics?
	 * </pre>
	 */
	private void createTable() throws IOException, RDFParseException, RDFHandlerException {

		assertIgnoreCase("CREATE");
		readSpace();

		if (tryIgnoreCase("GLOBAL")) {
			readSpace();
		}
		if (tryIgnoreCase("TEMPORARY")) {
			readSpace();
		}

		assertIgnoreCase("TABLE");
		readSpace();

		SQLTableSchema table = tableId();
		skipSpace();

		tableParts(table);
		skipSpace();
		tryTableSemantics(table);
		
		afterCreateTable(table);

	}

	private void afterCreateTable(SQLTableSchema table) {

		table.setNamespaceManager(namespaceManager);
		String namespace = table.getColumnNamespace();
		if (namespace == null) {
			namespace = columnNamespace;
		}
		
		for (SQLColumnSchema column : table.listColumns()) {
			URI predicate = column.getColumnPredicate();
			if (predicate == null && namespace!=null) {
				column.setColumnPredicate(valueFactory.createURI(namespace + column.getColumnName()));
			}
		}
		
	}

	/**
	 * <pre>
	 * tableSemantics ::= SEMANTICS tableSemanticStatement (';' tableSemanticStatement)* '.' 
	 * </pre>
	 */
	private boolean tryTableSemantics(SQLTableSchema table) throws IOException, RDFParseException, RDFHandlerException {

		if (tryIgnoreCase("SEMANTICS")) {
			readSpace();
			tableSemanticStatement(table);
			int c=next();
			while (c == ';') {
				skipSpace();
				tableSemanticStatement(table);
				c = next();
			}
			assertEquals('.', c);
			return true;
		}

		return false;

	}

	/**
	 * <pre>
	 * tableSemanticStatement ::= 
	 *     columnNamespaceDirective
	 *   | class
	 *   | physical 
	 *   | logical
	 * </pre>
	 */
	private boolean tableSemanticStatement(SQLTableSchema table) throws RDFParseException, IOException, RDFHandlerException {
		
		return 
			tryColumnNamespaceDirective(table) ||
			tryClass(table) ||
			tryPhysical(table) ||
			tryLogical(table);
	}

	/**
	 * <pre>
	 * logical ::= "LOGICAL" IriPropertyList
	 * </pre>
	 */
	private boolean tryLogical(SQLTableSchema table) throws RDFParseException, RDFHandlerException, IOException {
		if (tryIgnoreCase("LOGICAL")) {
			readSpace();
			Shape shape = readShape();
			table.setPhysicalShape(shape);
			
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * physical ::= "PHYSICAL" IriPropertyList
	 * </pre>
	 */
	private boolean tryPhysical(SQLTableSchema table) throws IOException, RDFParseException, RDFHandlerException {
		if (tryIgnoreCase("PHYSICAL")) {
			readSpace();
			Shape shape = readShape();
			table.setPhysicalShape(shape);
			
			return true;
		}
		return false;
	}

	private Shape readShape() throws RDFParseException, RDFHandlerException, IOException {
		MemoryGraph graph = new MemoryGraph();
		RDFHandler save = rdfHandler;
		
		GraphLoadHandler handler = new GraphLoadHandler(graph);
		if (rdfHandler == null) {
			rdfHandler = handler;
		} else {
			rdfHandler = new CompositeRdfHandler(rdfHandler, handler);
		}
		
		URI shapeId = iriPropertyList();
		graph.edge(shapeId, RDF.TYPE, SH.Shape);
		shapeLoader.load(graph);
		rdfHandler = save;
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		if (shape == null) {
			StringBuilder err = err();
			err.append("Failed to parse shape <");
			err.append(shapeId.stringValue());
			err.append(">");
			fail(err);
		}
		
		
		return shape;
	}

	/**
	 * <pre>
	 * class ::= "CLASS"  iri
	 * </pre>
	 */
	private boolean tryClass(SQLTableSchema table) throws IOException, RDFParseException, RDFHandlerException {
		if (tryIgnoreCase("CLASS")) {
			readSpace();
			table.setTargetClass(iri());
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * tableParts ::= '(' tablePart (',' tablePart)* ')'
	 * </pre>
	 */
	private void tableParts(SQLTableSchema table) throws RDFParseException, IOException {

		read('(');
		skipSpace();
		tablePart(table);

		int c = next();
		while (c == ',') {
			skipSpace();
			tablePart(table);
			c = next();
		}

		assertEquals(')', c);

	}

	/**
	 * <pre>
	 * tablePart ::= columnDef | tableConstraint
	 * </pre>
	 * 
	 * @throws IOException
	 * @throws RDFParseException
	 */
	private void tablePart(SQLTableSchema table) throws RDFParseException, IOException {

		if (!tryTableConstraint(table)) {
			columnDef(table);
		}

	}

	/**
	 * <pre>
	 * columnDef ::= columnName columnType columnConstraintDef* columnSemantics?
	 *           ::= id         columnType columnConstraintDef* columnSemantics?
	 * </pre>
	 * 
	 * @throws IOException
	 * @throws RDFParseException
	 */
	private void columnDef(SQLTableSchema table) throws RDFParseException, IOException {

		String columnName = id();
		readSpace();

		SQLColumnType columnType = columnType();

		SQLColumnSchema column = getOrCreateColumn(table, columnName, columnType);

		do {
			skipSpace();

		} while (tryColumnConstraintDef(column));

		tryColumnSemantics(column);

	}

	/**
	 * <pre>
	 * columnSemantics ::= SEMANTICS columnSemanticStatement (';' columnSemanticStatement)* 
	 * </pre>
	 */
	private boolean tryColumnSemantics(SQLColumnSchema column) throws IOException, RDFParseException {
		if (tryIgnoreCase("SEMANTICS")) {
			readSpace();
			columnSemanticsStatement(column);
			
			int c = next();
			while (c == ';') {
				skipSpace();
				columnSemanticsStatement(column);
				c = next();
			}
			unread(c);
			
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * columnSemanticStatement ::= columnNamespace | columnPath 
	 * </pre>
	 */
	private void columnSemanticsStatement(SQLColumnSchema column) throws RDFParseException, IOException {

		boolean ok =
			tryColumnNamespace(column) ||
			tryColumnPath(column);
		
		if (!ok) {
			fail("Expected 'NAMESPACE' or 'PATH'");
		}
		
	}

	/**
	 * <pre>
	 * columnPath ::= "PATH" path
	 * </pre>
	 * @throws RDFParseException 
	 */
	private boolean tryColumnPath(SQLColumnSchema column) throws IOException, RDFParseException {
		if (tryIgnoreCase("PATH")) {
			readSpace();
			Path path = pathParser.path(reader);
			column.setEquivalentPath(path);
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * columnNamespace ::= "NAMESPACE" (PN_PREFIX | iriRef)
	 * </pre>
	 */
	private boolean tryColumnNamespace(SQLColumnSchema column) throws IOException, RDFParseException {
		
		if (tryIgnoreCase("NAMESPACE")) {
			readSpace();
			int c = next();
			String namespace = null;
			if (c == '<') {
				namespace = iriRef(c);
			} else {
				unread(c);
				String prefix = pn_prefix();
				namespace = getNamespaceMap().get(prefix);
				if (namespace == null) {
					StringBuilder err = err();
					err.append("Namespace not defined for prefix '");
					err.append(prefix);
					err.append("'");
					fail(err);
				}
			}
			URI predicate = valueFactory.createURI(namespace + column.getColumnName());
			column.setColumnPredicate(predicate);
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * columnConstraintDef ::= constraintNameDef? columnConstraint
	 * </pre>
	 * 
	 * @throws IOException
	 * @throws RDFParseException
	 */
	private boolean tryColumnConstraintDef(SQLColumnSchema column) throws RDFParseException, IOException {
		String constraintName = tryConstraintNameDef();
		return tryColumnConstraint(column, constraintName);
	}

	/**
	 * <pre>
	 * columnConstraint ::= nullConstraint | notNull | uniqueSpec
	 * </pre>
	 * 
	 * @throws IOException
	 * @throws RDFParseException
	 */
	private boolean tryColumnConstraint(SQLColumnSchema column, String constraintName)
			throws IOException, RDFParseException {
		boolean ok = tryNullConstraint(column, constraintName) || tryNotNull(column, constraintName)
				|| tryUniqueSpec(column, constraintName);

		if (constraintName != null && !ok) {
			fail("Expected 'NULL' | 'NOT' 'NULL' | 'PRIMARY' 'KEY'");
		}

		return ok;
	}

	/**
	 * <pre>
	 * uniqueSpec ::= columnUnique | columnPrimaryKey
	 * </pre>
	 */
	private boolean tryUniqueSpec(SQLColumnSchema column, String constraintName) throws IOException, RDFParseException {

		return tryColumnUnique(column, constraintName) || tryColumnPrimaryKey(column, constraintName);
	}

	/**
	 * <pre>
	 * columnPrimaryKey ::= "PRIMARY" "KEY"
	 * </pre>
	 */
	private boolean tryColumnPrimaryKey(SQLColumnSchema column, String constraintName)
			throws IOException, RDFParseException {
		if (tryIgnoreCase("PRIMARY")) {
			readSpace();
			assertIgnoreCase("KEY");

			PrimaryKeyConstraint constraint = new PrimaryKeyConstraint(constraintName);
			column.setPrimaryKey(constraint);

			return true;
		}
		return false;
	}

	protected boolean tryIgnoreCase(String text) throws IOException {

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			int k = read();
			if (Character.toUpperCase((int) c) == Character.toUpperCase(k)) {
				BUFFER[i] = (char) k;
			} else {
				unread(k);
				for (int j = i - 1; j >= 0; j--) {
					unread(BUFFER[j]);
				}
				return false;
			}
		}
		if (nextIsIdChar()) {

			for (int j = text.length() - 1; j >= 0; j--) {
				unread(BUFFER[j]);
			}
			return false;
		}
		return true;
	}

	private boolean nextIsIdChar() throws IOException {
		int c = peek();
		return isLetter(c) || isDigit(c) || c == '_';
	}

	/**
	 * <pre>
	 * columnUnique ::= UNIQUE
	 * </pre>
	 * 
	 * @throws IOException
	 */
	private boolean tryColumnUnique(SQLColumnSchema column, String constraintName) throws IOException {
		if (tryIgnoreCase("UNIQUE")) {
			column.setUnique(new UniqueConstraint(constraintName));
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * notNull ::= "NOT" "NULL"
	 * </pre>
	 */
	private boolean tryNotNull(SQLColumnSchema column, String constraintName) throws IOException, RDFParseException {
		if (tryIgnoreCase("NOT")) {
			readSpace();
			assertIgnoreCase("NULL");
			column.setNotNull(new NotNullConstraint(constraintName));
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * nullConstraint ::= "NULL"
	 * </pre>
	 */
	private boolean tryNullConstraint(SQLColumnSchema column, String constraintName) throws IOException {

		if (tryIgnoreCase("NULL")) {
			column.setNullConstraint(new NullConstraint(constraintName));
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * constraintNameDef ::= "CONSTRAINT" constraintName 
	 *                   ::= "CONSTRAINT" id
	 * </pre>
	 */
	private String tryConstraintNameDef() throws IOException, RDFParseException {
		String result = null;
		if (tryIgnoreCase("CONSTRAINT")) {
			readSpace();
			result = id();
			skipSpace();
		}
		return result;
	}

	private SQLColumnSchema getOrCreateColumn(SQLTableSchema table, String columnName, SQLColumnType columnType) {
		SQLColumnSchema column = table.getColumnByName(columnName);
		if (column == null) {
			column = new SQLColumnSchema(table, columnName, columnType);
		}
		return column;
	}

	/**
	 * <pre>
	 * columnType ::= datatype columnSize? 
	 *            ::= datatype ( '(' sizeValue (',' precision)? ')' )?
	 *            ::= datatype ( '(' DIGIT+ (',' precision)? ')' )?
	 * </pre>
	 * 
	 * @throws IOException
	 * @throws RDFParseException
	 */
	private SQLColumnType columnType() throws IOException, RDFParseException {
		SQLDatatype datatype = datatype();

		SQLColumnType result = new SQLColumnType();
		result.setDatatype(datatype);

		int c = next();
		if (c == '(') {
			skipSpace();
			Integer sizeValue = digits();
			result.setSize(sizeValue);

			c = next();
			if (c == ',') {
				skipSpace();
				Integer precision = precision();
				result.setPrecision(precision);
				c = next();
			}

			assertEquals(')', c);

		} else {
			unread(c);
		}

		return result;
	}

	/**
	 * <pre>
	 * precision ::= DIGIT+
	 * </pre>
	 * 
	 * @throws IOException
	 * @throws RDFParseException
	 */
	private Integer precision() throws RDFParseException, IOException {
		return digits();
	}

	private Integer digits() throws IOException, RDFParseException {
		StringBuilder builder = buffer();
		int c = read();
		while (isDigit(c)) {
			builder.appendCodePoint(c);
			c = read();
		}
		unread(c);

		if (builder.length() == 0) {
			fail("Expected DIGIT");
		}

		return Integer.parseInt(builder.toString());
	}

	/**
	 * <pre>
	 * datatype ::= BIGINT | BINARY | BIT | CHAR |  DATE | DATETIME | DATETIME2 | DECIMAL | FLOAT |
	 *   IMAGE | INT | NCHAR | NTEXT | NUMERIC | NVARCHAR |REAL | SMALLDATETIME |
	 *   SMALLINT | TEMPORARY | TEXT | TIME | TIMESTAMP | TINYINT |
	 *   UNIQUEIDENTIFIER | VARBINARY | VARCHAR | XML
	 * </pre>
	 */
	private SQLDatatype datatype() throws IOException, RDFParseException {

		for (SQLDatatype type : SQLDatatype.values()) {
			String text = type.name();

			if (tryIgnoreCase(text)) {
				return type;
			}
		}

		StringBuilder err = err();
		err.append("Invalid SQL Datatype.  Expected ");
		String delim = "";
		for (SQLDatatype type : SQLDatatype.values()) {
			err.append(delim);
			delim = " | ";
			String text = type.name();
			err.append("'");
			err.append(text);
			err.append("'");
		}
		fail(err);
		return null;
	}

	/**
	 * <pre>
	 * tableConstraint ::= constraintNameDef? tableConstraintPhrase 
	 * </pre>
	 * @throws IOException 
	 * @throws RDFParseException 
	 */
	private boolean tryTableConstraint(SQLTableSchema table) throws RDFParseException, IOException {
		String constraintName = tryConstraintNameDef();
		return tryTableConstraintPhrase(table, constraintName);
	}

	/**
	 * <pre>
	 * tableConstraintPhrase ::= tablePrimaryKey | uniqueKeyConstraint | tableForeignKey
	 * </pre>
	 * @throws RDFParseException 
	 * @throws IOException 
	 */
	private boolean tryTableConstraintPhrase(SQLTableSchema table, String constraintName) throws RDFParseException, IOException {
		boolean ok = 
			tryTablePrimaryKey(table, constraintName) ||
			tryUniqueKeyConstraint(table, constraintName) ||
			tryTableForeignKey(table, constraintName);
		
		if (constraintName != null && !ok) {
			fail("Expected 'PRIMARY' 'KEY' | 'UNIQUE' | 'FOREIGN' 'KEY'");
		}
		return ok;
	}

	/**
	 * <pre>
	 * tableForeignKey ::= "FOREIGN" "KEY"  referencingColumnList referencesClause
	 *                 ::= "FOREIGN" "KEY"  columnList referencesClause
	 * </pre>
	 */
	private boolean tryTableForeignKey(SQLTableSchema table, String constraintName) throws IOException, RDFParseException {
		if (tryIgnoreCase("FOREIGN")) {
			readSpace();
			assertIgnoreCase("KEY");
			skipSpace();
			
			ForeignKeyConstraint fk = new ForeignKeyConstraint(constraintName);
			table.addConstraint(fk);
			fk.setSource(columnList(table));
			skipSpace();
			referencesClause(fk);
			return true;
		}
		return false;
	}


	/**
	 * <pre>
	 * columnList ::= '(' simpleColumnName ( ',' simpleColumnName )* ')' 
	 *            ::= '(' id ( ',' id )* ')' 
	 * </pre>
	 * @throws IOException 
	 */
	private List<SQLColumnSchema> columnList(SQLTableSchema table) throws RDFParseException, IOException {
		List<SQLColumnSchema> list = new ArrayList<>();
		
		read('(');
		
		String columnName = id();
		
		list.add(getOrCreateColumn(table, columnName, null));
		
		int c = next();
		while (c == ',') {
			skipSpace();
			columnName = id();
			list.add(getOrCreateColumn(table, columnName, null));
			c = next();
		}
		
		assertEquals(')', c);
		
		if (list.isEmpty()) {
			fail("Referencing column list cannot be empty");
		}
		return list;
	}

	

	/**
	 * <pre>
	 * referencesClause ::= REFERENCES tableRef columnList
	 * </pre>
	 * @throws IOException 
	 * @throws RDFParseException 
	 */
	private void referencesClause(ForeignKeyConstraint fk) throws RDFParseException, IOException {
		
		assertIgnoreCase("REFERENCES");
		readSpace();
		SQLTableSchema refTable = tableRef();
		List<SQLColumnSchema> columnList = columnList(refTable);
		
		fk.setTarget(columnList);
		
	}

	/**
	 * <pre>
	 * uniqueKeyConstraint ::= UNIQUE columnList 
	 * </pre>
	 */
	private boolean tryUniqueKeyConstraint(SQLTableSchema table, String constraintName) throws IOException, RDFParseException {
		if (tryIgnoreCase("UNIQUE")) {
			skipSpace();
			
			UniqueConstraint unique = new UniqueConstraint(constraintName);
			unique.setColumnList(columnList(table));
			table.addConstraint(unique);
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * tablePrimaryKey ::= "PRIMARY" "KEY" columnList
	 * </pre> 
	 */
	private boolean tryTablePrimaryKey(SQLTableSchema table, String constraintName) throws IOException, RDFParseException {
		
		if (tryIgnoreCase("PRIMARY")) {
			readSpace();
			assertIgnoreCase("KEY");
			readSpace();
			List<SQLColumnSchema> columnList = columnList(table);
			PrimaryKeyConstraint pk = new PrimaryKeyConstraint(constraintName);
			pk.setColumnList(columnList);
			table.addConstraint(pk);
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * tableId ::= tableRef
	 * </pre>
	 * 
	 * @throws IOException
	 * @throws RDFParseException
	 */
	private SQLTableSchema tableId() throws IOException, RDFParseException {
		return tableRef();
	}

	/**
	 * <pre>
	 * tableRef ::= (schemaName '.')? tableName 
	 *          ::= (id '.') ? id
	 * </pre>
	 * 
	 * @throws IOException
	 * @throws RDFParseException
	 */
	private SQLTableSchema tableRef() throws IOException, RDFParseException {

		String schemaName = id();
		String tableName = null;

		int c = next();

		if (c == '.') {
			tableName = id();
		} else {
			unread(c);
			tableName = schemaName;
		}

		SQLSchema schema = getOrCreateSchema(schemaName);

		SQLTableSchema table = getOrCreateTable(schema, tableName);

		return table;

	}

	private SQLTableSchema getOrCreateTable(SQLSchema schema, String tableName) {
		SQLTableSchema table = schema.getTableByName(tableName);
		if (table == null) {
			table = new SQLTableSchema(schema, tableName);
		}
		return table;
	}

	private SQLSchema getOrCreateSchema(String schemaName) {
		if (schemaName == null) {
			schemaName = SQLSchemaManager.DEFAULT_SCHEMA_NAME;
		}
		SQLSchema schema = sqlSchemaManager.getSchemaByName(schemaName);
		if (schema == null) {
			schema = new SQLSchema(schemaName);
			sqlSchemaManager.add(schema);
		}
		return schema;
	}

	/**
	 * <pre>
	 * id ::= (LETTER | '_' ) (LETTER | DIGIT | '_' )*
	 * </pre>
	 */
	private String id() throws IOException, RDFParseException {

		StringBuilder builder = buffer();
		int c = read();
		if (!isLetter(c) && c != '_') {
			StringBuilder err = err();
			err.append("Expected LETTER | '_' but found '");
			appendCodePoint(err, c);
			err.append("'");
			fail(err);
		}
		builder.appendCodePoint(c);
		c = read();
		while (isLetter(c) || isDigit(c) || c == '_') {
			builder.appendCodePoint(c);
			c = read();
		}
		unread(c);

		return builder.toString();
	}

	/**
	 * <pre>
	 * semanticDirective ::=
	 *   prefixID 
	 *   | classDirective 
	 *   | originShapeDirective
	 *   | targetShapeDirective
	 *   | columnNamespaceDirective
	 * </pre>
	 * 
	 * @param c
	 * @throws IOException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 */
	private void semanticDirective(int c) throws IOException, RDFParseException, RDFHandlerException {

		if (!tryPrefixID(c) && ((classId = tryClassDirective(c)) == null)
				&& ((originShapeId = tryOriginShapeDirective(c)) == null)
				&& ((targetShapeId = tryTargetShapeDirective(c)) == null) && !tryColumnNamespaceDirective(c)) {
			fail("Undefined semantic directive. Expected '@prefix' | '@class' | '@originShape' | '@targetShape' | '@columnNamespace'.");
		}

	}
	
	private boolean tryColumnNamespaceDirective(SQLTableSchema table) throws IOException, RDFParseException {
		
		String save = columnNamespace;
		
		int c = next();
		if (c == '@' && tryColumnNamespaceDirective(c)) {
			table.setColumnNamespace(columnNamespace);
			columnNamespace = save;
			return true;
		}
		unread(c);
		return false;
	}

	/**
	 * <pre>
	 * columnNamespaceDirective ::= '@columnNamespace' (PN_PREFIX | iriRef)
	 * </pre>
	 */
	private boolean tryColumnNamespaceDirective(int c) throws IOException, RDFParseException {

		if (tryWord("columnNamespace")) {
			columnNamespaceDirective();
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * columnNamespaceDirective ::= '@columnNamespace' (PN_PREFIX | iriRef)
	 * </pre>
	 * 
	 * This method assumes that '@columnNamespace' has been read.
	 */
	private void columnNamespaceDirective() throws RDFParseException, IOException {
		assertWhitespace();

		int c = next();
		if (c == '<') {
			columnNamespace = iriRef(c);
		} else {
			unread(c);
			String prefix = pn_prefix();
			columnNamespace = getNamespaceMap().get(prefix);
			if (columnNamespace == null) {
				StringBuilder err = err();
				err.append("Namespace not defined for prefix '");
				err.append(prefix);
				err.append("'");
				fail(err);
			}
		}

	}

	/**
	 * <pre>
	 * originShapeDirective ::= '@originShape' iriTemplate
	 * </pre>
	 */
	private IriTemplate tryOriginShapeDirective(int c) throws IOException, RDFParseException {

		if (c == '@' && tryWord("originShape")) {
			assertWhitespace();
			return iriTemplate();
		}

		return null;
	}

	/**
	 * <pre>
	 * targetShapeDirective ::= '@targetShape' iriTemplate
	 * </pre>
	 * 
	 * @throws IOException
	 * @throws RDFParseException
	 */
	private IriTemplate tryTargetShapeDirective(int c) throws IOException, RDFParseException {
		if (c == '@' && tryWord("targetShape")) {
			assertWhitespace();
			return iriTemplate();
		}
		return null;
	}

	/**
	 * <pre>
	 * classDirective ::= '@class' iriTemplate
	 * </pre>
	 * 
	 * @throws IOException
	 * @throws RDFParseException
	 */
	private IriTemplate tryClassDirective(int c) throws IOException, RDFParseException {
		if (c == '@' && tryWord("class")) {
			assertWhitespace();
			return iriTemplate();
		}
		return null;
	}

	/**
	 * <pre>
	 * prefixID	::=	'@prefix' PNAME_NS IRIREF '.'
	 * </pre>
	 */
	private boolean tryPrefixID(int c) throws IOException, RDFParseException, RDFHandlerException {
		if (tryWord("prefix")) {
			prefixID();
			return true;
		}
		return false;
	}

	static class TableRef {

		String projectName;
		String schemaName;
		String tableName;
	}
	
	private static class MyPathParser extends PathParser {

		private MyPathParser(NamespaceMap map, PushbackReader reader) {
			super(map, reader);
		}
		
		protected boolean done(int c) throws IOException {
			if (c==-1) {
				return true;
			};
			if (c==',' || c==';' || c==')') {
				unread(c);
				return true;
			}
			return false;
		}
		
	}
}
