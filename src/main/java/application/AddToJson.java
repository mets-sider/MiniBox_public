package application;

public class AddToJson {

 public JsonData addToJson(String localPathText, String destPathText) {
  if (!localPathText.isEmpty() && !destPathText.isEmpty()) {
   String localPath = localPathText;
   String destId = destPathText.replaceFirst("ID : ", "").split("フォルダ名 : ")[0];
   String destFolderName = destPathText.split("フォルダ名 : ")[1];
   String uploadType = "未設定";
   String uploadStatus = "待機中";

   JsonData jsonData = new JsonData();
   jsonData.setLocalPath(localPath);
   jsonData.setDestId(destId);
   jsonData.setDestFolderName(destFolderName);
   jsonData.setUploadType(uploadType);
   jsonData.setUploadStatus(uploadStatus);
   return jsonData;
  }
  return null;
 }
}