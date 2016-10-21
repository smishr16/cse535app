package asu.edu.cse535.locationawarereminder.database;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

/**
 * Created by Sooraj on 10/20/2016.
 */
public class DBManager {

    private static SQLiteDatabase db;

    public static void initializeDB(String packageName){
        Constants.setDbPath(packageName);
        db = SQLiteDatabase.openOrCreateDatabase(Constants.DB_PATH + Constants.DB_NAME, null);
        createTaskTable();
    }

    public static SQLiteDatabase getAppDataBase(){
        return db;
    }

    private static void createTaskTable(){
        String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + Constants.TABLE_TASK + " ( " +
                Task.COLUMN_TASK_ID + " " + Constants.DATATYPE_INT  + " PRIMARY KEY autoincrement" + Constants.COMMA_SEP +
                Task.COLUMN_DESC + " " + Constants.DATATYPE_STRING + Constants.COMMA_SEP +
                Task.COLUMN_TASK_DATE + " " + Constants.DATATYPE_DATETIME + Constants.COMMA_SEP +
                Task.COLUMN_MOT + " " + Constants.DATATYPE_STRING + Constants.COMMA_SEP +
                Task.COLUMN_LAT + " " + Constants.DATATYPE_DOUBLE + Constants.COMMA_SEP +
                Task.COLUMN_LONG + " " + Constants.DATATYPE_DOUBLE + Constants.COMMA_SEP +
                Task.COLUMN_TASK_STATUS + " " + Constants.DATATYPE_STRING + Constants.COMMA_SEP +
                Task.COLUMN_CREATED_TIME + " " + Constants.DATATYPE_DATETIME + " ) ";

        try{
            db.beginTransaction();
            try {
                db.execSQL(CREATE_TABLE_QUERY);
                db.setTransactionSuccessful();
            }
            catch (SQLiteException e) {
                //Toast.makeText(DBManager.this.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                db.endTransaction();
            }
        }catch (SQLException e){
            //Toast.makeText(AccelerometerService.this.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
