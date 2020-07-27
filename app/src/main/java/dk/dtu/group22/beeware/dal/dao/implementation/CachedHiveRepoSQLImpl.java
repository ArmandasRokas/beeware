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

        Cursor cursor = db.query(
                "HIVE_DATA",   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );


        int id = 0;
        String hiveName = "";
        List<Measurement> measurements = new ArrayList<>();
        while (cursor.moveToNext()){
            id = cursor.getInt(cursor.getColumnIndexOrThrow(HiveEntry.COLUMN_NAME_HIVE_ID));
            hiveName = cursor.getString(cursor.getColumnIndexOrThrow(HiveEntry.COLUMN_NAME_HIVE_NAME));
            Timestamp timestamp = new Timestamp(cursor.getInt(cursor.getColumnIndexOrThrow(HiveEntry.COLUMN_NAME_TIMESTAMP)));
            double weight = cursor.getDouble(cursor.getColumnIndexOrThrow(HiveEntry.COLUMN_NAME_HIVE_WEIGHT_KGS));
            measurements.add(new Measurement(timestamp, weight, 0.0, 0.0, 0.0));
            System.out.println("measurements: " + measurements.toString());
        }
        Hive hiveToReturn = new Hive(id, hiveName);
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

// Create a new map of values, where column names are the keys
        for (int i = 0; i<hive.getMeasurements().size(); i++ ){
            ContentValues values = new ContentValues();
            values.put(HiveEntry.COLUMN_NAME_HIVE_ID, hive.getId()); // FIXME should not update id on every measurement
            values.put(HiveEntry.COLUMN_NAME_HIVE_NAME, hive.getName()); // FIXME the same as above
            values.put(HiveEntry.COLUMN_NAME_TIMESTAMP, hive.getMeasurements().get(i).getTimestamp().getTime());
            values.put(HiveEntry.COLUMN_NAME_HIVE_WEIGHT_KGS, hive.getMeasurements().get(i).getWeight());
// Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(HiveEntry.TABLE_NAME, null, values); // -1 if was problem to insert data
            System.out.println("newRowId: " + newRowId);
        }
    }
}

final class HiveReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private HiveReaderContract() {}

    /* Inner class that defines the table contents */
    public static class HiveEntry implements BaseColumns {
        public static final String TABLE_NAME = "HIVE_DATA";
        public static final String COLUMN_NAME_HIVE_ID = "hive_id";
        public static final String COLUMN_NAME_HIVE_NAME = "hive_name";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_HIVE_WEIGHT_KGS = "hive_weight_kgs";
    }
}
class HiveReaderDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "hive.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + HiveEntry.TABLE_NAME + " (" +
                    HiveEntry._ID + " INTEGER PRIMARY KEY," +
                    HiveEntry.COLUMN_NAME_HIVE_ID + " INTEGER," +
                    HiveEntry.COLUMN_NAME_HIVE_NAME + " TEXT," +
                    HiveEntry.COLUMN_NAME_TIMESTAMP + " timestamp," +
                    HiveEntry.COLUMN_NAME_HIVE_WEIGHT_KGS+ " decimal(5,2))";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + HiveEntry.TABLE_NAME;

    public HiveReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}