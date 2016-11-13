package io.konig.spreadsheet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OwlVocab;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.VANN;

/**
 * A utility that loads the contents of a workbook into a Graph.
 * @author Greg McFall
 *
 */
public class WorkbookLoader {
	private static final URI LABEL = RDFS.LABEL;
	private static final int UNDEFINED = -1;
	private static final String ONTOLOGY_NAME = "Ontology Name";
	private static final String COMMENT = "Comment";
	private static final String NAMESPACE_URI = "Namespace URI";
	private static final String PREFIX = "Prefix";
	
	private static final String CLASS_NAME = "Class Name";
	private static final String CLASS_ID = "Class Id";
	private static final String CLASS_SUBCLASS_OF = "Subclass Of";
	
	private static final String PROPERTY_NAME = "Property Name";
	private static final String PROPERTY_ID = "Property Id";
	private static final String DOMAIN = "Domain";
	private static final String RANGE = "Range";
	private static final String INVERSE_OF = "Inverse Of";
	private static final String PROPERTY_TYPE = "Property Type";
	
	private static final String INDIVIDUAL_NAME = "Individual Name";
	private static final String INDIVIDUAL_ID = "Individual Id";
	private static final String INDIVIDUAL_TYPE = "Individual Type";
	
	private static final String SHAPE_ID = "Shape Id";
	private static final String SCOPE_CLASS = "Scope Class";
	private static final String MEDIA_TYPE = "Media Type";
	private static final String INPUT_CLASS = "Input Class";
	
	private static final String VALUE_TYPE = "Value Type";
	private static final String MIN_COUNT = "Min Count";
	private static final String MAX_COUNT = "Max Count";
	private static final String UNIQUE_LANG = "Unique Lang";
	private static final String VALUE_CLASS = "Value Class";
	private static final String STEREOTYPE = "Stereotype";
	private static final String VALUE_IN = "Value In";
	private static final String EQUIVALENT_PATH = "Equivalent Path";
	
	private static final String UNBOUNDED = "unbounded";
	
	private static final int ONTOLOGY_FLAG = 0x1;
	private static final int CLASS_FLAG = 0x2;
	private static final int PROPERTY_FLAG = 0x4;
	private static final int INDIVIDUAL_FLAG = 0x8;
	private static final int SHAPE_FLAG = 0x10;
	private static final int CONSTRAINT_FLAG = 0x14;
	
	private static final String[] CELL_TYPE = new String[6];
	static {
		CELL_TYPE[Cell.CELL_TYPE_BLANK] = "Blank";
		CELL_TYPE[Cell.CELL_TYPE_BOOLEAN] = "Boolean";
		CELL_TYPE[Cell.CELL_TYPE_ERROR] = "Error";
		CELL_TYPE[Cell.CELL_TYPE_FORMULA] = "Formula";
		CELL_TYPE[Cell.CELL_TYPE_NUMERIC] = "Number";
		CELL_TYPE[Cell.CELL_TYPE_STRING] = "String";
	}
	
	
	private NamespaceManager nsManager;
	private ValueFactory vf = new ValueFactoryImpl();
	
	public WorkbookLoader(NamespaceManager nsManager) {
		this.nsManager = nsManager;
		nsManager.add("vann", "http://purl.org/vocab/vann/");
	}

	public void load(Workbook book, Graph graph) throws SpreadsheetException {
		Worker worker = new Worker(book, graph);
		worker.run();
	}
	
	private class Worker {
		private Workbook book;
		private Graph graph;
		
		private int ontologyNameCol=UNDEFINED;
		private int ontologyCommentCol=UNDEFINED;
		private int namespaceUriCol=UNDEFINED;
		private int prefixCol=UNDEFINED;
		
		private int classNameCol = UNDEFINED;
		private int classCommentCol = UNDEFINED;
		private int classIdCol = UNDEFINED;
		private int classSubclassOfCol = UNDEFINED;
		
