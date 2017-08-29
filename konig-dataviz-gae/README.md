dataviz
==================

This project builds a Google App Engine webapp that provides a static javascript library for
data visualization.

## Updating the Development Server

Use the following procedure to deploy a new version of the dataviz.js library:

1.  Replace the file at `/src/main/webapp/js/dataviz.js` with the latest revision.
2.  Increment the `<version>` element in the [appengine-web.xml](https://github.com/konigio/konig/blob/master/konig-dataviz-gae/src/main/webapp/WEB-INF/appengine-web.xml) file.
3.  Commit and push the changes to GitHub.
4.  On your local machine, run the following command in the directory containing this README (and the pom.xml file):

    mvn appengine:update

Make sure you have a recent version of [maven](https://maven.apache.org/download.cgi) installed (at least 3.5).
The first time you run the `appengine:update` command, it will open a browser so that you can authenticate
yourself and obtain credentials to perform the deployment.

The app is configured as the `dataviz` microservice within the `pearson-edw-core` project in the Google Cloud.
This is our development server. This means that the javascript file is deployed to the following address:

     http://dataviz.pearson-edw-core.appspot.com/js/dataviz.js

 When we are ready to deploy for use in production, we will change the `<application>` 
element in the `appengine-web.xml` file, and redeploy.



