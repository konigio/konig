package io.konig.validation;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.impl.RdfUtil;
import io.konig.core.io.PrettyPrintWriter;

public class PlainTextModelValidationReportWriter implements ModelValidationReportWriter {
	
	

	@Override
	public void writeReport(ModelValidationReport report, Writer out) throws IOException {
		PrettyPrintWriter pretty = new PrettyPrintWriter(out);
		Worker worker = new Worker(report, pretty);
		worker.doWrite();

	}
	
	private static class Worker {
		private ModelValidationReport report;
		private PrettyPrintWriter out;
		private DecimalFormat percent = new DecimalFormat("###.#%");


		public Worker(ModelValidationReport report, PrettyPrintWriter out) {
			this.report = report;
			this.out = out;
		}
		
		private String percentage(String label, RationalNumber number) {
			String pattern = label + ": {0} ({1})";
			return MessageFormat.format(pattern, number.getNumerator(), percent.format(number.getValue()));
		}

		private void doWrite() {
			
			String classesWithDescription = percentage(
					"Number of OWL Classes with description", 
					report.getStatistics().getClassesWithDescription());
			
			String individualsWithDescription = percentage(
					"Number of Named Individuals with description", 
					report.getStatistics().getNamedIndividualsWithDescription());
			
			
			out.println("Data Model Statistics");
			out.println("=====================");
			out.println();
			out.print("Number of OWL Classes: ");
			out.println(report.getStatistics().getNumberOfClasses());
			out.println(classesWithDescription);
			out.println();
			out.print("Number of Properties: ");
			out.println(report.getStatistics().getNumberOfProperties());
			out.println();
			out.print("Number of Named Individuals: ");
			out.println(report.getStatistics().getNumberOfNamedIndividuals());
			out.println(individualsWithDescription);
			out.println();
			out.print("Number of Node Shapes: ");
			out.println(report.getStatistics().getNumberOfShapes());
			
			out.println();
			printClassReports(report, out);
			printPropertyReports(report, out);
			printNamedIndividualReports(report, out);
			printShapeReports(report, out);
			
			out.flush();
			
		}

		private void printNamedIndividualReports(ModelValidationReport report, PrettyPrintWriter out) {
			if (report.getNamedIndividualReports().isEmpty()) {
				return;
			}
			out.println("Named Individuals");
			out.println("=================");
			
			CaseStyle expectedStyle = report.getRequest().getCaseStyle().getNamedIndividuals();
			out.println();
			List<NamedIndividualReport> list = new ArrayList<>(report.getNamedIndividualReports());
			Collections.sort(list);
			for (NamedIndividualReport p : list) {
				out.print('<');
				out.print(p.getIndividualId().stringValue());
				out.println('>');
				out.pushIndent();
				if (p.getNameHasWrongCase()) {
					wrongCaseMessage(expectedStyle);
				}
				out.popIndent();
			}
			out.println();
			
		}

		private void printPropertyReports(ModelValidationReport report, PrettyPrintWriter out) {
			if (report.getPropertyReports().isEmpty()) {
				return;
			}
			out.println("Properties");
			out.println("==========");
			
			CaseStyle expectedStyle = report.getRequest().getCaseStyle().getProperties();
			
			out.println();
			List<PropertyReport> list = new ArrayList<>(report.getPropertyReports());
			Collections.sort(list);
			for (PropertyReport p : list) {
				out.print('<');
				out.print(p.getPropertyId().stringValue());
				out.println('>');
				out.pushIndent();
				if (p.getNameHasWrongCase()) {
					wrongCaseMessage(expectedStyle);
				}
				if (p.getInvalidXmlSchemaDatatype() != null) {
					invalidXmlSchemaDatatypeMessage(p);
				}
				printRangeConflict(p);
				out.popIndent();
			}
			out.println();
			
		}

		private void printRangeConflict(PropertyReport p) {
			List<RangeInfo> rangeConflict = p.getRangeConflict();
			if (!rangeConflict.isEmpty()) {
				out.indent();
				out.println("Incompatible values for range:");
				out.pushIndent();
				for (RangeInfo info : rangeConflict) {
					URI datatype = info.getDatatype();
					URI owlClass = info.getOwlClass();
					URI type = datatype==null ? owlClass : datatype;
					Resource shapeId = info.getParentShapeId();
					
					Resource source = shapeId==null ? RDFS.RANGE :	shapeId;
					
					out.indent();
					out.print(compactId(type));
					out.print(" defined by ");
					out.println(compactId(source));
				}
				out.popIndent();
			}
			
		}

		

		private String compactId(Resource id) {
			return RdfUtil.compactId(id, report.getNamespaceManager());
		}

		private void invalidXmlSchemaDatatypeMessage(PropertyReport p) {
			out.indent();
			out.print("Invalid XML Schema datatype: ");
			out.println(p.getInvalidXmlSchemaDatatype().getLocalName());
			
		}

