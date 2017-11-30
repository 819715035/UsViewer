package leltek.viewer.model;

import android.provider.BaseColumns;

/**
 * This class represents a contract for a table containing patient info index for
 * which to count rows.
 */
public final class IndexContract {

    /**
     * Contains the name of the table to create that contains the patient info index.
     */
    public static final String TABLE_NAME = "patientIndex";

    /**
     * Contains the SQL query to use to create the table containing the patient info index.
     */

    public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + IndexContract.TABLE_NAME +
            " ("+ IndexEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + IndexEntry.COL_PATIENT_ID + " INTEGER" + ");";
    /**
     * This class represents the rows for an entry in the patient info table. The
     * primary key is the _id column from the BaseColumn class.
     */
    public static abstract class IndexEntry implements BaseColumns {
        public static final String COL_PATIENT_ID = "patientId";
    }
}