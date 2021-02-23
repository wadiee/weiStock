package com.example.WeiStock;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
public class SQLDBCommunicator {
    
    @Autowired
    private JdbcTemplate template;
    final String INSERT_QUERY = "insert into Funds (FundName, StockSym, date) values (?, ?, ?)";

    public SQLDBCommunicator() {
    }

    public List<String> getStocksToEval(String fundName) {

        String curDateStr = LocalDate.now().toString();

        String query = "select * from funds where fundName = '" + fundName + "' AND date ='" + curDateStr + "'";

        List<String> listStocks = this.template.query(
            query,
            (rs, rowNum) -> rs.getString("StockSym"));

        return listStocks;
    }

    public int insertToStockTable(String fundName, String stockSym) {

        String curDateStr = LocalDate.now().toString();

        int retVal = template.update(INSERT_QUERY, fundName, stockSym, curDateStr);
        return retVal;
    }
}


@Data
class FundStock {

    private String fundName;
    private String stockSym;
    private String date;

    public FundStock(String fundName, String stockSym) {
        this.fundName = fundName;
        this.stockSym = stockSym;
        this.date = LocalDate.now().toString();
    }

    public FundStock() {}

    public String getFundName() { return fundName;}
    public String getStockSym() { return stockSym;}
    public String getDate() {return date; }
}
