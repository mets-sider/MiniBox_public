package application;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.ArrayList;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DestinationWindowController {

 @FXML
 Label label;
 String filePath;
 @FXML
 private ListView<String> boxFileListView;
 ScanContents scanContents;
 @FXML
 private VBox dynamicContainer;

 // BoxAPIConnectionをBoxApiFactoryから取得
 private ArrayList<String> destFolderInfo;

 Stack<Map<String, String>> backStack = new Stack<>();
 Stack<Map<String, String>> forwardStack = new Stack<>();
 @FXML
 private TextField selectedDirectory;

 @FXML
 private Button selectButton;
 private String selectedFolderId;

 // リストに付与する処理
 public void initialize() {
  scanContents = new ScanContents(boxFileListView);
  PauseTransition clickTimer = new PauseTransition(Duration.millis(300));
  boxFileListView.setOnMouseClicked(event -> {
   String selectedFolderName = boxFileListView.getSelectionModel().getSelectedItem();
   selectedFolderId = scanContents.getFolderNameToId().get(selectedFolderName);

   if (clickTimer.getStatus() == Animation.Status.RUNNING) {
    clickTimer.stop();
    selectedFolderId = scanContents.getFolderNameToId().get(selectedFolderName);
    String clickCount = "DOUBLE_CLICK";
    if (selectedFolderName != null) {
     scanContents.displayContentsOfFolder(selectedFolderId, clickCount);
     backStack.push(new HashMap<>(scanContents.getFolderNameToId()));
    }
   } else {
    clickTimer.setOnFinished(e -> {
     String clickCount = "SINGLE_CLICK";
     selectedFolderId = scanContents.getCurrentNameToId().get(selectedFolderName);
     scanContents.displayContentsOfFolder(selectedFolderId, clickCount);
    });
    clickTimer.play();
   }
   selectedDirectory.setText(selectedFolderName);
   destFolderInfo = new ArrayList<String>();
   destFolderInfo.add(selectedFolderName);
   destFolderInfo.add(selectedFolderId);
  });
  scanContents.displayContentsOfFolder("0", "default");
  backStack.push(new HashMap<>(scanContents.getFolderNameToId()));
 }

 // 選択ボタンのアクション
 @FXML
 public void selectButtonAction() {
  Stage currentStage = (Stage) selectButton.getScene().getWindow();
  currentStage.close();
 }

 // フォルダ名取得
 public ArrayList<String> getSelectedFolderName() {
  return destFolderInfo;
 }

 public ArrayList<FileData> getSelectedHash() {
  ArrayList<FileData> returnDataList = scanContents.getReturnDataList();
  return returnDataList;
 }

 // ホームを表示
 @FXML
 private void displayHome() {
  scanContents.displayContentsOfFolder("0", "HOME");
 }

 // 更新
 @FXML
 public void refresh() {
  boxFileListView.getItems().clear();
  scanContents.displayContentsOfFolder(selectedFolderId, "REFRESH");
 }

 // 戻る
 @FXML
 public void goBack() {
  if (!backStack.isEmpty()) {
   forwardStack.push(new HashMap<>(scanContents.getFolderNameToId()));
   Map<String, String> newFolderNameToId = backStack.pop();
   scanContents.setFolderNameToId(newFolderNameToId);
  }
 }

 // 進む
 @FXML
 public void goForward() {
  if (!forwardStack.isEmpty()) {
   backStack.push(new HashMap<>(scanContents.getFolderNameToId()));
   Map<String, String> newFolderNameToId = forwardStack.pop();
   scanContents.setFolderNameToId(newFolderNameToId);
  }
 }

}
