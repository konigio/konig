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


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class BundleEnqueueServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String TASK_HANDLER_URL = "/tasks/content-bundle-dequeue";

	@Override
	public final void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		
	
		BundleDequeueRequest request = null;
		
		try {
			request = new BundleDequeueRequest(req.getReader());
		} catch (BadRequestException bad) {
			throw new ServletException(bad);
		}
		
		
		Queue queue = QueueFactory.getDefaultQueue();
		
		TaskOptions task = TaskOptions.Builder
				.withUrl(TASK_HANDLER_URL)
				.param("bucketId", request.getBucketId())
				.param("objectId", request.getObjectId());
		
		queue.add(task);
		
		resp.setContentType("text/plain");
		resp.getWriter().println("Task has been added to queue");
		
	}

}
