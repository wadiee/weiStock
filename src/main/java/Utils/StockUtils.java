package Utils;

import Meta.GlobalDef;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Created by Wade on 7/19/16.
 */
public class StockUtils {

    public static final JsonFactory jsonFactory = new JsonFactory();

    public String formatGetReq(int get_type, String sym, String baseStr) {

        String req_url = baseStr;

        switch (get_type) {
            case GlobalDef.LOOKUP:
                req_url = req_url.concat("Lookup/");
                break;
            case GlobalDef.QUOTE:
                req_url = req_url.concat("Quote/");
                break;
            default:
                req_url = req_url.concat("Lookup/");
                break;
        }

        req_url = req_url.concat("jsonp?");

        switch (get_type) {
            case GlobalDef.LOOKUP:
                req_url = req_url.concat("input=" + sym);
                break;
            case GlobalDef.QUOTE:
                req_url = req_url.concat("symbol=" + sym);
                break;
            default:
                req_url = req_url.concat("input=" + sym);
                break;
        }

        return req_url;
    }

    public String urlStringToresponseStr(String input_url) throws Exception {
        URL obj = new URL(input_url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            String responseString = response.toString();
            System.out.println(responseString);

            return responseString;
        } else {
            throw new Exception("GET request did not work in openConnectionAndGet\n");
        }
    }

    public JsonParser urlStringToJsonParser(String input_url) {

        try {
            JsonParser jsonParser = jsonFactory.createParser(new URL(input_url));
            return jsonParser;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Object> jsonParserToMap(JsonParser inputParser) {

        Map<String, Object> MapToReturn = null;
        try {
            MapToReturn = new ObjectMapper().readValue(inputParser, new TypeReference<Map<String, Object>>(){});
        } catch (IOException e) {
            return null;
        }
        return MapToReturn;
    }

    public double rankQuantifier(String input) {
        if (input == null) return 0.0;
        switch (input) {
            case "Sell":
                return -20.0;
            case "Underweight":
                return -10.0;
            case "Hold":
                return 0.0;
            case "Overweight":
                return 10.0;
            case "Buy":
                return 20.0;
            default:
                return 0.0;
        }
    }
}
