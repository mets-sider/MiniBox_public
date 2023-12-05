package application;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Semaphore;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class LoginController {
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // ■ フィールド
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 BoxAPIConnectionManager connectionManager = BoxAPIConnectionManager.getInstance();
 TokenManager tokenManager;
 ConfigData configData;
 String clientId;
 String clientSecret;
 String redirectUri;
 String code;
 String accessToken;
 String refreshToken;
 Semaphore semaphore = new Semaphore(0);

 @FXML
 private Button loginButton;

 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // ■ メソッド
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // 最初に実行される処理
 @FXML
 private void initialize() {
  this.clientId = connectionManager.getClientId();
  this.clientSecret = connectionManager.getClientSecret();
  this.redirectUri = connectionManager.getRedirectUri();
  this.accessToken = connectionManager.getAccessToken();
  this.refreshToken = connectionManager.getRefreshToken();
 }

 // ログインハンドラー
 @FXML
 private void handleLoginButton() {
  // アクセストークンが有効な場合
  if (connectionManager.handleAccess() != null) {
   closeCurrentWindow();
   showMainWindow();
   return;
  }
  // アクセストークンとリフレッシュトークンどちらも無効な場合
  openAuthorizationWindow();
  startMyServer();
 }

 // Box認証画面を開く
 private void openAuthorizationWindow() {
  String authorizationUrl = "https://account.box.com/api/oauth2/authorize?response_type=code&client_id=" + clientId
    + "&redirect_uri=" + redirectUri;
  if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
   try {
    Desktop.getDesktop().browse(new URI(authorizationUrl));
   } catch (Exception e) {
    e.printStackTrace();
   }
  }
 }
 
 // HttpServerを起動
 private void startMyServer() {
  MyServer myServer = new MyServer();
  System.out.println("hoge5");
  myServer.setOnCodeReceivedCallback(this::reseiveCode);
  System.out.println("hoge6");
  try {
   myServer.start();
   System.out.println("hoge");
   semaphore.acquire();
   System.out.println("hoge2");
   connectionManager.handleCreateAPI(code);
   System.out.println("hoge3");
   closeCurrentWindow();
   System.out.println("hoge4");
   showMainWindow();
  } catch (IOException | InterruptedException e) {
   e.printStackTrace();
  }
 }

 // コードを取得
 private void reseiveCode(String code) {
  this.code = code;
  semaphore.release();
 }

 // メインウィンドウを開く
 private void showMainWindow() {
  MainWindow mainWindow = new MainWindow();
  Stage currentStage = (Stage) loginButton.getScene().getWindow();
  mainWindow.startWithStage(currentStage);
 }

 // 現在のウィンドウを閉じる
 private void closeCurrentWindow() {
  ((Stage) loginButton.getScene().getWindow()).close();
 }

}