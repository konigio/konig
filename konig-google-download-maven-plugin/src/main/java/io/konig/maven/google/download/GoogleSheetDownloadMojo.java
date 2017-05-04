package io.konig.maven.google.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;


@Mojo( name = "download")
public class GoogleSheetDownloadMojo extends AbstractMojo {
   
	
    
    @Parameter(required=true)
    private String documentId;
    
    @Parameter(required=true)
    private File saveAs;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		GoogleDownloadClient client = new GoogleDownloadClient();
		
		try {
			client.execute(documentId, saveAs);
		} catch (DownloadException e) {
			throw new MojoExecutionException("Failed to download and save Google Sheet", e);
		}

	}


}
