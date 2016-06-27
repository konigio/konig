package io.konig.schemagen.gcp;

/**
 * An interface that consumes BigQuery Table definitions.
 * @author Greg McFall
 *
 */
public interface BigQueryTableHandler {
	
	/**
	 * Consume a BigQuery table definition.
	 * @param table The table definition that is consumed.
	 */
	public void add(BigQueryTable table);

}
