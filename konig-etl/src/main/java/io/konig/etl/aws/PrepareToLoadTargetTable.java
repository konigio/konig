package io.konig.etl.aws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/*
 * #%L
 * Konig ETL
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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.IOUtils;

public class PrepareToLoadTargetTable implements Processor {

	public void process(Exchange exchange) throws Exception {
		String dmlScript = exchange.getIn().getHeader("dmlScript", String.class);
		exchange.getOut().setBody(fileToString(new File("konig/aws/camel-etl/" + dmlScript + ".sql")));
		exchange.getOut().setHeaders(exchange.getIn().getHeaders());
	}

	private String fileToString(File dmlFile) throws IOException {
		try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(dmlFile.getPath())) {
			return IOUtils.toString(inputStream);
		}
	}
}
