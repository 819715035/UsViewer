package leltek.viewer.model;

import android.provider.BaseColumns;

/**
 * This class represents a contract for a table containing patient info for
 * which to count rows.
 */
public final class PatientContract {

    /**
     * Contains the name of the table to create that contains the patient info.
     */
    public static final String TABLE_NAME = "patientInfo";

    /**
     * Contains the SQL query to use to create the table containing the patient info.
     */

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + PatientContract.TABLE_NAME +
            " ("+ PatientEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + PatientEntry.COL_MRN + " TEXT,"
            + PatientEntry.COL_FIRST_NAME + " TEXT,"
            + PatientEntry.COL_LAST_NAME + " TEXT,"
            + PatientEntry.COL_BIRTHDAY + " TEXT,"
            + PatientEntry.COL_DATE + " TEXT,"
            + PatientEntry.COL_TIME + " TEXT,"
            + PatientEntry.COL_DURATION + " INTEGER,"
            + PatientEntry.COL_START_TIME + " INTEGER,"
            + PatientEntry.COL_IMAGE_PATH + " TEXT,"
            + PatientEntry.COL_PERFORMED_BY + " TEXT" + ");";
    /**
     * This class represents the rows for an entry in the patient info table. The
     * primary key is the _id column from the BaseColumn class.
     */
    public static abstract class PatientEntry implements BaseColumns {
        public static final String COL_MRN = "MRN";
        public static final String COL_FIRST_NAME = "firstName";
        public static final String COL_LAST_NAME = "lastName";
        public static final String COL_BIRTHDAY = "birthday";
        public static final String COL_DATE = "date";
        public static final String COL_TIME = "time";
        public static final String COL_START_TIME = "startTime";
        public static final String COL_DURATION = "duration";
        public static final String COL_IMAGE_PATH = "imagePath";
        public static final String COL_PERFORMED_BY = "performedBy";
    }
}