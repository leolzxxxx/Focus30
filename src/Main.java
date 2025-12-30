import com.sun.jna.WString;
import com.sun.jna.platform.win32.Shell32;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * 倒计时应用程序主类
 * 实现一个可拖拽的透明倒计时窗口，具有彩虹色效果和交互功能
 */
public class Main extends Application {
    // 倒计时标签
    private Label countdownLabel;
    // 剩余秒数
    private int remainingSeconds;
    // 专注期倒计时时间线
    private Timeline countdownTimeline;
    // 放松期正计时时间线
    private Timeline countupTimeline;
    // 已过秒数
    private long elapsedSeconds = 0;
    // 正计时标签
    private Label countupLabel;
    // 颜色过渡时间线
    private Timeline colorTransitionTimeline;
    // 根布局容器
    private StackPane root;
    // 总倒计时秒数
    private int totalSeconds = 30 * 60;
    // 倒计时是否正在运行
    private boolean isCountdownRunning = false;
    // 拖拽距离
    private double dragDistance = 0;
    // 拖拽阈值（像素）
    private final double DRAG_THRESHOLD = 5;
    // 用于存储彩虹渐变效果元素的列表
    private List<javafx.scene.Node> rainbowEffectHolders = new ArrayList<>();

    /**
     * 应用程序启动方法
     * @param primaryStage 主舞台
     */
    @Override
    public void start(Stage primaryStage) {
        // 1. 创建浮动窗口，无边框且置顶
        primaryStage.initStyle(StageStyle.TRANSPARENT); // 设置为透明
        primaryStage.setAlwaysOnTop(true); // 设置置顶
        primaryStage.setOpacity(0.8); // 设置透明度（0.0~1.0，0.7表示半透明）

        // 在 start 方法中正确设置窗口图标
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/imag/kouTu.png")));

        // 2. 创建用于显示倒计时的标签
        countdownLabel = new Label("30:00");
        countdownLabel.setStyle("-fx-font-size: 23px; -fx-font-weight: bold; -fx-text-fill: white;");

        countupLabel = new Label("00:00:00");
        countupLabel.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;"
        );

        // 初始不显示正计时
        countupLabel.setVisible(false);

        // 创建根布局容器并添加时间标签
        root = new StackPane(countdownLabel);
        // 设置圆角和边框样式
        root.setStyle(
            "-fx-background-color: rgba(255, 0, 0, 0.6);" +  // 红色半透明背景
            "-fx-background-radius: 10;" +  // 圆角半径
            "-fx-border-color: rgba(255,0,0,0.95);" +     // 边框颜色
            "-fx-border-width: 1;" +         // 边框宽度
            "-fx-border-radius: 10;"         // 边框圆角
        );

        // 初始化倒计时状态
        resetCountdown();

        // 创建颜色过渡时间线
        colorTransitionTimeline = new Timeline();
        colorTransitionTimeline.setCycleCount(1); // 只执行一次

        // 设置鼠标进入时的颜色过渡效果
        root.setOnMouseEntered(e -> {
            colorTransitionTimeline.stop(); // 停止当前正在进行的动画
            // 创建从红色到粉色的颜色过渡
            createColorTransition(root, 255, 0, 0, 255, 105, 180, 0.6, 0.7, 300); // 300ms过渡时间
        });

        // 设置鼠标退出时的颜色过渡效果
        root.setOnMouseExited(e -> {
            colorTransitionTimeline.stop(); // 停止当前正在进行的动画
            // 创建从粉色到红色的颜色过渡
            createColorTransition(root, 255, 105, 180, 255, 0, 0, 0.7, 0.6, 300); // 300ms过渡时间
        });

        // 修改鼠标点击事件处理
        root.setOnMouseClicked(e -> {
            // 只有当拖拽距离小于阈值时才认为是点击事件
            if (dragDistance < DRAG_THRESHOLD && e.getButton() == MouseButton.PRIMARY) {
                if (countupLabel.isVisible()) {
                    // 正计时正在显示，点击重置倒计时
                    clearRainbow();             // 停止彩虹渐变
                    if (countupTimeline != null) countupTimeline.stop(); // 停止正计时
                    countupLabel.setVisible(false);
                    countdownLabel.setVisible(true);
                    resetCountdown();           // 重置倒计时
                } else if (!isCountdownRunning) {
                    // 当倒计时未运行时的点击处理
                    startCountdown();
                }
                // 当倒计时运行中时点击不执行任何特效
            }
        });

