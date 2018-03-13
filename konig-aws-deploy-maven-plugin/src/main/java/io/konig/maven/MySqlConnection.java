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

public class MySqlConnection {

	private static MySqlConnection connection;
	private String url;

	private MySqlConnection(String instance, String schema) throws Exception {
		try {
			url = "jdbc:mysql://" + instance +"/"+schema;
			
			String username = System.getenv("AWS_RDS_USERNAME") == null ? System.getProperty("aws.rds.username")
					: System.getenv("AWS_RDS_USERNAME");
			String password = System.getenv("AWS_RDS_PASSWORD") == null ? System.getProperty("aws.rds.password")
					: System.getenv("AWS_RDS_PASSWORD");

			if (username == null || password == null) {
				String error = "Please define the AWS_RDS_USERNAME and AWS_RDS_PASSWORD as "
						+ "environment variable, or set the property 'aws.rds.username' and 'aws.rds.password'.";
				throw new Exception(error);
			}
			url = url + "?user=" + username + "&password=" + password+ "&verifyServerCertificate=false&useSSL=false";
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			throw e;
		}
	}

	public static Connection getConnection(String instance, String schema) throws Exception {
		if (connection == null) {
			connection = new MySqlConnection(instance, schema);
		}
		try {
			Connection con = DriverManager.getConnection(connection.url);
			return con;
		} catch (SQLException e) {
			throw e;
		}
	}
}