		private void printClassReports(ModelValidationReport report, PrettyPrintWriter out) {
			if (report.getClassReports().isEmpty()) {
				return;
			}
			out.println("Classes");
			out.println("=======");
			
			CaseStyle expectedStyle = report.getRequest().getCaseStyle().getClasses();
			
			out.println();
			List<ClassReport> list = new ArrayList<>(report.getClassReports());
			Collections.sort(list);
			for (ClassReport c : list) {
				out.print('<');
				out.print(c.getClassId().stringValue());
				out.println('>');
				out.pushIndent();
				if (c.getNameHasWrongCase()) {
					wrongCaseMessage(expectedStyle);
				}
				if (c.getRequiresDescription()) {
					out.indent();
					out.println("SHOULD have a definition given by rdfs:comment");
				}
				out.println();
				
				out.popIndent();
			}
			out.println();
			
		}

		private void printShapeReports(ModelValidationReport report, PrettyPrintWriter out) {
			
			if (report.getShapeReports().isEmpty()) {
				return;
			}
			
			out.println("Node Shapes");
			out.println("===========");
			CaseStyle expectedStyle = report.getRequest().getCaseStyle().getNodeShapes();
			
			
			out.println();
			List<NodeShapeReport> list = new ArrayList<>(report.getShapeReports());
			Collections.sort(list);
			for (NodeShapeReport c : list) {
				out.print('<');
				out.print(c.getShapeId().stringValue());
				out.println('>');
				out.pushIndent();
				if (c.getNameHasWrongCase()) {
					wrongCaseMessage(expectedStyle);
				}
				out.popIndent();
				
				printAllPropertyShapeReports(c, out);
				
			}
			
		}

		private void wrongCaseMessage(CaseStyle expectedStyle) {

			String msg = MessageFormat.format("SHOULD use {0} for the local name", expectedStyle.name());
			out.indent();
			out.println(msg);
			out.println();
			
		}

		private void printAllPropertyShapeReports(NodeShapeReport nodeReport, PrettyPrintWriter out) {
			
			List<PropertyShapeReport> list = nodeReport.getPropertyReports();
			if (list.isEmpty()) {
				return;
			}
			Collections.sort(list);
			out.pushIndent();
			out.indent();
			out.println("Property Constraints");
			out.indent();
			out.println("====================");
			
			out.pushIndent();
			for (PropertyShapeReport p : list) {
				out.indent();
				out.print('<');
				out.print(p.getPropertyShape().getPredicate().stringValue());
				out.println('>');
				
				out.pushIndent();
				if (p.isDatatypeWithClass()) {
					String datatype = iriRef(p.getPropertyShape().getDatatype());
					String classId  = iriRef(p.getPropertyShape().getValueClass());
					String msg = MessageFormat.format("MUST NOT define both (sh:datatype {0}) and (sh:class {1})", datatype, classId);
					out.indent();
					out.println(msg);
					
				}
				if (p.isDatatypeWithIriNodeKind()) {

					String datatype = iriRef(p.getPropertyShape().getDatatype());
					String msg = MessageFormat.format("MUST NOT define both (sh:datatype {0}) and (sh:nodeKind sh:IRI)", datatype);
					out.indent();
					out.println(msg);
				}
				if (p.isDatatypeWithShape()) {
					String datatype = iriRef(p.getPropertyShape().getDatatype());
					String shapeId = iriRef(p.getPropertyShape().getShape().getId());
					String msg = MessageFormat.format("MUST NOT define both (sh:datatype {0}) and (sh:shape {1})", datatype, shapeId);
					out.indent();
					out.println(msg);
				}
				if (p.isRequiresDatatypeClassOrShape()) {
					out.indent();
					out.println("MUST specify one of sh:datatype, sh:class, OR sh:shape");
				}
				if (p.isRequiresMinCount()) {
					out.indent();
					out.println("MUST define the sh:minCount attribute");
				}
				if (p.isRequiresShapeOrIriNodeKind()) {
					out.indent();
					out.println("MUST define sh:shape or set (sh:nodeKind sh:IRI) since sh:class is defined");
				}
				if (p.getRequiresDescription()) {
					out.indent();
					out.println("SHOULD define rdfs:comment");
				}
				if (p.getInvalidXmlSchemaDatatype()!=null) {
					out.indent();
					out.print("Invalid XML Schema datatype: ");
					out.println(p.getInvalidXmlSchemaDatatype().getLocalName());
				}
				out.popIndent();
				out.println();
				
			}
			
			
			out.popIndent();
			out.popIndent();
			
		}

		private String iriRef(Resource id) {
			
			return RdfUtil.compactName(report.getRequest().getOwl().getGraph().getNamespaceManager(), id);
		}
		
	}



}