		private int propertyNameCol = UNDEFINED;
		private int propertyIdCol = UNDEFINED;
		private int domainCol = UNDEFINED;
		private int rangeCol = UNDEFINED;
		private int inverseOfCol = UNDEFINED;
		private int propertyTypeCol = UNDEFINED;
		private int propertyCommentCol = UNDEFINED;
		
		private int individualNameCol = UNDEFINED;
		private int individualCommentCol = UNDEFINED;
		private int individualIdCol = UNDEFINED;
		private int individualTypeCol = UNDEFINED;
		
		private int shapeIdCol = UNDEFINED;
		private int shapeCommentCol = UNDEFINED;
		private int shapeScopeCol = UNDEFINED;
		private int shapeInputClassCol = UNDEFINED;
		private int shapeMediaTypeCol = UNDEFINED;
		
		private int pcShapeIdCol = UNDEFINED;
		private int pcPropertyIdCol = UNDEFINED;
		private int pcValueTypeCol = UNDEFINED;
		private int pcMinCountCol = UNDEFINED;
		private int pcMaxCountCol = UNDEFINED;
		private int pcUniqueLangCol = UNDEFINED;
		private int pcValueClassCol = UNDEFINED;
		private int pcValueInCol = UNDEFINED;
		private int pcCommentCol = UNDEFINED;
		private int pcPredicateKindCol = UNDEFINED;
		private int pcEquivalentPathCol = UNDEFINED;
		
		public Worker(Workbook book, Graph graph) {
			this.book = book;
			this.graph = graph;
		}
		
		private void run() throws SpreadsheetException {
			for (int i=0; i<book.getNumberOfSheets(); i++) {
				Sheet sheet = book.getSheetAt(i);
				loadSheet(sheet);
			}
		}

		private void loadSheet(Sheet sheet) throws SpreadsheetException {
			Row header = sheet.getRow(sheet.getFirstRowNum());
			
			int colSize = header.getLastCellNum()+1;
			
			int bits = 0;
			
			for (int i=header.getFirstCellNum(); i<colSize; i++) {
				Cell cell = header.getCell(i);
				if (cell == null) continue;
				String name = cell.getStringCellValue();
				
				switch (name) {
				case NAMESPACE_URI : bits = bits | ONTOLOGY_FLAG; break;
				case CLASS_ID : bits = bits | CLASS_FLAG; break;
				case PROPERTY_ID : bits = bits | PROPERTY_FLAG; break;
				case INDIVIDUAL_ID : bits = bits | INDIVIDUAL_FLAG; break;
				case SHAPE_ID : bits = bits | SHAPE_FLAG; break;
				}
			}
			
			switch (bits) {
			case ONTOLOGY_FLAG : loadOntologies(sheet); break;
			case CLASS_FLAG : loadClasses(sheet); break;
			case PROPERTY_FLAG : loadProperties(sheet); break;
			case INDIVIDUAL_FLAG : loadIndividuals(sheet); break;
			case SHAPE_FLAG :loadShapes(sheet); break;
			case CONSTRAINT_FLAG : loadPropertyConstraints(sheet);
			
			}
			
		}
		
		

		private void loadPropertyConstraints(Sheet sheet) throws SpreadsheetException {
			readPropertyConstraintHeader(sheet);
			int rowSize = sheet.getLastRowNum()+1;
			
			for (int i=sheet.getFirstRowNum()+1; i<rowSize; i++) {
				Row row = sheet.getRow(i);
				loadPropertyConstraintRow(row);
			}
			
		}

