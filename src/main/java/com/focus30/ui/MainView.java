package main.java.com.focus30.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import main.java.com.focus30.core.TimerController;
import main.java.com.focus30.effect.AnimationManager;
import main.java.com.focus30.utils.Config;

/**
 * @className: MainView
 * @description: 负责界面布局
 * @author: liuzhong
 * @date: 2026/2/11 11:29
 * @version: 1.0
 */
public class MainView extends Application {
    // 根布局容器
    private StackPane root;
    // 拖拽距离
    private double dragDistance = 0;
    // 拖拽阈值（像素）
    private final double DRAG_THRESHOLD = Config.getDouble("drag.threshold");

    private TimerController timerController;
    private AnimationManager animationManager;

    private Label countdownLabel;
    private Label countupLabel;

    /**
     * 应用程序启动方法
     * @param primaryStage 主舞台
     */
    @Override
    public void start(Stage primaryStage) {
        // 1. 创建浮动窗口，无边框且置顶
        primaryStage.initStyle(StageStyle.TRANSPARENT); // 设置为透明
        primaryStage.setAlwaysOnTop(true); // 设置置顶
        primaryStage.setOpacity(Config.getDouble("primaryStage.opacity"));

        // 在 start 方法中正确设置窗口图标
        primaryStage.getIcons().add(new Image(MainView.class.getResourceAsStream("/src/main/resources/imag/kouTu.png")));

        animationManager = new AnimationManager();

        // 2. 创建用于显示倒计时的标签
        countdownLabel = new Label("30:00");
        countdownLabel.setStyle("-fx-font-size: 23px; -fx-font-weight: bold; -fx-text-fill: white;");

        countupLabel = new Label("00:00:00");
        countupLabel.setStyle("-fx-font-size: 16px;" + "-fx-font-weight: bold;" + "-fx-text-fill: white;");

        timerController = new TimerController(
                countdownLabel::setText,   // 倒计时文本更新，把它设置到 countdownLabel 上
                countupLabel::setText,     // 正计时文本更新，把它设置到 countupLabel 上
                () -> {                    // 倒计时结束回调，UI 切换 + 动画
                    countdownLabel.setVisible(false);
                    countupLabel.setVisible(true);
                    root.getChildren().remove(countdownLabel);
                    if (!root.getChildren().contains(countupLabel)) {
                        root.getChildren().add(countupLabel);
                    }

                    animationManager.startRainbow(root);
                    animationManager.startShake(root);
                },
                () -> {   // 5 秒没点击回调 → 弹窗
                    ForceDialog.show(root);  // 弹窗
                }
        );

        // 创建根布局容器并添加时间标签
        root = new StackPane(countdownLabel);
        // 设置圆角和边框样式
        root.setStyle(
                "-fx-background-color: rgba(255, 0, 0, 0.6);" +  // 半透明背景
                        "-fx-background-radius: 10;" +  // 圆角半径
                        "-fx-border-color: rgba(255, 0, 0, 0.95);" +     // 边框颜色
                        "-fx-border-width: 1;" +         // 边框宽度
                        "-fx-border-radius: 10;"         // 边框圆角
        );

        // 设置鼠标进入时的颜色过渡效果
        root.setOnMouseEntered(e -> {
            animationManager.playColorTransition(
                    root,
                    255, 0, 0,
                    255, 105, 180,
                    0.6, 0.6,
                    400
            );
        });

        // 设置鼠标退出时的颜色过渡效果
        root.setOnMouseExited(e -> {
            animationManager.playColorTransition(
                    root,
                    255, 105, 180,
                    255, 0, 0,
                    0.6, 0.6,
                    400
            );
        });

        // 修改鼠标点击事件处理
        root.setOnMouseClicked(e -> {
            // 如果拖拽距离大于阈值或者点击按钮不是左键，则不处理点击事件
            if (dragDistance >= DRAG_THRESHOLD || e.getButton() != MouseButton.PRIMARY) {
                return;
            }

            // 在放松阶段时的点击处理，即第一次点击
            if (timerController.isInRelaxPhase()) {
                timerController.handleUserClick(); // 处理用户点击

                animationManager.clearRainbow(root);
                animationManager.stopShake(root);

                countupLabel.setVisible(false);
                countdownLabel.setVisible(true);
                if (!root.getChildren().contains(countdownLabel)) {
                    root.getChildren().add(countdownLabel);
                }

                timerController.reset();
            // 当倒计时未运行时的点击处理，即第二次点击，则开始倒计时，是一切的开始
            } else if (!timerController.isRunning()) {
                timerController.startCountdown();
            }
            // 当倒计时运行中时点击不执行任何特效
        });

        // 创建场景并设置透明背景
        Scene scene = new Scene(root, Config.getInt("primaryStage.width"), Config.getInt("primaryStage.height"));
        scene.getStylesheets().clear(); // 清除默认样式
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT); // 设置场景背景透明
        primaryStage.setScene(scene);

        // 3. 实现拖拽功能，通过鼠标事件实现
        double[] offset = new double[2];
        scene.setOnMousePressed(e -> {
            offset[0] = e.getSceneX();
            offset[1] = e.getSceneY();
            dragDistance = 0;
        });
        scene.setOnMouseDragged(e -> {
            primaryStage.setX(e.getScreenX() - offset[0]);
            primaryStage.setY(e.getScreenY() - offset[1]);
            // 累计拖拽距离
            dragDistance += Math.abs(e.getSceneX() - offset[0]) + Math.abs(e.getSceneY() - offset[1]);
        });

        // 4. 初始位置：屏幕右上角
        primaryStage.setX(Config.getInt("primaryStage.x"));
        primaryStage.setY(Config.getInt("primaryStage.y"));
        primaryStage.show();

        alwaysOnTop(primaryStage);
    }

    /**
     * 定时重新窗口置顶
     */
    private void alwaysOnTop(Stage primaryStage) {
        Timeline alwaysOnTopTimeline = new Timeline(
                new KeyFrame(Duration.minutes(Config.getInt("primaryStage.alwaysOnTop")), e -> {
                    primaryStage.setAlwaysOnTop(false);  // 先取消
                    primaryStage.setAlwaysOnTop(true);   // 再恢复，达到重新置顶效果
                })
        );
        alwaysOnTopTimeline.setCycleCount(Animation.INDEFINITE);
        alwaysOnTopTimeline.play();
    }
}
