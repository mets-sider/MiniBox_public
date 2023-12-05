package application;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MyServer {
 
 private HttpServer server;
 private Consumer<String> onCodeReceivedCallback;
 private Runnable onCloseCallback;
 
 public void start() throws IOException {
  int port = 8080;
  server = HttpServer.create(new InetSocketAddress(port), 0);
  server.createContext("/", new MyHandler());
  server.start();
 }
 
 public void setOnCodeReceivedCallback(Consumer<String> callback) {
  this.onCodeReceivedCallback = callback;
 }
 
 public void setOnCloseCallback(Runnable onCloseCallback) {
  this.onCloseCallback = onCloseCallback;
 }
 
 private class MyHandler implements HttpHandler {
  
  public void handle(HttpExchange t) throws IOException {
   try {
   URI requestUri = t.getRequestURI();
   Map<String, String> queryParameters = parseQueryParameters(requestUri.getQuery());
   String resBody = "<html>" +
                    "<head><style>body { font-family: Arieal, sans-serif; text-align: center; }</style></head>" +
                    "<body>" +
                    "<br>" +
                    "<h2>認証が完了しました</h2>" +
                    "<p>このウィンドウを閉じてください。</p>" +
                    "</body>" +
                    "</html>";
   Headers resHeaders = t.getResponseHeaders();
   resHeaders.set("Content-Type", "text/html; charset=UTF-8");
   int statusCode = 200;
   byte[] resBytes = resBody.getBytes(StandardCharsets.UTF_8);
   long contentLength = resBytes.length;
   t.sendResponseHeaders(statusCode, contentLength);
   OutputStream os = t.getResponseBody();
   os.write(resBytes);
   os.close();

   if (queryParameters.containsKey("code")) {
    String code  = queryParameters.get("code");
    if (onCodeReceivedCallback != null) {
     onCodeReceivedCallback.accept(code);
    }
    if (onCloseCallback != null) {
     onCloseCallback.run();
    }
   }
   } catch (IOException e) {
    e.printStackTrace();
   }
  }
  
  private Map<String, String> parseQueryParameters(String query) {
   Map<String, String> result = new HashMap<>();
   if (query != null && !query.isEmpty()) {
    for (String param : query.split("&")) {
     String[] keyValue = param.split("=");
     if (keyValue.length > 1) {
      result.put(keyValue[0], keyValue[1]);
     }
    }
   }
   return result;
  }
  

  }
 

 
}