package com.adebski;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) throws IOException {
        createServer();
        logger.info("Server started");
    }

    private static final class RandomNumberOfHeadersHandler implements HttpHandler {
        private static final AtomicInteger NUMBER_OF_HEADERS = new AtomicInteger(100);

        @Override
        public void handle(HttpExchange t) throws IOException {
            int number_of_headers = NUMBER_OF_HEADERS.addAndGet(10);

            if (number_of_headers % 100 == 0) {
                logger.info("NUMBER_OF_HEADERS is {}", number_of_headers);
            }
            for (int i = 0; i < number_of_headers; ++i) {
                t.getResponseHeaders().add("Header-" + i, String.valueOf(number_of_headers));
            }

            String response = "Response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private static final class NoAdditionalHeadersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            logger.info("NoAdditionalHeadersHandler endpoint");

            String response = "Response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // Using HTTP server implementation available in the Jada SDK.
    // Based on https://stackoverflow.com/questions/3732109/simple-http-server-in-java-using-only-java-se-api
    // com.sun.* packages are part of the public API, as opposed to sun.* packages.
    // https://www.oracle.com/java/technologies/faq-sun-packages.html
    private static void createServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);
        server.createContext("/randomNumberOfHeaders", new RandomNumberOfHeadersHandler());
        server.createContext("/noAdditionalHeadersHandler", new NoAdditionalHeadersHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}
