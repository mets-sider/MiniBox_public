package application;

public class UploadData {
 private String localFilePath;
 private String destFolderName;
 private String destFolderId;

 public UploadData(String localFilePath, String destFolderId) {
  this.localFilePath = localFilePath;
  this.destFolderId = destFolderId;
 }

 // @Override
 public UploadData(String localFilePath, String destFolderName, String destFolderId) {
  this.destFolderName = destFolderName;
  this.destFolderId = destFolderId;
  this.localFilePath = localFilePath;
 }

 public String getDestFolderName() {
  return destFolderName;
 }

 public String getDestFolderId() {
  return destFolderId;
 }

 public String getLocalFilePath() {
  return localFilePath;
 }

 public void setDestFolderName(String destFolderName) {
  this.destFolderName = destFolderName;
 }

 public void setDestFolderId(String destFolderId) {
  this.destFolderId = destFolderId;
 }

 public void setLocalFilePath(String localFilePath) {
  this.localFilePath = localFilePath;
 }
}
