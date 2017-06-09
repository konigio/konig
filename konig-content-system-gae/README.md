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

# Google Cloud Resources

Storage Bucket: pearson-docs-content-bundle

Pub/Sub Topics: content-bundle-notification

Task Queue: content-bundle-unzip

## Commands to create resources
gsutil mb gs://pearson-docs-content-bundle
gsutil notification create -t content-bundle-notification -f json gs://pearson-docs-content-bundle 

gcloud beta pubsub subscriptions create GaeZipBundleTaskHandler --topic content-bundle-notification --push-endpoint https://pearson-docs.appspot.com/pubsub/content-bundle-notification  --ack-deadline 10

