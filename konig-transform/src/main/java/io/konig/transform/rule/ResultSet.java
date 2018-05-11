package io.konig.transform.rule;

/**
 * An interface for a temporary result set that can be used to construct a query or transform.
 * Instances of this interface get rendered as Common Table Expressions in SQL exposed via the WITH clause.
 * 
 * @author Greg McFall
 *
 */
public interface ResultSet {

	DataChannel getChannel();

}
