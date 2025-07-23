import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SimpleServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/", SimpleServer::handleIndex);
        server.createContext("/login", SimpleServer::handleLogin);

        server.setExecutor(null); // default executor
        server.start();
        System.out.println("Server started at http://localhost:8000");
    }

    private static void handleIndex(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            byte[] response = Files.readAllBytes(Paths.get("index.html"));
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    private static void handleLogin(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseQuery(body);
            String username = params.getOrDefault("username", "");
            String password = params.getOrDefault("password", "");
            String message;
            if ("admin".equals(username) && "pass".equals(password)) {
                message = "<h1>Login success</h1>";
            } else {
                message = "<h1>Login failed</h1>";
            }
            byte[] response = message.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private static Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return map;
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }
        return map;
    }
}
