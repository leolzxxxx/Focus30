package main.java.com.focus30.launcher;

import com.sun.jna.WString;
import com.sun.jna.platform.win32.Shell32;
import javafx.application.Application;
import main.java.com.focus30.ui.MainView;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

/**
 * @className: main.java.com.focus30.launcher.MainLauncher
 * @description:
 * 1. 设置 Windows 任务栏图标 AppUserModelID，保证固定图标统一
 * 2. 单实例控制，防止程序多开
 * 3. 启动 JavaFX 应用 main.java.Main
 * @author: liuzhong
 * @date: 2025/12/10 14:11
 * @version: 1.0
 */
public class MainLauncher {
    private static FileLock lock;

    /**
     * 设置应用程序用户模型ID，设置应用的任务栏标识
     * @param appID 应用程序用户模型ID
     */
    public static void setAppUserModelID(String appID) {
        Shell32.INSTANCE.SetCurrentProcessExplicitAppUserModelID(new WString(appID));
    }

    /**
     * 锁定单实例
     * @return 是否成功锁定单实例
     */
    private static boolean lockInstance() {
        try {
            File file = new File(System.getProperty("user.home"), ".focus30.lock"); // 在用户目录下创建一个隐藏锁文件 .focus30.lock
            RandomAccessFile raf = new RandomAccessFile(file, "rw"); // 以读写模式打开该文件
            lock = raf.getChannel().tryLock(); // 尝试获取该文件的独占锁（FileLock）
            return lock != null; // lock != null 成功获取锁，说明没有其他实例运行
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 释放单实例锁
     */
    private static void unlockInstance() {
        try {
            if (lock != null) lock.release();
        } catch (Exception ignored) {}
    }

    /**
     * 启动 JavaFX 应用
     * @param args 启动参数
     */
    public static void main(String[] args) {
        if (!lockInstance()) {
            System.out.println("程序已启动，请关闭后重试！");
            return;
        }

        setAppUserModelID("Focus30.App");
        Application.launch(MainView.class, args);

        unlockInstance();
    }
}
