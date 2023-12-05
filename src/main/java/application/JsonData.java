package application;

import java.time.LocalDateTime;

public class JsonData {
 private String localPath;
 private String destId;
 private String destFolderName;
 private String uploadType;
 private LocalDateTime uploadTime;
 private String uploadStatus;

 public String getPrimaryKey() {
  return localPath + "_" + destId;
 }

 public String getLocalPath() {
  return localPath;
 }

 public void setLocalPath(String localPath) {
  this.localPath = localPath;
 }

 public String getDestId() {
  return destId;
 }

 public void setDestId(String destId) {
  this.destId = destId;
 }

 public String getDestFolderName() {
  return destFolderName;
 }

 public void setDestFolderName(String destFolderName) {
  this.destFolderName = destFolderName;
 }

 public String getUploadType() {
  return uploadType;
 }

 public void setUploadType(String uploadType) {
  this.uploadType = uploadType;
 }

 public LocalDateTime getUploadTime() {
  return uploadTime;
 }

 public void setUploadTime(LocalDateTime uploadTime) {
  this.uploadTime = uploadTime;
 }

 public String getUploadStatus() {
  return uploadStatus;
 }

 public void setUploadStatus(String uploadStatus) {
  this.uploadStatus = uploadStatus;
 }

}
