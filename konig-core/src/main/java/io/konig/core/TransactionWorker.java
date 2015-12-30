package io.konig.core;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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


/**
 * A worker that is invoked when a transaction ends.
 * @author Greg McFall
 *
 */
public interface TransactionWorker {

	/**
	 * Notify this worker that a transaction on a specified graph is being committed, and give the worker 
	 * an opportunity to rollback the transaction.  If the worker wishes to vote to rollback, it 
	 * should call the {@link Transaction#rollback()} method on the supplied graph.
	 * <p>
	 * A worker may inject transaction side-effects by adding new statements to the graph.
	 * If any worker adds side-effects, then another round of commit notifications will commence.
	 * The worker may inspect the statements added since the last round by calling graph.tx().asList().
	 * </p>
	 * 
	 * @param graph The graph whose transaction has ended.
	 */
	void commit(Graph graph);
}
