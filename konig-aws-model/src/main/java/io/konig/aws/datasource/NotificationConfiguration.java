package io.konig.aws.datasource;

/*
 * #%L
 * Konig AWS Model
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


import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.AWS;

public class NotificationConfiguration {
	
	private TopicConfiguration topicConfiguration;
	private QueueConfiguration queueConfiguration;
	
	@RdfProperty(AWS.TOPIC_CONFIGURATION)
	public TopicConfiguration getTopicConfiguration() {
		return topicConfiguration;
	}

	public void setTopicConfiguration(TopicConfiguration topicConfiguration) {
		this.topicConfiguration = topicConfiguration;
	}
	
	@RdfProperty(AWS.QUEUE_CONFIGURATION)
	public QueueConfiguration getQueueConfiguration() {
		return queueConfiguration;
	}

	public void setQueueConfiguration(QueueConfiguration queueConfiguration) {
		this.queueConfiguration = queueConfiguration;
	}
}
