package Crawler;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Wade on 9/24/16.
 */

public class Spider {

    private int MAX_PAGES_TO_CRAWL;
    private final String ER_URL = "http://finance.yahoo.com/calendar/earnings?day=";
    private List<String> formattedDateList = null;

    // Constructor
    public Spider (int DaysToCrawl) {

        this.formattedDateList = new ArrayList<>();
        this.MAX_PAGES_TO_CRAWL = DaysToCrawl;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();
        Date curDate = new Date();
        calendar.setTime(curDate);

        for (int i = 0; i < MAX_PAGES_TO_CRAWL; i++) {
            // First iteration, use the current date.
            // From the 2nd iter on, add 1 day per loop
            if(i != 0) {
                calendar.add(Calendar.DATE, 1);
            }
            Date iterDate = calendar.getTime();
            formattedDateList.add(formatter.format(iterDate));

        }


    }

    public Map<String, List<ERWrapper>> crawl(int withEPS) {

        Map<String, List<ERWrapper>> erDateMapper = new HashMap<>();

        for (String date : this.formattedDateList) {

            //String ER_FULL_URL = ER_URL + date + ".html";
            String ER_FULL_URL = ER_URL + date;
            System.out.println(ER_FULL_URL);
            SpiderLeg leg = new SpiderLeg();
            // Get the html Document first, then crawl.
            leg.getHtmlDocument(ER_FULL_URL, date);
            erDateMapper.put(date, leg.crawlYahooER(withEPS));
        }

        // Set Date Info in the ERWrapper
        String date = null;
        for (Map.Entry<String, List<ERWrapper>> en : erDateMapper.entrySet())
        {
            date = en.getKey();
            for (ERWrapper erWrapper : en.getValue())
            {
                erWrapper.setERDate(date);
            }
        }

        return erDateMapper;

    }

    public static Map<String, Object> crawlZacksRating(String url, String sym, String curPrice) {
        SpiderLeg sl = new SpiderLeg();
        sl.getHtmlDocument(url);

        return sl.getZacksRating(sym, curPrice);
    }

    public static Map<String, Object> crawlMarketWatchRating(String url, String sym, String curPrice) {
        SpiderLeg sl = new SpiderLeg();
        sl.getHtmlDocument(url);

        return sl.getMarketWatchRating(sym, curPrice);
    }

    public static List<String> crawlArkHoldings(String url, String indexName) {
        SpiderLeg sl = new SpiderLeg();
        sl.getHtmlDocument(url);

        return sl.getArkHoldings(indexName);
    }

}