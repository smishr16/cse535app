package asu.edu.cse535.locationawarereminder.database;

import java.util.Date;

/**
 * Created by Sooraj on 10/20/2016.
 */
public class Task {

    public static String COLUMN_TASK_ID = "Task_ID";
    public static String COLUMN_DESC = "Description";
    public static String COLUMN_TASK_DATE = "Task_Date";
    public static String COLUMN_MOT = "MOT";
    public static String COLUMN_LAT = "Latitude";
    public static String COLUMN_LONG = "Longitude";
    public static String COLUMN_TASK_STATUS = "Status";
    public static String COLUMN_CREATED_DATE = "Created_Date";

    private int taskId;
    private String desc;
    private Date taskDate;
    private String mot;
    private double lat;
    private double lng;
    private String status;
    private Date createdDate;

    public Task(){

    }

    public Task(String desc, Date taskDate, String mot, double lat, double lng, String status, Date createdDate){
        this.desc = desc;
        this.taskDate = taskDate;
        this.mot = mot;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
        this.createdDate = createdDate;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getDesc() {
        return desc;
    }

    public Date getTaskDate() {
        return taskDate;
    }

    public String getMot() {
        return mot;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getStatus() {
        return status;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setTaskDate(Date taskDate) {
        this.taskDate = taskDate;
    }

    public void setMot(String mot) {
        this.mot = mot;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
