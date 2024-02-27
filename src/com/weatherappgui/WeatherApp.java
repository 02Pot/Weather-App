package com.weatherappgui;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class WeatherApp {
    
    //fetch data for given location
    public static JSONObject getWeatherData(String locationName) throws IOException, ParseException{

        //get location using geolocation API
        JSONArray locationData = getLocationData(locationName);
        if(locationData ==null || locationData.isEmpty()){
            System.out.println("Location data is empty or null");
            return null;
        }

        //Extract latitude and longitude data
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        //API request URL with location coordinates
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude="
        + latitude + "&longitude="  + longitude + "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Asia%2FTokyo";

        try {
            //call api and get response
            HttpURLConnection conn = fetchApiResponse(urlString);

            //check response status
            if(conn.getResponseCode() != 200){
                System.out.println("Error");
                return null;
            }else{
                //store result json data and store into stringbuilder
                StringBuilder resultJson = new StringBuilder();
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    while (scanner.hasNext()) {
                        resultJson.append(scanner.nextLine());
                    }
                    scanner.close();
                }finally{
                    conn.disconnect();
                }

                //parse through data
                JSONParser parser = new JSONParser();
                JSONObject resultJsonObj = (JSONObject) parser.parse(resultJson.toString());

                //get hourly data
                JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

                //get index of current hour
                JSONArray time = (JSONArray) hourly.get("time");
                int index = findIndexOfCurrentTime(time);

                //get temperature
                JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
                double temperature = (double) temperatureData.get(index);

                //get weather code
                JSONArray weathercode = (JSONArray) hourly.get("weather_code");
                String weatherCondition = converterWeatherCode((long) weathercode.get(index));

                //get humidity
                JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
                long humidity = (long) relativeHumidity.get(index);

                //get windspeed
                JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
                double windspeed = (double) windspeedData.get(index);

                HashMap<String,Object> weatherDataHash = new HashMap<String,Object>();
                weatherDataHash.put("temperature", temperature);
                weatherDataHash.put("weather_condition", weatherCondition);
                weatherDataHash.put("humidity", humidity);
                weatherDataHash.put("windspeed", windspeed);
                JSONObject weatherData = new JSONObject(weatherDataHash);


                return weatherData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //convert the api weathercode to a readable one
    private static String converterWeatherCode(long weathercode) {

        String weatherCondition = "";
        if(weathercode == 0L){
            weatherCondition = "Clear";
        }else if(weathercode > 0L && weathercode <= 3L){
            weatherCondition = "Cloudy";
        }else if(weathercode >= 51L && weathercode <= 67 
        || (weathercode >= 80L && weathercode <= 99L)){
            weatherCondition = "Rain";
        }else if(weathercode >= 71L && weathercode <= 77L){
            weatherCondition = "Snow";
        }else{
            return weatherCondition;
        }
        return weatherCondition;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();
        //iterate through time list and see which one matches current time and return the index
        for(int i = 0; i<timeList.size(); i++){
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime)){
                return i;
            }
        }
        return 0;
    }

    public static String getCurrentTime() {
        //get current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();

        //format date to be year/month/day
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        //format to current date and time
        String formattedDateTime = currentDateTime.format(formatter);
        return formattedDateTime;
    }

    //retrieves coordinates for given location name
    public static JSONArray getLocationData(String locationName) throws ParseException, IOException{
        //replace whitespace with + sign because of api request
        locationName = locationName.replaceAll(" ", "+");

        //Api location url with parameter
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" 
        + locationName + "&count=10&language=en&format=json";

        //Call API and get a response
        HttpURLConnection conn = fetchApiResponse(urlString);
         
        //Check response status
        if(conn.getResponseCode() != 200){
            System.out.println("Error: Couldnt connect to api");
            return null;
        }else{
            //store api result
            StringBuilder resultJson = new StringBuilder();
            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                //read and store json data to stringbuilder
                while (scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }
                scanner.close();
            }finally{
                conn.disconnect();
            }

            //Parse the string to JSON object
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(resultJson.toString());

            //get the list of location data the api generated from the location name
            JSONArray locationData = (JSONArray) resultJsonObj.get("results");
            return locationData;
        }
    }

    @SuppressWarnings("deprecation")
    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            //create connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();  

            //sent request to API
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept", "application/json");

            conn.connect();
            return conn;

        } catch (Exception e) {
            e.printStackTrace();
        }
        //cannot make connection
        return null;
    }

}
