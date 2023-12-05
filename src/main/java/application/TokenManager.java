package application;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TokenManager {
 private static final String CONFIG_FILE = "config.json";
 private Gson gson = new Gson();

 public Map<String, String> load () {
  try (
    Reader reader = new FileReader(CONFIG_FILE)) {
    Type type = new TypeToken<Map<String, String>>(){}.getType();
    Map<String, String> tokenMap = gson.fromJson(reader, type);
    return tokenMap != null ? tokenMap : new HashMap<>();
  } catch (IOException | RuntimeException e) {
   e.printStackTrace();
  }
  return new HashMap<>();
 }

 // 重複チェック
 public boolean checkDuplication(JsonData newData) {
  Map<String, String> tokenData = load();
  return tokenData != null && tokenData.containsKey(newData.getPrimaryKey());
 }

 // 保存
 private void store(Map<String, String> tokenData) {
  String json = gson.toJson(tokenData);
  try (
    FileWriter writer = new FileWriter(CONFIG_FILE)) {
    writer.write(json);
  } catch (IOException e) {
    e.printStackTrace();
  }
 }

 // データを追加
 public void saveToken(String tokenType, String token) {
  Map<String, String> tokenData = load();

  if (tokenData == null) {
   tokenData = new HashMap<>();
  }
  tokenData.put(tokenType, token);
  store(tokenData);
 }

}
