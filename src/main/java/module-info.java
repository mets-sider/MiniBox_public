module miniBox {
 requires javafx.controls;
 requires javafx.fxml;
 requires box.java.sdk;
 requires javafx.graphics;
 requires com.google.gson;
 requires java.desktop;
 requires javafx.swing;
 requires javafx.base;
 requires jdk.httpserver;
 requires javafx.web;
 requires jdk.jsobject;
 
 opens application to javafx.graphics, javafx.fxml;
}
