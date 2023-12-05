package application;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JsonManager {
 Gson gson = new GsonBuilder().setLenient().create();
 private static final String UPLOADLIST_FILE = "uploadList.json";

 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // ■ executionList ■
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // Jsonファイル読み込み
 public Map<String, Map<String, String>> load() {
  try (
   Reader reader = new FileReader(UPLOADLIST_FILE)){
   Type type = new TypeToken<Map<String, Map<String, String>>>() {
   }.getType();
   Map<String, Map<String, String>> loadedMap = gson.fromJson(reader, type);
   return loadedMap != null ? loadedMap : new HashMap<>();
  } catch (IOException |RuntimeException e) {
   e.printStackTrace();
  }
  return new HashMap<>();
 }

 // MapからJsonに変換
 public JsonData convertMapToJsonData(String localPath, Map<String, String> map) {
  JsonData data = new JsonData();
  data.setLocalPath(map.get("ローカルパス"));
  data.setDestId(map.get("宛先ID"));
  data.setDestFolderName(map.get("宛先フォルダ"));
  data.setUploadStatus(map.get("実行ステータス"));
  return data;
 }

 // 重複チェック
 public boolean checkDuplication(JsonData newData) {
  Map<String, Map<String, String>> executionList = load();
  return executionList != null && executionList.containsKey(newData.getPrimaryKey());
 }

 // データを追加
 public void addData(JsonData newData) {
  System.out.println("nnna");
  Map<String, Map<String, String>> executionList = load();

  if (executionList == null) {
   executionList = new HashMap<>();
  }

  // 重複チェックをしてから追加する
  if (!checkDuplication(newData)) {
   Map<String, String> innerMap = new HashMap<>();
   innerMap.put("ローカルパス", newData.getLocalPath());
   innerMap.put("宛先ID", newData.getDestId());
   innerMap.put("宛先フォルダ", newData.getDestFolderName());
   innerMap.put("実行タイプ", newData.getUploadType());
   innerMap.put("実行ステータス", newData.getUploadStatus());
   executionList.put(newData.getPrimaryKey(), innerMap);
   System.out.println(newData.getPrimaryKey());
   store(executionList);
  }
 }

 // 保存
 private void store(Map<String, Map<String, String>> updateList) {
  String json = gson.toJson(updateList);
  try (FileWriter writer = new FileWriter(UPLOADLIST_FILE)) {
   writer.write(json);
  } catch (IOException e) {
   e.printStackTrace();
  }
 }

 // 削除
 public void delete(String primaryKey) {
  Map<String, Map<String, String>> executionList = load();
  if (executionList != null) {
   executionList.containsKey(primaryKey);
   executionList.remove(primaryKey);
   store(executionList);
  }
 }

 // Jsonデータを取得する
 public JsonTimeAndDays getData(String primaryKey) {
  System.out.println("--START getData --");
  Map<String, Map<String, String>> executionList = load();
  if (executionList != null && executionList.containsKey(primaryKey)) {
   Map<String, String> innerMap = executionList.get(primaryKey);
   JsonTimeAndDays jTAD = new JsonTimeAndDays();
   String type = innerMap.get("実行タイプ");

   // 一回の場合
   if ("一回".equals(type)) {
    String dateString = innerMap.get("日時情報");
    LocalDateTime localDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    LocalDate localDate = localDateTime.toLocalDate();
    jTAD.setDate(localDate);
    int hour = localDateTime.getHour();
    int minute = localDateTime.getMinute();
    System.out.println(Integer.toString(hour) + Integer.toString(minute));
    jTAD.setTime(LocalTime.of(hour, minute));

    // 定期の場合
   } else if ("定期".equals(type)) {
    String time = innerMap.get("時間");
    jTAD.setTime(LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")));
    Type tokenType = new TypeToken<Map<String, Boolean>>() {
    }.getType();
    Map<String, Boolean> days = gson.fromJson(innerMap.get("曜日"), tokenType);
    jTAD.setDays(days);
   }
   return jTAD;
  }
  return null;
 }

 // 実行タイプ1
 // 日時データ追加
 public void addTimer(String primaryKey, String type, String dateTime) {
  System.out.println("--START addTimer --");
  Map<String, Map<String, String>> executionList = load();

  if (executionList != null && executionList.containsKey(primaryKey)) {
   Map<String, String> innerMap = executionList.get(primaryKey);

   // 別タイプの情報（曜日と時間）を削除
   innerMap.remove("曜日");
   innerMap.remove("時間");

   innerMap.put("実行タイプ", type);
   innerMap.put("日時情報", dateTime);
   executionList.put(primaryKey, innerMap);
  }
  store(executionList);
 }

 // 実行タイプ2
 // 曜日と時間データ追加
 public void addTimer(String primaryKey, String type, Map<String, Boolean> dayOfWeek, String formattedTime) {
  Map<String, Map<String, String>> executionList = load();
  String dayOfWeekJson = gson.toJson(dayOfWeek);

  if (executionList != null && executionList.containsKey(primaryKey)) {
   Map<String, String> innerMap = executionList.get(primaryKey);

   // 別タイプの情報（日時情報）を削除
   innerMap.remove("日時情報");

   innerMap.put("実行タイプ", type);
   innerMap.put("曜日", dayOfWeekJson);
   innerMap.put("時間", formattedTime);
   executionList.put(primaryKey, innerMap);
  }
  store(executionList);
 }

 // 実行ステータス更新
 public void updateStatus(String primaryKey, String status) {
  Map<String, Map<String, String>> executionList = load();

  if (executionList != null && executionList.containsKey(primaryKey)) {
   Map<String, String> innerMap = executionList.get(primaryKey);

   innerMap.put("実行ステータス", status);
   executionList.put(primaryKey, innerMap);
  }
  store(executionList);
 }

}