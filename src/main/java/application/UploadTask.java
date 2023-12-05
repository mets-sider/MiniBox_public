package application;

import javafx.concurrent.Task;

// バックグラウンド処理とUIを別けるためのTaskクラス
public class UploadTask extends Task<Void> {
 private String primaryKey;
 private UploadCallback callback;

 // コールバックインターフェース
 public interface UploadCallback {
  void onUploadStarted();

  void onUploadCompleted();

  void onUploadFailed(Exception e);
 }

 public UploadTask(String primaryKey, UploadCallback callback) {
  this.primaryKey = primaryKey;
  this.callback = callback;
 }

 @Override
 protected Void call() throws Exception {
  try {
   if (callback != null)
    callback.onUploadStarted();

   BoxUpload uploader = new BoxUpload();
   uploader.startUploadProcess(primaryKey);

   if (callback != null)
    callback.onUploadCompleted();
  } catch (Exception e) {
   if (callback != null)
    callback.onUploadFailed(e);
   throw e;
  }
  return null;
 }

}