package io.konig.maven;

/*
 * #%L
 * Konig GCP Deployment Maven Plugin
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


import java.io.File;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import io.konig.gcp.common.GoogleCloudService;

public class DeleteGoogleCloudStorageBucketAction {
	
	private KonigDeployment deployment;
	
	public DeleteGoogleCloudStorageBucketAction(KonigDeployment deployment) {
		this.deployment = deployment;
	}
	
	public KonigDeployment from(String path) throws Exception {
		GoogleCloudService service = deployment.getService();
		File file = deployment.file(path);
		Storage storage = StorageOptions.getDefaultInstance().getService();
		try {
			BucketInfo info = service.readBucketInfo(file);
			Bucket bucket = storage.get(info.getName());
			if(bucket != null) {
				boolean isDeleted = storage.delete(info.getName());
				if(isDeleted) {
					deployment.setResponse("Deleted Bucket " + info.getName());
				}
			} else {
				deployment.setResponse("Bucket " + info.getName()+ " not found" );
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
		return deployment;
	}
}
