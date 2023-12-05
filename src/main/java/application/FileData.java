package application;

public class FileData {
 private String fileId;
 private String fileName;
 private String filePath;
 private String fileSha1;
 private long fileSize;

 // -- コンストラクタ --
 public FileData() {
  this.fileId = "";
  this.fileName = "";
  this.filePath = "";
  this.fileSha1 = "";
  this.fileSize = 0L;
 }

 // @Override
 public FileData(String fileId, String fileName) {
  this.fileId = fileId;
  this.fileName = fileName;
 }

 // @Override
 public FileData(String fileId, String fileName, String fileSha1) {
  this.fileId = fileId;
  this.fileName = fileName;
  this.fileSha1 = fileSha1;
 }

 // @Override
 public FileData(String fileId, String fileName, String filePath, String fileSha1) {
  this.fileId = fileId;
  this.fileName = fileName;
  this.filePath = filePath;
  this.fileSha1 = fileSha1;
 }

 // @Override
 public FileData(String fileId, String fileName, String filePath, String fileSha1, long fileSize) {
  this.fileId = fileId;
  this.fileName = fileName;
  this.filePath = filePath;
  this.fileSha1 = fileSha1;
  this.fileSize = fileSize;
 }

 public String getFileId() {
  return fileId;
 }

 public void setFileId(String fileId) {
  this.fileId = fileId;
 }

 public String getFileName() {
  return fileName;
 }

 public void setFileName(String fileName) {
  this.fileName = fileName;
 }

 public String getFilePath() {
  return filePath;
 }

 public void setFilePath(String filePath) {
  this.filePath = filePath;
 }

 public String getFileSha1() {
  return fileSha1;
 }

 public void setFileSha1(String fileSha1) {
  this.fileSha1 = fileSha1;
 }

 public long getFileSize() {
  return fileSize;
 }

 public void setFileSize(long fileSize) {
  this.fileSize = fileSize;
 }

}
