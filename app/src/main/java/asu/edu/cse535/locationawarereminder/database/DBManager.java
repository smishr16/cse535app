package asu.edu.cse535.locationawarereminder.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Sooraj on 10/20/2016.
 */
public class DBManager {

    private static SQLiteDatabase db;
    private static Context dbContext;
    private static boolean DEBUG = false;

    public DBManager(Context context){
        dbContext = context;
    }

    public void initializeDB(String packageName){
        Constants.setDbPath(packageName);
        db = SQLiteDatabase.openOrCreateDatabase(Constants.DB_PATH + Constants.DB_NAME, null);
        createTaskTable();
        createPropertiesTable();
        createFavLocsTable();
        initializeProperties();
    }

    public SQLiteDatabase getAppDataBase(){
        return db;
    }

    private static void createTaskTable(){
        String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_TASK + " ( " +
                Task.COLUMN_TASK_ID + " " + Constants.DATATYPE_INT  + " PRIMARY KEY autoincrement" + Constants.COMMA_SEP +
                Task.COLUMN_DESC + " " + Constants.DATATYPE_STRING + " NOT NULL " + Constants.COMMA_SEP +
                Task.COLUMN_TASK_DATE + " " + Constants.DATATYPE_DATETIME + Constants.COMMA_SEP +
                Task.COLUMN_MOT + " " + Constants.DATATYPE_STRING + Constants.COMMA_SEP +
                Task.COLUMN_LAT + " " + Constants.DATATYPE_DOUBLE + " NOT NULL " + Constants.COMMA_SEP +
                Task.COLUMN_LONG + " " + Constants.DATATYPE_DOUBLE + " NOT NULL " + Constants.COMMA_SEP +
                Task.COLUMN_LOC_DESC + " " + Constants.DATATYPE_STRING + " NOT NULL " + Constants.COMMA_SEP +
                Task.COLUMN_TASK_STATUS + " " + Constants.DATATYPE_STRING + Constants.COMMA_SEP +
                Task.COLUMN_CREATED_DATE + " " + Constants.DATATYPE_DATETIME + " ) ";

        try{
            db.beginTransaction();
            try {
                db.execSQL(CREATE_TABLE_QUERY);
                db.setTransactionSuccessful();
                if(DEBUG)
                    Toast.makeText(dbContext, "Table Task created successfully", Toast.LENGTH_SHORT).show();
            }
            catch (SQLiteException e) {
                e.printStackTrace();
                Toast.makeText(dbContext , e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                db.endTransaction();
            }
        }catch (SQLException e){
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void insertIntoTask(Task t) {
        String INSERT_TABLE_QUERY = "INSERT INTO " + Constants.TABLE_TASK + " ( " + Task.COLUMN_DESC + Constants.COMMA_SEP +
                Task.COLUMN_TASK_DATE + Constants.COMMA_SEP + Task.COLUMN_MOT + Constants.COMMA_SEP + Task.COLUMN_LAT +
                Constants.COMMA_SEP + Task.COLUMN_LONG + Constants.COMMA_SEP + Task.COLUMN_LOC_DESC + Constants.COMMA_SEP +
                Task.COLUMN_TASK_STATUS + Constants.COMMA_SEP + Task.COLUMN_CREATED_DATE + " ) VALUES (" +
                Constants.QUOTE +  t.getDesc() + Constants.QUOTE + Constants.COMMA_SEP + Constants.QUOTE + t.getTaskDate() +
                Constants.QUOTE + Constants.COMMA_SEP + Constants.QUOTE + t.getMot() + Constants.QUOTE + Constants.COMMA_SEP +
                t.getLat() + Constants.COMMA_SEP + t.getLng() + Constants.COMMA_SEP + Constants.QUOTE +
                t.getLocDesc().replace("\"", " ").replace("'",".") + Constants.QUOTE + Constants.COMMA_SEP + Constants.QUOTE +
                t.getStatus() + Constants.QUOTE + Constants.COMMA_SEP + Constants.QUOTE + t.getCreatedDate() +
                Constants.QUOTE + ")";
        try{
            db.beginTransaction();
            try {
                db.execSQL(INSERT_TABLE_QUERY);
                db.setTransactionSuccessful();
                if(DEBUG)
                    Toast.makeText(dbContext, "Task added successfully", Toast.LENGTH_SHORT).show();
            }
            catch (SQLiteException e) {
                e.printStackTrace();
                Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                db.endTransaction();
            }
        }catch (SQLException e){
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void updateTask(Task t){
        String UPDATE_TASK_QUERY = "UPDATE " + Constants.TABLE_TASK + " SET " + Task.COLUMN_DESC + " = " + Constants.QUOTE + t.getDesc() +
                Constants.QUOTE + Constants.COMMA_SEP + " " + Task.COLUMN_TASK_DATE + " = " + Constants.QUOTE + t.getTaskDate() + Constants.QUOTE +
                Constants.COMMA_SEP + " " + Task.COLUMN_MOT + " = " + Constants.QUOTE + t.getMot() + Constants.QUOTE + Constants.COMMA_SEP +
                " " + Task.COLUMN_LAT + " = " + Constants.QUOTE + t.getLat() + Constants.QUOTE + Constants.COMMA_SEP + " " + Task.COLUMN_LONG +
                " = " + Constants.QUOTE + t.getLng() + Constants.QUOTE + Constants.COMMA_SEP + " " + Task.COLUMN_TASK_STATUS + " = " +
                Constants.QUOTE + t.getStatus() + Constants.QUOTE + " WHERE " + Task.COLUMN_TASK_ID + " = " + Constants.QUOTE +
                t.getTaskId() + Constants.QUOTE;

        try{
            db.beginTransaction();
            try {
                db.execSQL(UPDATE_TASK_QUERY);
                db.setTransactionSuccessful();
                if(DEBUG)
                    Toast.makeText(dbContext, "Task updated successfully", Toast.LENGTH_SHORT).show();
            }
            catch (SQLiteException e) {
                e.printStackTrace();
                Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                db.endTransaction();
            }
        }catch (SQLException e){
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    public static ArrayList<Task> get_all_tasks(){
        ArrayList<Task> task_list = new ArrayList<>();
        String query = "SELECT *" + " FROM " + Constants.TABLE_TASK + " WHERE " +
                Task.COLUMN_TASK_STATUS + " NOT IN ( " + Constants.QUOTE + "Completed" + Constants.QUOTE
                + Constants.COMMA_SEP + Constants.QUOTE + "Removed" + Constants.QUOTE + " )";
        String actualFormat = "EEE MMM dd HH:mm:ss Z yyyy";
        Cursor c;
        c = db.rawQuery(query, null);
        if (c.moveToFirst()){
            do{
                Task task = new Task();
                Date storedDate;
                task.setTaskId(c.getInt(c.getColumnIndex(Task.COLUMN_TASK_ID)));
                task.setDesc(c.getString(c.getColumnIndex(Task.COLUMN_DESC)));
                task.setStatus(c.getString(c.getColumnIndex(Task.COLUMN_TASK_STATUS)));
                String taskDate = c.getString(c.getColumnIndex(Task.COLUMN_TASK_DATE));
                try{
                    if(!TextUtils.isEmpty(taskDate) && !taskDate.equals("null")){
                        storedDate = new SimpleDateFormat(actualFormat, Locale.US).parse(taskDate);
                        task.setTaskDate(storedDate);
                    }
                } catch(ParseException e) {
                    e.printStackTrace();
                }
                task_list.add(task);
            }while(c.moveToNext());
        }
        c.close();

        return task_list;
    }

    public static Task getTaskByTaskId(int task_id) {
        String SELECT_QUERY = "SELECT * FROM " + Constants.TABLE_TASK + " WHERE " +
                Task.COLUMN_TASK_ID + " = " + task_id;
        Task t = new Task();
        try {
            Cursor c = db.rawQuery(SELECT_QUERY, null);
            if(c.moveToFirst()){
                String actualFormat = "EEE MMM dd HH:mm:ss Z yyyy";
                Date storedDate;
                t.setTaskId(c.getInt(c.getColumnIndex(Task.COLUMN_TASK_ID)));
                t.setDesc(c.getString(c.getColumnIndex(Task.COLUMN_DESC)));
                t.setLat(c.getDouble(c.getColumnIndex(Task.COLUMN_LAT)));
                t.setLng(c.getDouble(c.getColumnIndex(Task.COLUMN_LONG)));
                t.setLocDesc(c.getString(c.getColumnIndex(Task.COLUMN_LOC_DESC)));
                t.setMot(c.getString(c.getColumnIndex(Task.COLUMN_MOT)));
                String createdDate = c.getString(c.getColumnIndex(Task.COLUMN_CREATED_DATE));
                if(!TextUtils.isEmpty(createdDate) && !createdDate.equals("null")){
                    storedDate = new SimpleDateFormat(actualFormat, Locale.US).parse(createdDate);
                    t.setCreatedDate(storedDate);
                }
                t.setStatus(c.getString(c.getColumnIndex(Task.COLUMN_TASK_STATUS)));
                String taskDate = c.getString(c.getColumnIndex(Task.COLUMN_TASK_DATE));
                if(!TextUtils.isEmpty(taskDate) && !taskDate.equals("null")){
                    storedDate = new SimpleDateFormat(actualFormat,  Locale.US).parse(taskDate);
                    t.setTaskDate(storedDate);
                }
            }
            c.close();
        } catch(SQLException|ParseException e) {
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return t;
    }

    private static void createPropertiesTable() {
        String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_PROPERTIES + " ( " +
                Properties.COLUMN_NAME + " " + Constants.DATATYPE_STRING + " PRIMARY KEY" + Constants.COMMA_SEP +
                Properties.COLUMN_VALUE + " " + Constants.DATATYPE_STRING + " ) ";
        try{
            db.beginTransaction();
            try {
                db.execSQL(CREATE_TABLE_QUERY);
                db.setTransactionSuccessful();
                if(DEBUG)
                    Toast.makeText(dbContext, "Table Properties created successfully", Toast.LENGTH_SHORT).show();
            }
            catch (SQLiteException e) {
                e.printStackTrace();
                Toast.makeText(dbContext , e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                db.endTransaction();
            }
        }catch (SQLException e){
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void initializeProperties(){
        insertIntoProperties(new Properties(Constants.propertyEmail, ""));
        insertIntoProperties(new Properties(Constants.propertyPhone, ""));
    }

    private static void insertIntoProperties(Properties p) {
        String INSERT_TABLE_QUERY = "INSERT INTO " + Constants.TABLE_PROPERTIES + " ( " +
                Properties.COLUMN_NAME + Constants.COMMA_SEP + Properties.COLUMN_VALUE + " ) SELECT " +
                Constants.QUOTE +  p.getName() + Constants.QUOTE + Constants.COMMA_SEP +
                Constants.QUOTE + p.getValue() + Constants.QUOTE + " WHERE NOT EXISTS " +
                "(SELECT 1 FROM " + Constants.TABLE_PROPERTIES + " WHERE " + Properties.COLUMN_NAME +
                " = " + Constants.QUOTE +  p.getName() + Constants.QUOTE + " )";
        try{
            db.beginTransaction();
            try {
                db.execSQL(INSERT_TABLE_QUERY);
                db.setTransactionSuccessful();
                if(DEBUG)
                    Toast.makeText(dbContext, "Property " + p.getName() + " added successfully", Toast.LENGTH_SHORT).show();
            }
            catch (SQLiteException e) {
                e.printStackTrace();
                Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                db.endTransaction();
            }
        }catch (SQLException e){
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static ArrayList<Properties> getProperties(String[] propertyArray) {
        ArrayList<Properties> result = new ArrayList<>();
        for(String searchProperty : propertyArray)
             result.add(getProperty(searchProperty));
        return result;
    }

    private static Properties getProperty(String searchProperty){
        String SEARCH_TABLE_QUERY = "SELECT " + Properties.COLUMN_NAME + Constants.COMMA_SEP + Properties.COLUMN_VALUE +
                " FROM " + Constants.TABLE_PROPERTIES + " WHERE " +
                Properties.COLUMN_NAME + " = " + Constants.QUOTE + searchProperty + Constants.QUOTE;
        Properties p = new Properties();
        try {
            Cursor c = db.rawQuery(SEARCH_TABLE_QUERY, null);
            if (c.moveToFirst()) {
                p.setName(c.getString(c.getColumnIndex(Properties.COLUMN_NAME)));
                p.setValue(c.getString(c.getColumnIndex(Properties.COLUMN_VALUE)));
            }
            c.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return p;
    }

    public static void setProperties(ArrayList<Properties> propertyList){
            for(Properties property : propertyList)
                updateProperty(property);
    }

    private static void updateProperty(Properties property){
        String UPDATE_TABLE_QUERY = "UPDATE " + Constants.TABLE_PROPERTIES + " SET " +
                Properties.COLUMN_VALUE + " = " + Constants.QUOTE + property.getValue() + Constants.QUOTE + " WHERE " +
                Properties.COLUMN_NAME + " = " + Constants.QUOTE + property.getName() + Constants.QUOTE ;
        try{
            db.beginTransaction();
            try {
                db.execSQL(UPDATE_TABLE_QUERY);
                db.setTransactionSuccessful();
                if(DEBUG)
                    Toast.makeText(dbContext, "Property " + property.getName() + " updated successfully", Toast.LENGTH_SHORT).show();
            }
            catch (SQLiteException e) {
                e.printStackTrace();
                Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                db.endTransaction();
            }
        }catch (SQLException e){
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static int getLastInserted() {
        String SEARCH_TABLE_QUERY = "SELECT MAX(" + Task.COLUMN_TASK_ID + ") AS taskId FROM " + Constants.TABLE_TASK;
        int taskId = 0;
        try {
            Cursor c = db.rawQuery(SEARCH_TABLE_QUERY, null);
            if (c.moveToFirst()) {
                taskId = c.getInt(c.getColumnIndex("taskId"));
            }
            c.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return taskId;
    }

    public static void createFavLocsTable() {
        String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_MY_LOCATIONS + " ( " +
                FavouriteLocations.COLUMN_LOC_ID + " " + Constants.DATATYPE_INT  + " PRIMARY KEY autoincrement" + Constants.COMMA_SEP +
                FavouriteLocations.COLUMN_LOC_DESC + " " + Constants.DATATYPE_STRING + " NOT NULL " + Constants.COMMA_SEP +
                FavouriteLocations.COLUMN_LOC_LAT + " " + Constants.DATATYPE_DOUBLE + " NOT NULL " + Constants.COMMA_SEP +
                FavouriteLocations.COLUMN_LOC_LONG + " " + Constants.DATATYPE_DOUBLE + " NOT NULL )";

        try{
            db.beginTransaction();
            try {
                db.execSQL(CREATE_TABLE_QUERY);
                db.setTransactionSuccessful();
                if(DEBUG)
                    Toast.makeText(dbContext, "Table My Locations created successfully", Toast.LENGTH_SHORT).show();
            }
            catch (SQLiteException e) {
                e.printStackTrace();
                Toast.makeText(dbContext , e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                db.endTransaction();
            }
        }catch (SQLException e){
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static ArrayList<FavouriteLocations> getFavLocations() {
        ArrayList<FavouriteLocations> favLocs = new ArrayList<>();
        String SEARCH_TABLE_QUERY = "SELECT * FROM " + Constants.TABLE_MY_LOCATIONS;
        try {
            Cursor c = db.rawQuery(SEARCH_TABLE_QUERY, null);
            if (c.moveToFirst()) {
                do{
                    FavouriteLocations favLoc = new FavouriteLocations();
                    favLoc.setLocId(c.getInt(c.getColumnIndex(FavouriteLocations.COLUMN_LOC_ID)));
                    favLoc.setDescription(c.getString(c.getColumnIndex(FavouriteLocations.COLUMN_LOC_DESC)));
                    favLoc.setLatitude(c.getDouble(c.getColumnIndex(FavouriteLocations.COLUMN_LOC_LAT)));
                    favLoc.setLongitude(c.getDouble(c.getColumnIndex(FavouriteLocations.COLUMN_LOC_LONG)));

                    favLocs.add(favLoc);
                } while(c.moveToNext());
            }
            c.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return favLocs;
    }

    public static void insertIntoFavLocations(FavouriteLocations location) {
        String INSERT_TABLE_QUERY = "INSERT INTO " + Constants.TABLE_MY_LOCATIONS + " (" +
                FavouriteLocations.COLUMN_LOC_DESC + Constants.COMMA_SEP + FavouriteLocations.COLUMN_LOC_LAT + Constants.COMMA_SEP +
                FavouriteLocations.COLUMN_LOC_LONG + ") VALUES (" + Constants.QUOTE + location.getDescription() + Constants.QUOTE + Constants.COMMA_SEP +
                location.getLatitude()  + Constants.COMMA_SEP + location.getLongitude() + ")";
        try {
            db.beginTransaction();
            try {
                db.execSQL(INSERT_TABLE_QUERY);
                db.setTransactionSuccessful();
                if(DEBUG)
                    Toast.makeText(dbContext, "Favourite Location added successfully", Toast.LENGTH_SHORT).show();
            }
            catch (SQLiteException e) {
                e.printStackTrace();
                Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                db.endTransaction();
            }
        }catch (SQLException e){
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void deleteFromFavLocation(FavouriteLocations locToDelete) {
        String DELETE_QUERY = "DELETE FROM " + Constants.TABLE_MY_LOCATIONS + " WHERE " +
                FavouriteLocations.COLUMN_LOC_ID + " = " + locToDelete.getLocId();
        try{
            db.beginTransaction();
            try{
                db.execSQL(DELETE_QUERY);
                db.setTransactionSuccessful();
            }catch(SQLiteException e){
                e.printStackTrace();
                Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }catch(SQLException e){
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        finally {
            db.endTransaction();
        }
    }

    public static ArrayList<String> getCompletedTaskNames(){
        ArrayList<String> taskNames = new ArrayList<>();
        String query = "SELECT * FROM " + Constants.TABLE_TASK + " WHERE " +
                Task.COLUMN_TASK_STATUS + " = " + Constants.QUOTE + "Completed" + Constants.QUOTE +
                " ORDER BY " + Task.COLUMN_CREATED_DATE + " DESC ";
        Cursor c;
        db.beginTransaction();
        c = db.rawQuery(query,null);
        if (c.moveToFirst()){
            do{
                String temp = c.getString(c.getColumnIndex("Description"));
                taskNames.add(temp);
            }while(c.moveToNext());
        }
        c.close();
        db.endTransaction();
        return taskNames;
    }

    public static ArrayList<String> getCompletedTaskAddress(){
        ArrayList<String> taskAddress = new ArrayList<>();
        String query = "SELECT * FROM " + Constants.TABLE_TASK + " WHERE " +
                Task.COLUMN_TASK_STATUS + " = " + Constants.QUOTE + "Completed" + Constants.QUOTE +
                " ORDER BY " + Task.COLUMN_CREATED_DATE + " DESC ";
        Cursor c;
        db.beginTransaction();
        c = db.rawQuery(query,null);
        if (c.moveToFirst()){
            do{
                String temp = c.getString(c.getColumnIndex("Location_Desc"));
                taskAddress.add(temp);
            }while(c.moveToNext());
        }
        c.close();
        db.endTransaction();
        return taskAddress;
    }

    public static ArrayList<Task> getCompletedTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        String SEARCH_TABLE_QUERY = "SELECT * FROM " + Constants.TABLE_TASK + " WHERE " +
                Task.COLUMN_TASK_STATUS + " = " + Constants.QUOTE + "Completed" + Constants.QUOTE +
                " ORDER BY " + Task.COLUMN_CREATED_DATE + " DESC ";
        try {
            Cursor c = db.rawQuery(SEARCH_TABLE_QUERY, null);
            if (c.moveToFirst()) {
                do {
                    Task t = new Task();
                    String actualFormat = "EEE MMM dd HH:mm:ss Z yyyy";
                    Date storedDate;
                    t.setDesc(c.getString(c.getColumnIndex(Task.COLUMN_DESC)));
                    t.setLat(c.getDouble(c.getColumnIndex(Task.COLUMN_LAT)));
                    t.setLng(c.getDouble(c.getColumnIndex(Task.COLUMN_LONG)));
                    t.setLocDesc(c.getString(c.getColumnIndex(Task.COLUMN_LOC_DESC)));
                    t.setMot(c.getString(c.getColumnIndex(Task.COLUMN_MOT)));
                    String createdDate = c.getString(c.getColumnIndex(Task.COLUMN_CREATED_DATE));
                    if (!TextUtils.isEmpty(createdDate) && !createdDate.equals("null")) {
                        storedDate = new SimpleDateFormat(actualFormat, Locale.US).parse(createdDate);
                        t.setCreatedDate(storedDate);
                    }
                    t.setStatus(c.getString(c.getColumnIndex(Task.COLUMN_TASK_STATUS)));
                    String taskDate = c.getString(c.getColumnIndex(Task.COLUMN_TASK_DATE));
                    if (!TextUtils.isEmpty(taskDate) && !taskDate.equals("null")) {
                        storedDate = new SimpleDateFormat(actualFormat, Locale.US).parse(taskDate);
                        t.setTaskDate(storedDate);
                    }
                    tasks.add(t);
                } while (c.moveToNext());
                c.close();
            }
        }catch(SQLException | ParseException e) {
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return tasks;
    }

    public static void updateTaskStatus(int task_id, String status) {
        String UPDATE_QUERY = "UPDATE " + Constants.TABLE_TASK + " SET " + Task.COLUMN_TASK_STATUS + " = " + Constants.QUOTE +
                status + Constants.QUOTE + " WHERE " + Task.COLUMN_TASK_ID + " = " + task_id;

        try {
            db.beginTransaction();
            db.execSQL(UPDATE_QUERY);
            db.setTransactionSuccessful();
        } catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.endTransaction();
        }
    }
}

