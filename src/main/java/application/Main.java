package application;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

 public void start(Stage primaryStage) {
  try {
   Parent root = FXMLLoader.load(getClass().getResource("LoginWindow.fxml"));
   Scene scene = new Scene(root);
   // ステージにシーンをセットして表示する
   primaryStage.setScene(scene);
   primaryStage.setTitle("MiniBox");
   primaryStage.show();

  } catch (IOException e) {
   // TODO 自動生成された catch ブロック
   e.printStackTrace();
  }
 }

 public static void main(String[] args) {
  launch(args);
 }
}
