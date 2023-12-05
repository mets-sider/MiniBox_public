package application;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainWindow extends Application {
 private String fxmlName = "MainWindow.fxml";
 private String fxmlTitle = "MiniBox";
 private String iconPath = "icon.jpg";
 private Stage primaryStage;
 private SystemTray tray;
 private TrayIcon icon;

 public MainWindow() {
 }

 public MainWindow(Stage primaryStage) {
  this.primaryStage = primaryStage;
  setupSystemTray();
 }

 // 開く
 public void showStage() {
  if (this.primaryStage != null) {
   this.primaryStage.show();
   showOrFocusStage();
  }
 }

 public void showOrFocusStage() {
  if (primaryStage.isShowing()) {
   primaryStage.setIconified(false);
   primaryStage.toFront();
   primaryStage.requestFocus();
  } else {
   showStage();
  }
 }

 public void hideStage() {
  if (this.primaryStage != null) {
   this.primaryStage.hide();
  }
 }

 public void close() {
  if (primaryStage != null) {
   this.primaryStage.close();
  }
  if (tray != null && icon != null) {
   tray.remove(icon);
  }
 }

 public void start(Stage primaryStage) {
  this.primaryStage = primaryStage;
  Platform.setImplicitExit(false);
  try {
   // FXMLからのシーングラフの読み込み
   FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlName));
   Parent root = loader.load();

   // シーングラフのルートノードを設定したシーンの作成
   Scene scene = new Scene(root, 950, 500);
   // ステージへのシーンの設定
   primaryStage.setScene(scene);
   primaryStage.setTitle(fxmlTitle);
   primaryStage.setOnCloseRequest((WindowEvent event) -> {
    event.consume();
    hideStage();
   });
   primaryStage.show();
   if (this.primaryStage != null) {
   } else if (this.primaryStage == null) {
   }
  } catch (Exception e) {
   e.printStackTrace();
  }
 }

 public void startWithStage(Stage currentStage) {
  this.primaryStage = currentStage;
  setupSystemTray();
  start(this.primaryStage);
  showOrFocusStage();
 }

 // ダブルクリックイベント
 private void doubleClick() {
  icon.addMouseListener(new MouseAdapter() {
   @Override
   public void mouseClicked(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
     Platform.runLater(() -> {
      showStage();
     });
    }
   }
  });
 }

 public void setupSystemTray() {
  tray = SystemTray.getSystemTray();
  PopupMenu popup = new PopupMenu();
  Image image = Toolkit.getDefaultToolkit().createImage(ClassLoader.getSystemResource(iconPath));
  icon = new TrayIcon(image, "MiniBox", popup);
  icon.setImageAutoSize(true);
  doubleClick();

  // 他のMenuItemと同様に作成
  MenuItem item1 = new MenuItem("開く");
  item1.addActionListener(e -> Platform.runLater(this::showStage));

  MenuItem item2 = new MenuItem("終了");
  item2.addActionListener(e -> Platform.runLater(this::close));

  popup.add(item1);
  popup.add(item2);

  try {
   tray.add(icon);
  } catch (AWTException e) {
   e.printStackTrace();
  }
 }
}
