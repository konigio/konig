dataviz
==================

This is Google App Engine webapp that provides a static javascript library for
data visualization.

See the [Google App Engine standard environment documentation][ae-docs] for more
detailed instructions.

[ae-docs]: https://cloud.google.com/appengine/docs/java/

To build this project, you must have a recent version of maven installed (at least 3.5).

* [Maven](https://maven.apache.org/download.cgi)


## Maven
### Running locally

    mvn appengine:devserver

### Deploying

    mvn appengine:update

The first time you run this command, it will open your browser so that you can authenticate
yourself and obtain credentials to perform the deployment.

## Updating to latest Artifacts

An easy way to keep your projects up to date is to use the maven [Versions plugin][versions-plugin].

    mvn versions:display-plugin-updates
    mvn versions:display-dependency-updates
    mvn versions:use-latest-versions


[versions-plugin]: http://www.mojohaus.org/versions-maven-plugin/
