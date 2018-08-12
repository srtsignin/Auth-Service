package Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    public static void LoadFromFile(String fileName) {
        try {
            loadProperties(fileName);
        } catch (IOException exception) {
            exception.printStackTrace();
            throw new RuntimeException("Unable to load file " + fileName);
        }
    }

    private static void loadProperties(String fileName) throws IOException {
        Properties newProperties = System.getProperties();

        InputStream inputStream = getFileStream(fileName);
        newProperties.load(inputStream);
        inputStream.close();

        System.setProperties(newProperties);
    }

    private static InputStream getFileStream(String fileName) {
        ClassLoader classLoader = PropertiesLoader.class.getClassLoader();
        return classLoader.getResourceAsStream(fileName);
    }
}
