import com.sun.jna.WString;
import com.sun.jna.platform.win32.Shell32;
import javafx.application.Application;

/**
 * @className: MainLauncher
 * @description: 设置 Windows AppUserModelID，并启动 JavaFX 应用
 * @author: liuzhong
 * @date: 2025/12/10 14:11
 * @version: 1.0
 */
public class MainLauncher {
    /**
     * 设置应用程序用户模型ID
     * @param appID 应用程序用户模型ID
     */
    public static void setAppUserModelID(String appID) {
        Shell32.INSTANCE.SetCurrentProcessExplicitAppUserModelID(new WString(appID));
    }
    public static void main(String[] args) {
        setAppUserModelID("Focus30.App");
        Application.launch(Main.class, args);
    }
}