		private void loadPropertyConstraintRow(Row row) throws SpreadsheetException {
			URI shapeId = uriValue(row, pcShapeIdCol);
			if (shapeId==null) return;
			URI propertyId = uriValue(row, pcPropertyIdCol);
			if (propertyId==null) return;
			Literal comment = stringLiteral(row, pcCommentCol);
			URI valueType = uriValue(row, pcValueTypeCol);
			Literal minCount = intLiteral(row, pcMinCountCol);
			Literal maxCount = intLiteral(row, pcMaxCountCol);
			URI valueClass = uriValue(row, pcValueClassCol);
			URI predicateKind = uriValue(row, pcPredicateKindCol);
			List<Value> valueIn = valueList(row, pcValueInCol);
			Literal uniqueLang = booleanLiteral(row, pcUniqueLangCol);
			Literal equivalentPath = stringLiteral(row, pcEquivalentPathCol);
			
				
			if (Konig.id.equals(propertyId)) {
				int min = minCount==null ? 0 : minCount.intValue();
				int max = maxCount==null ? -1 : maxCount.intValue();
				
				if (max > 1) {
					String msg = MessageFormat.format(
						"Invalid maxCount for property konig:id on Shape <{0}>: must be less than or equal to 1.",
						shapeId);
					throw new SpreadsheetException(msg);
				}
								
				URI nodeKind =	min==0 ? SH.BlankNodeOrIRI : SH.IRI ;
				edge(shapeId, SH.nodeKind, nodeKind);
				return;
			}
			
			Resource constraint = graph.vertex().getId();
			
			edge(shapeId, SH.property, constraint);
			edge(constraint, SH.predicate, propertyId);
			edge(constraint, RDFS.COMMENT, comment);
			
			if (valueClass != null && (valueType==null || XMLSchema.ANYURI.equals(valueType))) {
				edge(constraint, SH.valueClass, valueClass);
				edge(constraint, SH.nodeKind, SH.IRI);
				edge(valueClass, RDF.TYPE, OWL.CLASS);
			} else if (isDatatype(valueType)) {
				edge(constraint, SH.datatype, valueType);
			} else {
				edge(constraint, SH.valueShape, valueType);
			}
			
			edge(constraint, SH.minCount, minCount);
			edge(constraint, SH.maxCount, maxCount);
			edge(constraint, SH.uniqueLang, uniqueLang);
			edge(constraint, SH.in, valueIn);
			edge(constraint, Konig.equivalentPath, equivalentPath);
			edge(constraint, Konig.stereotype, predicateKind);
			
		}

		private void edge(Resource subject, URI predicate, List<Value> object) {
			if (subject!=null && object!=null) {
				Vertex first = null;
				Vertex prev = null;
				
				for (Value value : object) {
					Vertex list = graph.vertex();
					if (first == null) {
						first = list;
						graph.edge(subject, predicate, list.getId());
					}
					if (prev != null) {
						graph.edge(prev.getId(), RDF.REST, list.getId());
					}
					graph.edge(list.getId(), RDF.FIRST, value);
					prev = list;
				}
				if (prev != null) {
					graph.edge(prev.getId(), RDF.REST, RDF.NIL);
				}
				
			}
			
		}

		private List<Value> valueList(Row row, int column) throws SpreadsheetException {
			if (column > 0) {
				String text = stringValue(row, column);
				if (text != null) {
					StringTokenizer tokens = new StringTokenizer(text, " \r\n\t");
					List<Value> list = new ArrayList<>();
					while (tokens.hasMoreTokens()) {
						URI curie = expandCurie(tokens.nextToken());
						list.add(curie);
					}
					return list;
				}
				
			}
			return null;
		}

		private void edge(Resource subject, URI predicate, Value object) {
			if (subject != null && object!=null) {
				graph.edge(subject, predicate, object);
			}
			
		}

		private Literal booleanLiteral(Row row, int col) {
			String text = stringValue(row, col);
			if (text != null) {
				text.trim();
				if (text.length() > 0) {
					return vf.createLiteral(text, XMLSchema.BOOLEAN);
				}
			}
			return null;
		}

