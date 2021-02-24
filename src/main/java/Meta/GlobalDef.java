package Meta;

import java.util.HashMap;

/**
 * Created by Wade on 7/19/16.
 */
public class GlobalDef {

    public static final String zacksRatingUrl = "https://www.zacks.com/stock/research/";
    public static final String brokerageRecSuffix = "/brokerage-recommendations";
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
    public static final String arkFundingUrl = "https://ark-funds.com/";
    public static final String arkFundingUrlHoldingSuffix = "#holdings";

    public static final String weiIndexName = "weiindex";
    public static final String arkkIndexName = "arkk";
    public static final String arkwIndexName = "arkw";
    public static final String nasdaqIndexName = "nasdaq";
}
