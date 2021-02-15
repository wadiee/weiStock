package Utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ReadJsonFromFile {
    
    public static List<String> getStockList(String indexName) {
        List<String> stockList = new ArrayList<String>();

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        
        String path = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\" + indexName + ".json"; 
        try (FileReader reader = new FileReader(path))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
 
            JSONArray stockArray = (JSONArray) obj;

            //Iterate over stock array
            stockArray.forEach(a -> stockList.add((String) a));
 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return stockList;
    }
}
