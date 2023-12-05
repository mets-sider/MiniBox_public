package application;

import java.util.List;

import application.BoxUpload.compareResult;

public class CompareResultWithId {
 public compareResult result;
 public String matchedFileId;
 public List<FileData> comparedName;

 public CompareResultWithId(compareResult result) {
  this.result = result;
 }

 public CompareResultWithId(compareResult result, String matchedFileId) {
  this.result = result;
  this.matchedFileId = matchedFileId;
 }

 public CompareResultWithId(compareResult result, List<FileData> comparedName) {
  this.result = result;
  this.comparedName = comparedName;
 }

 public compareResult getResult() {
  return result;
 }

 public void setResult(compareResult result) {
  this.result = result;
 }

 public String getMatchedFileId() {
  return matchedFileId;
 }

 public void setMatchedFileId(String matchedFileId) {
  this.matchedFileId = matchedFileId;
 }

}
