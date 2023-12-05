package application;

public class ConfigData {
 private String clientId;
 private String clientSecret;
 private String redirectUri;
 private String accessToken;
 private String refreshToken;

 public String getClientId() {
  return clientId;
 }

 public void setClientId(String clientId) {
  this.clientId = clientId;
 }

 public String getClientSecret() {
  return clientSecret;
 }

 public void setClientSecret(String clientSecret) {
  this.clientSecret = clientSecret;
 }

 public String getRedirectUri() {
  return redirectUri;
 }

 public void setRedirectUri(String redirectUri) {
  this.redirectUri = redirectUri;
 }

 public String getAccessToken() {
  return accessToken;
 }

 public void setAccessToken(String accessToken) {
  this.accessToken = accessToken;
 }

 public String getRefreshToken() {
  return refreshToken;
 }

 public void setRefreshToken(String refreshToken) {
  this.refreshToken = refreshToken;
 }

}
