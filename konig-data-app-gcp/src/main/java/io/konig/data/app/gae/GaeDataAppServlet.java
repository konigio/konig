package io.konig.data.app.gae;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;

import io.konig.dao.core.ShapeReadService;
import io.konig.data.app.common.DataAppServlet;
import io.konig.sql.runtime.BigQueryShapeReadService;
import io.konig.sql.runtime.ClasspathEntityStructureService;

public class GaeDataAppServlet extends DataAppServlet {
	private static final long serialVersionUID = 1L;
	private static final String CREDENTIALS_FILE = "konig/gcp/credentials.json";

	@Override
	protected ShapeReadService createShapeReadService() throws ServletException {
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(CREDENTIALS_FILE)) {
			GoogleCredentials credentials = GoogleCredentials.fromStream(input);
			BigQuery bigQuery = BigQueryOptions.newBuilder().setCredentials(credentials).build().getService();
			ClasspathEntityStructureService structureService = ClasspathEntityStructureService.defaultInstance();
			return new BigQueryShapeReadService(structureService, bigQuery);
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

}
