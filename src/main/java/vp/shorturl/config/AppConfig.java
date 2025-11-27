package vp.shorturl.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private final int defaultTtlHours;
    private final int defaultMaxUsages;

    public AppConfig() {
        Properties props = new Properties();

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                System.out.println("application.properties not found, using defaults.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }

        // читаем значения с дефолтами
        this.defaultTtlHours = Integer.parseInt(props.getProperty("shorturl.defaultTtlHours", "24"));
        this.defaultMaxUsages = Integer.parseInt(props.getProperty("shorturl.defaultMaxUsages", "10"));
    }

    public int getDefaultTtlHours() {
        return defaultTtlHours;
    }

    public int getDefaultMaxUsages() {
        return defaultMaxUsages;
    }
}
