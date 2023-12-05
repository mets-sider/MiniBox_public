package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.box.sdk.BoxAPIConnection;
//import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

import javafx.scene.control.ListView;

// ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
// ■ 2023/09/13要リファクタリング！！                             
// ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■

public class ScanContents {
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // ■ フィールド
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // JWT認証
 // BoxDeveloperEditionAPIConnection api = BoxApiFactory.createApiConnection();
 BoxAPIConnectionManager manager = BoxAPIConnectionManager.getInstance();
 BoxAPIConnection api = manager.handleAccess();

 private ListView<String> boxFileListView;
 private Map<String, String> folderNameToId = new HashMap<>();
 private ArrayList<FileData> fileDataList = new ArrayList<>();
 private Map<String, String> currentNameToId = new HashMap<>();
 private ArrayList<FileData> currentDataList = new ArrayList<>();

 private Map<String, String> firstNameToId = new HashMap<>();
 private ArrayList<FileData> firstDataList = new ArrayList<>();

 private Map<String, String> returnNameToId = new HashMap<>();
 private ArrayList<FileData> returnDataList = new ArrayList<>();

 private Map<String, String> folderMap;
 private ArrayList<FileData> fileList;

 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // ■ ゲッター
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 public Map<String, String> getFolderNameToId() {
  return folderNameToId;
 }

 public ArrayList<FileData> getFileDataList() {
  return fileDataList;
 }

 public Map<String, String> getCurrentNameToId() {
  return currentNameToId;
 }

 public ArrayList<FileData> getCurrentDataList() {
  return currentDataList;
 }

 public Map<String, String> getFirstNameToId() {
  return firstNameToId;
 }

 public ArrayList<FileData> getFirstDataList() {
  return firstDataList;
 }

 public ArrayList<FileData> getReturnDataList() {
  return returnDataList;
 }

 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // ■ セッター
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 public void setFolderNameToId(Map<String, String> folderNameToId) {
  this.folderNameToId = folderNameToId;
 }

 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
 // ■ メソッド
 // ■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■

 // コンストラクタ
 public ScanContents(ListView<String> boxFileListView) {
  this.boxFileListView = boxFileListView;
 }

 public void displayContentsOfFolder(String currentFolderId, String clickCount) {

  scanContentsOfFolder(currentFolderId, clickCount);

  switch (clickCount) {
  case "DOUBLE_CLICK":
  case "REFRESH":
   folderMap = getFolderNameToId();
   fileList = getFileDataList();
   boxFileListView.getItems().clear();

   for (Map.Entry<String, String> entry : folderMap.entrySet()) {
    String folderName = entry.getKey();
    boxFileListView.getItems().add(folderName);
   }

   for (FileData fileData : fileList) {
    String fileName = fileData.getFileName();
    boxFileListView.getItems().add(fileName);
   }
   break;
  case "SINGLE_CLICK":
   folderMap = getCurrentNameToId();
   fileList = getCurrentDataList();
   break;

  case "HOME":
   folderMap = getFirstNameToId();
   fileList = getFirstDataList();
   boxFileListView.getItems().clear();

   for (Map.Entry<String, String> entry : folderMap.entrySet()) {
    String folderName = entry.getKey();
    boxFileListView.getItems().add(folderName);
   }

   for (FileData fileData : fileList) {
    String fileName = fileData.getFileName();
    boxFileListView.getItems().add(fileName);
   }
   break;

  default:
   folderMap = getFirstNameToId();
   fileList = getFirstDataList();
   boxFileListView.getItems().clear();

   for (Map.Entry<String, String> entry : folderMap.entrySet()) {
    String folderName = entry.getKey();
    boxFileListView.getItems().add(folderName);
   }

   for (FileData fileData : fileList) {
    String fileName = fileData.getFileName();
    boxFileListView.getItems().add(fileName);
   }
   break;
  }

 }

 public void scanContentsOfFolder(String currentFolderId, String clickCount) {
  BoxFolder folder = getBoxFolder(currentFolderId);
  Map<String, String> localFolderNameToId = new HashMap<>();
  ArrayList<FileData> localFileDataList = new ArrayList<>();
  scanFolderItems(folder, localFolderNameToId, localFileDataList);
  updateStates(localFolderNameToId, localFileDataList, clickCount);
 }

 private BoxFolder getBoxFolder(String currentFolderId) {
  if ("0".equals(currentFolderId)) {
   return BoxFolder.getRootFolder(api);
  } else {
   return new BoxFolder(api, currentFolderId);
  }
 }

 private void scanFolderItems(BoxFolder folder, Map<String, String> localFolderNameToId,
   ArrayList<FileData> localFileDataList) {

  for (BoxItem.Info itemInfo : folder) {
   if (itemInfo instanceof BoxFolder.Info) {
    scanBoxFolderInfo((BoxFolder.Info) itemInfo, localFolderNameToId);
   } else if (itemInfo instanceof BoxFile.Info) {
    scanBoxFileInfo((BoxFile.Info) itemInfo, localFileDataList);
   }
  }
 }

 private void updateStates(Map<String, String> localFolderNameToId, ArrayList<FileData> localFileDataList,
   String clickCount) {
  switch (clickCount) {
  case "DOUBLE_CLICK":
  case "HOME":
   currentNameToId.clear();
   currentDataList.clear();
   if (currentNameToId.isEmpty()) {
    currentNameToId.putAll(localFolderNameToId);
    currentDataList.addAll(localFileDataList);
   }
   folderNameToId.clear();
   folderNameToId.putAll(localFolderNameToId);
   fileDataList.clear();
   fileDataList.addAll(localFileDataList);
   returnNameToId.clear();
   returnNameToId.putAll(localFolderNameToId);
   returnDataList.clear();
   returnDataList.addAll(localFileDataList);
   break;

  case "SINGLE_CLICK":
   if (currentNameToId.isEmpty()) {
    currentNameToId.putAll(localFolderNameToId);
    currentDataList.addAll(localFileDataList);
   }
   returnNameToId.clear();
   returnNameToId.putAll(localFolderNameToId);
   returnDataList.clear();
   returnDataList.addAll(localFileDataList);
   break;

  case "REFRESH":
   break;

  default:
   if (firstNameToId.isEmpty()) {
    firstNameToId.clear();
    firstNameToId.putAll(localFolderNameToId);
    firstDataList.clear();
    firstDataList.addAll(localFileDataList);
   }
   if (currentNameToId.isEmpty()) {
    currentNameToId.putAll(localFolderNameToId);
    currentDataList.addAll(localFileDataList);
   }
   folderNameToId.clear();
   folderNameToId.putAll(localFolderNameToId);
   fileDataList.clear();
   fileDataList.addAll(localFileDataList);
   returnNameToId.clear();
   returnNameToId.putAll(localFolderNameToId);
   returnDataList.clear();
   returnDataList.addAll(localFileDataList);
   break;
  }
 }

 private void scanBoxFolderInfo(BoxFolder.Info folderInfo, Map<String, String> localFolderNameToId) {
  String folderName = folderInfo.getName();
  String folderId = folderInfo.getID();
  localFolderNameToId.put(folderName, folderId);
  System.out.println(folderInfo.getName() + " : " + folderId);
 }

 private void scanBoxFileInfo(BoxFile.Info fileInfo, ArrayList<FileData> localFileDataList) {
  String fileSha1 = fileInfo.getSha1();
  String fileName = fileInfo.getName();
  String fileId = fileInfo.getID();
  localFileDataList.add(new FileData(fileId, fileName, fileSha1));
 }

}
