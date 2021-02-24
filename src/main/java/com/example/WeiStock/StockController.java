package com.example.WeiStock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
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
import Meta.WeiIndexStock;
import Quote.QuoteStock;
import Utils.StockUtils;
import stockException.CustomGenericException;
import stockTwits.StockTwitsReader;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

@RestController
@Component
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
    public Map<String, Object> zacksRating(
        @RequestParam(value = "sym", defaultValue = "MSFT") String lookupSymbol,
        @RequestParam(value = "curPrice", defaultValue = "-1") String curPrice) {

        String zacksRatingUrl = GlobalDef.zacksRatingUrl.concat(lookupSymbol).concat(GlobalDef.brokerageRecSuffix);

        return Spider.crawlZacksRating(zacksRatingUrl, lookupSymbol, curPrice);
    }

    @RequestMapping(value = "/marketWatchRating", method = RequestMethod.GET)
    public Map<String, Object> marketWatchRating(
            @RequestParam(value = "sym", defaultValue = "MSFT") String lookupSymbol, 
            @RequestParam(value = "curPrice", defaultValue = "-1") String curPrice) {
        String marketWatchRatingUrl = GlobalDef.marketWatchUrl.concat(lookupSymbol)
                .concat(GlobalDef.marketWatchUrlAnalystestimatesSuffix);

        Map<String, Object> returnMap = Spider.crawlMarketWatchRating(marketWatchRatingUrl, lookupSymbol, curPrice);
        return returnMap;
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
            Map<String, Object> zacksRatingInfoMap = this.zacksRating(stockSym, qs.getLastPrice().toString());

            // Only add zacks rating info if it is not null
            if (zacksRatingInfoMap != null)
            {
                stockDim.putAll(zacksRatingInfoMap);
            }

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
            stockDim.put("ZacksRating Target Price", (Double.parseDouble((String)zacksRatingInfoMap.get("ZacksRating Target Price"))));
            stockDim.put("MarketWatch Target Price", (Double.parseDouble((String)marketWatchInfoMap.get("Market Watch Target Price"))));
            
            Double potentialIncreasePercent = null;
            if (stockDim.get("ZacksRating Target Price") != null)
            {
                potentialIncreasePercent = ((Double)stockDim.get("ZacksRating Target Price") - qs.getLastPrice().doubleValue())*100.0/qs.getLastPrice().doubleValue();
            }
            stockDim.put("To TP potential increase %", potentialIncreasePercent);
            stockDim.put("Today Increase %", qs.getChangePercent());

            // Creating wei Index for stock
            double weiScore = 0.0;
            weiScore += stockUtils.rankQuantifier((String) stockDim.get("ZacksRating Recommendation"));
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

    @RequestMapping(value = "/forceRefreshIndexTable", method = RequestMethod.GET)
    public String debug(@RequestParam(value="index", defaultValue="weiIndex") String indexName) {
        this.updateStockTable(indexName);
        return "Index " + indexName + " successfully updated in Funds table.";
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 2)
    public void updateFundDBWithStockHoldings() {

        updateStockTable(GlobalDef.weiIndexName);
        System.out.println(
            "Updated Funds table, index name: " + GlobalDef.weiIndexName + 
            " at " + Calendar.getInstance().getTime());

    }

    private void updateStockTable(String indexName) {
        List<String> stockList = new ArrayList<>();
        
        switch (indexName.toLowerCase()){
            case GlobalDef.weiIndexName:
                for (WeiIndexStock stock : WeiIndexStock.values()) {
                    stockList.add(stock.toString());
                }
                break;
            case GlobalDef.arkkIndexName:
                String arkkHoldingsUrl = GlobalDef.arkFundingUrl.concat(indexName)
                    .concat(GlobalDef.arkFundingUrlHoldingSuffix);

                stockList = Spider.crawlArkHoldings(arkkHoldingsUrl, indexName);
                break;
            default:
                break;
        }
    
        for (String st : stockList) this.sqldb.insertToStockTable(indexName, st);
    }

    public Map<String, Object> getStockTwitsHelper(String twitSym) {

        JsonParser jsonTwitsParser = twitsReader.readFromStockTwits(twitSym);
        if (jsonTwitsParser == null)
        {
            return null;
        } else {
            return stockUtils.jsonParserToMap(jsonTwitsParser);
        }

    }

    public Map<String, Object> getStockTwitsDimensionsHelper(String twitSym, String includeMessage) {

        boolean includeMessageBool = Integer.parseInt(includeMessage) == 1;
        Map<String, Object> rawTwitsJson  = this.getStockTwitsHelper(twitSym);

        return twitsReader.rawTwitsToDimensional(rawTwitsJson, twitSym, includeMessageBool);

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
