package io.konig.maven;

/*
 * #%L
 * konig-omcs-deploy-maven-plugin Maven Plugin
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OmcsConnection {

	private static OmcsConnection connection;
	private String url;
	private String username;
	private String password;

	private OmcsConnection(String instance, String database) throws Exception {
		try {
			url = "jdbc:oracle:thin:@" + instance;
			if (database != null) {
				url = "jdbc:oracle:thin:@" + instance + ":" + database;
			}
			username = System.getenv("OMCS_USERNAME") == null ? System.getProperty("omcs.username")
					: System.getenv("OMCS_USERNAME");
			password = System.getenv("OMCS_PASSWORD") == null ? System.getProperty("omcs.password")
					: System.getenv("OMCS_PASSWORD");

			if (username == null || password == null) {
				String error = "Please define the OMCS_USERNAME and OMCS_PASSWORD as "
						+ "environment variable, or set the property 'omcs.username' and 'omcs.password'.";
				throw new Exception(error);
			}
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			throw e;
		}
	}

	public static Connection getConnection(String instance, String database) throws Exception {
		if (connection == null) {
			connection = new OmcsConnection(instance, database);
		}
		try {
			return DriverManager.getConnection(connection.url, connection.username, connection.password);
		} catch (SQLException e) {
			throw e;
		}
	}
}
