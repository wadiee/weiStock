package Crawler;

/**
 * Created by Wade on 9/24/16.
 */

import Meta.GlobalDef;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpiderLeg
{
    // We'll use a fake USER_AGENT so the web server thinks the robot is a normal web browser.
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private Document htmlDocument;

    private String date;
    private static String Url;
    private static int withEPS;

    /**
     * This performs all the work. It makes an HTTP request, checks the response, and then gathers
     * up all the links on the page. Perform a searchForWord after the successful crawl
     *
     * @param url
     *            - The URL to visit
     * @return whether or not the crawl was successful
     */
    public boolean getHtmlDocument(String url, String date)
    {
        this.date = date; // Set the date for this spider leg
        return getHtmlDocument(url);
    }

    /**
     * This performs all the work. It makes an HTTP request, checks the response, and then gathers
     * up all the links on the page. Perform a searchForWord after the successful crawl
     *
     * @param url
     *            - The URL to visit
     * @return whether or not the crawl was successful
     */
    public boolean getHtmlDocument(String url)
    {
        int getHtmlRetryTime = 3;
        try
        {
            this.Url = url; // Set the url for this spider leg
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            Document htmlDocument = connection.get();

            while(getHtmlRetryTime > 0) {
                if (htmlDocument == null) {
                    getHtmlRetryTime--;
                    Thread.sleep(3000);
                    htmlDocument = connection.get();
                } else {
                    break;
                }
            }

            this.htmlDocument = htmlDocument;
            if(connection.response().statusCode() == 200) // 200 is the HTTP OK status code
            // indicating that everything is great.
            {
                System.out.println("\n**Visiting** Received web page at " + url);
            }
            if(!connection.response().contentType().contains("text/html"))
            {
                System.out.println("**Failure** Retrieved something other than HTML");
                return false;
            }
            return true;
        }
        catch(Exception ioe)
        {
            // We were not successful in our HTTP request
            return false;
        }
    }

    public Map<String, Object> getZacksRating(String sym, String curPrice) {

        // Defensive coding. This method should only be used after a successful crawl.
        if(this.htmlDocument == null)
        {
            System.out.println("ERROR! Call getHtmlDocument() before performing analysis on the document");
            return null;
        }

        String bodyText = this.htmlDocument.body().text();

        return scanContentForZacksRating(bodyText, sym, curPrice);
    }

    public Map<String, Object> getMarketWatchRating(String sym, String curPrice) {

        // Defensive coding. This method should only be used after a successful crawl.
        if(this.htmlDocument == null)
        {
            System.out.println("ERROR! Call getHtmlDocument() before performing analysis on the document");
            return null;
        }

        String bodyText = this.htmlDocument.body().text();

        return scanContentForMarketWatch(bodyText, sym, curPrice);
    }

    public List<String> getArkHoldings(String indexName) {

        if (this.htmlDocument == null)
        {
            System.out.println("ERROR! Call getHtmlDocument() before performing analysis on the document");
            return null;
        }

        String bodyText = this.htmlDocument.body().text();

        System.out.println(bodyText);

        return null;
    }

    public List<ERWrapper> crawlYahooER(int withEPS) {

        this.withEPS = withEPS;

        // Defensive coding. This method should only be used after a successful crawl.
        if(this.htmlDocument == null)
        {
            System.out.println("ERROR! Call getHtmlDocument() before performing analysis on the document");
            return null;
        }

        String bodyText = this.htmlDocument.body().text();

        //System.out.println(bodyText);

        List<ERWrapper> erList = scanContent(bodyText);
        System.out.println("finished with scanContent");

        // Getting only stocks with EPS
        if (withEPS != ERWrapper.withNoEPS && erList != null) {
            if (withEPS == ERWrapper.withPositiveEPS) {
                erList = erList.stream()
                        .filter(w -> w.isHasEPS() && w.getEPSEstimated() >= 0.0)
                        .collect(Collectors.toList());
            } else {
                erList = erList.stream()
                        .filter(w -> w.isHasEPS() && w.getEPSEstimated() < 0.0)
                        .collect(Collectors.toList());
            }
        }
        // If we reach here, we return the stocks with or without EPS, or null stock list
        return erList;
    }

    // Input: yahoo biz content,
    // Output: list of ER info for US stocks.
    private List<ERWrapper> scanContent (String content) {

        String crawlFromHere = "Symbol Company Earnings Call Time EPS Estimate Reported EPS Surprise(%) ";
        // This means it is weekends, there is no stock ER info
        if (content.lastIndexOf(crawlFromHere) < crawlFromHere.length()) return new ArrayList<>();

        String numOfStockExpression = "[0-9]-[0-9]* of [0-9]*";

        Pattern numStockPattern = Pattern.compile(numOfStockExpression);
        Matcher numStockMatcher = numStockPattern.matcher(content);
        if (!numStockMatcher.find()) {
            return null; // Did not find number of stocks. Probably Yahoo has changed its website html
        }

        String numOfStockRawString = numStockMatcher.group().trim();
        int numStock = Integer.parseInt(numOfStockRawString.substring(numOfStockRawString.lastIndexOf(" ") + 1));
        int endNumOfStockThisPage = Integer.parseInt(numOfStockRawString.substring(numOfStockRawString.indexOf("-") + 1, numOfStockRawString.indexOf(" ")));
        System.out.println("num of stock: " + numStock + " end num stock this page: " + endNumOfStockThisPage);
        if (numStock <= 0) {
            return null; // No stock for today.
        }

        List<ERWrapper> ERWrappers = new ArrayList<>();

        // Recursion to get all ERs in the offset pages.
        if (endNumOfStockThisPage < numStock) {
            SpiderLeg leg = new SpiderLeg();
            // Get the html Document first, then crawl.
            String newUrlWithOffSet = this.Url.substring(0, this.Url.indexOf(this.date) + (this.date.length()));
            newUrlWithOffSet += ("&offset=" + endNumOfStockThisPage + "&size=100");
            leg.getHtmlDocument(newUrlWithOffSet, this.date);
            // Recursively add sub-pages
            ERWrappers.addAll(leg.crawlYahooER(this.withEPS));

        }

        if (content.contains("We’re sorry, we weren’t able to find any data."))
        {
            // this page is empty, return empty list as result of this page.
            System.out.println("Found an empty page, returning empty list");
            return ERWrappers;
        }

        String prefixRemoved = content.substring(content.lastIndexOf(crawlFromHere) + crawlFromHere.length());
        // String postFixRemoved = StringUtils.substringBefore(prefixRemoved, " *Analyst opinion data");

        String symPattern = "([a-zA-Z0-9]|\\.)*";
        String EPSPattern = "(\\- |[\\-]?[0-9]*(\\.[0-9]+)? )";
        String EPS3Patterns = "(\\- |[\\-]?[0-9]*(\\.[0-9]+)? ){3}";
        String callTimePattern = "(Time Not Supplied |Before Market Open |After Market Close |[0-9]*:[0-9]{2}(A|P)M EST )";

        Pattern ERPattern = Pattern.compile(callTimePattern + EPS3Patterns);
        Matcher ERMatcher = ERPattern.matcher(prefixRemoved);

        int count = 0;

        int ERStringStart = 0;
        int ERStringEnd = 0;
        int ERStringEPSStart = -1;

        while (ERMatcher.find()) {
            count++;

            ERStringStart = ERStringEnd;
            ERStringEPSStart = ERMatcher.start();
            ERStringEnd = ERMatcher.end();

            String ERMatchString = prefixRemoved.substring(ERStringStart, ERStringEnd);

            Matcher tempMatcher = Pattern.compile(symPattern).matcher(ERMatchString);
            tempMatcher.find();
            String symbol = tempMatcher.group().trim();

            if (symbol.contains(".")) {
                // Skip this stock ER info, because it is not an US stock.
                continue;
            }

            // start index of callTime EPSEstimated EPSReported EPSsurprise
            int relativeStartOfERStringEPSStart = ERStringEPSStart - ERStringStart;

            String companyName = ERMatchString.substring(ERMatchString.indexOf(" ") + 1, relativeStartOfERStringEPSStart).trim();

            tempMatcher = Pattern.compile(callTimePattern).matcher(ERMatchString.substring(relativeStartOfERStringEPSStart));
            tempMatcher.find();
            String callTime = tempMatcher.group(1).trim();

            int relativeStart3EPS = relativeStartOfERStringEPSStart + callTime.length() + 1;

            tempMatcher = Pattern.compile(EPSPattern).matcher(ERMatchString.substring(relativeStart3EPS));
            tempMatcher.find();
            String EPSEstimated = tempMatcher.group().trim();
            tempMatcher.find();
            String EPSReported = tempMatcher.group().trim();
            tempMatcher.find();
            String EPSsurprise = tempMatcher.group().trim();


            ERWrapper newWrapper = new ERWrapper(symbol, companyName, EPSEstimated, EPSReported, EPSsurprise, callTime);
            ERWrappers.add(newWrapper);

        }

        if (count == 0) {
            System.out.println("This is not possible: num of stock is" + numStock + ", but parser got zero ER info.");
            return null; // Did not find number of stocks. Probably Yahoo has changed its website html
        }

        int stockNumOnThisPage = numStock == endNumOfStockThisPage ? numStock % 100 : 100;
        if (count != stockNumOnThisPage) {
            System.err.println("Number of Stock read differs from stockNumOnThisPage, Number is " + count + " stockNumOnThisPage is " + stockNumOnThisPage);
        }

        return ERWrappers;

    }

    private Map<String, Object> scanContentForZacksRating (String content, String sym, String curPrice) {


        String ratingsExpression = "1-Strong Buy|2-Buy|3-Hold|4-Sell|5-Strong Sell";
        String zacksRating;

        Pattern ratingsPattern = Pattern.compile(ratingsExpression);
        Matcher ratingsMatcher = ratingsPattern.matcher(content);
        if (!ratingsMatcher.find()) {
            zacksRating = "Hold"; // If we don't find zacksRating rec, then just say Hold
        } else {
            String rank = ratingsMatcher.group().split("-")[0];
            zacksRating = GlobalDef.zacksRankMap.get(rank);
        }
        
        // ****
        // Get Target Price from ZackRatings
        String targetPriceString = "Average Target Price";
        String targetPrice;
        int targetPriceIdentifierIndex = content.lastIndexOf(targetPriceString);
        if (targetPriceIdentifierIndex < 0) {
            // Did not find Average target price
            targetPrice = null;
        } else {
            String targetPriceExpression = "\\d+\\.\\d+";

            Pattern targetPricePattern = Pattern.compile(targetPriceExpression);
            Matcher targetPriceMatcher = targetPricePattern.matcher(content.substring(targetPriceIdentifierIndex + targetPriceString.length()));
            if (!targetPriceMatcher.find()) {
                targetPrice = null; // Did not find any target price.
            } else {
                targetPrice = targetPriceMatcher.group().trim();
            }
        }
        // ***

        Map<String, Object> res =  new HashMap<String, Object>();
        res.put("ZacksRating Recommendation", zacksRating == null ? "Hold" : zacksRating);
        res.put("ZacksRating Target Price", targetPrice == null ? curPrice : targetPrice);

        return res;
    }

    // Input: Market Watch Stock page, Symbol of that stock
    // Output: Map of Market Watch rating for the stock, and Target Price of the stock.
    private Map<String, Object> scanContentForMarketWatch (String content, String sym, String curPrice) {

        // 3 data points from market Watch.
        String marketWatchRecommendation;
        String targetPrice;
        String numRating;

        // ****
        // Get Recommendation Ranking from Market Watch
        String recommendationString = "Average Recommendation";

        int recommendationIdentifierIndex = content.lastIndexOf(recommendationString);
        if (recommendationIdentifierIndex < 0) {
            // Did not find Average Recommendation
            marketWatchRecommendation = null;
        } else {
            // Found Average Recommendation
            String ratingsExpression = "\\b([A-Z]\\w*)\\b";

            Pattern ratingsPattern = Pattern.compile(ratingsExpression);
            Matcher ratingsMatcher = ratingsPattern.matcher(content.substring(recommendationIdentifierIndex + recommendationString.length()));
            if (!ratingsMatcher.find()) {
                marketWatchRecommendation = null; // Did not find any recommendation.
            } else {
                marketWatchRecommendation = ratingsMatcher.group().trim();
            }
        }
        // ****

        // ****
        // Get Target Price from Market Watch
        String targetPriceString = "Average Target Price";

        int targetPriceIdentifierIndex = content.lastIndexOf(targetPriceString);
        if (targetPriceIdentifierIndex < 0) {
            // Did not find Average target price
            targetPrice = null;
        } else {
            String targetPriceExpression = "\\d+\\.\\d+";

            Pattern targetPricePattern = Pattern.compile(targetPriceExpression);
            Matcher targetPriceMatcher = targetPricePattern.matcher(content.substring(targetPriceIdentifierIndex + targetPriceString.length()));
            if (!targetPriceMatcher.find()) {
                targetPrice = null; // Did not find any target price.
            } else {
                targetPrice = targetPriceMatcher.group().trim();
            }
        }
        // ***

        // ****
        // Get Number of Ratings from Market Watch
        String numRatingString = "Number Of Ratings";

        int numRatingIdentifierIndex = content.lastIndexOf(numRatingString);
        if (numRatingIdentifierIndex < 0) {
            // Did not find number of ratings
            numRating = null;
        } else {
            String numRatingExpression = "\\d+";

            Pattern numRatingPattern = Pattern.compile(numRatingExpression);
            Matcher numRatingMatcher = numRatingPattern.matcher(content.substring(numRatingIdentifierIndex + numRatingString.length()));
            if (!numRatingMatcher.find()) {
                numRating = null; // Did not find any number of ratings.
            } else {
                numRating = numRatingMatcher.group().trim();
            }
        }
        // ***


        Map<String, Object> res =  new HashMap<String, Object>();
        res.put("Market Watch Recommendation", marketWatchRecommendation == null ? "Hold" : marketWatchRecommendation);
        res.put("Market Watch Target Price", targetPrice == null ? curPrice : targetPrice);
        res.put("Market Watch Number of Ratings", numRating == null ? "0" : numRating);

        return res;
    }

}