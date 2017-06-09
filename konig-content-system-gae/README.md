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

Here's an overview of the usage pattern...

*STEP 1:* Get listof modified files.

Client posts the list of files with Base64 encoded, SHA-1 hash codes

```
POST https://pearson-docs.appspot.com/content/{bundleName}/{bundleVersion}
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
This is the URL to which the content bundle zip archive must be pushed.

*STEP 2:* Submit content bundle archive

The client creates a Zip archive containing all the files listed in the response
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

