package asu.edu.cse535.locationawarereminder.database;

/**
 * Created by Sooraj on 10/20/2016.
 */
public class Constants {

    public static String DB_NAME = "LAR";
    public static String DB_PATH;
    public static String TABLE_TASK = "TASK";
    public static String TABLE_PROPERTIES = "PROPERTIES";

    public static String DATATYPE_INT = "integer";
    public static String DATATYPE_STRING = "varchar(500)";
    public static String DATATYPE_DATETIME = "datetime";
    public static String DATATYPE_DOUBLE = "double";

    public static String COMMA_SEP = ", ";
    public static String QUOTE = "'";

    public static String propertyEmail = "Email";
    public static String propertyPhone = "Phone";

    public static void setDbPath(String packageName) {
        DB_PATH = "/data/data/"+ packageName + "/";
    }
}
