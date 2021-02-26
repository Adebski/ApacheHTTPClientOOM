package com.adebski;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Client {
    private static final Logger logger = LogManager.getLogger(Client.class);
    private static final String RANDOM_NUMBER_OF_HEADERS_ADDRESS = "http://localhost:12345/randomNumberOfHeaders";
    private static final String NO_ADDITIONAL_HEADERS_ADDRESS = "http://localhost:12345/noAdditionalHeadersHandler";
    private static final HttpClient CLIENT =
        // We will have multiple threads calling the server so we need to use a pool of connections
        HttpClientBuilder.create()
            .setConnectionManager(
                new PoolingHttpClientConnectionManager())
            .build();
    private static final int NUMBER_OF_THREADS = 4;

    public static void main(String[] args) throws IOException, InterruptedException {
        // Printing PID just in case we want to use one of the JDK command line tools to inpsect the process status
        logger.info("PID {}", ProcessHandle.current().pid());
        List<Thread> workerThreads = createWorkerThreads();
        for (Thread thread : workerThreads) {
            thread.start();
        }
        for (Thread thread : workerThreads) {
            thread.join();
        }

        // At this point in the execution all the worker threads should have terminated and the HttpClient is not usable
        // Let's confirm that by making one more request
        try {
            HttpGet request = new HttpGet(NO_ADDITIONAL_HEADERS_ADDRESS);
            CLIENT.execute(request);
        } catch (IllegalStateException e) {
            if ("Connection pool shut down".equals(e.getMessage())) {
                logger.info("Expected exception received!");
            } else {
                throw e;
            }
        }
    }

    private static List<Thread> createWorkerThreads() {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_THREADS; ++i) {
            Thread thread = new Thread(Client::workerThreadLogic, "WorkerThread-" + i);
            threads.add(thread);
        }
        return threads;
    }

    private static void workerThreadLogic() {
        // We will loop infinitely until we receive IllegalStateException indicating that the library has shut down
        // the connection pool
        while (true) {
            try {
                HttpGet request = new HttpGet(RANDOM_NUMBER_OF_HEADERS_ADDRESS);
                HttpResponse response = CLIENT.execute(request);
                // We need to consume the response in order to release the connection in the pool maintained
                // by the Apache client library
                EntityUtils.consume(response.getEntity());

                Thread.sleep(ThreadLocalRandom.current().nextInt(150));
            } catch (IllegalStateException e) {
                logger.info("Illegal state exception received, stopping the loop");
                break;
            } catch (Throwable t) {
                logger.info(
                    "Exception other than IllegalStateException received, logging and retrying the connection",
                    t);
            }
        }

        try {
            HttpGet request = new HttpGet(RANDOM_NUMBER_OF_HEADERS_ADDRESS);
            request.setConfig(
                RequestConfig.custom().setSocketTimeout(1000000).build()
            );
            CLIENT.execute(request);
        } catch (Exception e) {
            logger.error("Last request", e);
        }
    }
}
