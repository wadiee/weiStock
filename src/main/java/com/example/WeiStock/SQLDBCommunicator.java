package com.example.WeiStock;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
public class SQLDBCommunicator {
    
    @Autowired
    private JdbcTemplate template;

    public SQLDBCommunicator() {
    }

    public List<String> getStocksToEval(String fundName) {

        String query = "select * from funds where fundName = '" + fundName + "'";

        List<String> listStocks = this.template.query(
            query,
            (rs, rowNum) -> rs.getString("StockSym"));

        return listStocks;
    }
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class FundStock {

    private String fundName;
    private String stockSym;

    public FundStock(String fundName, String stockSym) {
        this.fundName = fundName;
        this.stockSym = stockSym;
    }

    public FundStock() {}

    public String getFundName() { return fundName;}
    public String getStockSym() { return stockSym;}
}
