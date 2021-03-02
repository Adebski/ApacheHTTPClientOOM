# Overview
This repository contains a client and server that can be used to reproduce an issue
where an Apache HTTP client becomes unusable if `java.lang.OutOfMemoryError` is thrown from the
library code.

To compile and run the code you need to have Maven installed, you can get if 
from [here](https://maven.apache.org/download.cgi). 

# Compiling
Run `mvn package`

## Running
Firstly you need to spawn the server and after it's ready you can start the client.

Server: `java -Xmx128m -cp target/ApacheHTTPClientOOM-1.0-jar-with-dependencies.jar com.adebski.Server`

Client: `java -Xms9m -Xmx9m -cp target/ApacheHTTPClientOOM-1.0-jar-with-dependencies.jar com.adebski.Client`

