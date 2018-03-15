package io.konig.aws.datasource;

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


public class QueueConfiguration{
	private Queue queue;
	private String eventType;
	

	@RdfProperty(AWS.QUEUE)
	public Queue getQueue() {
		return queue;
	}
	public void setQueue(Queue queue) {
		this.queue = queue;
	}
	
	
	@RdfProperty(AWS.EVENT_TYPE)
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public String getQueueArn() {
		return queue==null || queue.getId()==null ? null : queue.getId().stringValue();
	}
	
}
