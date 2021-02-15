package com.example.WeiStock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import Crawler.ERWrapper;
import Crawler.Spider;
import Meta.GlobalDef;
import Quote.QuoteStock;
import Utils.ReadJsonFromFile;
import Utils.StockUtils;
import stockException.CustomGenericException;
import stockTwits.StockTwitsReader;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

@RestController
public class StockController {

    @Autowired
    private SQLDBCommunicator sqldb;

    private static StockUtils stockUtils = new StockUtils();
    private static StockTwitsReader twitsReader = new StockTwitsReader();

    private final String weiScore = "weiScore";

    private final int DaysToCrawl = 10;

    @GetMapping("/")
    public String home() {
        return "Welcomg to WeiStock";
    }

    @RequestMapping(value = "/apiDoc", method = RequestMethod.GET)
    public Map<String, String> apiDocumentation() {
        Map<String, String> apiDoc = new HashMap<>();
        apiDoc.put("/lookup",
                "Looks up the Stock basic info given stock symbol from markitondemand, MSFT as default stock symbol.");
        apiDoc.put("/quote", "Gets basic quote of the Stock from markitondemand, MSFT as default stock to quote.");
        apiDoc.put("/er",
                "Gets stocks with ER announcement within the next 10 days from Yahoo finance. Take in 0/1 withEPS argument.");
        apiDoc.put("/twits", "Gets raw info about a stock from stocktwits, MSFT as default stock.");
        apiDoc.put("/twitsdimension", "Gets Stocktwits dimension of a given stock, MSFT as default stock.");
        apiDoc.put("/zacksRating", "Gets Zacks Rating of a give stock symbol, MSFT as default stock.");
        apiDoc.put("/marketWatchRating", "Gets Market Watch Rating of a given stock symbol, MSFT as default stock.");
        apiDoc.put("/simpleRec", "Most used api, gives Recommendation of stocks with ER in comming 10 days.");
        apiDoc.put("/sendEmail", "Sends email.");
        apiDoc.put("/getERStocksForToday", "Gets the stock recommendation for today, from backend DB.");
        apiDoc.put("/recordERStocksForToday", "Records the stock recommendations of today, to backend DB.");

        return apiDoc;
    }

    @RequestMapping(value = "/quote", method = RequestMethod.GET)
    public QuoteStock quote(@RequestParam(value = "sym", defaultValue = "MSFT") String lookupSymbol) throws Exception {

        Stock stock = YahooFinance.get(lookupSymbol);
        QuoteStock qs = new QuoteStock(stock);
        return qs;
    }

    @RequestMapping(value = "/er", method = RequestMethod.GET)
    public Map<String, List<ERWrapper>> obtainER(@RequestParam(value = "withEPS", defaultValue = "0") String withEPS)
            throws Exception {

        Spider mySpider = new Spider(this.DaysToCrawl);
        return mySpider.crawl(Integer.parseInt(withEPS));
    }

    @RequestMapping(value = "/twits", method = RequestMethod.GET)
    public Map<String, Object> getStockTwits(@RequestParam(value = "sym", defaultValue = "MSFT") String twitSym) {

        return getStockTwitsHelper(twitSym);
    }

    @RequestMapping(value = "/twitsdimension", method = RequestMethod.GET)
    public Map<String, Object> getStockTwitsDimensions(
            @RequestParam(value = "sym", defaultValue = "MSFT") String twitSym,
            @RequestParam(value = "includeMessage", defaultValue = "1") String includeMessage) {

        return this.getStockTwitsDimensionsHelper(twitSym, includeMessage);
    }

    @RequestMapping(value = "/zacksRating", method = RequestMethod.GET)
    public String zacksRating(@RequestParam(value = "sym", defaultValue = "MSFT") String lookupSymbol) {
        String zacksRatingUrl = GlobalDef.zacksRatingUrl.concat(lookupSymbol);

        return Spider.crawlZacksRating(zacksRatingUrl, lookupSymbol);
    }

    @RequestMapping(value = "/marketWatchRating", method = RequestMethod.GET)
    public Map<String, Object> marketWatchRating(
            @RequestParam(value = "sym", defaultValue = "MSFT") String lookupSymbol, String curPrice) {
        String marketWatchRatingUrl = GlobalDef.marketWatchUrl.concat(lookupSymbol)
                .concat(GlobalDef.marketWatchUrlAnalystestimatesSuffix);

        Map<String, Object> returnMap = Spider.crawlMarketWatchRating(marketWatchRatingUrl, lookupSymbol, curPrice);
        return returnMap;
    }

