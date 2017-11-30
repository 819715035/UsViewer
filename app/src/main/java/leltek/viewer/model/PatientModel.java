package leltek.viewer.model;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by puma on 9/7/2017.
 */

public class PatientModel implements Serializable{

    private long _patientId = 0;
    private String _MRN = "";
    private String _firstName = "";
    private String _lastName = "";
    private String _birthday = "";
    private String _performedBy = "";
    private String _date = "";
    private String _time = "";
    private long _startTime = 0;
    private long _duration = 0;
    private String _imagePath = "";

    public PatientModel() {};

    public PatientModel(String _MRN, String _firstName, String _lastName, String _birthday, String _performedBy){

        this._MRN = _MRN;
        this._firstName = _firstName;
        this._lastName = _lastName;
        this._birthday = _birthday;
        this._performedBy = _performedBy;
    }

    public long get_patientId() {
        return _patientId;
    }
    public void set_patientId(long _patientId) {
        this._patientId = _patientId;
    }

    public String get_MRN() {
        return _MRN;
    }
    public void set_MRN(String _MRN) {
        this._MRN = _MRN;
    }

    public String get_firstName() {
        return _firstName;
    }
    public void set_firstName(String _firstName) {
        this._firstName = _firstName;
    }

    public String get_lastName() {
        return _lastName;
    }
    public void set_lastName(String _lastName) {
        this._lastName = _lastName;
    }

    public String get_birthday() {
        return _birthday;
    }
    public void set_birthday(String _birthday) {
        this._birthday = _birthday;
    }

    public String get_performedBy() {
        return _performedBy;
    }
    public void set_performedBy(String _performedBy) { this._performedBy = _performedBy; }

    public String get_time() {
        return _time;
    }
    public void set_time(String _time) {
        this._time = _time;
    }

    public String get_date() {
        return _date;
    }
    public void set_date(String _date) {
        this._date = _date;
    }

    public String get_imagePath() {
        return _imagePath;
    }
    public void set_imagePath(String _imagePath) {
        this._imagePath = _imagePath;
    }
    
    public long get_startTime() {
        return _startTime;
    }
    public void set_startTime(long _startTime) {
        this._startTime = _startTime;
    }
    
    public long get_duration() {
        return _duration;
    }
    public void set_duration(long _duration) {
        this._duration = _duration;
    }

    public void addImage(String path) {
        _imagePath =  (_imagePath.length() > 0) ? _imagePath.concat(",").concat(path) : path;
    }
}
