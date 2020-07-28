package dk.dtu.group22.beeware.dal.dao.implementation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import dk.dtu.group22.beeware.dal.dao.interfaces.CachedHiveRepoI;
import dk.dtu.group22.beeware.dal.dto.Hive;
import dk.dtu.group22.beeware.dal.dto.Measurement;
import static dk.dtu.group22.beeware.dal.dao.implementation.HiveReaderContract.*;

public class CachedHiveRepoSQLImpl implements CachedHiveRepoI {

    private Context ctx;
    private HiveReaderDbHelper dbHelper;

    public CachedHiveRepoSQLImpl(Context ctx){
        this.ctx = ctx;
        dbHelper = new HiveReaderDbHelper(ctx);
    }

    // TODO updateHiveConfigurations(Hive hive) maybe not anymore. updateHive is enough

    @Override
    public Hive getCachedHiveWithAllData(int hiveId) {

       SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                BaseColumns._ID,
                HiveEntry.COLUMN_NAME_HIVE_ID,
                HiveEntry.COLUMN_NAME_TIMESTAMP,
                HiveEntry.COLUMN_NAME_HIVE_WEIGHT_KGS
        };
        String selection = HiveEntry.COLUMN_NAME_HIVE_ID + " = ? ";
        String[] selectionArgs = {hiveId+""};

        // Fetch hive data
        Cursor hiveCursor = db.query(
                HiveEntry.TABLE_HIVE,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        hiveCursor.moveToNext();
        int returnedHiveId = hiveCursor.getInt(hiveCursor.getColumnIndexOrThrow(HiveEntry.COLUMN_NAME_HIVE_ID));
        String returnedHiveName = hiveCursor.getString(hiveCursor.getColumnIndexOrThrow(HiveEntry.COLUMN_NAME_HIVE_NAME));
        int returnedWeightIndicator = hiveCursor.getInt(hiveCursor.getColumnIndexOrThrow(HiveEntry.COLUMN_NAME_HIVE_WEIGHT_INDICATOR));
        int returnedTempIndicator = hiveCursor.getInt(hiveCursor.getColumnIndexOrThrow(HiveEntry.COLUMN_NAME_HIVE_TEMP_INDICATOR));

        // Fetch measurements of a hive
        Cursor measurementsCursor = db.query(
                HiveEntry.TABLE_HIVE_MEASUREMENT,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        List<Measurement> measurements = new ArrayList<>();
        while (measurementsCursor.moveToNext()){
            Timestamp timestamp = new Timestamp(measurementsCursor.getInt(measurementsCursor.getColumnIndexOrThrow(HiveEntry.COLUMN_NAME_TIMESTAMP)));
            double weight = measurementsCursor.getDouble(measurementsCursor.getColumnIndexOrThrow(HiveEntry.COLUMN_NAME_HIVE_WEIGHT_KGS));
            double tempIn = measurementsCursor.getDouble(measurementsCursor.getColumnIndexOrThrow(HiveEntry.COLUMN_NAME_HIVE_TEMP_C_IN));
            double hum = measurementsCursor.getDouble(measurementsCursor.getColumnIndexOrThrow(HiveEntry.COLUMN_NAME_HIVE_HUM));
            double illum = measurementsCursor.getDouble(measurementsCursor.getColumnIndexOrThrow(HiveEntry.COLUMN_NAME_HIVE_ILLUM));

            measurements.add(new Measurement(timestamp, weight, tempIn, hum, illum));
            System.out.println("measurements: " + measurements.toString());
        }

        //Construct the hive
        Hive hiveToReturn = new Hive(returnedHiveId, returnedHiveName);
        hiveToReturn.setWeightIndicator(returnedWeightIndicator);
        hiveToReturn.setTempIndicator(returnedTempIndicator);
        hiveToReturn.setMeasurements(measurements);
        return hiveToReturn;
    }

    /**
     * What should the method do is given hive, does not have any measurements?
     * Maybe throw an IllegalArgumentException
     * @param hive
     */
    @Override
    public void createCachedHive(Hive hive) {

        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues hiveValues = new ContentValues();
        hiveValues.put(HiveEntry.COLUMN_NAME_HIVE_ID, hive.getId());
        hiveValues.put(HiveEntry.COLUMN_NAME_HIVE_NAME, hive.getName());
        hiveValues.put(HiveEntry.COLUMN_NAME_HIVE_WEIGHT_INDICATOR, hive.getWeightIndicator());
        hiveValues.put(HiveEntry.COLUMN_NAME_HIVE_TEMP_INDICATOR, hive.getTempIndicator());
        long rowIDHiveValues = db.insert(HiveEntry.TABLE_HIVE, null, hiveValues);
        System.out.println("rowIDHiveValues: " + rowIDHiveValues);
        insertHiveMeasurements(hive);

    }

    private void insertHiveMeasurements(Hive hive) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        for (int i = 0; i<hive.getMeasurements().size(); i++ ){
            ContentValues measurements = new ContentValues();
            measurements.put(HiveEntry.COLUMN_NAME_HIVE_ID, hive.getId());
            measurements.put(HiveEntry.COLUMN_NAME_TIMESTAMP, hive.getMeasurements().get(i).getTimestamp().getTime());
            measurements.put(HiveEntry.COLUMN_NAME_HIVE_WEIGHT_KGS, hive.getMeasurements().get(i).getWeight());
            measurements.put(HiveEntry.COLUMN_NAME_HIVE_TEMP_C_IN, hive.getMeasurements().get(i).getTempIn());
            measurements.put(HiveEntry.COLUMN_NAME_HIVE_HUM, hive.getMeasurements().get(i).getHumidity());
            measurements.put(HiveEntry.COLUMN_NAME_HIVE_ILLUM, hive.getMeasurements().get(i).getIlluminance());
// Insert the new row, returning the primary key value of the new row. -1 if there was a problem to insert data
            long newRowId = db.insert(HiveEntry.TABLE_HIVE_MEASUREMENT, null, measurements);
            System.out.println("newRowId: " + newRowId);
        }
    }

