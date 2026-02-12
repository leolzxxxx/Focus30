package main.java.com.focus30.ui;

import javafx.animation.PauseTransition;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * @className: ForceDialog
 * @description: 负责强制休息弹窗
 * @author: liuzhong
 * @date: 2026/2/11 11:29
 * @version: 1.0
 */
public class ForceDialog {
    private static Stage forceDialog;

    /**
     * 显示强制休息弹窗
     */
    public static void show(StackPane root) {
        if (forceDialog != null && forceDialog.isShowing()) {
            return;
        }

        // 提示文字
        Label msg = new Label("强制暂停 10 秒钟，去休息一下吧~");
        msg.setStyle("-fx-font-size: 36px;" + "-fx-font-weight: bold;" + "-fx-text-fill: white;");

        // 根容器：大面积半透明圆角
        StackPane pane = new StackPane(msg);
        pane.setStyle(
                "-fx-background-color: rgba(236,206,206,0.9);" + // 背景及透明度
                        "-fx-background-radius: 20;" +               // 圆角
                        "-fx-border-color: rgba(255,0,0,0.9);" + // 边框
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 20;"
        );
        pane.setPrefSize(900, 300); // 大面积，可以根据屏幕调节

        // 创建弹窗
        forceDialog = new Stage();
        forceDialog.initOwner(root.getScene().getWindow()); // 设置弹窗的所有者为当前主窗口
        forceDialog.initModality(Modality.APPLICATION_MODAL); // 禁止操作主窗口
        forceDialog.initStyle(StageStyle.TRANSPARENT);         // 透明无边框
        forceDialog.setResizable(false);                       // 不可改变大小
        Scene scene = new Scene(pane);
        scene.setFill(Color.TRANSPARENT);                     // 场景透明
        forceDialog.setScene(scene);

        // 禁止用户关闭
        forceDialog.setOnCloseRequest(e -> e.consume());

        // 获取屏幕可视区域，弹窗在屏幕中间
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - pane.getPrefWidth()) / 2;
        double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - pane.getPrefHeight()) / 2;
        forceDialog.setX(centerX);
        forceDialog.setY(centerY);

        forceDialog.show();

        // x秒后自动消失
        PauseTransition autoClose = new PauseTransition(Duration.seconds(10));  //todo 30
        autoClose.setOnFinished(e -> {
            forceDialog.close();
            forceDialog = null;
        });
        autoClose.play();
    }
}