		private Literal intLiteral(Row row, int column) throws SpreadsheetException {
			
			Literal literal = null;
			if (column>=0) {
				Cell cell = row.getCell(column);
				if (cell != null) {
					
					int cellType = cell.getCellType();
					if (cellType==Cell.CELL_TYPE_STRING) {
						String value = cell.getStringCellValue();
						if (UNBOUNDED.equalsIgnoreCase(value)) {
							return null;
						}
					}
						
					if (cellType==Cell.CELL_TYPE_NUMERIC) {
						int value = (int) cell.getNumericCellValue();
						literal = vf.createLiteral(value);
					} else if (cellType == Cell.CELL_TYPE_BLANK) {
						return null;
					} else {
						String pattern = "Expected integer value in cell ({0}, {1}) on sheet {2} but found {3}";
						String typeName = CELL_TYPE[cellType];
						String msg = MessageFormat.format(
							pattern, row.getRowNum(), column, row.getSheet().getSheetName(), typeName);
						
						
						throw new SpreadsheetException(msg);
					}
				}
			}
			return literal;
		}

		private void readPropertyConstraintHeader(Sheet sheet) {
			pcShapeIdCol = pcCommentCol = pcPropertyIdCol = pcValueTypeCol = pcMinCountCol = pcMaxCountCol = pcUniqueLangCol =
				pcValueClassCol = pcPredicateKindCol = UNDEFINED;
				
			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);
			
