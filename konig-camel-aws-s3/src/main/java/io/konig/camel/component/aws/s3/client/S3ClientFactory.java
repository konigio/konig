package io.konig.camel.component.aws.s3.client;

/*
 * #%L
 * Camel DeleteObject Component
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


import io.konig.camel.aws.s3.S3Configuration;
import io.konig.camel.component.aws.s3.client.impl.S3ClientIAMOptimizedImpl;
import io.konig.camel.component.aws.s3.client.impl.S3ClientStandardImpl;

public class S3ClientFactory {
	private S3ClientFactory() {
		// Prevent instantiation of this factory class.
		throw new RuntimeException(
				"Do not instantiate a Factory class! Refer to the class "
						+ "to learn how to properly use this factory implementation.");
	}

	/**
	 * Return the correct aws s3 client (based on remote vs local).
	 * 
	 * @param maxConnections
	 *            max connections
	 * @return AWSS3Client
	 */
	public static S3Client getAWSS3Client(S3Configuration configuration,int maxConnections) {
		return configuration.isUseIAMCredentials() ? new S3ClientIAMOptimizedImpl(configuration, maxConnections) : new S3ClientStandardImpl(
				configuration, maxConnections);
	}

}
