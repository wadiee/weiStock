package DataLayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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

    public void test(String fundName) {

        String query = "select * from funds where fundName = '" + fundName + "'";

        List<FundStock> fundStocks = this.template.query(
            query,
            (rs, rowNum) ->
                new FundStock(
                    rs.getString("FundName"),
                    rs.getString("StockSym")
                ));

        for (FundStock fundStock : fundStocks) {
            System.out.println(fundStock.getFundName() + " " + fundStock.getStockSym());
        }
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