    @RequestMapping(value = "/simpleRec", method = RequestMethod.GET)
    public List<Map<String, Object>> simpleRecommendations(
            @RequestParam(value = "withEPS", defaultValue = "1") String withEPS) {
        List<Map<String, Object>> recList = new ArrayList<Map<String, Object>>();

        try {
            Map<String, List<ERWrapper>> ERList = this.obtainER(withEPS);
            for (List<ERWrapper> l : ERList.values()) {
                if (l == null)
                    continue;
                for (ERWrapper erWrapper : l) {
                    String stockSym = erWrapper.getSymbol();
                    // Do not include StockTwits Message
                    Thread.sleep(1000);
                    Map<String, Object> stockDim = getStockTwitsDimensions(stockSym, "0");
                    if (stockDim == null) {
                        continue;
                    } else {
                        // Adding other attributes to this stock recommendation system.

                        // Adding EPS
                        stockDim.put("EPS", erWrapper.getEPSEstimated());

                        // Adding today's date as prediction date
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        stockDim.put("Prediction Date", formatter.format(new Date()));

                        // Adding ERDate
                        stockDim.put("Earning Date", erWrapper.getERDate());

                        // Adding Zacks Rating
                        stockDim.put("ZackRank", this.zacksRating(stockSym));
                        // Adding MarketWatch Info
                        Map<String, Object> marketWatchInfoMap = this.marketWatchRating(stockSym);

                        // Only add marketwatch info if it is not null
                        if (marketWatchInfoMap != null) {
                            stockDim.putAll(marketWatchInfoMap);
                        }

                        // Adding Stock Quote, Volume, Change percent, Target-to-Price increase %
                        QuoteStock tempQuote = this.quote(stockSym);

                        stockDim.put("Volume Today", tempQuote.getVolume());
                        stockDim.put("Price Now", tempQuote.getLastPrice());
                        Double potentialIncreasePercent = null;
                        if (marketWatchInfoMap != null && marketWatchInfoMap.get("Market Watch Target Price") != null) {
                            potentialIncreasePercent = (Double
                                    .parseDouble((String) marketWatchInfoMap.get("Market Watch Target Price"))
                                    - tempQuote.getLastPrice().doubleValue()) * 100.0
                                    / tempQuote.getLastPrice().doubleValue();
                        }
                        stockDim.put("To TP potential increase %", potentialIncreasePercent);
                        stockDim.put("Today Increase %", tempQuote.getChangePercent());

                        // Creating wei Score for stock
                        double weiScore = 0.0;
                        weiScore += stockUtils.rankQuantifier((String) stockDim.get("ZackRank"));
                        weiScore += stockUtils.rankQuantifier(marketWatchInfoMap == null ? null
                                : (String) marketWatchInfoMap.get("Market Watch Recommendation"));
                        weiScore += stockDim.get("To TP potential increase %") == null ? 0.0
                                : Double.parseDouble(stockDim.get("To TP potential increase %").toString());
                        weiScore += Double.parseDouble(stockDim.get("bullPercent").toString())
                                - Double.parseDouble(stockDim.get("bearPercent").toString());

                        stockDim.put(this.weiScore, weiScore);

                        recList.add(stockDim);
                    }
                }
            }

            recList.sort((o1, o2) -> {
                double valToCompare = Double.parseDouble(o1.get(this.weiScore).toString())
                        - Double.parseDouble(o2.get(this.weiScore).toString());
                if (valToCompare > 0) {
                    return -1;
                } else if (valToCompare < 0) {
                    return 1;
                } else {
                    return 0;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return recList;
    }

    // @RequestMapping(value = "/sendemail", method = RequestMethod.GET)
    // public void sendEmail(@RequestParam String fromEmail, @RequestParam String
    // toEmail) {
    // try {
    // Gmail gmail = GmailClass.getGmailService();
    // MimeMessage emailToSend = gmailClass.createEmail(toEmail, fromEmail,
    // "testyo", "hey");
    // gmailClass.sendMessage(gmail, "wei.zeng1993@gmail.com", emailToSend);

    // System.out.println("/sendemail sent from userID wei.zeng1993 with message: "
    // + emailToSend.toString() + "\n");
    // } catch (MessagingException | IOException e) {
    // e.printStackTrace();
    // }
    // }

    @RequestMapping(value = "/evalStock", method = RequestMethod.GET)
    public Map<String, Object> evalStock(@RequestParam(value = "sym", defaultValue = "MSFT") String stockSym) {

        Map<String, Object> stockDim = getStockTwitsDimensions(stockSym, "0");

        QuoteStock qs = QuoteStock.EmptyInitializer();
        try {
            qs = this.quote(stockSym);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (stockDim != null)
        {
            // Adding other attributes to this stock recommendation system.

            // Adding EPS
            stockDim.put("EPS", qs.getEpsEstimatedCurrentYearalYield());

            // Adding today's date as prediction date
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            stockDim.put("Prediction Date", formatter.format(new Date()));

            // Adding Zacks Rating
            stockDim.put("ZackRank", this.zacksRating(stockSym));
            // Adding MarketWatch Info
            Map<String, Object> marketWatchInfoMap = this.marketWatchRating(stockSym, qs.getLastPrice().toString());

            // Only add marketwatch info if it is not null
            if (marketWatchInfoMap != null)
            {
                stockDim.putAll(marketWatchInfoMap);
            }

            // Adding Stock Quote, Volume, Change percent, Target-to-Price increase %

            stockDim.put("Volume Today", qs.getVolume());
            stockDim.put("Price Now", qs.getLastPrice());
            stockDim.put("Target Price", (Double.parseDouble((String)marketWatchInfoMap.get("Market Watch Target Price"))));
            
            Double potentialIncreasePercent = null;
            if (marketWatchInfoMap.get("Market Watch Target Price") != null)
            {
                potentialIncreasePercent = (Double.parseDouble((String)marketWatchInfoMap.get("Market Watch Target Price")) - qs.getLastPrice().doubleValue())*100.0/qs.getLastPrice().doubleValue();
            }
            stockDim.put("To TP potential increase %", potentialIncreasePercent);
            stockDim.put("Today Increase %", qs.getChangePercent());

            // Creating wei Index for stock
            double weiScore = 0.0;
            weiScore += stockUtils.rankQuantifier((String) stockDim.get("ZackRank"));
            weiScore += stockUtils.rankQuantifier((String) marketWatchInfoMap.get("Market Watch Recommendation"));
            weiScore += stockDim.get("To TP potential increase %") == null ? 0.0 : Double.parseDouble(stockDim.get("To TP potential increase %").toString());
            double stockTwitsSentiment = Double.parseDouble(stockDim.get("bullPercent").toString()) - Double.parseDouble(stockDim.get("bearPercent").toString());
            weiScore += stockTwitsSentiment/10.0;

            stockDim.put(this.weiScore, weiScore);
        }
        return stockDim;
    }

    @RequestMapping(value = "/eval", method = RequestMethod.GET)
    public List<Map<String,Object>> evalIndex(@RequestParam(value="index", defaultValue="weiIndex") String indexName) {
        List<String> stockList = this.sqldb.getStocksToEval(indexName); 
        List<Map<String, Object>> infolist = new ArrayList();
        
        stockList.forEach(stock -> infolist.add(this.evalStock(stock)));

        infolist.sort((o1, o2) -> {
            double valToCompare = Double.parseDouble(o1.get(this.weiScore).toString())
                    - Double.parseDouble(o2.get(this.weiScore).toString());
            if (valToCompare > 0) {
                return -1;
            } else if (valToCompare < 0) {
                return 1;
            } else {
                return 0;
            }
        });

        return infolist;
    }

    @RequestMapping(value = "/debug", method = RequestMethod.GET)
    public String debug(@RequestParam(value="index", defaultValue="weiIndex") String indexName) {

        return this.sqldb.getStocksToEval("weiIndex").get(1);
    }

    @RequestMapping(value = "/recordERStockForToday", method = RequestMethod.GET)
    public void recordERStockForToday(@RequestParam(value="withEPS", defaultValue="1") String withEPS,
                                      @RequestParam(value="topN", defaultValue="5") String topN) {

        List<Map<String, Object>> recommendations = simpleRecommendations(withEPS);

        System.out.println("how many stock in rec? : " + recommendations.size());

        int topNInt = Integer.parseInt(topN);

        List<Map<String, Object>> filteredRecommendations = recommendations
                .stream()
                .filter(rec -> rec.get(this.weiScore) != "Infinity")
                .limit(topNInt)
                .collect(Collectors.toList());

        System.out.println("how many stock in insert? : " + filteredRecommendations.size());

        // mongoConnection.insertIntoERStocksCollection(filteredRecommendations);

    }

    @RequestMapping(value = "/getERStocksForToday", method = RequestMethod.GET)
    public void getERStockForToday(@RequestParam(value="date") String date) {

        // mongoConnection.readFromERStocksCollection();

    }

    public Map<String, Object> getStockTwitsHelper(String twitSym) {

        JsonParser jsonTwitsParser = twitsReader.readFromStockTwits(twitSym);

        return stockUtils.jsonParserToMap(jsonTwitsParser);

    }

    public Map<String, Object> getStockTwitsDimensionsHelper(String twitSym, String includeMessage) {

        boolean includeMessageBool = Integer.parseInt(includeMessage) == 1;
        Map<String, Object> rawTwitsJson  = this.getStockTwitsHelper(twitSym);

        try {
            return twitsReader.rawTwitsToDimensional(rawTwitsJson, includeMessageBool);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("/twitsDimensions had problem forming dimensions, returned raw JSON data \n");
        return rawTwitsJson;
    }

    @ExceptionHandler(CustomGenericException.class)
    public ModelAndView handleCustomException(CustomGenericException ex) {

        ModelAndView model = new ModelAndView("error/generic_error");
        model.addObject("exception", ex);
        return model;

    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllException(Exception ex) {

        ModelAndView model = new ModelAndView("error/exception_error");
        return model;

    }
}
