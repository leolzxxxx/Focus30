package main.java.com.focus30.effect;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * @className: AnimationManager
 * @description: 负责动画效果
 * @author: liuzhong
 * @date: 2026/2/11 11:30
 * @version: 1.0
 */
public class AnimationManager {
    // 颜色过渡时间线
    private Timeline colorTransitionTimeline = new Timeline();
    // 用于存储彩虹渐变效果元素的列表
    private List<Node> rainbowEffectHolders = new ArrayList<>();
    // 震动动画
    private TranslateTransition shakeTransition;

    /**
     * 显示平滑彩虹色渐变效果
     */
    public void startRainbow(StackPane root) {
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
    public void clearRainbow(StackPane root) {
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
     * 上下震动效果
     */
    public void startShake(StackPane  root) {
        if (shakeTransition != null) {
            shakeTransition.stop();
        }

        shakeTransition = new TranslateTransition(Duration.millis(150), root); // 动画持续时间x毫秒
        shakeTransition.setFromY(+5);
        shakeTransition.setToY(-5);
        shakeTransition.setAutoReverse(true);
        shakeTransition.setCycleCount(Animation.INDEFINITE);
        shakeTransition.play();
    }

    /**
     * 停止上下震动效果
     */
    public void stopShake(StackPane  root) {
        if (shakeTransition != null) {
            shakeTransition.stop();
            shakeTransition = null;
            root.setTranslateY(0); // 复位
        }
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
    public void createColorTransition(StackPane pane,
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
}
