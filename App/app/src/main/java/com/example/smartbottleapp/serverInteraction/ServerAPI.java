package com.example.smartbottleapp.serverInteraction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

import org.json.*;

public class ServerAPI {
    private static final String HOST = "https://yharon.pythonanywhere.com/";


    private static String get(String resource){
        try{
            URL url = new URL(HOST + resource);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Read the response from the server
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject createJsonFromReadingsList(List<Reading> readings) throws JSONException {
        JSONObject result = new JSONObject();
        JSONArray readingsArray = new JSONArray();

        for (Reading reading : readings) {
            JSONObject readingObject = new JSONObject();
            readingObject.put("bottle_id", reading.bottleId);
            readingObject.put("date_time", reading.datetime);
            readingObject.put("value", reading.value);

            readingsArray.put(readingObject);
        }

        result.put("readings", readingsArray);

        return result;
    }

    private static String post(String resource, String postData) {
        try {
            URL url = new URL(HOST + resource);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Write the POST data to the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
                os.write(postDataBytes);
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Read the response from the server
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Double getWaterDrankUser(int userId){
        String r = get("water_drank/" + userId);
        return Double.parseDouble(r);
    }

    public static List<Dispenser> getRecommendations(int userId) {
        try {
            String response = get("recommendations/" + userId);

            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray dispensersArray = jsonResponse.getJSONArray("dispensers");

            // Create a list to store Dispenser objects
            List<Dispenser> dispensers = new ArrayList<>();

            // Iterate over the JSON array and create Dispenser objects
            for (int i = 0; i < dispensersArray.length(); i++) {
                JSONObject dispenserJson = dispensersArray.getJSONObject(i);
                Dispenser dispenser = new Dispenser(
                        dispenserJson.getInt("id"),
                        dispenserJson.getString("name"),
                        dispenserJson.getString("location")
                );

                dispensers.add(dispenser);
            }

            // Return the list of Dispenser objects
            return dispensers;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isBusy(int dispenserId){
        String r = get("engagement/is_busy/" + dispenserId);
        if (r.equals("True"))
            return true;
        return false;
    }

    public static void addReadings(List<Reading> readings) {
        try {
            // Create JSON object from the readings list
            JSONObject jsonBody = createJsonFromReadingsList(readings);

            // Convert the JSON object to a string
            String postData = jsonBody.toString();

            // Perform the HTTP POST request
            post("readings", postData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerBottle(int bottleId, int userId, double capacity) throws JSONException {
        // Create JSON payload
        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("bottle_id", bottleId);
        jsonPayload.put("user_id", userId);
        jsonPayload.put("capacity", capacity);
        String jsonString = jsonPayload.toString();
        System.out.println(jsonPayload);
        post("bottles/register", jsonString);
    }
}