			int colSize = row.getLastCellNum() + 1;
			for (int i=row.getFirstCellNum(); i<colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();
					
					switch (text) {
					case SHAPE_ID :  pcShapeIdCol = i; break;
					case COMMENT : pcCommentCol = i; break;
					case PROPERTY_ID : pcPropertyIdCol = i; break;
					case VALUE_TYPE : pcValueTypeCol = i; break;
					case MIN_COUNT : pcMinCountCol = i; break;
					case MAX_COUNT : pcMaxCountCol = i; break;
					case UNIQUE_LANG : pcUniqueLangCol = i; break;
					case VALUE_CLASS : pcValueClassCol = i; break;
					case VALUE_IN :	pcValueInCol = i; break;
					case STEREOTYPE : pcPredicateKindCol = i; break;
					case EQUIVALENT_PATH : pcEquivalentPathCol = i; break;
						
					}
				}
			}
			
			
		}

		private void loadShapes(Sheet sheet) throws SpreadsheetException {
			
			readShapeHeader(sheet);

			int rowSize = sheet.getLastRowNum()+1;
			
			for (int i=sheet.getFirstRowNum()+1; i<rowSize; i++) {
				Row row = sheet.getRow(i);
				loadShapeRow(row);
			}
			
		}

		private void loadShapeRow(Row row) throws SpreadsheetException {
			
			URI shapeId = uriValue(row, shapeIdCol);
			Literal shapeComment = stringLiteral(row, shapeCommentCol);
			URI targetClass = uriValue(row, shapeScopeCol);
			URI inputClass = uriValue(row, shapeInputClassCol);
			Literal mediaType = stringLiteral(row, shapeMediaTypeCol);
			
			if (shapeId == null) {
				return;
			}
			
			edge(shapeId, RDF.TYPE, SH.Shape);
			edge(shapeId, RDFS.COMMENT, shapeComment);
			edge(shapeId, SH.targetClass, targetClass);
			edge(shapeId, Konig.inputClass, inputClass);
			edge(shapeId, Konig.mediaTypeBaseName, mediaType);
			
		}

		private void readShapeHeader(Sheet sheet) {
			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);
			
			int colSize = row.getLastCellNum() + 1;
			for (int i=row.getFirstCellNum(); i<colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();
					
					switch (text) {
					case SHAPE_ID :  shapeIdCol = i; break;
					case COMMENT : shapeCommentCol = i; break;
					case SCOPE_CLASS : shapeScopeCol = i; break;
					case INPUT_CLASS : shapeInputClassCol = i; break;
					case MEDIA_TYPE : shapeMediaTypeCol = i; break;
						
					}
				}
			}
			
		}

		private void loadIndividuals(Sheet sheet) throws SpreadsheetException {
						
			readIndividualHeader(sheet);
			int rowSize = sheet.getLastRowNum()+1;
			
			for (int i=sheet.getFirstRowNum()+1; i<rowSize; i++) {
				Row row = sheet.getRow(i);
				loadIndividualRow(row);
			}
			
		}

		private void loadIndividualRow(Row row) throws SpreadsheetException {
			
			Literal label = stringLiteral(row, individualNameCol);
			Literal comment = stringLiteral(row, individualCommentCol);
			URI individualId = uriValue(row, individualIdCol);
			List<URI> typeList = uriList(row, individualTypeCol);
			if (individualId == null) {
				return;
			}
			graph.edge(individualId, RDF.TYPE, OwlVocab.NamedIndividual);
			if (typeList != null) {
				for (URI value : typeList) {
					if (!value.equals(OwlVocab.NamedIndividual)) {
						graph.edge(individualId, RDF.TYPE, value);
					}
				}
			}
			
			if (label != null) {
				graph.edge(individualId, LABEL, label);
			}
			if (comment != null) {
				graph.edge(individualId, RDFS.COMMENT, comment);
			}
		}

		private void readIndividualHeader(Sheet sheet) {

			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);
			
			int colSize = row.getLastCellNum() + 1;
			for (int i=row.getFirstCellNum(); i<colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();
					
					switch (text) {
					case INDIVIDUAL_NAME :  individualNameCol = i; break;
					case COMMENT : individualCommentCol = i; break;
					case INDIVIDUAL_ID : individualIdCol = i; break;
					case INDIVIDUAL_TYPE : individualTypeCol = i; break;
						
					}
				}
			}
			
		}

		private void loadProperties(Sheet sheet) throws SpreadsheetException {
			
			readPropertyHeader(sheet);
			
			int rowSize = sheet.getLastRowNum()+1;
			
			for (int i=sheet.getFirstRowNum()+1; i<rowSize; i++) {
				Row row = sheet.getRow(i);
				loadPropertyRow(row);
			}
			
		}

		private void loadPropertyRow(Row row) throws SpreadsheetException {
			
			Literal propertyName = stringLiteral(row, propertyNameCol);
			Literal comment = stringLiteral(row, propertyCommentCol);
			URI propertyId = uriValue(row, propertyIdCol);
			List<URI> domain = uriList(row, domainCol);
			List<URI> range = uriList(row, rangeCol);
			URI inverseOf = uriValue(row, inverseOfCol);
			List<URI> propertyType = uriList(row, propertyTypeCol);
			
			
			if (propertyId == null) {
				return;
			}
			

			Vertex subject = graph.vertex(propertyId);
			propertyType = analyzePropertyType(subject, propertyType, range);
			
			graph.edge(propertyId, RDF.TYPE, RDF.PROPERTY);
			for (URI value : propertyType) {
				if (!RDF.PROPERTY.equals(value)) {
					graph.edge(propertyId, RDF.TYPE, value);
				}
			}
			
			if (propertyName != null) {
				graph.edge(propertyId, LABEL, propertyName);
			}
			
			if (comment != null) {
				graph.edge(propertyId, RDFS.COMMENT, comment);
			}
			
			if (domain != null) {
				if (domain.size() == 1) {
					graph.edge(propertyId, RDFS.DOMAIN, domain.get(0));
				} else {
					for (URI value : domain) {
						graph.edge(propertyId, Schema.domainIncludes, value);
					}
				}
			}
			
			if (range != null) {
				if (range.size() == 1) {
					graph.edge(propertyId, RDFS.RANGE, range.get(0));
				} else {
					for (URI value : range) {
						graph.edge(propertyId, Schema.rangeIncludes, value);
					}
				}
			}
			
			if (inverseOf != null) {
				graph.edge(propertyId, OWL.INVERSEOF, inverseOf);
			}
			
			
		}

		

		private List<URI> analyzePropertyType(Vertex subject, List<URI> propertyType, List<URI> range) {
			if (propertyType == null) {
				propertyType = new ArrayList<>();
				boolean datatypeProperty = false;
				boolean objectProperty = false;
				
				for (URI type : range) {
					
					if (isDatatype(type)) {
						datatypeProperty = true;
					} else {
						objectProperty = true;
					}
				}
				
				if (datatypeProperty && !objectProperty) {
					propertyType.add(OWL.DATATYPEPROPERTY);
				}
				if (!datatypeProperty && objectProperty) {
					propertyType.add(OWL.OBJECTPROPERTY);
				}
				
			} 
			return propertyType;
		}

		/**
		 * Returns true if the given subject is in the xsd namespace, or one of the following individuals:
		 * rdfs:Literal, rdfs:Datatype, rdf:XMLLiteral, schema:Boolean, schema:Date, schema:DateTime, schema:Number, 
		 * schema:Text, or schema:Time.
		 */
		private boolean isDatatype(URI subject) {
			if (XMLSchema.NAMESPACE.equals(subject.getNamespace())) {
				return true;
			}
			
			if (subject.equals(Schema.Boolean) ||
				subject.equals(Schema.Date) ||
				subject.equals(Schema.DateTime) ||
				subject.equals(Schema.Number) ||
				subject.equals(Schema.Float) ||
				subject.equals(Schema.Integer) ||
				subject.equals(Schema.Text) ||
				subject.equals(Schema.Time) ||
				subject.equals(RDFS.LITERAL) ||
				subject.equals(RDFS.DATATYPE) ||
				subject.equals(RDF.XMLLITERAL)
				
			) {
				return true;
			}
				
			
			return false;
		}

		private List<URI> uriList(Row row, int col) throws SpreadsheetException {
			List<URI> result = null;
			String text = stringValue(row, col);
			if (text != null) {
				String[] array = text.split(" \t\r\n");
				if (array.length>0) {
					result = new ArrayList<>();
					for (String value : array) {
						result.add(expandCurie(value));
					}
				}
			}
			return result;
		}

		private void readPropertyHeader(Sheet sheet) throws SpreadsheetException {
			propertyNameCol = UNDEFINED;
			propertyIdCol = UNDEFINED;
			domainCol = UNDEFINED;
			rangeCol = UNDEFINED;
			inverseOfCol = UNDEFINED;
			propertyTypeCol = UNDEFINED;
			propertyCommentCol = UNDEFINED;
			
			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);
			
			int colSize = row.getLastCellNum() + 1;
			for (int i=row.getFirstCellNum(); i<colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();
					
					switch (text) {
					case PROPERTY_NAME :  propertyNameCol = i; break;
					case PROPERTY_ID : propertyIdCol = i; break;
					case DOMAIN : domainCol = i; break;
					case RANGE : rangeCol = i; break;
					case INVERSE_OF : inverseOfCol = i; break;
					case PROPERTY_TYPE : propertyTypeCol = i; break;
					case COMMENT : propertyCommentCol = i; break;
						
					}
				}
			}
			
			String sheetName = sheet.getSheetName();
			
			if (propertyIdCol == UNDEFINED) {
				throw new MissingColumnException(PROPERTY_ID, sheetName);
			}
			
			if (domainCol == UNDEFINED) {
				throw new MissingColumnException(DOMAIN, sheetName);
			}
			
			if (rangeCol == UNDEFINED) {
				throw new MissingColumnException(RANGE, sheetName);
			}
			
		}

		private void loadClasses(Sheet sheet) throws SpreadsheetException {
			
			
			readClassHeader(sheet);
			
			int rowSize = sheet.getLastRowNum()+1;
			
			for (int i=sheet.getFirstRowNum()+1; i<rowSize; i++) {
				Row row = sheet.getRow(i);
				loadClassRow(row);
			}
			
		}

		private void loadClassRow(Row row) throws SpreadsheetException {
			
			Literal className = stringLiteral(row, classNameCol);
			Literal comment = stringLiteral(row, classCommentCol);
			URI classId = uriValue(row, classIdCol);
			URI subclassOf = uriValue(row, classSubclassOfCol);
			
			if (classId != null) {
				graph.edge(classId, RDF.TYPE, OWL.CLASS);
				if (className != null) {
					graph.edge(classId, LABEL, className);
				}
				if (comment != null) {
					graph.edge(classId, RDFS.COMMENT, comment);
				}
				if (subclassOf != null) {
					graph.edge(classId, RDFS.SUBCLASSOF, subclassOf);
					graph.edge(subclassOf, RDF.TYPE, OWL.CLASS);
				}
			}
			
			
			
		}

		private Literal stringLiteral(Row row, int col) {
			Literal result = null;
			String text = stringValue(row, col);
			if (text != null) {
				result = vf.createLiteral(text);
			}
			return result;
		}

		private URI uriValue(Row row, int col) throws SpreadsheetException {
		
			try {
			
				String text = stringValue(row, col);
				return expandCurie(text);
			} catch (SpreadsheetException e) {
			
				String template =
					"Error in row {0}, column {1} on sheet {2}: {3}";
					
				String msg = MessageFormat.format(template, row.getRowNum(), col, row.getSheet().getSheetName(), e.getMessage());
				throw new SpreadsheetException(msg);
				
			}
		}
		
		private String curie(URI uri) {
			
			String name = uri.getNamespace();
			Namespace ns = nsManager.findByName(name);
			if (ns == null) {
				return uri.stringValue();
			}
			
			StringBuilder builder = new StringBuilder();
			builder.append(ns.getPrefix());
			builder.append(':');
			builder.append(uri.getLocalName());
			
			return builder.toString();
		}

		private URI expandCurie(String text) throws SpreadsheetException {
			if (text == null) {
				return null;
			}
			if (text.startsWith("http://") || text.startsWith("https://") || text.startsWith("urn:")) {
				return vf.createURI(text);
			}
			int colon = text.indexOf(':');
			if (colon < 1) {
				throw new SpreadsheetException("Invalid URI: " + text);
			}
			String prefix = text.substring(0, colon);
			Namespace ns = nsManager.findByPrefix(prefix);
			if (ns == null) {
				throw new SpreadsheetException(format("Namespace not found for prefix ''{0}''", prefix) );
			}
			StringBuilder builder = new StringBuilder();
			builder.append(ns.getName());
			builder.append(text.substring(colon+1));
			
			
			return vf.createURI(builder.toString());
		}

		private void readClassHeader(Sheet sheet) {

			classNameCol = UNDEFINED;
			classCommentCol = UNDEFINED;
			classIdCol = UNDEFINED;
			classSubclassOfCol = UNDEFINED;
			
			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);
			
			int colSize = row.getLastCellNum() + 1;
			for (int i=row.getFirstCellNum(); i<colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();
					
					switch (text) {
					case CLASS_NAME :  classNameCol = i; break;
					case COMMENT : classCommentCol = i; break;
					case CLASS_ID : classIdCol = i; break;
					case CLASS_SUBCLASS_OF : classSubclassOfCol = i; break;
						
					}
				}
			}
		}

		private void loadOntologies(Sheet sheet) throws SpreadsheetException {
			
			readOntologyHeader(sheet);
			
			int rowSize = sheet.getLastRowNum()+1;
			
			for (int i=sheet.getFirstRowNum()+1; i<rowSize; i++) {
				Row row = sheet.getRow(i);
				loadOntologyRow(row);
			}
			
			
			
			
		}
		
		private String format(String pattern, Object...args) {
			return MessageFormat.format(pattern, args);
		}

		private void loadOntologyRow(Row row) throws SpreadsheetException {
			
			
			String ontologyName = stringValue(row, ontologyNameCol);
			String comment = stringValue(row, ontologyCommentCol);
			String namespaceURI = stringValue(row, namespaceUriCol);
			String prefix = stringValue(row, prefixCol);
			
			if (ontologyName==null && comment==null && namespaceURI==null && prefix==null) {
				return;
			}
			
			String sheetName = row.getSheet().getSheetName();
			
			if (namespaceURI==null) {
				throw new SpreadsheetException(
					format("'{0}' is missing on row {1} of the '{2}' sheet.", NAMESPACE_URI, row.getRowNum(), sheetName));
			}
			
			if (prefix==null) {
				throw new SpreadsheetException(
					format("'{0}' is missing on row {1} of the '{2}' sheet.", PREFIX, row.getRowNum(), sheetName));
			}

			nsManager.add(prefix, namespaceURI);
			
			URI subject = uri(namespaceURI);
			
			graph.edge(subject, RDF.TYPE, OWL.ONTOLOGY);
			graph.edge(subject, VANN.preferredNamespacePrefix, literal(prefix));
			
			if (ontologyName != null) {
				graph.edge(subject, LABEL, literal(ontologyName));
			}
			if (comment != null) {
				graph.edge(subject, RDFS.COMMENT, literal(comment));
			}
			
		}
		
		private URI uri(String text) {
			return vf.createURI(text);
		}
		
		private Literal literal(String text) {
			return vf.createLiteral(text);
		}

		private String stringValue(Row row, int column) {
			if (row == null) {
				return null;
			}
			String text = null;
			if (column>=0) {
				Cell cell = row.getCell(column);
				if (cell != null) {
					
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_BOOLEAN :
						text = Boolean.toString(cell.getBooleanCellValue());
						break;
						
					case Cell.CELL_TYPE_NUMERIC :
						text = Double.toString(cell.getNumericCellValue());
						break;
						
					case Cell.CELL_TYPE_STRING :
						text = cell.getStringCellValue();
						break;
						
					case Cell.CELL_TYPE_FORMULA :
						Hyperlink link = cell.getHyperlink();
						if (link != null) {
							text = link.getAddress();
						}
						
					}
					
					if (text != null) {
						text = text.trim();
						if (text.length() == 0) {
							text = null;
						}
					}
				}
			}
			return text;
		}

		private void readOntologyHeader(Sheet sheet) throws SpreadsheetException {

			ontologyNameCol=UNDEFINED;
			ontologyCommentCol=UNDEFINED;
			namespaceUriCol=UNDEFINED;
			prefixCol=UNDEFINED;

			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);
			
			int colSize = row.getLastCellNum() + 1;
			for (int i=row.getFirstCellNum(); i<colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();
					
					switch (text) {
					case ONTOLOGY_NAME :  ontologyNameCol = i; break;
					case COMMENT : ontologyCommentCol = i; break;
					case NAMESPACE_URI : namespaceUriCol = i; break;
					case PREFIX : prefixCol = i; break;
						
					}
				}
			}
			
			String sheetName = sheet.getSheetName();
			if (ontologyNameCol == UNDEFINED) {
				throw new MissingColumnException(ONTOLOGY_NAME, sheetName);
			}
			
			if (ontologyCommentCol == UNDEFINED) {
				throw new MissingColumnException(COMMENT, sheetName);
			}
			
			if (namespaceUriCol == UNDEFINED) {
				throw new MissingColumnException(NAMESPACE_URI, sheetName);
			}
			
			if (prefixCol == UNDEFINED) {
				throw new MissingColumnException(PREFIX, sheetName);
			}
			
			
		}
	}

}
