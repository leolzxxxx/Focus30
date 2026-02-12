package main.java.com.focus30.core;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * @className: TimerController
 * @description: 负责计时逻辑
 * @author: liuzhong
 * @date: 2026/2/11 11:30
 * @version: 1.0
 */
public class TimerController {
    // 总倒计时秒数
    private int totalSeconds = 30 * 60; //todo
    // 剩余秒数
    private int remainingSeconds;
    // 已过秒数
    private long elapsedSeconds = 0;
    // 专注期倒计时时间线
    private Timeline countdownTimeline;
    // 放松期正计时时间线
    private Timeline countupTimeline;
    // 倒计时是否正在运行
    private boolean running;
    // 函数式接口，当倒计时文本变化时，格式化字符串
    private Consumer<String> onCountdownUpdate;
    private Consumer<String> onCountupUpdate;
    // 这是一个回调函数，倒计时结束后会调用它，切换到正计时
    private Runnable onCountdownFinished;
    // 这是一个回调函数，5 秒内如果用户没有点击则通知外面，触发强制弹窗
    private Runnable onNoClickTimeout;
    // 在倒计时结束后，启动一个「只执行一次」的 5 秒定时器，5 秒内如果用户点击则取消它，否则就执行倒计时结束逻辑
    private Timeline noClickTimeline;
    // 是否处于正计时阶段
    private boolean relaxPhase;

    public TimerController(Consumer<String> onCountdownUpdate,
                           Consumer<String> onCountupUpdate,
                           Runnable onCountdownFinished,
                           Runnable onNoClickTimeout) {
        this.onCountdownUpdate = onCountdownUpdate;
        this.onCountupUpdate = onCountupUpdate;
        this.onCountdownFinished = onCountdownFinished;
        this.onNoClickTimeout = onNoClickTimeout;
        reset();
    }

    public void startCountdown() {
        if (running) return;

        running = true;
        remainingSeconds = totalSeconds;
        notifyCountdown();

        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    remainingSeconds--;
                    notifyCountdown();
                    // 倒计时结束
                    if (remainingSeconds <= 0) {
                        stopCountdown();
                        startCountUp();

                        relaxPhase = true;
                        onCountdownFinished.run();
                        startNoClickDetect();
                    }
                })
        );
        countdownTimeline.setCycleCount(Animation.INDEFINITE);
        countdownTimeline.play();
    }

    public void reset() {
        stopCountdown();
        stopCountup();
        remainingSeconds = totalSeconds;
        elapsedSeconds = 0;
        running = false;
        notifyCountdown();
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isInRelaxPhase() {
        return relaxPhase;
    }

    public void startCountUp() {
        elapsedSeconds = 0;
        notifyCountup();

        countupTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    elapsedSeconds++;
                    notifyCountup();
                })
        );
        countupTimeline.setCycleCount(Animation.INDEFINITE);
        countupTimeline.play();
    }

    private void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
        running = false;
    }

    private void stopCountup() {
        if (countupTimeline != null) {
            countupTimeline.stop();
            countupTimeline = null;
        }
    }

    /**
     * 启动一个定时器，5 秒后执行
     */
    private void startNoClickDetect() {
        // 如果之前有定时器，则取消它
        if (noClickTimeline != null) {
            noClickTimeline.stop();
        }

        noClickTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), e -> {
                    // 如果处于relaxPhase，则执行
                    if (relaxPhase) {
                        onNoClickTimeout.run();
                    }
                })
        );
        noClickTimeline.setCycleCount(1);
        noClickTimeline.play();
    }

    /**
     * 用户点击后的处理逻辑
     */
    public void handleUserClick() {
        if (!relaxPhase) return;

        stopCountup();

        if (noClickTimeline != null) {
            noClickTimeline.stop();
            noClickTimeline = null;
        }

        relaxPhase = false;
        reset();
    }


    /* ================= 文本格式化 ================= */

    private void notifyCountdown() {
        int m = remainingSeconds / 60;
        int s = remainingSeconds % 60;
        onCountdownUpdate.accept(String.format("%02d:%02d", m, s));
    }

    private void notifyCountup() {
        long h = elapsedSeconds / 3600;
        long m = (elapsedSeconds % 3600) / 60;
        long s = elapsedSeconds % 60;
        onCountupUpdate.accept(String.format("%02d:%02d:%02d", h, m, s));
    }
}
