package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxAPIResponseException;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFileUploadSession;
import com.box.sdk.BoxFileUploadSessionPart;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

public class BoxUpload {

 BoxAPIConnectionManager manager = BoxAPIConnectionManager.getInstance();
 BoxAPIConnection api = manager.handleAccess();
 BoxFolder rootFolder = BoxFolder.getRootFolder(api);
 BoxFolder folder;
 String matchedFileId;
 int fileSize;
 BoxFileUploadSession.Info sessionInfo;
 BoxFileUploadSession session;
 MessageDigest digest;
 List<BoxFileUploadSessionPart> parts;
 List<FileData> matchedFiles = new ArrayList<>();
 Map<String, String> destFolderHash = new HashMap<>();
 Map<String, String> matchedFileInfo = new HashMap<>();

 // ファイルアップロード
 public void upload(String localFilePath, String localFileName, String destFolderId) {
  folder = new BoxFolder(api, destFolderId);
  try (FileInputStream stream = new FileInputStream(localFilePath)) {
   System.out.println("アップロードに成功しました");

   // アップロード後の処理
  } catch (IOException | BoxAPIResponseException e) {
   e.printStackTrace();
  }
 }

 // ファイルバージョンアップデート
 public void versionUpload(String localFilePath, String matchedFileId) {
  try {
   // Box APIの呼び出し等
   BoxFile file = new BoxFile(api, matchedFileId);
   try {
    FileInputStream stream = new FileInputStream(localFilePath);
    file.uploadNewVersion(stream);
   } catch (IOException | BoxAPIResponseException e) {
    e.printStackTrace();
   }
  } catch (BoxAPIException e) {
   e.printStackTrace();
  }
 }

 // セッション共通処理
 private void configureSession(long fileSize) {
  session = sessionInfo.getResource();
  try {
   digest = MessageDigest.getInstance("SHA1");
  } catch (NoSuchAlgorithmException ae) {
   throw new BoxAPIException("Digest alghorithm not found", ae);
  }
 }

 // 分割アップロード①
 // セッション作成（新規ファイル）
 public void createSessionNewFile(String localFileName, long fileSize, String folderId) {
  BoxFolder folder = new BoxFolder(api, folderId);
  try {
   sessionInfo = folder.createUploadSession(localFileName, fileSize);
  } catch (BoxAPIResponseException e) {
   e.printStackTrace();
  }
  configureSession(fileSize);
 }

 // セッション作成（既存ファイル）
 public void createSessionUpdateFile(String localFilePath, long fileSize, String fileId) {
  BoxFile file = new BoxFile(api, fileId);
  sessionInfo = file.createUploadSession(fileSize);
  configureSession(fileSize);
 }

 // 分割アップロード②
 // パーツをアップロード
 public void uploadParts(String localFilePath, long fileSize) {
  // Reading a large file
  try {
   FileInputStream fis = new FileInputStream(localFilePath);

   // Create the digest input stream to calculata the digest for the whole file.
   DigestInputStream dis = new DigestInputStream(fis, digest);

   parts = new ArrayList<BoxFileUploadSessionPart>();

   // Get the part size. Each uploaded part should match the part size returned as
   // part of the upload session.
   // The last part of the file can be less than part size if the remaining bytes
   // of the last part is less than
   // the given part size
   long partSize = sessionInfo.getPartSize();
   // Start byte of the part
   long offset = 0;
   // Overall of bytes processed so far
   long processed = 0;
   while (processed < fileSize) {
    long diff = fileSize - processed;
    // The size last part of the file can be less than the part size.
    if (diff < partSize) {
     partSize = diff;
    }
    // Upload a part. It can be uploaded asynchrously
    BoxFileUploadSessionPart part = session.uploadPart(dis, offset, (int) partSize, fileSize);
    parts.add(part);

    // Increase the offset and processed bytes to calculate the Content-Range
    // header.
    processed += partSize;
    offset += partSize;
   }
  } catch (IOException e) {
   e.printStackTrace();
  }
 }

 // 分割アップロード③
 // アップロードセッションをコミット
 public void commitSession() {
  // Create the file hash
  byte[] digestBytes = digest.digest();
  // Base64 encoding of the hash
  String digestStr = Base64.getEncoder().encodeToString(digestBytes);
  // Commit the upload session. If there is a failure, abort the commit.
  BoxFile.Info fileInfo = session.commit(digestStr, parts, null, null, null);
 }

 private void abortSession() {
  session.abort();
 }

