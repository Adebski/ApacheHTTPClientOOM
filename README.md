# Overview
This repository contains the code for a blog post on what happens when `java.lang.OutOfMemoryError` is thrown 
from within the Apache HTTP client library. You can find the blog post [here] (https://blog.adebski.com/posts/apache-http-client-shutting-down/).

To compile and run the code you need to have Maven installed, you can get if 
from [here](https://maven.apache.org/download.cgi). 

# Compiling
Run `mvn package`

## Running
Firstly you need to spawn the server and after it's ready you can start the client.

Server: `java -Xmx128m -cp target/ApacheHTTPClientOOM-1.0-jar-with-dependencies.jar com.adebski.Server`

Client: `java -Xms9m -Xmx9m -cp target/ApacheHTTPClientOOM-1.0-jar-with-dependencies.jar com.adebski.Client`

