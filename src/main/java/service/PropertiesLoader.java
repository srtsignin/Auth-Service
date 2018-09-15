package service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class PropertiesLoader {

    public static void LoadFromFile(Path filepath) {
        try {
            loadProperties(filepath.toString());
        } catch (IOException exception) {
            exception.printStackTrace();
            throw new RuntimeException("Unable to load file " + filepath.toString());
        }
    }

    private static void loadProperties(String fileName) throws IOException {
        Properties newProperties = System.getProperties();

        InputStream inputStream = new FileInputStream(fileName);
        newProperties.load(inputStream);
        inputStream.close();

        System.setProperties(newProperties);
    }
}
