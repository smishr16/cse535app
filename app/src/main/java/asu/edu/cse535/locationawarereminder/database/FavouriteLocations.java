package asu.edu.cse535.locationawarereminder.database;

/**
 * Created by Sooraj on 10/23/2016.
 */
public class FavouriteLocations {

    public static String COLUMN_LOC_ID = "Loc_ID";
    public static String COLUMN_LOC_DESC = "Description";
    public static String COLUMN_LOC_LAT = "Latitude";
    public static String COLUMN_LOC_LONG = "Longitude";

    private int locId;
    private String description;
    private double latitude;
    private double longitude;

    public int getLocId() {
        return locId;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
