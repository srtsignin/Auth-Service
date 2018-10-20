package cardfire;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exceptions.InvalidTokenException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import models.User;
import org.apache.log4j.Logger;
import service.App;

public class CardfireVerifier {

    private static final String CARDFIRE_URL = System.getProperty("cardfire.url").replace("\"", "");
    private static final Logger LOGGER = Logger.getLogger(App.class);

    public User verifyCardfireToken(String cardfireToken) {
        try{
            String response = getResponse(cardfireToken);
            return createUserFromResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e);
            throw new InvalidTokenException("Unable to verify Cardfire Token");
        }
    }

    private String getResponse(String cardfireToken) {
        try {
            URL url = new URL(CARDFIRE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("CardfireToken", cardfireToken);
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while((inputLine = inputReader.readLine()) != null) {
                content.append(inputLine);
            }
            inputReader.close();
            connection.disconnect();
            LOGGER.debug(content);
            return content.toString();
        } catch (Exception e) {
            LOGGER.error(e);
            throw new RuntimeException("Unable to connect");
        }
    }

    private User createUserFromResponse(String response) {
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(response);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonElement userElement = jsonObject.get("user");

        return new Gson().fromJson(userElement.toString(), User.class);
    }

}