 // ハッシュ値生成
 public String createSha1(String localFilePath) {
  MessageDigest sha1;
  try {
   sha1 = MessageDigest.getInstance("SHA-1");
  } catch (NoSuchAlgorithmException e) {
   throw new RuntimeException("SHA-1 algorithm not found", e);
  }
  try (InputStream is = Files.newInputStream(Paths.get(localFilePath))) {
   byte[] buffer = new byte[1024];
   int read;
   while ((read = is.read(buffer)) != -1) {
    sha1.update(buffer, 0, read);
   }
  } catch (IOException e) {
   throw new RuntimeException("Failed to read file for hashing", e);
  }
  byte[] hash = sha1.digest();
  return String.format("%40x", new BigInteger(1, hash));
 }

 // ファイルサイズを量る
 private long measureFileSize(String localPath) {
  File file = new File(localPath);
  long fileSize = file.length();
  return fileSize;
 }

 // ファイルサイズを制限と比較
 private DataResult compareFileSize(long fileSize) {
  long limit = 20L * 1024 * 1024;
  if (fileSize > limit) {
   return DataResult.SESSION_UPLOAD;
  } else {
   return DataResult.NEW_UPLOAD;
  }
 }

 // パターン定数
 public enum compareResult {
  NEW_UPLOAD, VERSION_UPLOAD, SESSION_UPLOAD, NO_ACTION, NO_MATCH, NAME_MATCH, FULL_MATCH,

 }

 // 空白を削除
 private String trim(String str) {
  String trimedStr = str.trim();
  return trimedStr;
 }

 // アップロード処理スタート
 public void startUploadProcess(String primaryKey) {
  UploadData uploadData = primaryKeyToLocalPathAndDestId(primaryKey);
  String localPath = uploadData.getLocalFilePath();
  String destId = uploadData.getDestFolderId();
  String trimedDestId = trim(destId);
  DataResult dataResult = isFileOrDirectory(localPath);
  String localFileName = getNameFromPath(localPath);
  if (dataResult == DataResult.DIRECTORY) {
   System.out.println("ディレクトリアップロード");
   uploadDirectory(localPath, trimedDestId);
  } else if (dataResult == DataResult.FILE) {
   System.out.println("ファイルアップロード");
   handleFileUpload(localPath, localFileName, trimedDestId);
  }
 }

 // キーからローカルパスと宛先IDを取得
 private UploadData primaryKeyToLocalPathAndDestId(String primaryKey) {
  int underscoreIndex = primaryKey.lastIndexOf("_");
  if (underscoreIndex != -1) {
   String localPath = primaryKey.substring(0, underscoreIndex);
   String destId = primaryKey.substring(underscoreIndex + 1);
   return new UploadData(localPath, destId);
  }
  return null;
 }

 // ファイルアップロードハンドラー
 private void handleFileUpload(String localPath, String localFileName, String trimedDestId) {
  String localSha1 = createSha1(localPath);
  CompareResultWithId compareResult = compareNameAndSha1(localFileName, localSha1, trimedDestId);
  compareResult result = compareResult.getResult();
  String matchedFileId = compareResult.getMatchedFileId();
  long fileSize = measureFileSize(localPath);
  DataResult dataSizeResult = compareFileSize(fileSize);

  switch (result) {
  case NO_MATCH:
   if (dataSizeResult == DataResult.SESSION_UPLOAD) {
    System.out.println("新規アップロード（セッション）");
    createSessionNewFile(localFileName, fileSize, trimedDestId);
    uploadParts(localPath, fileSize);
    commitSession();
    abortSession();
   } else {
    System.out.println("新規アップロード");
    upload(localPath, localFileName, trimedDestId);
   }
   break;
  case NAME_MATCH:
   if (dataSizeResult == DataResult.SESSION_UPLOAD) {
    System.out.println("バージョンアップロード（セッション）");
    createSessionUpdateFile(localPath, fileSize, matchedFileId);
    uploadParts(localPath, fileSize);
    commitSession();
    abortSession();
   } else {
    System.out.println("バージョンアップロード");
    versionUpload(localPath, matchedFileId);
   }

   break;
  case FULL_MATCH:
   System.out.println("アップロードしない");
   // なにもしない
   break;
  }
 }