    @Override
    public void updateHive(Hive hive) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = HiveEntry.COLUMN_NAME_HIVE_ID + " = ? ";
        String[] selectionArgs = {hive.getId()+""};
        int deletedRows = db.delete(HiveEntry.TABLE_HIVE_MEASUREMENT, selection, selectionArgs);
        System.out.println("deletedRows" + deletedRows);
        insertHiveMeasurements(hive);
        // Update indicators
        ContentValues indicators = new ContentValues();
        indicators.put(HiveEntry.COLUMN_NAME_HIVE_WEIGHT_INDICATOR, hive.getWeightIndicator());
        indicators.put(HiveEntry.COLUMN_NAME_HIVE_TEMP_INDICATOR, hive.getTempIndicator());
        int count = db.update(
                HiveEntry.TABLE_HIVE,
                indicators,
                selection,
                selectionArgs);
    }
}

final class HiveReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private HiveReaderContract() {}

    /* Inner class that defines the table contents */
    public static class HiveEntry implements BaseColumns {
        public static final String TABLE_HIVE_MEASUREMENT = "HIVE_MEASUREMENT";
        public static final String TABLE_HIVE = "HIVE";
        public static final String COLUMN_NAME_HIVE_ID = "hive_id";
        public static final String COLUMN_NAME_HIVE_NAME = "hive_name";
        public static final String COLUMN_NAME_HIVE_WEIGHT_INDICATOR = "hive_weight_indicator";
        public static final String COLUMN_NAME_HIVE_TEMP_INDICATOR = "hive_temp_indicator";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_HIVE_WEIGHT_KGS = "hive_weight_kgs";
        public static final String COLUMN_NAME_HIVE_TEMP_C_IN = "hive_temp_c_in";
        public static final String COLUMN_NAME_HIVE_TEMP_C_OUT = "hive_temp_c_out";
        public static final String COLUMN_NAME_HIVE_HUM = "hive_hum";
        public static final String COLUMN_NAME_HIVE_ILLUM = "hive_illum";
    }
}
class HiveReaderDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "hive.db";
    private static final String CREATE_TABLE_HIVE_MEASUREMENT =
            "CREATE TABLE " + HiveEntry.TABLE_HIVE_MEASUREMENT + " (" +
                    HiveEntry._ID + " INTEGER PRIMARY KEY," +
                    HiveEntry.COLUMN_NAME_HIVE_ID + " INTEGER," +
                    HiveEntry.COLUMN_NAME_TIMESTAMP + " timestamp," +
                    HiveEntry.COLUMN_NAME_HIVE_WEIGHT_KGS+ " decimal(5,2),"+
                    HiveEntry.COLUMN_NAME_HIVE_TEMP_C_IN+ " decimal(5,2),"+
                    HiveEntry.COLUMN_NAME_HIVE_TEMP_C_OUT+ " decimal(5,2),"+
                    HiveEntry.COLUMN_NAME_HIVE_HUM+ " decimal(5,2),"+
                    HiveEntry.COLUMN_NAME_HIVE_ILLUM+ " decimal(5,2)"+
                    ")";

    private static final String CREATE_TABLE_HIVE =
            "CREATE TABLE " + HiveEntry.TABLE_HIVE + " (" +
                    HiveEntry.COLUMN_NAME_HIVE_ID + " INTEGER PRIMARY KEY," +
                    HiveEntry.COLUMN_NAME_HIVE_NAME + " TEXT,"+
                    HiveEntry.COLUMN_NAME_HIVE_WEIGHT_INDICATOR + " INTEGER," +
                    HiveEntry.COLUMN_NAME_HIVE_TEMP_INDICATOR + " INTEGER" +
                    ")";

    private static final String SQL_DELETE_HIVE_MEASUREMENT_TABLE =
            "DROP TABLE IF EXISTS " + HiveEntry.TABLE_HIVE_MEASUREMENT;
    private static final String SQL_DELETE_HIVE_TABLE =
            "DROP TABLE IF EXISTS " + HiveEntry.TABLE_HIVE;

    public HiveReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_HIVE_MEASUREMENT);
        db.execSQL(CREATE_TABLE_HIVE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_HIVE_MEASUREMENT_TABLE);
        db.execSQL(SQL_DELETE_HIVE_TABLE);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}