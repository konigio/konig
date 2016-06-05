package io.konig.datagen;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.NamespaceManager;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.KOL;
import io.konig.schemagen.jsonschema.JsonSchemaDatatype;
import io.konig.schemagen.jsonschema.JsonSchemaTypeMapper;
import io.konig.schemagen.jsonschema.impl.SimpleJsonSchemaTypeMapper;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeMediaTypeNamer;

public class DataGenerator {
	private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);

	private NamespaceManager nsManager;
	private ShapeManager shapeManager;
	private ShapeMediaTypeNamer mediaTypeNamer;
	private JsonSchemaTypeMapper typeMapper = new SimpleJsonSchemaTypeMapper();
	private List<String> vocab;
	private Random random = new Random(new Date().getTime());
	private int minWordCount = 3;
	private int maxWordCount = 6;
	private IriGenerator iriGenerator = new SimpleIriGenerator("http://example.com/");
	
	public DataGenerator(NamespaceManager nsManager, ShapeManager shapeManager, ShapeMediaTypeNamer mediaTypeNamer) 
		throws IOException {
		this.nsManager = nsManager;
		this.shapeManager = shapeManager;
		this.mediaTypeNamer = mediaTypeNamer;
		loadVocab();
	}

	private void loadVocab() throws IOException {
		vocab = new ArrayList<>();
		InputStream input = getClass().getClassLoader().getResourceAsStream("lorem.txt");
		InputStreamReader reader = new InputStreamReader(input);
		BufferedReader buffer = new BufferedReader(reader);
		try {
			String line=null;
			while ( (line=buffer.readLine()) != null) {
				StringTokenizer tokens = new StringTokenizer(line);
				while (tokens.hasMoreTokens()) {
					vocab.add(tokens.nextToken());
				}
			}
		} finally {
			close(buffer);
		}
		
	}

	public void generate(DataGeneratorConfig config, File outDir) throws IOException, DataGeneratorException {
		
		Worker worker = new Worker(config, outDir);
		worker.run();
		
	}
	
	static class Counter {
		int current;
		int max;
		public Counter(int max) {
			this.max = max;
		}
		
		
	}
	
	class Worker {

		private File outDir;
		private DataGeneratorConfig config;
		private Shape shape;
		private int counter;
		
		private Map<String,Counter> counterMap = new HashMap<>();
		
		public Worker(DataGeneratorConfig config, File outDir) {
			this.outDir = outDir;
			outDir.mkdirs();
			this.config = config;
		}
		
		private void run() throws IOException, DataGeneratorException {
			
			buildCounterMap();
			List<ShapeConfig> list = config.getShapeConfigList();
			
			for (ShapeConfig s : list) {
				generate(s);
			}
		}

		private void buildCounterMap() throws DataGeneratorException {

			List<ShapeConfig> list = config.getShapeConfigList();
			for (ShapeConfig s : list) {
				URI targetShape = s.getTargetShape();
				Shape shape = shapeManager.getShapeById(targetShape);
				if (shape == null) {
					throw new DataGeneratorException("Shape not found: " + targetShape);
				}
				URI scopeClass = shape.getScopeClass();
				if (scopeClass == null) {
					throw new DataGeneratorException("scopeClass not defined for shape " + targetShape);
				}
				Integer shapeCount = s.getShapeCount();
				if (shapeCount == null) {
					shapeCount = 1;
				}
				
				String key = scopeClass.stringValue();
				Counter c = counterMap.get(key);
				if (c == null || c.max<shapeCount) {
					counterMap.put(key, new Counter(shapeCount));
				}
				
			}
			
			handleClassConstraints();
			
		}

		private void handleClassConstraints() {
			List<ClassConstraint> list = config.getClassConstraintList();
			for (ClassConstraint c : list) {
				URI targetClass = c.getTargetClass();
				int count = c.getInstanceCount();
				
				String key = targetClass.stringValue();
				Counter counter = counterMap.get(key);
				if (counter == null || counter.max<count) {
					counterMap.put(key, new Counter(count));
				}
				
			}
			
			
		}

		private void generate(ShapeConfig s) throws DataGeneratorException, IOException {
			
			URI targetShapeId = s.getTargetShape();
			Integer shapeCount = s.getShapeCount();
			int count = shapeCount==null ? 1 : shapeCount;
			
			if (targetShapeId != null) {
				shape = shapeManager.getShapeById(targetShapeId);
				if (shape == null) {
					throw new DataGeneratorException("Shape not found: " + targetShapeId.stringValue());
				}
				
				generateShape(shape, count);
			}
			
		}

		private void generateShape(Shape shape, int count) throws IOException {
			
			String mediaType = mediaTypeNamer.baseMediaTypeName(shape);
			int slash = mediaType.indexOf('/');
			if (slash>0) {
				mediaType = mediaType.substring(slash+1);
			}
			File file = new File(outDir, mediaType);
			
			byte[] newLine = "\n".getBytes();
			
			FileOutputStream out = new FileOutputStream(file);
			try {
				JsonFactory factory = new JsonFactory();
				JsonGenerator json = factory.createGenerator(out);
				
				for (int i=0; i<count; i++) {
					generateInstance(shape, json, 1);
					json.flush();
					out.write(newLine);
				}
			} finally {
				close(out);
			}
		}

		private void generateInstance(Shape shape, JsonGenerator json, int depth) throws IOException {
			
			List<PropertyConstraint> list = shape.getProperty();
			
			json.writeStartObject();
			for (PropertyConstraint p : list) {
				writeProperty(json, p, depth);
			}
			
			json.writeEndObject();
			
			
		}

		private void writeProperty(JsonGenerator json, PropertyConstraint p, int depth) throws IOException {
			URI predicate = p.getPredicate();
			URI datatype = p.getDatatype();
			NodeKind nodeKind = p.getNodeKind();
			Integer maxCountValue = p.getMaxCount();
			Set<Value> hasValue = p.getHasValue();
			Resource valueClass = p.getValueClass();
			
			int maxCount = 1;
			if (maxCountValue == null) {
				maxCount = random.nextInt(3) + 2;
			} else if (maxCountValue>1) {
				maxCount = maxCountValue.intValue();
				maxCount = random.nextInt(maxCount) + 1;
			}
			
			String fieldName = predicate.getLocalName();
			
			if (KOL.id.equals(predicate)) {
				
				URI scopeClass = shape.getScopeClass();
				Counter c = counterMap.get(scopeClass.stringValue());
				int index = index(c, depth);
				URI id = iriGenerator.createIRI(shape.getScopeClass(), index);
				json.writeStringField(fieldName, id.stringValue());
				
			} else if (datatype != null) {
				JsonSchemaDatatype jsonType = typeMapper.type(p);
				if (maxCount==1) {

					if (jsonType == JsonSchemaDatatype.STRING) {
						String fieldValue = text(minWordCount, maxWordCount);
						
						json.writeStringField(fieldName, fieldValue);
					}
				}
				
			} else if (nodeKind == NodeKind.IRI) {
				if (maxCount==1) {
					if (hasValue!=null && hasValue.size()>0) {
						
						Value value = hasValue.iterator().next();
						if (value instanceof URI) {
							URI uri = (URI) value;
							String curie = curie(uri);
							json.writeStringField(fieldName, curie);
						}
					} else {
						if (valueClass instanceof URI) {
							Counter c = counterMap.get(valueClass.stringValue());
							int index = 0;
							if (c != null) {
								index = random.nextInt(c.max) + 1;
							} else {
								index = ++counter;
							}
							URI uri = iriGenerator.createIRI((URI)valueClass, index);
							json.writeStringField(fieldName, uri.stringValue());
						}
					}
				} else { // maxCount>1
					json.writeFieldName(fieldName);
					json.writeStartArray();
					
					if (hasValue!=null && !hasValue.isEmpty()) {
						for (Value v : hasValue) {
							if (v instanceof URI) {
								String curie = curie((URI)v);
								json.writeString(curie);
							}
						}
						
					} else { // Need to generate a URI for the related entity.
						
						// For now, we'll just write one value.
						// But we really ought to write several.
						
						if (valueClass instanceof URI) {
							Counter c = counterMap.get(valueClass.stringValue());
							int index = 0;
							if (c != null) {
								index = random.nextInt(c.max) + 1;
							} else {
								index = ++counter;
							}
							URI uri = iriGenerator.createIRI((URI)valueClass, index);
							json.writeString(uri.stringValue());
						}
						
					}
					json.writeEndArray();
				}
			}
			
			
		}

		private String curie(URI uri) {
			String namespace = uri.getNamespace();
			Namespace ns = nsManager.findByName(namespace);
			if (ns != null) {
				String prefix = ns.getPrefix();
				StringBuilder builder = new StringBuilder();
				builder.append(prefix);
				builder.append(':');
				builder.append(uri.getLocalName());
				return builder.toString();
			}
			
			return uri.stringValue();
		}

		private int index(Counter c, int depth) {
			int index = c==null ? ++counter : (c.current++ % (c.max)) + 1;
			return index;
		}
		
	}
	
	private String text(int minWords, int maxWords) {
		
		int max = (minWords == maxWords) ? minWords : random.nextInt(maxWords - minWords) + minWords;
		
		StringBuilder builder = new StringBuilder();
		int index = random.nextInt(vocab.size());
		
		for (int i=0; i<max; i++) {
			int j = (index + i) % vocab.size();
			String word = vocab.get(j).toLowerCase();
			
			if (i==0) {
				word = StringUtil.capitalize(word);
			} else {
				builder.append(' ');
			}
			builder.append(word);
		}
		
		
		return builder.toString();
	}


	private void close(Closeable stream) {
		
		try {
			stream.close();
		} catch (Throwable oops) {
			logger.warn("Failed to close stream", oops);
		}
	}

}
