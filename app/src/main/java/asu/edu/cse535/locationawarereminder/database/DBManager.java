package asu.edu.cse535.locationawarereminder.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.content.ContentValues;
import android.database.DatabaseUtils;

/**
 * Created by Sooraj on 10/20/2016.
 */
public class DBManager {
    public static Task task_details;

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

    public static Task fetch_data(String task_name) {


        Log.v("task_name_db", task_name);
        //task_name= "'%" +task_name + "%'";
        Task task_tuple = new Task();
        //String query = "Select * from " + Constants.TABLE_TASK + " where " + Task.COLUMN_DESC + " = " + task_name ;
        String query = "Select * from " + Constants.TABLE_TASK;
        Log.v("query is", query);
        Cursor c;

        try {
            db.beginTransaction();
            c = db.rawQuery(query, null);
            Log.v("task_name_passed", task_name);

            if (c.moveToFirst()) {
                do {
                    String temp = c.getString(c.getColumnIndex("Description"));
                    Log.v("temp", temp);

                    //task_list.add(temp);
                    task_details = new Task();
                    if (temp.equals(task_name)) {
                        Log.v("temp1", temp);
                        String actualFormat = "EEE MMM dd HH:mm:ss Z yyyy";
                        Date storedDate;
                        storedDate = new SimpleDateFormat(actualFormat).parse(c.getString(c.getColumnIndex(Task.COLUMN_CREATED_DATE)));
                        task_details.setCreatedDate(storedDate);
                        storedDate = new SimpleDateFormat(actualFormat).parse(c.getString(c.getColumnIndex(Task.COLUMN_TASK_DATE)));
                        task_details.setTaskDate(storedDate);


                        Log.v("status",c.getString(6));
                        task_details.setDesc(c.getString(1));
                        task_details.setMot(c.getString(3));
                        task_details.setLat(c.getDouble(4));
                        task_details.setLng(c.getDouble(5));
                        task_details.setStatus(c.getString(6));
                        Log.v("task_name_descrip", task_details.getDesc());
                        //Log.v("task_name_date",task_details.getTaskDate());
                        Log.v("task_name_lat", Double.toString(task_details.getLat()));

                        return task_details;


                    }


                    // do what ever you want here
                } while (c.moveToNext());
                db.endTransaction();


            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return task_tuple;
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
        ArrayList<String> task_list = new ArrayList<String>();
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

    public static Task getTaskByTaskId(int task_id) {
        String SELECT_QUERY = "SELECT * FROM " + Constants.TABLE_TASK + " WHERE " +
                Task.COLUMN_TASK_ID + " = " + task_id;
        Task t = new Task();
        try {
            Cursor c = db.rawQuery(SELECT_QUERY, null);
            if(c.moveToFirst()){
                String actualFormat = "EEE MMM dd HH:mm:ss Z yyyy";
                Date storedDate;
                t.setDesc(c.getString(c.getColumnIndex(Task.COLUMN_DESC)));
                t.setLat(c.getDouble(c.getColumnIndex(Task.COLUMN_LAT)));
                t.setLng(c.getDouble(c.getColumnIndex(Task.COLUMN_LONG)));
                t.setMot(c.getString(c.getColumnIndex(Task.COLUMN_MOT)));
                String createdDate = c.getString(c.getColumnIndex(Task.COLUMN_CREATED_DATE));
                if(!TextUtils.isEmpty(createdDate) && !createdDate.equals("null")){
                    storedDate = new SimpleDateFormat(actualFormat).parse(createdDate);
                    t.setCreatedDate(storedDate);
                }
                t.setStatus(c.getString(c.getColumnIndex(Task.COLUMN_TASK_STATUS)));
                String taskDate = c.getString(c.getColumnIndex(Task.COLUMN_TASK_DATE));
                if(!TextUtils.isEmpty(taskDate) && !taskDate.equals("null")){
                    storedDate = new SimpleDateFormat(actualFormat).parse(taskDate);
                    t.setTaskDate(storedDate);
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
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
                Task.COLUMN_TASK_STATUS + " = " + "COMPLETED";
        Cursor c;
        db.beginTransaction();
        c= db.rawQuery(query,null);
        if (c.moveToFirst()){
            do{
                String temp = c.getString(c.getColumnIndex("Description"));
                taskNames.add(temp);
                // do what ever you want here
            }while(c.moveToNext());
        }
        db.endTransaction();
        return task_list;
    }

    public static ArrayList<Task> getCompletedTasks() {
        ArrayList<Task> tasks = new ArrayList<>();
        String SEARCH_TABLE_QUERY = "SELECT * FROM " + Constants.TABLE_TASK + " WHERE " +
                Task.COLUMN_TASK_STATUS + " = " + "COMPLETED";
        Task t = new Task();
        try {
            Cursor c = db.rawQuery(SEARCH_TABLE_QUERY, null);
            if (c.moveToFirst()) {
                do {
                    String actualFormat = "EEE MMM dd HH:mm:ss Z yyyy";
                    Date storedDate;
                    t.setDesc(c.getString(c.getColumnIndex(Task.COLUMN_DESC)));
                    t.setLat(c.getDouble(c.getColumnIndex(Task.COLUMN_LAT)));
                    t.setLng(c.getDouble(c.getColumnIndex(Task.COLUMN_LONG)));
                    t.setMot(c.getString(c.getColumnIndex(Task.COLUMN_MOT)));
                    String createdDate = c.getString(c.getColumnIndex(Task.COLUMN_CREATED_DATE));
                    if (!TextUtils.isEmpty(createdDate) && !createdDate.equals("null")) {
                        storedDate = new SimpleDateFormat(actualFormat).parse(createdDate);
                        t.setCreatedDate(storedDate);
                    }
                    t.setStatus(c.getString(c.getColumnIndex(Task.COLUMN_TASK_STATUS)));
                    String taskDate = c.getString(c.getColumnIndex(Task.COLUMN_TASK_DATE));
                    if (!TextUtils.isEmpty(taskDate) && !taskDate.equals("null")) {
                        storedDate = new SimpleDateFormat(actualFormat).parse(taskDate);
                        t.setTaskDate(storedDate);
                    }
                    tasks.add(t);
                } while (c.moveToNext());
                c.close();
            }
        }catch(SQLException e) {
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(dbContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return tasks;
    }



    public static void Update_task(String t_name) {

        int count =1;
        Log.v("enterd delete_task","entered");
        String query = "Select * from " + Constants.TABLE_TASK;
        String query1 = "Select * from " + Constants.TABLE_TASK;
        Log.v("query is", query);
        Cursor c,d;

        try {
            db.beginTransaction();
            Long numRows = DatabaseUtils.queryNumEntries(db,Constants.TABLE_TASK);
            Log.v("count before deleting",numRows.toString());
            c = db.rawQuery(query, null);
            Log.v("task_name_passed", t_name);


            if (c.moveToFirst()) {
                do {
                    String temp = c.getString(c.getColumnIndex("Description"));
                    Log.v("tasks_in_db", temp);

                    //task_list.add(temp
                    // task_details = new Task();

                    if (temp.equals(t_name)) {

                        ContentValues cv = new ContentValues();
                        cv.put("Status","completed"); //These Fields should be your String values of actual column names

                        Log.v("task_being_Del", t_name);
                        String task_id = c.getString(0);
                        Log.v("task_id_deleting", task_id);
                        db.update(Constants.TABLE_TASK,cv, Task.COLUMN_TASK_ID + " = " + task_id , null);

                        count++;
                        Log.v("count",Integer.toString(count));

                    }
                } while (c.moveToNext());
                Log.v("after while","deleting tasks");

//                d = db.rawQuery(query1,null);
//                Log.v("d",Integer.toString(d.getCount()));
//                if(d.moveToFirst()) {
//                    do {
//                        Log.v("task desc db", d.getString(0));
//                    } while (d.moveToNext());
//                }

                numRows = DatabaseUtils.queryNumEntries(db,Constants.TABLE_TASK);
                db.setTransactionSuccessful();

                db.endTransaction();
                Log.v("count_after_deleting",numRows.toString());

            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public static void DeleteTask(String t_name) {

        int count =1;
        Log.v("enterd delete_task","entered");
        String query = "Select * from " + Constants.TABLE_TASK;
        String query1 = "Select * from " + Constants.TABLE_TASK;
        Log.v("query is", query);
        Cursor c,d;

        try {
            db.beginTransaction();
            Long numRows = DatabaseUtils.queryNumEntries(db,Constants.TABLE_TASK);
            Log.v("count before deleting",numRows.toString());
            c = db.rawQuery(query, null);
            Log.v("task_name_passed", t_name);


            if (c.moveToFirst()) {
                do {
                    String temp = c.getString(c.getColumnIndex("Description"));
                    Log.v("tasks_in_db", temp);

                    //task_list.add(temp
                    // task_details = new Task();

                    if (temp.equals(t_name)) {
                        Log.v("task_being_Del", t_name);
                        String task_id = c.getString(0);
                        Log.v("task_id_deleting", task_id);
                        db.delete(Constants.TABLE_TASK, Task.COLUMN_TASK_ID + " = " + task_id , null);

                        count++;
                        Log.v("count",Integer.toString(count));

                    }
                } while (c.moveToNext());
                Log.v("after while","deleting tasks");

//                d = db.rawQuery(query1,null);
//                Log.v("d",Integer.toString(d.getCount()));
//                if(d.moveToFirst()) {
//                    do {
//                        Log.v("task desc db", d.getString(0));
//                    } while (d.moveToNext());
//                }

                numRows = DatabaseUtils.queryNumEntries(db,Constants.TABLE_TASK);
                db.setTransactionSuccessful();

                db.endTransaction();
                Log.v("count_after_deleting",numRows.toString());

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

