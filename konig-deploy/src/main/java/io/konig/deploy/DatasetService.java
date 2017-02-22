package io.konig.deploy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.BigqueryScopes;
import com.google.api.services.bigquery.model.Dataset;
import com.google.api.services.bigquery.model.DatasetList;
import com.google.api.services.bigquery.model.DatasetList.Datasets;
import com.google.api.services.bigquery.model.DatasetReference;


public class DatasetService {
	
	public void processRequest(String action, String filePath) {
		try {
				JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
				HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
				GoogleCredential credential = GoogleCredential.getApplicationDefault();
			
				if (credential.createScopedRequired()) {
					Collection<String> bigqueryScopes = BigqueryScopes.all();
					credential = credential.createScoped(bigqueryScopes);
				}
				
				Bigquery bigQuery = new Bigquery.Builder(httpTransport, jsonFactory, credential)
														.setApplicationName("Google Cloud Platform Pearson")
														.build();
				
				Pattern pattern = Pattern.compile(".*\\.json");
		        List<File> fileList = new LinkedList<File>();
		        getFiles(new File(filePath), pattern, fileList);
				
				for (File jsonFile : fileList) {
					FileReader jsonFileReader = new FileReader(jsonFile);
				    BufferedReader jsonBufReader = new BufferedReader(jsonFileReader);
				    JsonParser jsonParser = jsonFactory.createJsonParser(jsonBufReader);
				    Dataset dataset = jsonParser.parseAndClose(Dataset.class);
				    System.out.println(dataset.getId());
				    System.out.println(dataset.getDatasetReference().getDatasetId());
				}
				System.out.println("----------------------------------------------------------------------");
				
				Dataset dataset = new Dataset();
				DatasetReference datasetRef = new DatasetReference();
			    datasetRef.setProjectId("pearson-rr");
			    datasetRef.setDatasetId("Konig_Test");
			    dataset.setDatasetReference(datasetRef);
			    
//			    Bigquery.Datasets.Insert insert = bigQuery.datasets().insert("pearson-rr", dataset);
//			    Dataset dsin = insert.execute();
			    
//			    Bigquery.Datasets.Update update = bigQuery.datasets().update("pearson-rr", "Konig_Test", dataset);
//			    Dataset dsup = update.execute();
//			    System.out.println(dsup.getId());
//			    System.out.println(dsup.getDescription());
			    
//			    Bigquery.Datasets.Delete delete = bigQuery.datasets().delete("pearson-rr", "Konig_Test");
//			    delete.execute();

			    
			    Bigquery.Datasets.List list = bigQuery.datasets().list("pearson-rr");
				DatasetList dlist = list.execute();
				List<Datasets> datasetsList = dlist.getDatasets();
				for (Datasets datasets : datasetsList) {
//					System.out.println(datasets.getId());
//					System.out.println(dataset.getDescription());
//					System.out.println(datasets.getDatasetReference().getProjectId());
					System.out.println(datasets.getDatasetReference().getDatasetId());

				}
				
						
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
	private static void getFiles(File dir, Pattern pattern, List<File> fileList) {
		File[] files = dir.listFiles();
        if(null == files) return;
        for (File file : files)
        	if (file.isDirectory()) getFiles(file, pattern, fileList);
            else if (file.isFile()) {
            	Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) fileList.add(file);
            	}
    		}
		}
