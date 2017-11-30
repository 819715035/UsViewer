package leltek.viewer.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import leltek.viewer.model.PatientModel;

public class SqliteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "patientInfo.db";
    private static final int DATABASE_VERSION = 29;
    private static SqliteHelper sInstance;

    public static synchronized SqliteHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SqliteHelper(context);
        }
        return sInstance;
    }

    private SqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PatientContract.SQL_CREATE_TABLE);
        db.execSQL(IndexContract.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PatientContract.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + IndexContract.TABLE_NAME);
        onCreate(db);
    }

    public long insertInto(ContentValues cv) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(PatientContract.TABLE_NAME, null, cv);
    }

    public long insertIntoIndex(ContentValues cv) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(IndexContract.TABLE_NAME, null, cv);
    }

    public Cursor selectAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "select * from " + PatientContract.TABLE_NAME;
        Cursor result = db.rawQuery(query, null);
        return result;
    }

    public Cursor selectAllIndex() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + IndexContract.TABLE_NAME;
        Cursor result = db.rawQuery(query, null);
        return result;
    }

    public Cursor selectData(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + PatientContract.TABLE_NAME + " where " + PatientContract.PatientEntry._ID + "='" + id + "'";
        Cursor result = db.rawQuery(query, null);
        return result;
    }

    public Cursor selectIndex(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + IndexContract.TABLE_NAME + " where " + IndexContract.IndexEntry._ID + "='" + id + "'";
        Cursor result = db.rawQuery(query, null);
        return result;
    }

    public boolean updateData(ContentValues cv, long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(PatientContract.TABLE_NAME, cv, PatientContract.PatientEntry._ID + "=" + id, null) > 0;
    }

    public boolean updateIndex(ContentValues cv, long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.update(IndexContract.TABLE_NAME, cv, IndexContract.IndexEntry._ID + "=" + id, null) > 0;
    }

    public boolean deleteData(long id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(PatientContract.TABLE_NAME, PatientContract.PatientEntry._ID + "=" + id, null) > 0;
    }

    public boolean deleteIndex(long id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(IndexContract.TABLE_NAME, IndexContract.IndexEntry._ID + "=" + id, null) > 0;
    }
}