 // ディレクトリアップロードハンドラー
 public void uploadDirectory(String localDirPath, String destFolderId) {
  System.out.println("-- START uploadDirectory --");
  File localDir = new File(localDirPath);
  // ローカルディレクトリの名前を取得
  String localDirName = localDir.getName();

  BoxFolder destFolder = new BoxFolder(api, destFolderId);

  // 宛先フォルダ内のアイテムリストを取得
  List<BoxItem.Info> itemList = getFolderInfo(destFolderId);

  String newDestFolderId = findFolderIdByName(itemList, localDirName);
  if (newDestFolderId == null) {
   System.out.println("新規作成します");
   // 同名のフォルダが存在しない場合、新規フォルダを作成
   BoxFolder.Info newDestFolderInfo = destFolder.createFolder(localDirName);
   newDestFolderId = newDestFolderInfo.getID();
  }

  File[] localFiles = localDir.listFiles();

  for (File localFile : localFiles) {
   String localFileName = localFile.getName();
   System.out.println(localFileName);
   if (localFile.isDirectory()) {
    uploadDirectory(localFile.getAbsolutePath(), newDestFolderId);
    continue;
   }

   String localFileHash = createSha1(localFile.getAbsolutePath());
   System.out.println(localFileHash);
   handleFileUpload(localFile.getAbsolutePath(), localFileName, newDestFolderId);
  }
  System.out.println("-- END uploadDirectory --");
 }

 // 宛先フォルダ内のアイテムリストを取得
 private String findFolderIdByName(List<BoxItem.Info> itemList, String folderName) {
  for (BoxItem.Info item : itemList) {
   if (item instanceof BoxFolder.Info && item.getName().equals(folderName)) {
    return item.getID();
   }
  }
  return null;
 }

 // ファイルかディレクトリか調べる
 private DataResult isFileOrDirectory(String stringPath) {
  Path path = Paths.get(stringPath);
  System.out.println("path : " + path);
  if (Files.isDirectory(path)) {
   System.out.flush();
   return DataResult.DIRECTORY;
  } else if (Files.isRegularFile(path)) {
   return DataResult.FILE;
  }
  return null;
 }

 // データ分析結果
 private enum DataResult {
  FILE, DIRECTORY, NEW_UPLOAD, VERSION_UPLOAD, SESSION_UPLOAD,
 }

 // パスからファイル名を取得
 private String getNameFromPath(String stringPath) {
  Path path = Paths.get(stringPath);
  String name = path.getFileName().toString();
  return name;
 }

 // 名前とハッシュ値で比較する
 private CompareResultWithId compareNameAndSha1(String localFileName, String localSha1, String destFolderId) {
  List<BoxItem.Info> itemList = getFolderInfo(destFolderId);
  for (BoxItem.Info item : itemList) {
   if (item instanceof BoxFile.Info) {
    BoxFile.Info fileInfo = (BoxFile.Info) item;
    System.out.println("比較 ファイル名 : " + localFileName + " : " + fileInfo.getName() + "localSha1 : " + localSha1 + " : "
      + fileInfo.getSha1());
    if (fileInfo != null) {
     if (localFileName.equals(fileInfo.getName())) {
      if (localSha1.equals(fileInfo.getSha1())) {
       // 名前とハッシュ値が一致
       // アップロードしない
       System.out.println("FULL MATCH");
       return new CompareResultWithId(compareResult.FULL_MATCH);
      }
      // 名前のみ一致
      // バージョンアップロード
      return new CompareResultWithId(compareResult.NAME_MATCH, fileInfo.getID());
     }
    }
   }
  }
  // 一致しない
  // 新規アップロード
  return new CompareResultWithId(compareResult.NO_MATCH);
 }

 // フォルダIDから中身をスキャン
 public List<BoxItem.Info> getFolderInfo(String folderId) {
  List<BoxItem.Info> itemList = new ArrayList<>();
  try {
   BoxFolder folder = new BoxFolder(api, folderId);
   for (BoxItem.Info itemInfo : folder) {
    if (itemInfo instanceof BoxFile.Info) {
     BoxFile.Info fileInfo = (BoxFile.Info) itemInfo;
     itemList.add(itemInfo);
    } else if (itemInfo instanceof BoxFolder.Info) {
     BoxFolder.Info folderInfo = (BoxFolder.Info) itemInfo;
     itemList.add(folderInfo);
    }
   }
  } catch (BoxAPIException e) {
   e.printStackTrace();
  }
  return itemList;
 }

 public List<BoxItem.Info> getFolderInfo(String folderId, BoxAPIConnection api) {
  List<BoxItem.Info> itemList = new ArrayList<>();
  try {
   BoxFolder folder = new BoxFolder(api, folderId);
   for (BoxItem.Info itemInfo : folder) {
    if (itemInfo instanceof BoxFile.Info) {
     BoxFile.Info fileInfo = (BoxFile.Info) itemInfo;
     itemList.add(itemInfo);
    } else if (itemInfo instanceof BoxFolder.Info) {
     BoxFolder.Info folderInfo = (BoxFolder.Info) itemInfo;
     itemList.add(folderInfo);
     System.out.println(folderInfo.getName());
    }
   }
  } catch (BoxAPIException e) {
   e.printStackTrace();
  }
  return itemList;
 }

}
