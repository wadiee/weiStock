package Meta;

import java.util.HashMap;

/**
 * Created by Wade on 7/19/16.
 */
public class GlobalDef {
    public static final int LOOKUP = 1;
    public static final int QUOTE = 2;

    public static final String zacksRatingUrl = "https://www.zacks.com/stock/quote/";
    public static final String zacksRankPrefix = "rankrect_";
    public static HashMap<String, String> zacksRankMap;
    static {
        zacksRankMap = new HashMap<>();
        zacksRankMap.put("1", "Buy");
        zacksRankMap.put("2", "Overweight");
        zacksRankMap.put("3", "Hold");
        zacksRankMap.put("4", "Underweight");
        zacksRankMap.put("5", "Sell");
    }

    public static final String marketWatchUrl = "http://www.marketwatch.com/investing/stock/";
    public static final String marketWatchUrlAnalystestimatesSuffix = "/analystestimates";
}
