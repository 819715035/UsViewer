package leltek.viewer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.corelibs.utils.ToastMgr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import leltek.viewer.model.CurrentPatientInfo;
import leltek.viewer.model.PatientInfo;
import leltek.viewer.model.PatientModel;

public class PatientActivity extends AppCompatActivity implements View.OnClickListener{

    final static Logger logger = LoggerFactory.getLogger(PatientActivity.class);

    @BindView(R.id.imgBack) ImageView imgBack;
    @BindView(R.id.txvSave) TextView txvSave;

    @BindView(R.id.edtMrn) TextView edtMrn;
    @BindView(R.id.edtLastName) TextView edtLastName;
    @BindView(R.id.edtFirstName) TextView edtFirstName;
    @BindView(R.id.edtBirthday) TextView edtBirthday;
    @BindView(R.id.edtPerform) TextView edtPerformBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        ButterKnife.bind(this);

        loadLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        PatientInfo pi = new PatientInfo(getApplicationContext());
        long id =  CurrentPatientInfo.getPatientId();
        PatientModel pm = pi.getPatientInfo(id);
        edtMrn.setText(pm.get_MRN());
        edtLastName.setText(pm.get_lastName());
        edtFirstName.setText(pm.get_firstName());
        edtBirthday.setText(pm.get_birthday());
        edtPerformBy.setText(pm.get_performedBy());
    }

    private void loadLayout(){
        imgBack.setOnClickListener(this);
        txvSave.setOnClickListener(this);
    }

    private boolean validateInput() {
        if (edtMrn.getText().toString().length() == 0) {
            ToastMgr.show("MRN is empty");
            return false;
        }
        if (edtLastName.getText().toString().length() == 0) {
            ToastMgr.show("Last Name is empty");
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgBack:
                finish();
                break;
            case R.id.txvSave:
                boolean saveDone = false;
                if (!validateInput())
                    break;
                PatientInfo pi = new PatientInfo(getApplicationContext());
                long id =  CurrentPatientInfo.getPatientId();
                PatientModel pm = pi.getPatientInfo(id);
                pm.set_MRN(edtMrn.getText().toString());
                pm.set_firstName(edtFirstName.getText().toString());
                pm.set_lastName(edtLastName.getText().toString());
                pm.set_birthday(edtBirthday.getText().toString());
                pm.set_performedBy(edtPerformBy.getText().toString());
                pi.updatePatientInfo(id, pm);
                ToastMgr.show("Successful to save");
                finish();
                break;
        }
    }
}
