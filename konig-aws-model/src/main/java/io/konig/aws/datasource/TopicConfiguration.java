package io.konig.aws.datasource;

import org.openrdf.model.URI;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.AWS;

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


public class TopicConfiguration{
	
	private Topic topic;
	private String topicArn;
	private String eventType;
	
	@RdfProperty(AWS.TOPIC)
	public Topic getTopic() {
		return topic;
	}
	public void setTopic(Topic topic) {
		this.topic = topic;
	}
	
	public void setTopicArn(String topicArn) {
		this.topicArn = topicArn;
	}
	
	public String getTopicArn() {
		return topicArn==null?(topic==null||topic.getId()==null ? null : topic.getId().stringValue()):topicArn;
	}
	
	@RdfProperty(AWS.EVENT_TYPE)
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	
}
