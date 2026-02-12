package main.java.com.focus30.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @className: Config
 * @description:
 * @author: liuzhong
 * @date: 2026/2/11 11:10
 * @version: 1.0
 */
public class Config {
    private static Properties properties = new Properties();

    static {
        try {
            InputStream in = Config.class.getResourceAsStream("/src/main/resources/config/config.properties");
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getInt(String key) {
        String value = properties.getProperty(key);
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {}
        return 0;
    }

    public static double getDouble(String key) {
        String value = properties.getProperty(key);
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ignored) {}
        return 0;
    }
}
