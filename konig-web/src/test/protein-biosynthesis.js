$(document).ready(function() {
	
	if (typeof(konigTestData) === 'undefined') {
		konigTestData = {};
	}
	
	konigTestData.suggest = {
		resource: {
			"ri" : [{
	    		id: "ke:Ribonucelease_III",
	    		prefLabel: "Ribonuclease III"
	    	},{
	    		id: "ke:Ribosomal_binding_site",
	    		prefLabel: "Ribosomal binding site"
	    	},{
	    		id: "ke:Ribosomal_protein",
	    		prefLabel: "Ribosomal protein"
	    	},{
	    		id: "ke:Ribosomal_RNA",
		    	prefLabel: "Ribosomal RNA"
	    	},{
	    		id: "ke:Ribosome",
	    		prefLabel: "Ribosome"
	    	},{
	    		id: "ke:Ribosome_shunting",
	    		prefLabel: "Ribosome shunting"
	    	}]
		},
		property: {
			
		}
	};
	
	konigTestData["http://www.konig.io/entity/Ribonucelease_III"] = {
		"id" : "ke:Ribonucelease_III",
		"prefLabel" : "Ribonuclease III"
	};

	konigTestData["http://www.konig.io/entity/Ribosomal_binding_site"] = {
		"id" : "ke:Ribonucelease_III",
		"prefLabel" : "Ribosomal binding site"
	};

	konigTestData["http://www.konig.io/entity/Ribosomal_protein"] = {
		"id" : "ke:Ribosomal_protein",
		"prefLabel" : "Ribosomal protein"
	};

	konigTestData["http://www.konig.io/entity/Ribosomal_RNA"] = {
		"id" : "ke:Ribosomal_RNA",
		"prefLabel" : "Ribosomal RNA"
	};

	konigTestData["http://www.konig.io/entity/Ribosome_shunting"] = {
		"id" : "ke:Ribosome_shunting",
		"prefLabel" : "Ribosome shunting"
	};

	konigTestData["http://www.konig.io/entity/Ribosome"] = {
		"id" : "ke:Ribosome",
		"prefLabel" : "Ribosome",
		"isPrimaryTopicOf" : {
        	 "id" : "https://en.wikipedia.org/wiki/Ribosome",
        	 "type" : "schema:WebPage",
        	 "prefLabel" : "Ribosome (Wikipedia)"
		}
	};

});	