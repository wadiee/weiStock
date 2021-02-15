package Lookup;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Wade on 7/8/16.
 */
public class LookupUtils {


    private final Pattern lookupPattern = Pattern.compile("\"[^\"]*\":\"[^\"]*\"");

    private final Pattern fieldPattern = Pattern.compile(":\".*\"");

    public ArrayList<LookupStock> findLookupStock (String response) throws Exception {

        ArrayList<LookupStock> stocks = new ArrayList<>();

        Matcher lookupMatcher = lookupPattern.matcher(response);
        Matcher fieldMatcher = null;
        String tempSym, tempName, tempExchange = null;

        // Save all match pairs.
        ArrayList<String> matchPairs = new ArrayList<>();




        while(lookupMatcher.find()) {
            matchPairs.add(lookupMatcher.group());
        }
        // Match pairs should be a multiple of 3. Symbol;Name;Exchange
        if (matchPairs.size() % 3 != 0) throw new Exception("Lookup stock did not get info of 3 pairs: Symbol,Name,Exchange");

        for (String s : matchPairs) System.out.println(s);

        int iter = 0;
        while (iter < matchPairs.size()) {

            fieldMatcher = fieldPattern.matcher(matchPairs.get(iter));
            if (fieldMatcher.find()) {
                tempSym = fieldMatcher.group();
                tempSym = tempSym.substring(2, tempSym.lastIndexOf('"'));
            }
            else throw new Exception("Could not match Symbol \n");
            fieldMatcher = fieldPattern.matcher(matchPairs.get(iter+1));
            if (fieldMatcher.find()) {
                tempName = fieldMatcher.group();
                tempName = tempName.substring(2, tempName.lastIndexOf('"'));
            }
            else throw new Exception("Could not match Name \n");
            fieldMatcher = fieldPattern.matcher(matchPairs.get(iter+2));
            if (fieldMatcher.find()) {
                tempExchange = fieldMatcher.group();
                tempExchange = tempExchange.substring(2, tempExchange.lastIndexOf('"'));
            }
            else throw new Exception("Could not match Exchange \n");
            stocks.add(new LookupStock(tempSym, tempName, tempExchange));

            iter += 3;
        }


        return stocks;
    }


}
