package io.konig.maven;

/*
 * #%L
 * Konig GCP Deployment Maven Plugin
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


import com.google.pubsub.v1.TopicName;

import io.konig.gcp.common.GoogleCloudService;

public class DeleteGooglePubSubTopicAction {
	private KonigDeployment deployment;
	
	public DeleteGooglePubSubTopicAction(KonigDeployment deployment) {
		this.deployment = deployment;
	}

	public KonigDeployment named(String topicName) {
		GoogleCloudService service = deployment.getService();
		String projectId = service.getProjectId();
		try {
			TopicName topic = TopicName.create(projectId, topicName);
			service.topicAdmin().deleteTopic(topic);
			deployment.setResponse("Topic " + topicName + " was deleted");
		} catch (Exception ex) {
			throw ex;
		}
		return deployment;
	}
}
