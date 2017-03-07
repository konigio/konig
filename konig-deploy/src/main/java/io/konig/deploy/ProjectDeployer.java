package io.konig.deploy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.ListProjectsResponse;
import com.google.api.services.cloudresourcemanager.model.Project;

public class ProjectDeployer {

	public static void main(String args[]) {
		
//		do arguments validation
		String jsonFile = "D:/Tmp/konig.json";
		String action = "create";
		
		try {
			    FileReader jsonFileReader = new FileReader(jsonFile);
			    BufferedReader jsonBufReader = new BufferedReader(jsonFileReader);
			    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			    JsonParser jsonParser = jsonFactory.createJsonParser(jsonBufReader);
			    Project project = jsonParser.parseAndClose(Project.class);
			    System.out.println(project);
			    jsonBufReader.close();
			    jsonFileReader.close();
		    
			    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
				GoogleCredential credential = GoogleCredential.getApplicationDefault();

			    if (credential.createScopedRequired()) {
			    	credential = credential.createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
			    	}

			    CloudResourceManager cloudresourcemanagerService = new CloudResourceManager.Builder(httpTransport, jsonFactory, credential)
		  	            																						.setApplicationName("Google Cloud Platform Pearson")
		  	            																						.build();
/*
		  	    CloudResourceManager.Projects.Create requestCreate = cloudresourcemanagerService.projects().create(project);
		  	    Project responseCreate = requestCreate.execute();
		  	    System.out.println(responseCreate.getProjectNumber());
		  	    System.out.println(responseCreate.getProjectId());
		  	    System.out.println(responseCreate.getLifecycleState());
		  	    System.out.println(responseCreate.getName());
		  	    System.out.println(responseCreate.getLabels());
		  	    System.out.println(responseCreate.getCreateTime());
*/
		  	    CloudResourceManager.Projects.List requestList = cloudresourcemanagerService.projects().list();
		  	    ListProjectsResponse responseList;
		  	    do {
		  	    		responseList = requestList.execute();
		  	    		if (null == responseList.getProjects()) continue;
		  	    		for (Project projectList : responseList.getProjects()) {
		  	    			System.out.println(projectList.getName());
		  	    			System.out.println(projectList.getProjectId());
		  	    			System.out.println(projectList.getCreateTime());
		  	    			System.out.println(projectList.getProjectNumber());
		  	    			System.out.println(projectList.getLabels());
		  	    		}
		  	    		requestList.setPageToken(responseList.getNextPageToken());
		  	    	} while (null != responseList.getNextPageToken());

//		  	  	Project projectUpdateRequest = new Project();
//		  	  	String projIdUpdate = projectUpdateRequest.getProjectId();
/*		  	    
		  	    String projIdUpdate = project.getProjectId();
		  	    CloudResourceManager.Projects.Update requestUpdate = cloudresourcemanagerService.projects().update(projIdUpdate, project);
		  	    Project projectUpdateResponse = requestUpdate.execute();
		  	    System.out.println(projectUpdateResponse.getName());
	
*/
		  	  	String projIdDelete = project.getProjectId();
		  	  	CloudResourceManager.Projects.Delete requestDelete = cloudresourcemanagerService.projects().delete(projIdDelete);
		  	  	requestDelete.execute();
		  	 
		  	 
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
