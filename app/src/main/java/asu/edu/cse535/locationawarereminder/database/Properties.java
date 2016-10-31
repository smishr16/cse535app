package asu.edu.cse535.locationawarereminder.database;

/**
 * Created by Sooraj on 10/23/2016.
 */
public class Properties {

    public static String COLUMN_NAME = "Name";
    public static String COLUMN_VALUE = "Value";

    private String name;
    private String value;

    public Properties(){}

    public Properties(String name, String value){
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
