package lk.jiat.fiadabook.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Env {
    private static final Properties properties = new Properties();

    static {
        try {
         InputStream inputStream = Env.class.getClassLoader().getResourceAsStream("app.properties");
            properties.load(inputStream);

        }catch (IOException e){
            throw new RuntimeException("Application properties loading failed\n"+e.getMessage());
        }
    }

    public static String getProperty(String key){
        return properties.getProperty(key);
    }

    public static Properties getAppProperties(){
        return properties;
    }
}
