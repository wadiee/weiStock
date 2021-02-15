package Lookup;

public class LookupStock {

    private final String symbol;
    private final String name;
    private final String exchange;

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getExchange() {
        return exchange;
    }



    public LookupStock(String symbol, String name, String exchange) {
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
    }


}