        // 创建场景并设置透明背景
        Scene scene = new Scene(root, 70, 26);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
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
        primaryStage.setX(1170);
        primaryStage.setY(47);
        primaryStage.show();

        // 初始显示
        updateCountdownTimeLabel();

        // 定时每 1 分钟重新置顶
        Timeline alwaysOnTopTimeline = new Timeline(
                new KeyFrame(Duration.minutes(1), e -> {
                    primaryStage.setAlwaysOnTop(false);  // 先取消
                    primaryStage.setAlwaysOnTop(true);   // 再恢复，达到重新置顶效果
                })
        );
        alwaysOnTopTimeline.setCycleCount(Animation.INDEFINITE);
        alwaysOnTopTimeline.play();
    }

    /**
     * 重置倒计时
     */
    private void resetCountdown() {
        remainingSeconds = totalSeconds;
        isCountdownRunning = false;
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        updateCountdownTimeLabel();
    }

    /**
     * 开始倒计时
     */
    private void startCountdown() {
        isCountdownRunning = true;
        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    remainingSeconds--;
                    updateCountdownTimeLabel();
                    if (remainingSeconds <= 0) {
                        countdownTimeline.stop();
                        countdownLabel.setVisible(false);
                        countupLabel.setVisible(true);
                        isCountdownRunning = false;
                        // 倒计时结束时显示彩虹渐变效果
                        startCountUp();
                        showRainbow();
                    }
                })
        );
        countdownTimeline.setCycleCount(Animation.INDEFINITE);
        countdownTimeline.play();
    }

    /**
     * 开始正计时
     */
    private void startCountUp() {
        // 重置正计时，从0开始
        elapsedSeconds = 0;

        // 先刷新一次，防止上一次残留
        updateCountupTimeLabel();

        if (countupTimeline != null) {
            countupTimeline.stop();
        }

        countupTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    elapsedSeconds++;
                    updateCountupTimeLabel();
                })
        );
        countupTimeline.setCycleCount(Animation.INDEFINITE);
        countupTimeline.play();
    }

    /**
     * 更新正计时标签的显示内容
     */
    private void updateCountupTimeLabel() {
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;

        countupLabel.setText(
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
        );
    }

    /**
     * 显示平滑彩虹色渐变效果
     */
    private void showRainbow() {
        // 移除时间标签以显示纯色背景效果
        root.getChildren().remove(countdownLabel);

        // 添加正计时标签
        if (!root.getChildren().contains(countupLabel)) {
            root.getChildren().add(countupLabel);
        }
        countupLabel.setVisible(true);

        // 定义彩虹颜色数组
        Color[] rainbowColors = {
            Color.RED, Color.ORANGE, Color.YELLOW,
            Color.GREEN, Color.BLUE, Color.INDIGO, Color.VIOLET
        };

        // 创建自定义过渡动画
        Transition rainbowTransition = new Transition() {
            {
                setCycleDuration(Duration.millis(2000)); // 2秒完成一轮彩虹色循环
                setCycleCount(Animation.INDEFINITE);
            }

            @Override
            protected void interpolate(double frac) {
                // 计算当前在彩虹色数组中的位置
                double scaledFrac = frac * rainbowColors.length;
                int index = (int) Math.floor(scaledFrac);
                double localFrac = scaledFrac - index;

                // 获取当前和下一个颜色
                Color currentColor = rainbowColors[index % rainbowColors.length];
                Color nextColor = rainbowColors[(index + 1) % rainbowColors.length];

                // 插值计算颜色
                double r = currentColor.getRed() + localFrac * (nextColor.getRed() - currentColor.getRed());
                double g = currentColor.getGreen() + localFrac * (nextColor.getGreen() - currentColor.getGreen());
                double b = currentColor.getBlue() + localFrac * (nextColor.getBlue() - currentColor.getBlue());

                // 应用渐变颜色到背景样式
                root.setStyle(
                    String.format("-fx-background-color: rgba(%d, %d, %d, %.1f);",
                        (int)(r * 255), (int)(g * 255), (int)(b * 255), 0.7) +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: rgba(255,0,0,0.95);" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;"
                );
            }
        };

        rainbowTransition.play();

        // 使用简单的标记对象保存过渡动画引用以便清理
        javafx.scene.shape.Rectangle marker = new javafx.scene.shape.Rectangle(0, 0);
        marker.setUserData(rainbowTransition); // 将过渡动画存储在userData中
        rainbowEffectHolders.add(marker);
    }

    /**
     * 清理彩虹效果
     */
    private void clearRainbow() {
        // 停止所有颜色过渡动画
        for (javafx.scene.Node node : rainbowEffectHolders) {
            if (node.getUserData() instanceof Transition) {
                Transition transition = (Transition) node.getUserData();
                transition.stop();
            } else if (node.getUserData() instanceof Timeline) {
                Timeline countdownTimeline = (Timeline) node.getUserData();
                countdownTimeline.stop();
            }
        }

        // 清理元素
        rainbowEffectHolders.clear();

        // 隐藏正计时，显示倒计时
        countupLabel.setVisible(false);
        if (!root.getChildren().contains(countdownLabel)) {
            root.getChildren().add(countdownLabel);
        }

        // 恢复原始背景色
        root.setStyle(
            "-fx-background-color: rgba(255, 0, 0, 0.6);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(255,0,0,0.95);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 10;"
        );
    }

    /**
     * 格式化时间为 HH:MM
     */
    private void updateCountdownTimeLabel() {
        int mins = remainingSeconds / 60;
        int secs = remainingSeconds % 60;
        countdownLabel.setText(String.format("%02d:%02d", mins, secs));
    }

    /**
     * 创建颜色过渡动画
     * @param pane 要应用过渡的面板
     * @param fromR 起始红色值
     * @param fromG 起始绿色值
     * @param fromB 起始蓝色值
     * @param toR 目标红色值
     * @param toG 目标绿色值
     * @param toB 目标蓝色值
     * @param fromOpacity 起始透明度
     * @param toOpacity 目标透明度
     * @param durationMs 过渡持续时间（毫秒）
     */
    private void createColorTransition(StackPane pane,
                                     int fromR, int fromG, int fromB,
                                     int toR, int toG, int toB,
                                     double fromOpacity, double toOpacity,
                                     int durationMs) {
        colorTransitionTimeline.getKeyFrames().clear();

        // 创建关键帧
        KeyFrame keyFrame = new KeyFrame(Duration.millis(durationMs), event -> {
            // 动画结束时确保颜色为目标颜色
            int r = toR;
            int g = toG;
            int b = toB;
            double opacity = toOpacity;
            // 应用圆角和边框样式
            pane.setStyle(
                String.format("-fx-background-color: rgba(%d, %d, %d, %.1f);", r, g, b, opacity) +
                "-fx-background-radius: 10;" +
                "-fx-border-color: rgba(255,0,0,0.95);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 10;"
            );
        });

        colorTransitionTimeline.getKeyFrames().add(keyFrame);

        // 使用Transition实现平滑颜色过渡效果
        Transition transition = new Transition() {
            {
                setCycleDuration(Duration.millis(durationMs));
            }

            @Override
            protected void interpolate(double frac) {
                // 根据进度计算当前颜色值
                int r = (int) (fromR + frac * (toR - fromR));
                int g = (int) (fromG + frac * (toG - fromG));
                int b = (int) (fromB + frac * (toB - fromB));
                double opacity = fromOpacity + frac * (toOpacity - fromOpacity);
                // 应用圆角和边框样式
                pane.setStyle(
                    String.format("-fx-background-color: rgba(%d, %d, %d, %.1f);", r, g, b, opacity) +
                    "-fx-background-radius: 10;" +
                    "-fx-border-color: rgba(255,0,0,0.95);" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;"
                );
            }
        };

        transition.play();
    }

    @Override
    public void init() throws Exception {
        super.init();
    }
}
