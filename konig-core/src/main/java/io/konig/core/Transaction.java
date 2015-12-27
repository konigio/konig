package io.konig.core;

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
