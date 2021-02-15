package MongoDB;


import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MongoConnection {

    public static String StockDBName = "Stocks";

    public static String ERStockCollectionName = "ERStocks";

    public static String ProfittableIndexCollectionName = "ProfittableIndex";

    public MongoDatabase stockDB;

    public MongoCollection<Document> ERStockCollection;

    public MongoCollection<Document> ProfittableIndexCollection;

    public MongoConnection() {

        String filePath = System.getProperty("user.dir") + "/pwd.txt";

        String pwd = readFileAsString(filePath);

        System.out.println("mango pwd str: " + pwd);

        String mangoConnectionString = String.format(
                "mongodb://weizeng1993:%s@weicluster-shard-00-00-cfib0.mongodb.net:27017,weicluster-shard-00-01-cfib0.mongodb.net:27017,weicluster-shard-00-02-cfib0.mongodb.net:27017/test?ssl=true&replicaSet=weiCluster-shard-0&authSource=admin",
                pwd);

        System.out.println("mango connection str: " + mangoConnectionString);

        MongoClientURI uri = new MongoClientURI(mangoConnectionString);

        MongoClient mongoClient = new MongoClient(uri);
        this.stockDB = mongoClient.getDatabase(StockDBName);
        this.ERStockCollection = stockDB.getCollection(ERStockCollectionName);
        this.ProfittableIndexCollection = stockDB.getCollection(ProfittableIndexCollectionName);
    }

    public void readFromERStocksCollection() {

        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                System.out.println(document.toJson());
            }
        };

        this.ERStockCollection.find().forEach((Consumer<? super Document>) printBlock);

    }

    public void insertIntoERStocksCollection(List<Map<String, Object>> rowsToInsert) {
        List<Document> recommendationsToInsert = new ArrayList<>();
        rowsToInsert.forEach(record -> recommendationsToInsert.add(constructNewDocumentGivenMap(record)));
        this.insertIntoERStocksCollectionInternal(recommendationsToInsert);

    }

    public void insertIntoERStocksCollectionInternal(List<Document> rowsToInsert) {

        try {
            this.ERStockCollection.insertMany(rowsToInsert);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Document constructNewDocumentGivenMap(Map<String, Object> inputMap) {
        Document documentToReturn = new Document();
        inputMap.entrySet().forEach(entry -> documentToReturn.put(entry.getKey(), entry.getValue()));

        return documentToReturn;
    }


    public static String readFileAsString(String fileName) {
        String text = "";
        try {
            text = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text;
    }


}