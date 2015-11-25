package io.konig.core;

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
