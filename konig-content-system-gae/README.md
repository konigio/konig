appengine-standard-archetype
============================

This is a generated App Engine Standard Java application from the appengine-standard-archetype archetype.


* Java 7
* [Maven](https://maven.apache.org/download.cgi) (at least 3.3.9)
* [Gradle](https://gradle.org/gradle-download/) (optional)
* [Google Cloud SDK](https://cloud.google.com/sdk/) (aka gcloud)

Initialize the Google Cloud SDK using:

    gcloud init

This skeleton is ready to run.



    mvn appengine:run


    mvn appengine:deploy

This solution is based on [cloud-pubsub-samples-java](https://github.com/GoogleCloudPlatform/cloud-pubsub-samples-java/tree/master/appengine-push)

# Content System Overview
This project implements a very simple content management system.

Here's how it works in a nutshell.

1. *Get a list of files that are new or modified.*  A client has a collection of files on the local disk that it wishes to upload.  The client creates a CSV file where each line contains two values: (i) the path to some file (relative to a given root directory), and (ii) a Base64 encoding of the SHA-1 hash of the file contents.  The client posts this CSV file to `https://pearson-docs.appspot.com/content/{bundleName}/{bundleVersion}`.  The server determines which of the given files are already available from the server.  It sends back a response that lists those files that are new or modified (i.e. have a different hash) from the version currently available from the server.

2. *Submit content bundle.* The client creates a zip archive of the files that are new or modified.  The client pushes the archive to the Cloud Storage bucket named `pearson-docs-content-bundle`. 

3. *Process the content bundle.* The `pearson-docs-content-bundle` bucket publishes an `OBJECT_FINALIZE` event to Pub/Sub.  A servlet at `https://pearson-docs.appspot.com/pubsub/content-bundle-notification` subscribes to the event.  It repackages the event as a Task and pushes the task into the default Task Queue.  A second servlet at `https://pearson-docs.appspot.com/tasks/content-bundle-unzip` receives the task.  This servlet gets the zip archive from the `pearson-docs-content-bundle` bucket and for each file from the archive stores two entities in Datastore: (i) a metadata record about the file, (ii) a Blob that holds the contents of the file.

# Sample Interactions

*STEP 1:* Get list of modified files.

Client posts the list of files with Base64 encoded, SHA-1 hash codes

```
POST https://pearson-docs.appspot.com/content/content-test/latest
CONTENT-TYPE: text/csv

allclasses-index.html,Ck1VqNd45QIvq3AZd8XYQLvEhtA
schema/ontology-summary.html,1Am102BoWSocBsKfw7fxaDk5h5M
schema/classes/Person.html,jEGuiEZ_5butCfzUiGYrJcPsUzM
schema/classes/Organization.html,UZJVrh90_8Xd0pl5KVx1cvBIrYE
...

```

Server responds with a list of files that need to be uploaded.

```
HTTP/1.1 200 OK
Content-Type: text/plain
Link: <gs://pearson-docs-content-bundle>; rel=edit

schema/classes/Person.html
schema/classes/Organization.html
...

```

Notice that the response includes a Link header with relationship type "edit".
This is the URL to which the zip archive should be pushed.

*STEP 2:* Submit content bundle archive

The client creates a zip archive containing all the new and modified files listed in the response
from the content server.

Clients pushes the zip archive to the specified Cloud Storage bucket with the
content type `application/zip`

The bucket emits a `create` notification to  `/pearson-docs/topics/content-bundle-notification`

*STEP 3:* Process the content bundle archive

The Content System server receives the `create` notification at `https://pearson-docs.appspot.com/pubsub/content-bundle-notification`

The Content System adds a task into the `content-bundle-unzip` queue.

The App Engine container pushes the task to `https://pearson-docs.appspot.com/tasks/content-bundle-unzip`


# Google Cloud Resources

To summarize, we have the following Google Cloud resources:

- Storage Bucket: pearson-docs-content-bundle

- Pub/Sub Topics: content-bundle-notification

- Task Queue: content-bundle-unzip

## Commands to create resources
gsutil mb gs://pearson-docs-content-bundle
gsutil notification create -t content-bundle-notification -f json gs://pearson-docs-content-bundle 

gcloud beta pubsub subscriptions create GaeZipBundleTaskHandler --topic content-bundle-notification --push-endpoint https://pearson-docs.appspot.com/pubsub/content-bundle-notification  --ack-deadline 10

