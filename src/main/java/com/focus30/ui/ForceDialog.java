package main.java.com.focus30.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
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
import main.java.com.focus30.utils.Config;

import java.util.List;

/**
 * @className: ForceDialog
 * @description: 负责强制休息弹窗
 * @author: liuzhong
 * @date: 2026/2/11 11:29
 * @version: 1.0
 */
public class ForceDialog {
    private static List<Stage> forceDialogs; // 存储多个弹窗实例

    /**
     * 显示强制休息弹窗（支持多屏幕）
     */
    public static void show(StackPane root) {
        // 如果已有弹窗正在显示，则不再重复创建
        if (forceDialogs != null && !forceDialogs.isEmpty()) {
            return;
        }

        // 获取所有屏幕
        List<Screen> screens = Screen.getScreens();
        forceDialogs = new java.util.ArrayList<>();

        for (Screen screen : screens) {
            // 提示文字
//            Label msg = new Label("强制暂停 " + Config.getInt("showSeconds") + " 秒钟，去休息一下吧~");
            Label msg = new Label("\u5f3a\u5236\u6682\u505c " + Config.getInt("showSeconds") + " \u79d2\u949f\uff0c\u53bb\u4f11\u606f\u4e00\u4e0b\u5427~"); // 解决乱码问题
            msg.setStyle("-fx-font-size: 36px;" + "-fx-font-weight: bold;" + "-fx-text-fill: white;");

            // 根容器：大面积半透明圆角
            StackPane pane = new StackPane(msg);
            pane.setStyle(
                    "-fx-background-color: rgba(236,206,206,1);" + // 背景及透明度
                            "-fx-background-radius: 20;" +               // 圆角
                            "-fx-border-color: rgba(255,0,0,1);" + // 边框
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 20;"
            );
            pane.setPrefSize(Config.getInt("show.width"), Config.getInt("show.height")); // 弹窗尺寸

            // 创建弹窗
            Stage dialog = new Stage();
            dialog.initOwner(root.getScene().getWindow()); // 设置弹窗的所有者为当前主窗口
            dialog.initModality(Modality.APPLICATION_MODAL); // 禁止操作主窗口
            dialog.initStyle(StageStyle.TRANSPARENT);         // 透明无边框
            dialog.setResizable(false);                       // 不可改变大小
            Scene scene = new Scene(pane);
            scene.setFill(Color.TRANSPARENT);                 // 场景透明
            dialog.setScene(scene);

            // 禁止用户关闭
            dialog.setOnCloseRequest(e -> e.consume());

            // 获取当前屏幕的可视区域，并将弹窗居中显示
            Rectangle2D screenBounds = screen.getVisualBounds();
            double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - pane.getPrefWidth()) / 2;
            double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - pane.getPrefHeight()) / 2;
            dialog.setX(centerX);
            dialog.setY(centerY);

            dialog.show();
            forceDialogs.add(dialog); // 将弹窗加入列表
        }

        dialogAlwaysOnTop(forceDialogs);

        // x秒后自动关闭所有弹窗
        PauseTransition autoClose = new PauseTransition(Duration.seconds(Config.getInt("showSeconds")));
        autoClose.setOnFinished(e -> {
            for (Stage dialog : forceDialogs) {
                dialog.close();
            }
            forceDialogs.clear(); // 清空弹窗列表
        });
        autoClose.play();
    }

    private static void dialogAlwaysOnTop(List<Stage> dialogs) {
        Timeline alwaysOnTopTimeline = new Timeline(
                new KeyFrame(Duration.seconds(Config.getInt("show.alwaysOnTop")), e -> {
                    for (Stage dialog : dialogs) {
                        dialog.setAlwaysOnTop(false);  // 先取消
                        dialog.setAlwaysOnTop(true);   // 再恢复，达到重新置顶效果
                    }
                })
        );
        alwaysOnTopTimeline.setCycleCount(Animation.INDEFINITE);
        alwaysOnTopTimeline.play();
    }
}
