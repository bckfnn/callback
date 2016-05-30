# callback
Callback helpers for vertx

[![Download](https://api.bintray.com/packages/bckfnn/maven/callback/images/download.svg)](https://bintray.com/bckfnn/maven/callback/_latestVersion) 
[![Build Status](https://travis-ci.org/bckfnn/callback.svg?branch=master)](https://travis-ci.org/bckfnn/callback)

## Installation
    <repositories>
        <repository>
            <id>jcenter</id>
            <url>http://jcenter.bintray.com/</url>
        </repository>
    </repositories>

    <dependency>
        <groupId>io.github.bckfnn</groupId>
        <artifactId>callback</artifactId>
        <version>0.x.y</version>
    </dependency>

## Release

Releases management are done with [jgit-flow](https://bitbucket.org/atlassian/jgit-flow/wiki/Home) and deployment to done to bintray's jcenter.

Step to perform a release:

1. develop new features on the `develop` branch
2. `mvn jgitflow:release-start`
3. `mvn jgitflow:release-finish`
4. In eclipse do `Team/Remote/Push`, `Next`, `Next`, `Finish`