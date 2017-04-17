package io.konig.shacl.sample;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.joda.time.format.ISODateTimeFormat;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.RandomGenerator;
import io.konig.core.util.SimpleValueMap;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.util.ValueFormat.ElementType;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

/**
 * A utility that generates sample data conforming to a given shape
 * @author Greg McFall
 *
 */
public class SampleGenerator {
	
	private int maxValueCount = 3;
	private int stringWordCount = 4;
	private long startDate = 1492418400000L; // April 17, 2017 at 8:40 AM
	private long endDate = 1493023200000L; // startDate + one week
	private long maxDuration = 1000*60*2; // 2 minutes in milliseconds

	private int maxDouble = 10000;
	private int maxInt = 100;
	

	public Vertex generate(Shape shape, Graph graph) {
		Worker worker = new Worker(graph);
		return worker.generate(shape);
	}
	
	private class Worker {

		private Graph graph;
		private RandomGenerator random;
		
		public Worker(Graph graph) {
			this.graph = graph;
		}

		public Vertex generate(Shape shape) {
			random = new RandomGenerator(shape.getId().hashCode());
			return generateShape(shape);
		}

		private Vertex generateShape(Shape shape) {
			
			Vertex v = generateVertex(shape);
			addProperties(v, shape);
			
			return v;
		}

		private void addProperties(Vertex v, Shape shape) {
			for (PropertyConstraint p : shape.getProperty()) {
				if (p.getPredicate() != null) {
					addProperty(p, v);
				}
			}
			
		}

		private void addProperty(PropertyConstraint p, Vertex v) {
			Integer maxCount = p.getMaxCount();
			Integer minCount = p.getMinCount();
			
			int valueCount = (maxCount==null) ? maxValueCount : Math.min(maxValueCount, maxCount.intValue());
			
			if (minCount!=null) {
				if (minCount==0) return;
				valueCount = Math.max(valueCount, minCount.intValue());
			}
			
			for (int i=0; i<valueCount; i++) {
				Value object = generateValue(p);
				v.addProperty(p.getPredicate(), object);
			}
		}

		private Value generateValue(PropertyConstraint p) {
			
			URI datatype = p.getDatatype();
			if (datatype != null) {
				String value = null;
				
				if (
					XMLSchema.STRING.equals(datatype) ||
					XMLSchema.NORMALIZEDSTRING.equals(datatype)
				) {
					value = random.loremIpsum(stringWordCount);
				} else if (XMLSchema.ANYURI.equals(datatype)) {
					value = randomURI("resource").stringValue();
				} else if (XMLSchema.BOOLEAN.equals(datatype)) {
					value = Boolean.toString(random.nextBoolean());
				} else if (XMLSchema.BYTE.equals(datatype)) {
					value = Byte.toString(random.nextByte());
				} else if (XMLSchema.DATE.equals(datatype)) {
					value = nextDate();
				} else if (XMLSchema.DATETIME.equals(datatype)) {
					value = nextDateTime();
				} else if (XMLSchema.DAYTIMEDURATION.equals(datatype)) {
					value = nextDuration();
				} else if (
					XMLSchema.DOUBLE.equals(datatype) ||
					XMLSchema.DECIMAL.equals(datatype) ||
					XMLSchema.FLOAT.equals(datatype)
				) {
					value = nextDouble();
				} else if (
					XMLSchema.INT.equals(datatype) ||
					XMLSchema.INTEGER.equals(datatype) ||
					XMLSchema.LONG.equals(datatype) ||
					XMLSchema.NON_NEGATIVE_INTEGER.equals(datatype) ||
					XMLSchema.POSITIVE_INTEGER.equals(datatype)
				) {
					value = nextInt();
				} else if (
					XMLSchema.NEGATIVE_INTEGER.equals(datatype) ||
					XMLSchema.NON_POSITIVE_INTEGER.equals(datatype)
				) {
					value = nextNegativeInt();
				} else if (XMLSchema.SHORT.equals(datatype)) {
					value = nextShort();
				} else if (XMLSchema.TIME.equals(datatype)) {
					value = nextTime();
				} else if (XMLSchema.TOKEN.equals(datatype)) {
					value = random.loremIpsum(1);
				} else {
					throw new KonigException("Unsupported datatype: " + datatype);
				}
				
				return new LiteralImpl(value, datatype);
			}
			throw new KonigException("Unsupported property: " + p.getPredicate());
		}
		
		private String nextTime() {
			long instant = random.nextLong(startDate, endDate);
			LocalTime time = new LocalTime(instant);
			return time.toString();
		}

		private String nextShort() {
			short value = (short) random.nextInt(Short.MAX_VALUE);
			return Short.toString(value);
		}

		private String nextNegativeInt() {
			int value = -random.nextInt(maxInt);
			return Integer.toString(value);
		}

		private String nextInt() {
			int value = random.nextInt(maxInt);
			return Integer.toString(value);
		}

		private String nextDouble() {
			double value = random.nextInt(maxDouble)/100.0;
			return Double.toString(value);
		}

		private String nextDuration() {
			Duration duration = new Duration(random.nextLong(1000, maxDuration));
			return duration.toString();
		}

		private String nextDateTime() {
			long instant = random.nextLong(startDate, endDate);
			DateTime localTime = new DateTime(instant).toDateTime(DateTimeZone.UTC);
			return localTime.toString();
		}

		private String nextDate() {
			long instant = random.nextLong(startDate, endDate);
			DateTime localTime = new DateTime(instant).toDateTime(DateTimeZone.UTC);
			return  localTime.toString(ISODateTimeFormat.date());
		}

		private Vertex generateVertex(Shape shape) {
			IriTemplate template = shape.getIriTemplate();
			if (template == null) {
				if (shape.getNodeKind()==NodeKind.IRI || shape.getNodeKind()==NodeKind.BlankNodeOrIRI) {
					String typeName = shape.getTargetClass()==null ?
							"resource" : shape.getTargetClass().getLocalName();
					
					return graph.vertex(randomURI(typeName));
				}
				
				
			} else {
				URI id = generateIri(shape);
			}
			return graph.vertex(new BNodeImpl(random.alphanumeric(8)));
		}
		
		private URI generateIri(Shape shape) {
			IriTemplate template = shape.getIriTemplate();
			List<? extends Element> list = template.toList();
			SimpleValueMap map = new SimpleValueMap();
			for (Element e : list) {
				if (e.getType() == ElementType.VARIABLE) {
					String propertyName = e.getText();
					PropertyConstraint p = getProperty(shape, propertyName);
				}
			}
			return null;
		}

		private PropertyConstraint getProperty(Shape shape, String propertyName) {
			for (PropertyConstraint p : shape.getProperty()) {
				URI predicate = p.getPredicate();
			}
			return null;
		}

		private URI randomURI(String typeName) {
			StringBuilder builder = new StringBuilder();
			builder.append("http://example.com/");
			builder.append(typeName);
			builder.append('/');
			builder.append(random.alphanumeric(8));
			return new URIImpl(builder.toString());
		}
	}
}
