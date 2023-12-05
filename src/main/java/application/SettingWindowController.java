package application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class SettingWindowController {
 private MainWindowController mainWindowController;
 private String localPath;
 private String primaryKey;

 @FXML
 private DatePicker dateOneTime;

 @FXML
 private ComboBox<Integer> hourBoxOneTime;

 @FXML
 private ComboBox<Integer> minuteBoxOneTime;

 @FXML
 private ComboBox<Integer> hourBoxWeek;

 @FXML
 private ComboBox<Integer> minuteBoxWeek;

 @FXML
 private Button saveButton;

 @FXML
 private Button closeButton;

 @FXML
 private ToggleGroup selectWhen;

 @FXML
 private RadioButton oneTimeButton;

 @FXML
 private RadioButton dailyButton;

 @FXML
 private CheckBox mondayCheckBox;

 @FXML
 private CheckBox tuesdayCheckBox;

 @FXML
 private CheckBox wednesdayCheckBox;

 @FXML
 private CheckBox thursdayCheckBox;

 @FXML
 private CheckBox fridayCheckBox;

 @FXML
 private CheckBox saturdayCheckBox;

 @FXML
 private CheckBox sundayCheckBox;

 @FXML
 private List<CheckBox> checkBoxes;

 private String type;

 // MainScreenの情報を参照
 public void setMainWindowController(MainWindowController controller) {
  this.mainWindowController = controller;
 }

// 最初に実行されるメソッド
 @FXML
 private void initialize() {
  for (int i = 0; i < 24; i++)
   hourBoxOneTime.getItems().add(i);
  hourBoxOneTime.setValue(0);

  for (int i = 0; i < 60; i += 5)
   minuteBoxOneTime.getItems().add(i);
  minuteBoxOneTime.setValue(0);

  for (int i = 0; i < 24; i++)
   hourBoxWeek.getItems().add(i);
  hourBoxWeek.setValue(0);

  for (int i = 0; i < 60; i += 5)
   minuteBoxWeek.getItems().add(i);
  minuteBoxWeek.setValue(0);

  checkBoxes = Arrays.asList(mondayCheckBox, tuesdayCheckBox, wednesdayCheckBox, thursdayCheckBox, fridayCheckBox,
    saturdayCheckBox, sundayCheckBox);
 }

 private void postInitialize() {
  JsonManager jsonManager = new JsonManager();
  JsonTimeAndDays jTAD = jsonManager.getData(primaryKey);

  // 一回の場合,
  if (jTAD.getDate() != null) {
   oneTimeButton.setSelected(true);
   dateOneTime.setValue(jTAD.getDate());
   hourBoxOneTime.setValue(jTAD.getTime().getHour());
   minuteBoxOneTime.setValue(jTAD.getTime().getMinute());
   Platform.runLater(() -> {
    oneTimeButton.requestFocus();
   });

   // 定期の場合
  } else if (jTAD.getDays() != null) {
   dailyButton.setSelected(true);
   mondayCheckBox.setSelected(jTAD.getDays().getOrDefault("月", false));
   tuesdayCheckBox.setSelected(jTAD.getDays().getOrDefault("火", false));
   wednesdayCheckBox.setSelected(jTAD.getDays().getOrDefault("水", false));
   thursdayCheckBox.setSelected(jTAD.getDays().getOrDefault("木", false));
   fridayCheckBox.setSelected(jTAD.getDays().getOrDefault("金", false));
   saturdayCheckBox.setSelected(jTAD.getDays().getOrDefault("土", false));
   sundayCheckBox.setSelected(jTAD.getDays().getOrDefault("日", false));
   hourBoxWeek.setValue(jTAD.getTime().getHour());
   minuteBoxWeek.setValue(jTAD.getTime().getMinute());
   Platform.runLater(() -> {
    dailyButton.requestFocus();
   });
  }
 }

 public void setData(String primaryKey) {
  this.primaryKey = primaryKey;
  postInitialize();
 }

 public String getLocalPath() {
  return localPath;
 }

 // 保存
 @FXML
 private void save() {
  String selectedType = selectType();
  JsonManager jsonManager = new JsonManager();

  switch (selectedType) {
  case "oneTime":
   type = "一回";
   Optional<LocalDate> date = Optional.ofNullable(dateOneTime.getValue());
   Optional<Integer> hour = Optional.ofNullable(hourBoxOneTime.getValue());
   Optional<Integer> minute = Optional.ofNullable(minuteBoxOneTime.getValue());
   if (date.isPresent() && hour.isPresent() && minute.isPresent()) {
    LocalDateTime dateTime = LocalDateTime.of(date.get(), LocalTime.of(hour.get(), minute.get()));
    String dateTimeString = dateTime.toString();
    jsonManager.addTimer(primaryKey, type, dateTimeString);
   } else {
    showErrorMessage("日付を入力してください");
   }
   break;
  case "dayOfWeek":
   type = "定期";
   List<String> selectedDays = getSelectedDays();
   Optional<Integer> hourWeek = Optional.ofNullable(hourBoxWeek.getValue());
   Optional<Integer> minuteWeek = Optional.ofNullable(minuteBoxWeek.getValue());
   String formattedTime = String.format("%02d:%02d", hourWeek.orElse(0), minuteWeek.orElse(0));
   Map<String, Boolean> daysMap = new HashMap<>();
   List<String> allDays = Arrays.asList("月", "火", "水", "木", "金", "土", "日");
   for (String day : allDays) {
    daysMap.put(day, selectedDays.contains(day));
   }
   if (!selectedDays.isEmpty()) {
    jsonManager.addTimer(primaryKey, type, daysMap, formattedTime);
   } else {
    showErrorMessage("曜日を選択してください");
   }
   break;
  default:
   showErrorMessage("実行方法を選択してください");
   break;
  }
 }

 // 閉じる
 @FXML
 private void close() {
  Stage currentStage = (Stage) closeButton.getScene().getWindow();
  currentStage.close();
  mainWindowController.reload();
 }

 // 一回か曜日で自動実行かを取得
 private String selectType() {
  Toggle selectedToggle = selectWhen.getSelectedToggle();
  if (selectedToggle == oneTimeButton) {
   return "oneTime";
  } else if (selectedToggle == dailyButton) {
   return "dayOfWeek";
  } else {
   return "unknown";
  }
 }

 // チェックボックスの状態を確認
 private List<String> getSelectedDays() {
  List<String> selectedDays = new ArrayList<>();
  for (CheckBox checkBox : checkBoxes) {
   if (checkBox.isSelected()) {
    selectedDays.add(checkBox.getText());
   }
  }
  return selectedDays;
 }

 // エラーウィンドウを表示
 private void showErrorMessage(String message) {
  Alert alert = new Alert(Alert.AlertType.ERROR);
  alert.setTitle("Error");
  alert.setHeaderText(null);
  alert.setContentText(message);
  alert.showAndWait();
 }

}
