package application;

import java.util.Map;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxUser;

public class BoxAPIConnectionManager {
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // ■ フィールド
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 private static BoxAPIConnectionManager instance;

 private BoxAPIConnection api;
 private String clientId;
 private String clientSecret;
 private String redirectUri;
 private String accessToken;
 private String refreshToken;
 private final TokenManager tokenManager;
 ConfigData configData;

 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // ■ ゲッター
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 public String getClientId() {
  return clientId;
 }

 public String getClientSecret() {
  return clientSecret;
 }

 public String getRedirectUri() {
  return redirectUri;
 }

 public String getAccessToken() {
  return accessToken;
 }

 public String getRefreshToken() {
  return refreshToken;
 }

 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // ■ セッター
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 private void setAccessToken(BoxAPIConnection api) {
  this.accessToken = api.getAccessToken();
 }

 private void setRefreshToken(BoxAPIConnection api) {
  this.refreshToken = api.getRefreshToken();
 }

 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // ■ メソッド
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // シングルトン
 public static synchronized BoxAPIConnectionManager getInstance() {
  if (instance == null) {
   instance = new BoxAPIConnectionManager();
  }
  return instance;
 }

 // コンストラクタ
 private BoxAPIConnectionManager() {
  this.tokenManager = new TokenManager();
  load();
 }

 // トークン生成
 public void handleCreateAPI(String code) {
  BoxAPIConnection api = new BoxAPIConnection(clientId, clientSecret, code);
  setAccessToken(api);
  setRefreshToken(api);
  this.api = api;
  saveToJson();
 }

 // JSONへ保存
 private void saveToJson() {
  tokenManager.saveToken("accessToken", accessToken);
  tokenManager.saveToken("refreshToken", refreshToken);
 }

 // アクセストークンで認証
 public BoxAPIConnection authorizeByToken() {
  return new BoxAPIConnection(accessToken);
 }

 // JSONデータ読み込み
 private void load() {
  Map<String, String> configData = tokenManager.load();
  this.clientId = configData.get("clientId");
  this.clientSecret = configData.get("clientSecret");
  this.redirectUri = configData.get("redirectUri");
  this.accessToken = configData.getOrDefault("accessToken", null);
  this.refreshToken = configData.getOrDefault("refreshToken", null);
 }

 // リフレッシュトークンでアクセス（リフレッシュ）トークンを再生成
 public BoxAPIConnection refreshToken() {
  try {
   BoxAPIConnection newApi = new BoxAPIConnection(clientId, clientSecret, accessToken, refreshToken);
   newApi.refresh();
   handleSaveToken(newApi);
   this.api = newApi;
   return api;
  } catch (BoxAPIException | IllegalStateException e) {
   e.printStackTrace();
   return null;
  }
 }

 // セーブハンドラー
 private void handleSaveToken(BoxAPIConnection api) {
  String accessTokenString = api.getAccessToken();
  String refreshTokenString = api.getRefreshToken();
  this.accessToken = accessTokenString;
  this.refreshToken = refreshTokenString;
  tokenManager.saveToken("accessToken", accessTokenString);
  tokenManager.saveToken("refreshToken", refreshTokenString);
  setAccessToken(api);
  setRefreshToken(api);
 }

 // アクセスハンドラー
 public BoxAPIConnection handleAccess() {
  // トークンが存在する場合は接続テスト
  if (accessToken != null) {
   try {
    api = authorizeByToken();
    BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo();
    System.out.println("User ID" + userInfo.getID());
    return api;
   } catch (BoxAPIException | IllegalStateException e) {
    e.printStackTrace();
    // 接続テストに失敗した場合
    // アクセストークンが期限切れの場合
    if (((BoxAPIException) e).getResponseCode() == 401) {
     // リフレッシュトークンで更新
     return refreshToken();
     // リフレッシュトークンが古かった場合
    } else {
     e.printStackTrace();
    }
   } catch (Exception e) {
    e.printStackTrace();
   }
  }
  return null;
 }

}