package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainWindowController {
 private FileData selectedData;
 private String iconPath = "icon.jpg";

 @FXML
 String filePath;

 @FXML
 private ListView<String> boxFileListView;

 @FXML
 private VBox dynamicContainer;

 @FXML
 TextField localPath;
 @FXML
 TextField destPath;
 @FXML
 Button destButton;

 UploadData uploadData = new UploadData(null, this.filePath);

 public MainWindowController() {
 }

 @FXML
 // 最初に実行されるメソッド
 private void initialize() {
  JsonManager jsonList = new JsonManager();
  Map<String, Map<String, String>> executionJson = jsonList.load();

  if (executionJson != null) {
   executionJson.entrySet().forEach(this::addRow);
  }
  getJsonList();
 }

 @FXML
 // Swing
 // ファイルとフォルダどちらも選択可能
 private void chooser() {
  SwingUtilities.invokeLater(() -> {
   try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
   } catch (Exception e) {
    e.printStackTrace();
   }
   JFileChooser chooser = new JFileChooser();
   chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
   JFrame frame = new JFrame();
   frame.setAlwaysOnTop(true);
   int result = chooser.showOpenDialog(frame);
   frame.dispose();

   if (result == JFileChooser.APPROVE_OPTION) {
    File selectedFileOrDir = chooser.getSelectedFile();
    // SwingとJavaFXを同時使用しているのでスレッド操作推奨
    Platform.runLater(() -> {
     processSelectedData(selectedFileOrDir);
    });
   }
  });
 }

 private void processSelectedData(File file) {
  selectedData = new FileData();
  setFileData(file);
  updateUI();
 }

 private void setFileData(File file) {
  selectedData.setFileName(file.getName());
  selectedData.setFilePath(file.getPath());
  selectedData.setFileSize(file.length());
  BoxUpload uploader = new BoxUpload();
  if (file.isFile()) {
   String localHash = uploader.createSha1(file.getPath());
   selectedData.setFileSha1(localHash);
  }
 }

 private void updateUI() {
  if (selectedData != null) {
   localPath.setText(selectedData.getFilePath());
  }
 }

 // 宛先選択ウィンドウを開く
 public DestinationWindowController openSelectDestinationWindow() {
  try {
   FXMLLoader loader = new FXMLLoader(getClass().getResource("DestinationWindow.fxml"));
   Parent root = loader.load();
   Scene scene = new Scene(root);
   Stage stage = new Stage();
   stage.setScene(scene);
   stage.setTitle("宛先選択");
   DestinationWindowController controller = loader.getController();
   stage.initModality(Modality.APPLICATION_MODAL);
   stage.showAndWait();
   return controller;
  } catch (IOException e) {
   e.printStackTrace();
   return null;
  }
 }

 @FXML
 // 宛先ボタン
 private void destButton() {
  TextField destBoxName = new TextField();
  DestinationWindowController subController = openSelectDestinationWindow();
  // 正常に開かなかったら終了
  if (subController == null) {
   showAlert();
   backToLoginWindow();
   return;
  }
  ArrayList<String> folderInfo = subController.getSelectedFolderName();
  // フォルダ名（情報）を取得している場合は要素をセット
  if (folderInfo != null) {
   String folderName = folderInfo.get(0);
   String folderId = folderInfo.get(1);
   destBoxName.setText(folderName);
   uploadData.setDestFolderName(folderName);
   uploadData.setDestFolderId(folderId);
   destPath.setText("ID : " + folderId + " フォルダ名 : " + folderName);
  }
 }

 @FXML
 // 追加ボタン
 private void addListButton() {
  AddToJson addToJson = new AddToJson();
  JsonData jsonData = addToJson.addToJson(localPath.getText(), destPath.getText());
  if (jsonData != null) {
   JsonManager jsonManager = new JsonManager();
   jsonManager.addData(jsonData);
   reload();
   clearText();
  }
 }

 // 要素を追加
 private void addRow(Map.Entry<String, Map<String, String>> entry) {

  // 行作成
  HBox newRow = new HBox(10);
  Insets insets = new Insets(3, 3, 3, 3);
  newRow.setPadding(insets);
  newRow.setAlignment(Pos.CENTER);

  // テキストボックス作成
  final TextField localPathText = new TextField();
  final TextField destIdText = new TextField();
  final TextField destNameText = new TextField();
  final Label uploadStatusLabel = new Label();
  JsonManager jsonManager = new JsonManager();
  JsonData jsonData = jsonManager.convertMapToJsonData(entry.getKey(), entry.getValue());

  localPathText.setPrefWidth(350);
  localPathText.setText(jsonData.getLocalPath());
  localPathText.setEditable(false);

  destIdText.setPrefWidth(95);
  destIdText.setText(jsonData.getDestId());
  destIdText.setEditable(false);
  destNameText.setText(jsonData.getDestFolderName());
  destNameText.setEditable(false);

  uploadStatusLabel.setText(jsonData.getUploadStatus());

  String primaryKey = (localPathText.getText() + "_" + destIdText.getText());

  // 実行ボタンを生成しアクションを設定
  Button executeUploadButton = new Button("実行");
  executeUploadButton.setOnAction(event -> {
   executeUpload(primaryKey);
  });

  // タイマー設定
  Button executeTimerButton = new Button("設定");
  executeTimerButton.setOnAction(event -> {
   openSettingWindow(primaryKey);
  });

  // 削除ボタンを生成しアクションを設定
  Button deleteButton = new Button("削除");
  deleteButton.setOnAction(event -> {
   deleteButton(primaryKey);
  });

  // 子要素をまとめる
  newRow.getChildren().addAll(localPathText, destIdText, destNameText, executeUploadButton, executeTimerButton,
    deleteButton, uploadStatusLabel);
  // 行追加実行
  dynamicContainer.getChildren().add(newRow);
 }

 // 削除メソッド
 private void deleteButton(String primaryKey) {
  JsonManager jsonManager = new JsonManager();
  jsonManager.delete(primaryKey);
  reload();
 }

 // テキストをリセット
 private void clearText() {
  localPath.setText("");
  destPath.setText("");
 }

 // 実行メソッド
 private void executeUpload(String primaryKey) {
  UploadTask.UploadCallback callback = new UploadTask.UploadCallback() {
   JsonManager jsonManager = new JsonManager();

   // コールバック
   @Override
   public void onUploadStarted() {
    // アップロード開始
    // スレッドセーフ対応
    Platform.runLater(() -> {
     jsonManager.updateStatus(primaryKey, "実行中");
     reload();
    });
   }

   @Override
   public void onUploadCompleted() {
    // 待機中
    // スレッドセーフ対応
    Platform.runLater(() -> {
     jsonManager.updateStatus(primaryKey, "待機中");
     reload();
    });
   }

   @Override
   public void onUploadFailed(Exception e) {
    // 失敗
    // スレッドセーフ対応
    Platform.runLater(() -> {
     jsonManager.updateStatus(primaryKey, "　失敗");
     reload();
    });
   }
  };

  UploadTask uploadTask = new UploadTask(primaryKey, callback);
  new Thread(uploadTask).start();
 }

 // タイマー設定画面
 private SettingWindowController openSettingWindow(String primaryKey) {
  try {
   FXMLLoader loader = new FXMLLoader(getClass().getResource("SettingWindow.fxml"));
   Parent root = loader.load();
   Scene scene = new Scene(root);
   Stage stage = new Stage();
   stage.setScene(scene);
   stage.setTitle("設定");
   Image image = new Image(MainWindowController.class.getClassLoader().getResourceAsStream(iconPath));
   stage.getIcons().add(image);
   SettingWindowController controller = loader.getController();
   controller.setMainWindowController(this);
   controller.setData(primaryKey);
   stage.initModality(Modality.APPLICATION_MODAL);
   stage.showAndWait();
   return controller;
  } catch (IOException e) {
   e.printStackTrace();
   return null;
  }
 }

 private void getJsonList() {
  JsonManager jsonManager = new JsonManager();
  TimeSchedule timeSchedule = new TimeSchedule();
  Map<String, Map<String, String>> executionList = jsonManager.load();
  if (executionList != null) {
   for (Map.Entry<String, Map<String, String>> entry : executionList.entrySet()) {
    String primaryKey = entry.getKey();
    Map<String, String> innerMap = entry.getValue();
    String type = innerMap.get("実行タイプ");

    // 一回実行
    if ("一回".equals(type)) {
     String dateString = innerMap.get("日時情報");
     Long result = timeSchedule.scheduledOneTime(primaryKey, dateString);
     if (result != null) {
      long millisResult = result;
      scheduleExecution(primaryKey, millisResult);
     }

     // 定期実行
    } else if ("定期".equals(type)) {
     Map<String, Boolean> dayOfWeek = new HashMap<>();
     dayOfWeek = new Gson().fromJson(innerMap.get("曜日"), new TypeToken<HashMap<String, Boolean>>() {
     }.getType());
     String time = innerMap.get("時間");
     for (Map.Entry<String, Boolean> dayEntry : dayOfWeek.entrySet()) {
      if (dayEntry.getValue()) {
       Long result = timeSchedule.scheduleWeekly(primaryKey, dayEntry.getKey(), time);
       if (result != null) {
        long millisResult = result;
        scheduleExecution(primaryKey, millisResult);
       }
      }
     }
    }
   }
  }
 }

 // タイマー実行
 private void scheduleExecution(String primaryKey, long millisResult) {
  Runnable task = () -> {
   executeUpload(primaryKey);
  };

  // JavaFXのDurationを使用してタイマーを設定
  javafx.util.Duration javafxDuration = javafx.util.Duration.millis(millisResult);
  Timeline timeline = new Timeline(new KeyFrame(javafxDuration, e -> task.run()));
  timeline.play();
 }

 @FXML
 // 更新
 public void reload() {
  // UIの子要素を一旦クリア
  dynamicContainer.getChildren().clear();
  // UIを再構築
  initialize();
 }

 // ログイン画面に戻る
 private void backToLoginWindow() {
  try {
   // 現在のStageを取得
   Stage currentStage = (Stage) destButton.getScene().getWindow();

   // ログイン画面のシーンをロードして設定
   FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginWindow.fxml"));
   Parent loginRoot;

   loginRoot = loader.load();

   Scene loginScene = new Scene(loginRoot);
   currentStage.setScene(loginScene);
   currentStage.show();
  } catch (IOException e) {
   // TODO 自動生成された catch ブロック
   e.printStackTrace();
  }
 }

 // 警告アラート表示
 private void showAlert() {
  Alert alert = new Alert(Alert.AlertType.ERROR);
  alert.setContentText("認証に失敗しました。ログインしてください。");
  alert.setHeaderText(null);
  alert.showAndWait();
 }
}
