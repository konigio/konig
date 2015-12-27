package io.konig.core.io;

public class GraphConstants {

	static final short VERSION = 1;
	
	static final byte EOF = 0;
	static final byte TERM = 1;
	static final byte QNAME = 2;
	static final byte IRI = 3; 
	static final byte BNODE = 4;
	static final byte RESOURCE = 5;
	static final byte LITERAL_TERM = 6;
	static final byte LITERAL_QNAME = 7;
	static final byte LITERAL_IRI = 8;
	static final byte LANG = 9;
	static final byte PLAIN = 10;
	static final byte GRAPH = 11;
	
	static final String[] LABEL = {
		"",
		"TERM",
		"QNAME",
		"IRI",
		"BNODE",
		"RESOURCE",
		"LITERAL_TERM",
		"LITERAL_QNAME",
		"LITERAL_IRI",
		"LANG",
		"PLAIN",
		"GRAPH"
	};
}
