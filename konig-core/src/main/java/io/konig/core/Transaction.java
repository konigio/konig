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


import java.util.List;

public interface Transaction {
	
	public enum Status {
		
		/**
		 * The transaction is closed.
		 */
		CLOSED,
		
		/**
		 * The transaction is open.
		 */
		OPEN,
		
		/**
		 * The transaction workers are voting on disposition of the transaction.
		 */
		VOTE,
		
		/**
		 * All transaction workers have voted to commit the transaction
		 */
		COMMIT,
		
		/**
		 * The client application or at least one worker has voted to rollback the transaction.
		 */
		ROLLBACK
	}
	

	/**
	 * Open a transaction.
	 */
	void open();
	
	/**
	 * Check whether a transaction is open
	 * @return True if a transaction is open and false otherwise.
	 */
	Status getStatus();
	
	/**
	 * Commit the currently open transaction
	 */
	void commit();
	void rollback();
	
	/**
	 * Get the list of edges added to the graph in the current transaction
	 * @return The list of edges added to the graph in the current transaction, or null if no transaction is open.
	 */
	List<Edge> asList();
	
	void addWorker(TransactionWorker worker);
	void removeWorker(TransactionWorker worker);
	

}
