package Crawler;

/**
 * Created by Wade on 9/25/16.
 */
public class ERWrapper {

    private String symbol;
    private String companyName;
    private float EPSEstimated;
    private float EPSReported;
    private float EPSSurprise;
    private boolean hasEPS = false;
    private String ERDayTime;
    private String ERDate;

    public static int withNoEPS = 0;
    public static int withPositiveEPS = 1;
    public static int withNegativeEPS = 2;

    public ERWrapper(String symbol, String companyName, String EPSEstimated, String EPSReported, String EPSSurprise, String erDayTime) {
        this.symbol = symbol;
        this.ERDayTime = erDayTime;
        this.companyName = companyName;

        try {
            this.EPSEstimated = Float.parseFloat(EPSEstimated);
            this.hasEPS = true;
        } catch (Exception e){
            this.EPSEstimated = Float.NEGATIVE_INFINITY;
            this.hasEPS = false;
        }

        try {
            this.EPSReported = Float.parseFloat(EPSReported);
        } catch (Exception e){
            this.EPSReported = Float.NEGATIVE_INFINITY;
        }

        try {
            this.EPSSurprise = Float.parseFloat(EPSSurprise);
        } catch (Exception e){
            this.EPSSurprise = Float.NEGATIVE_INFINITY;
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public String getERDayTime() {
        return ERDayTime;
    }

    public float getEPSEstimated() { return EPSEstimated; }

    public float getEPSReported() { return EPSReported; }

    public float getEPSSurprise() { return EPSSurprise; }

    public String getCompanyName() { return companyName; }

    public boolean isHasEPS() {
        return hasEPS;
    }

    public String toString() {
        if (this.hasEPS) {
            return "Symbol: " + this.symbol + " Company Name: " + this.companyName + " ESP estimate: " + this.EPSEstimated + " ESP reported: " +
                    this.EPSReported + " ESP Surprise: " + this.EPSSurprise + " ER time: " + this.ERDayTime;
        } else {
            return "Symbol: " + this.symbol + " ESP estimate: " + "N/A" + " ER time: " + this.ERDayTime;
        }
    }

    public String getERDate() {
        return ERDate;
    }

    public void setERDate(String ERDate) {
        this.ERDate = ERDate;
    }
}
