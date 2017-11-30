package leltek.viewer.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class PatientInfo {

    final static Logger logger = LoggerFactory.getLogger(PatientInfo.class);

    Context context;
    SimpleDateFormat fDate = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat fTime = new SimpleDateFormat("HH:mm:ss");

    public PatientInfo(Context context) {
        this.context = context;
    }

    public PatientModel getPatientInfo(long id) {
        SqliteHelper sqlite = SqliteHelper.getInstance(context);
        Cursor cursor = sqlite.selectData(id);
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        cursor.moveToNext();
        PatientModel pm = new PatientModel();
        pm.set_patientId(cursor.getLong(cursor.getColumnIndex(PatientContract.PatientEntry._ID)));
        pm.set_MRN(cursor.getString(cursor.getColumnIndex(PatientContract.PatientEntry.COL_MRN)));
        pm.set_firstName(cursor.getString(cursor.getColumnIndex(PatientContract.PatientEntry.COL_FIRST_NAME)));
        pm.set_lastName(cursor.getString(cursor.getColumnIndex(PatientContract.PatientEntry.COL_LAST_NAME)));
        pm.set_birthday(cursor.getString(cursor.getColumnIndex(PatientContract.PatientEntry.COL_BIRTHDAY)));
        pm.set_date(cursor.getString(cursor.getColumnIndex(PatientContract.PatientEntry.COL_DATE)));
        pm.set_time(cursor.getString(cursor.getColumnIndex(PatientContract.PatientEntry.COL_TIME)));
        pm.set_startTime(cursor.getLong(cursor.getColumnIndex(PatientContract.PatientEntry.COL_START_TIME)));
        pm.set_duration(cursor.getLong(cursor.getColumnIndex(PatientContract.PatientEntry.COL_DURATION)));
        pm.set_imagePath(cursor.getString(cursor.getColumnIndex(PatientContract.PatientEntry.COL_IMAGE_PATH)));
        pm.set_performedBy(cursor.getString(cursor.getColumnIndex(PatientContract.PatientEntry.COL_PERFORMED_BY)));
        
        cursor.close();
        return pm;
    }

    private ContentValues getCV(PatientModel pm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(PatientContract.PatientEntry.COL_MRN, pm.get_MRN());
        contentValues.put(PatientContract.PatientEntry.COL_FIRST_NAME, pm.get_firstName());
        contentValues.put(PatientContract.PatientEntry.COL_LAST_NAME, pm.get_lastName());
        contentValues.put(PatientContract.PatientEntry.COL_BIRTHDAY, pm.get_birthday());
        contentValues.put(PatientContract.PatientEntry.COL_DATE, pm.get_date());
        contentValues.put(PatientContract.PatientEntry.COL_TIME, pm.get_time());
        contentValues.put(PatientContract.PatientEntry.COL_START_TIME, pm.get_startTime());
        contentValues.put(PatientContract.PatientEntry.COL_DURATION, pm.get_duration());
        contentValues.put(PatientContract.PatientEntry.COL_IMAGE_PATH, pm.get_imagePath());
        contentValues.put(PatientContract.PatientEntry.COL_PERFORMED_BY, pm.get_performedBy());
        return contentValues;
    }

    public long addPatientInfo(PatientModel pm) {
        SqliteHelper sqlite = SqliteHelper.getInstance(context);
        return sqlite.insertInto(getCV(pm));
    }

    public boolean updatePatientInfo(long id, PatientModel pm) {
        SqliteHelper sqlite = SqliteHelper.getInstance(context);
        return sqlite.updateData(getCV(pm), id);
    }

    public long addPatientIndex(long id) {
        SqliteHelper sqlite = SqliteHelper.getInstance(context);
        ContentValues contentValues = new ContentValues();
        contentValues.put(IndexContract.IndexEntry.COL_PATIENT_ID, id);
        return sqlite.insertIntoIndex(contentValues);
    }

    public boolean deletePatientIndex() {
        SqliteHelper sqlite = SqliteHelper.getInstance(context);
        long indexId = CurrentPatientInfo.getIndexIdx();
        return sqlite.deleteIndex(indexId);
    }

    private long createCurrentPatient() {
        SqliteHelper sqlite = SqliteHelper.getInstance(context);
        PatientModel pm = new PatientModel();
        Random rand = new Random();
        pm.set_MRN("" + (rand.nextInt() & Integer.MAX_VALUE));
        pm.set_lastName("Quick ID");
        long startTime = System.currentTimeMillis();
        pm.set_startTime(startTime);
        Date startDate = new Date(startTime);
        String date = fDate.format(startDate);
        String time = fTime.format(startDate);
        pm.set_date(date);
        pm.set_time(time);
        long id = addPatientInfo(pm);
        if (id < 0) {
            logger.debug("addPatientInfo failed");
            return id;
        }
        logger.debug("createCurrentPatient successful");
        return id;
    }

    public boolean updateCurrentPatientIndex() {
        long indexId;
        long patientId;
        SqliteHelper sqlite = SqliteHelper.getInstance(context);
        Cursor cursor = sqlite.selectAllIndex();
        if (cursor.getCount() == 0) {
            cursor.close();
            patientId = createCurrentPatient();
            if (patientId < 0)
                return false;
            indexId = addPatientIndex(patientId);
            if (indexId < 0)
                return false;
        } else {
            cursor.moveToNext();
            indexId = cursor.getInt(cursor.getColumnIndex(IndexContract.IndexEntry._ID));
            patientId = cursor.getInt(cursor.getColumnIndex(IndexContract.IndexEntry.COL_PATIENT_ID));
            cursor.close();
        }
        logger.debug("patientId=" + patientId);
        CurrentPatientInfo.setIndexId(indexId);
        CurrentPatientInfo.setPatientId(patientId);
        return true;
    }
}
