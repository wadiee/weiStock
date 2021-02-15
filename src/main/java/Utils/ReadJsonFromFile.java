package Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.util.ResourceUtils;

public class ReadJsonFromFile {
    
    public static List<String> getStockList(String indexName) {
        List<String> stockList = new ArrayList<String>();

        try {
            File file = ResourceUtils.getFile(ReadJsonFromFile.getFilePath(indexName));
            InputStream in = new FileInputStream(file);

            ObjectMapper mapper = new ObjectMapper();
            stockList = mapper.readValue(in, new TypeReference<List<String>>(){});

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stockList;
    }

    public static String getFilePath(String indexName) {
        String path = "classpath:\\static\\" + indexName + ".json";
        return path;
    }
}
