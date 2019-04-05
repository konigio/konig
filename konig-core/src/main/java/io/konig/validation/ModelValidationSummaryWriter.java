package io.konig.validation;

/*
 * #%L
 * Konig Core
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


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class ModelValidationSummaryWriter implements ModelValidationReportWriter {
	private static final String PROJECT_ID = "projectId";
	private static final String PROJECT_NAME = "projectName";
	private static final String DATE = "Date";
	private static final String NUMBER_OF_TERMS = "termCount";
	private static final String TERMS_WITH_DESCRIPTION = "termDescriptionCount";
	private static final String NUMBER_OF_CLASSES = "classCount";
	private static final String CLASSES_WITH_DESCRIPTION = "classDescriptionCount";
	private static final String NUMBER_OF_PROPERTIES = "propertyCount";
	private static final String PROPERTIES_WITH_DESCRIPTION = "propertyDescripionCount";
	private static final String NUMBER_OF_NAMED_INDIVIDUALS = "namedIndividualCount";
	private static final String NAMED_INDIVIDUALS_WITH_DESCRIPTION = "namedIndividualDescriptionCount";
	private static final String NUMBER_OF_ERRORS = "errorCount";

	private boolean withHeader;

	public ModelValidationSummaryWriter(boolean withHeader) {
		this.withHeader = withHeader;
	}

	@Override
	public void writeReport(ModelValidationReport report, Writer out) throws IOException {
		
		CSVPrinter csv = csvPrinter(out);

		csv.print(report.getProject().getId());
		csv.print(report.getProject().getName());
		csv.print(date(report));
		csv.print(report.getStatistics().getNumberOfTerms());
		csv.print(report.getStatistics().getTermsWithDescription().asPercentage());
		csv.print(report.getStatistics().getNumberOfClasses());
		csv.print(report.getStatistics().getClassesWithDescription().asPercentage());
		csv.print(report.getStatistics().getNumberOfProperties());
		csv.print(report.getStatistics().getPropertiesWithDescription().asPercentage());
		csv.print(report.getStatistics().getNumberOfNamedIndividuals());
		csv.print(report.getStatistics().getNamedIndividualsWithDescription().asPercentage());
		csv.print(report.getStatistics().getNumberOfErrors());
		
		csv.flush();
	}

	private String date(ModelValidationReport report) {
		Date created = report.getCreatedTimestamp().getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		return format.format(created);
	}

	private CSVPrinter csvPrinter(Writer out) throws IOException {
		CSVFormat format = CSVFormat.DEFAULT;
		
		if (withHeader) {
			format = format.withHeader(
				PROJECT_ID, PROJECT_NAME, DATE, NUMBER_OF_TERMS, TERMS_WITH_DESCRIPTION,
				NUMBER_OF_CLASSES, CLASSES_WITH_DESCRIPTION, NUMBER_OF_PROPERTIES, PROPERTIES_WITH_DESCRIPTION,
				NUMBER_OF_NAMED_INDIVIDUALS, NAMED_INDIVIDUALS_WITH_DESCRIPTION, NUMBER_OF_ERRORS);
		}
				
		
		BufferedWriter buf = out instanceof BufferedWriter ? (BufferedWriter) out : new BufferedWriter(out);
		return new CSVPrinter(buf, format);
	}

}
