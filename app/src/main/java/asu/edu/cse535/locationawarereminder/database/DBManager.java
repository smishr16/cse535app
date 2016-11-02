package asu.edu.cse535.locationawarereminder.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Sooraj on 10/20/2016.
 */
public class DBManager {

    private static SQLiteDatabase db;
    private static Context dbContext;
    private static boolean DEBUG = false;
    public static ArrayList<String> task_list = new ArrayList<String>();

    public DBManager(Context context){
        dbContext = context;
    }

    public void initializeDB(String packageName){
        Constants.setDbPath(packageName);
        db = SQLiteDatabase.openOrCreateDatabase(Constants.DB_PATH + Constants.DB_NAME, null);
        createTaskTable();
        createPropertiesTable();
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
                Task.COLUMN_LAT + " " + Constants.DATATYPE_DOUBLE + "NOT NULL" + Constants.COMMA_SEP +
                Task.COLUMN_LONG + " " + Constants.DATATYPE_DOUBLE + "NOT NULL" + Constants.COMMA_SEP +
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
                Constants.COMMA_SEP + Task.COLUMN_LONG + Constants.COMMA_SEP + Task.COLUMN_TASK_STATUS + Constants.COMMA_SEP +
                Task.COLUMN_CREATED_DATE + " ) VALUES (" +
                Constants.QUOTE +  t.getDesc() + Constants.QUOTE + Constants.COMMA_SEP + Constants.QUOTE + t.getTaskDate() +
                Constants.QUOTE + Constants.COMMA_SEP + Constants.QUOTE + t.getMot() + Constants.QUOTE + Constants.COMMA_SEP +
                t.getLat() + Constants.COMMA_SEP + t.getLng() + Constants.COMMA_SEP + Constants.QUOTE + t.getStatus() +
                Constants.QUOTE + Constants.COMMA_SEP + Constants.QUOTE + t.getCreatedDate() + Constants.QUOTE + ")";
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

    public static ArrayList<String> get_all_tasks(){
        String query = "select *" + " " + "from" + " "+ Constants.TABLE_TASK ;
        Cursor c;
        db.beginTransaction();
        c= db.rawQuery(query,null);
        if (c.moveToFirst()){
            do{
                String temp = c.getString(c.getColumnIndex("Description"));
                task_list.add(temp);



                // do what ever you want here
            }while(c.moveToNext());


        }
        db.endTransaction();
        return task_list;

    }

    public static String getTaskname(){
        String query = "select *" + " " + "from" + " "+ Constants.TABLE_TASK + " "+ "where" +
                " "+Task.COLUMN_TASK_ID + " " + " = (select max("+ Task.COLUMN_TASK_ID +") from" +" " +Constants.TABLE_TASK + ")";
        Cursor c;

        db.beginTransaction();
        Log.v("Query",query);

        c = db.rawQuery(query, null);
        c.moveToFirst();

        String temp = c.getString(c.getColumnIndex("Description"));


        Log.v("task_name",temp);
        db.endTransaction();

        return temp;
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

    public static int getTaskCount(){
        String SEARCH_TABLE_QUERY = "SELECT COUNT(*) AS cnt FROM " + Constants.TABLE_TASK;
        int count = 0;
        try {
            Cursor c = db.rawQuery(SEARCH_TABLE_QUERY, null);
            if (c.moveToFirst()) {
                count = c.getInt(c.getColumnIndex("cnt"));
            }
            c.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return count;
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
}
