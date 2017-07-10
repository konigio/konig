package io.konig.content.gae;

/*
 * #%L
 * Konig Content System, Google App Engine implementation
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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;


public class BundleNotificationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String CONTENT_BUNDLE_QUEUE_NAME = "content-bundle-unzip";
	private static final String TASK_HANDLER_URL = "/tasks/content-bundle-unzip";

	public final void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		
		ServletInputStream input = req.getInputStream();
		
		int count;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] data = new byte[1024];
		while ((count = input.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, count);
		}

		buffer.flush();
		data = buffer.toByteArray();
		
//		Queue queue = QueueFactory.getQueue(CONTENT_BUNDLE_QUEUE_NAME);
		Queue queue = QueueFactory.getDefaultQueue();
		
		TaskOptions task = TaskOptions.Builder.withPayload(data, StandardCharsets.UTF_8.name()).url(TASK_HANDLER_URL);
		
		queue.add(task);
		
		resp.setContentType("text/plain");
		resp.getWriter().println("Task has been added to queue");
		
	}
}
