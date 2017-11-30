package leltek.viewer.model;

public class CurrentPatientInfo {
    private static CurrentPatientInfo instance;
    private static long _patientId;
    private static long _indexId;

    private CurrentPatientInfo() {
    }

    public static synchronized void setPatientId(long id) {
        if (instance == null)
            instance = new CurrentPatientInfo();
        _patientId = id;
    }

    public static synchronized void setIndexId(long id) {
        if (instance == null)
            instance = new CurrentPatientInfo();
        _indexId = id;
    }

    public static long getPatientId() {
        return _patientId;
    }

    public static long getIndexIdx() {
        return _indexId;
    }
}