package stockTwits;


import Utils.StockUtils;
import com.fasterxml.jackson.core.JsonParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by Wade on 10/4/16.
 */
public class StockTwitsReader {

    public static final String stockTwitsApi = "https://api.stocktwits.com/api/2/streams/symbol/";
    private static StockUtils stockUtils = new StockUtils();


    public JsonParser readFromStockTwits (String stockSymbol) {

        String urlToGetFrom = this.stockTwitsApi + stockSymbol + ".json";

        return stockUtils.urlStringToJsonParser(urlToGetFrom);
    }

    public Map<String, Object> rawTwitsToDimensional (Map<String, Object> inputMap, String twitSym, boolean includeMessage) {

        Map<String, Object> twitDimensionsMap = new LinkedHashMap<>();


        if (inputMap == null) {
            
            // shit hit the fence, prematurally return
            twitDimensionsMap.put("stockID", 0);
            twitDimensionsMap.put("stockSymbol", twitSym);
            twitDimensionsMap.put("bullPercent", 0.0);
            twitDimensionsMap.put("bearPercent", 0.0);

            return twitDimensionsMap;
        }

        Map<String,Integer> responseMap = (Map<String,Integer>) inputMap.get("response");
        
        if (!responseMap.containsKey("status") || responseMap.get("status") != 200) {
            
            // shit hit the fence, prematurally return
            twitDimensionsMap.put("stockID", 0);
            twitDimensionsMap.put("stockSymbol", twitSym);
            twitDimensionsMap.put("bullPercent", 0.0);
            twitDimensionsMap.put("bearPercent", 0.0);

            return twitDimensionsMap;
        }

        Map<String,Map> symbolMap = (Map<String,Map>) inputMap.get("symbol");
        twitDimensionsMap.put("stockID", symbolMap.get("id"));
        twitDimensionsMap.put("stockSymbol", symbolMap.get("symbol"));

        // counters for bull and bear sentiments
        int bullCounter = 0;
        int bearCounter = 0;

        ArrayList<Map> messageList = (ArrayList<Map>) inputMap.get("messages");
        for (Map<String, Object> msg : messageList) {
            int officialWeight = 1; // normal user = 1 official = 3
            if(msg.containsKey("body")) msg.remove("body");
            if(msg.containsKey("user")) {
                Map userMap = (Map) msg.get("user");
                if(userMap.containsKey("official")) {
                    if ((boolean) userMap.get("official")) officialWeight = 3;
                }
                if(userMap.containsKey("username")) userMap.remove("username");
                if(userMap.containsKey("name")) userMap.remove("name");
                if(userMap.containsKey("avatar_url")) userMap.remove("avatar_url");
                if(userMap.containsKey("avatar_url_ssl")) userMap.remove("avatar_url_ssl");
                if(userMap.containsKey("join_date")) userMap.remove("join_date");
                // if(userMap.containsKey("classification")) userMap.remove("classification");


                msg.put("user", userMap);
            }
            if(msg.containsKey("source")) msg.remove("source");
            if(msg.containsKey("symbols")) {
                ArrayList<Map> symbolsMap = (ArrayList<Map>) msg.get("symbols");
                for (Map<String, Object> mp : symbolsMap) {
                    if(mp.containsKey("title")) mp.remove("title");
                    if(mp.containsKey("is_following")) mp.remove("is_following");
                }
                msg.put("related_stocks", symbolsMap);
                msg.remove("symbols");
            }
            if(msg.containsKey("links")) msg.remove("links");
            if(msg.containsKey("reshares")) msg.remove("reshares");
            if(msg.containsKey("reshare_message")) msg.remove("reshare_message");
            if(msg.containsKey("conversation")) msg.remove("conversation");
            if(msg.containsKey("mentioned_users")) msg.remove("mentioned_users");
            msg.put("sentiment", "null"); // default to null
            if(msg.containsKey("entities")) {
                Map<String, Object> entitiesMap = (Map<String, Object>) msg.get("entities");
                if (entitiesMap.containsKey("sentiment")) msg.put("sentiment", entitiesMap.get("sentiment"));
                msg.remove("entities");
            }



            // count and compute bull/bear percentage
            //String sentimentStr = (String ) ((Map) msg.get("sentiment")).get("Basic");
            String sentimentStr = ((Map) msg.get("sentiment") != null) ? ((String ) ((Map) msg.get("sentiment")).get("basic")) : null;
            if(sentimentStr == null) continue;
            if(sentimentStr.compareTo("Bullish") == 0) {
                bullCounter += officialWeight;
            } else if (sentimentStr.compareTo("Bearish") == 0) {
                bearCounter += officialWeight;
            }
        }

        twitDimensionsMap.put("bullPercent", bullCounter*100.0/messageList.size());
        twitDimensionsMap.put("bearPercent", bearCounter*100.0/messageList.size());
        // if we need to include message, which is pretty heavy.
        if (includeMessage) twitDimensionsMap.put("messages", messageList);

        return twitDimensionsMap;
    }

}
