package Quote;

import java.math.BigDecimal;

import yahoofinance.Stock;
import yahoofinance.quotes.stock.StockQuote;
import yahoofinance.quotes.stock.StockStats;

/**
 * Created by Wade on 7/18/16.
 */
public class QuoteStock {
    private final String name;
    private final String symbol;
    private final BigDecimal lastPrice;
    private final BigDecimal change;
    private final BigDecimal changePercent;
    private final BigDecimal annualYield;
    private final BigDecimal annualYieldPercent;
    private final BigDecimal priceAvg50;
    private final BigDecimal priceAvg200;
    private final BigDecimal changeFromAvg200InPercent;
    private final BigDecimal changeFromAvg50InPercent;
    private final long volume;
    private final BigDecimal eps;
    private final BigDecimal epsEstimatedCurrentYear;
    private final BigDecimal pe;
    private final BigDecimal marketCap;

    public static QuoteStock EmptyInitializer() {
        return new QuoteStock(null);
    }

    public QuoteStock(Stock stock) {

        if (stock == null) {
            this.name = null;
            this.symbol = null;
            this.lastPrice = BigDecimal.ZERO;
            this.change = BigDecimal.ZERO;
            this.changePercent = BigDecimal.ZERO;
            this.annualYield = BigDecimal.ZERO;
            this.annualYieldPercent = BigDecimal.ZERO;
            this.volume = 0L;
            this.priceAvg50 = BigDecimal.ZERO;
            this.priceAvg200 = BigDecimal.ZERO;
            this.changeFromAvg200InPercent = BigDecimal.ZERO;
            this.changeFromAvg50InPercent = BigDecimal.ZERO;
            this.eps = BigDecimal.ZERO;
            this.epsEstimatedCurrentYear = BigDecimal.ZERO;
            this.pe = BigDecimal.ZERO;
            this.marketCap = BigDecimal.ZERO;
        } else {
            StockQuote sq = stock.getQuote();
            StockStats ss = stock.getStats();
            this.name = stock.getName();
            this.symbol = stock.getSymbol();
            this.lastPrice = sq.getPrice();
            this.change = sq.getChange();
            this.changePercent = sq.getChangeInPercent();
            this.annualYield = stock.getDividend().getAnnualYield();
            this.annualYieldPercent = stock.getDividend().getAnnualYieldPercent();
            this.volume = sq.getVolume();
            this.priceAvg50 = sq.getPriceAvg50();
            this.priceAvg200 = sq.getPriceAvg200();
            this.changeFromAvg200InPercent = sq.getChangeFromAvg200InPercent();
            this.changeFromAvg50InPercent = sq.getChangeFromAvg50InPercent();
            this.eps = ss.getEps();
            this.epsEstimatedCurrentYear = ss.getEpsEstimateCurrentYear();
            this.pe = ss.getPe();
            this.marketCap = ss.getMarketCap();
        }
    }

    public BigDecimal getEps() {
        return eps;
    }
    
    public BigDecimal getEpsEstimatedCurrentYearalYield() {
        return epsEstimatedCurrentYear;
    }

    public BigDecimal getPe() {
        return pe;
    }

    public BigDecimal getAnnualYield() {
        return annualYield;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public BigDecimal getAnnualYieldPercent() {
        return annualYieldPercent;
    }

    public BigDecimal getPriceAvg50() {
        return priceAvg50;
    }

    public BigDecimal getPriceAvg200() {
        return priceAvg200;
    }

    public BigDecimal getChangeFromAvg200InPercent() {
        return changeFromAvg200InPercent;
    }
    public BigDecimal getChangeFromAvg50InPercent() {
        return changeFromAvg50InPercent;
    }

    public BigDecimal getChange() {
        return change;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }

    public long getVolume() {
        return volume;
    }

}
