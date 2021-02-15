package Utils;

import Lookup.LookupStock;
import Quote.QuoteStock;

import java.util.ArrayList;

/**
 * Created by Wade on 7/19/16.
 */
public class WrapperReturnVal {

    private final ArrayList<LookupStock> lookupStocks;
    private final ArrayList<QuoteStock> quoteStocks;

    public WrapperReturnVal(ArrayList<LookupStock> lookupStocks, ArrayList<QuoteStock> quoteStocks) {
        this.lookupStocks = lookupStocks;
        this.quoteStocks = quoteStocks;
    }


    public ArrayList<LookupStock> getLookupStocks() {
        return lookupStocks;
    }

    public ArrayList<QuoteStock> getQuoteStocks() {
        return quoteStocks;
    }
}
