package leltek.viewer;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.corelibs.utils.ToastMgr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import leltek.viewer.model.CurrentPatientInfo;
import leltek.viewer.model.PatientContract;
import leltek.viewer.model.PatientInfo;
import leltek.viewer.model.PatientModel;
import leltek.viewer.model.SqliteHelper;
import leltek.viewer.adapter.SavedExamAdapter;
import leltek.viewer.helper.PrefrenceData;

import static leltek.viewer.SavedExamDetailActivity.EXTRA_PATIENT_ID;

public class SavedExamActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    final static Logger logger = LoggerFactory.getLogger(SavedExamActivity.class);


    private ListView lstSavedExam;
    private SavedExamAdapter adapter;
    private ImageView imgMenu;
    private TextView txvDelete;
    private int nSelected = 0;

    ArrayList<PatientModel> patientModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_exam);
        ButterKnife.bind(this);
        loadLayout();
    }

    private void loadLayout() {
        lstSavedExam = findViewById(R.id.lstSavedExam);
        lstSavedExam.setOnItemClickListener(this);
        lstSavedExam.setOnItemLongClickListener(this);
        imgMenu = findViewById(R.id.imgMenu);
        imgMenu.setOnClickListener(this);
        txvDelete = findViewById(R.id.txvDelete);
        txvDelete.setOnClickListener(this);
        updatePatientModels();

        adapter = new SavedExamAdapter(this, patientModels);
        lstSavedExam.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgMenu:

                int[] location = new int[2];

                imgMenu.getLocationOnScreen(location);
                Point point = new Point();
                point.x = location[0];
                point.y = location[1];
                showPopup(point);
                break;

            case R.id.txvDelete:
                int nRemoved = 0;
                for (int i = patientModels.size()-1; i >= 0; i--) {
                    View v = lstSavedExam.getChildAt(i);
                    SavedExamAdapter.PatientHolder ph = (SavedExamAdapter.PatientHolder)v.getTag();
                    if (ph.selected) {
                        v.setBackgroundColor(ph.background);
                        removeFromDB(i);
                        patientModels.remove(i);
                        nRemoved++;
                    }
                }
                if (nRemoved > 0) {
                    if (patientModels.size() == 0) {
                        logger.debug("delete remaining images");
                        logger.debug("SavedImage=" + PrefrenceData.getInstance().getSavedImage());
                        String[] images = PrefrenceData.getInstance().getSavedImage().split(",");
                        for (int i=0; i<images.length; i++) {
                            logger.debug("deleting " + images[i]);
                            File file = new File(images[i]);
                            if (file.exists())
                                if (file.delete())
                                    logger.debug("successful to delete");
                                else
                                    logger.debug("failed to delete");
                            else
                                logger.debug("file not exists");
                        }
                        PrefrenceData.getInstance().cleanSavedImage();
                    }                
                    adapter.refreshView(patientModels);
                    ToastMgr.show("delete done");
                }
                break;
        }
    }

    private void showPopup(Point p) {

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup, null);
        final PopupWindow popupWindow = new PopupWindow(this);
        popupWindow.setContentView(layout);
        popupWindow.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        int OFFSET_X = 30;
        int OFFSET_Y = 30;

        popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);

        TextView txvParentInfo = (TextView) layout.findViewById(R.id.txvEditParentInfo);
        TextView txvSavedExam = (TextView) layout.findViewById(R.id.txvSavedExam);
        TextView txvCurrentExam = (TextView) layout.findViewById(R.id.txvCurrentExam);
        TextView txvSetting = (TextView) layout.findViewById(R.id.txvSetting);
        TextView txvAbout = (TextView) layout.findViewById(R.id.txvAbout);
        TextView txvAbdomen = (TextView) layout.findViewById(R.id.txvAbdomen);
        TextView txvLung = (TextView) layout.findViewById(R.id.txvLung);
        TextView txvOb = (TextView) layout.findViewById(R.id.txvOb);

        txvParentInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(SavedExamActivity.this, PatientActivity.class));
                popupWindow.dismiss();
            }
        });

        txvSavedExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(SavedExamActivity.this, SavedExamActivity.class));
                popupWindow.dismiss();
            }
        });

        txvCurrentExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(SavedExamActivity.this, "Current Exam", Toast.LENGTH_LONG).show();
            }
        });

        txvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(SavedExamActivity.this, "Setting", Toast.LENGTH_LONG).show();
            }
        });
        txvAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SavedExamActivity.this, "About", Toast.LENGTH_LONG).show();
            }
        });

        txvAbdomen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
                finish();
            }
        });
        txvLung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
                finish();
            }
        });

        txvOb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
                finish();
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(this, SavedExamDetailActivity.class);
        PatientModel pm = patientModels.get(position);
        intent.putExtra(EXTRA_PATIENT_ID, pm.get_patientId());
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        View v = lstSavedExam.getChildAt(position);
        SavedExamAdapter.PatientHolder ph = (SavedExamAdapter.PatientHolder)v.getTag();
        ph.selected = !ph.selected;
        view.setBackgroundColor((ph.selected) ? Color.YELLOW : ph.background);
        if (ph.selected)
            nSelected++;
        else
            nSelected--;
        txvDelete.setVisibility((nSelected > 0) ? View.VISIBLE : View.GONE);
        return true;
    }

    private void removeFromDB(int idx) {
        PatientModel pm = patientModels.get(idx);
        SqliteHelper sqlite = SqliteHelper.getInstance(getApplicationContext());
        String[] images =  pm.get_imagePath().split(",");
        for (int i=0; i<images.length; i++) {
            File file = new File(images[i]);
            logger.debug("deleting " + images[i]);
            if (file.exists())
                if (file.delete())
                    logger.debug("successful to delete");
                else
                    logger.debug("failed to delete");
            else
                logger.debug("file not exists");
        }
        sqlite.deleteData(pm.get_patientId());
        if (pm.get_patientId() == CurrentPatientInfo.getPatientId()) {
            sqlite.deleteIndex(pm.get_patientId());
        }
    }

    private void updatePatientModels() {
        List<Long> ids = new ArrayList<Long>();
        SqliteHelper sqlite = SqliteHelper.getInstance(getApplicationContext());
        Cursor cursor = sqlite.selectAllData();
        patientModels.clear();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(cursor.getColumnIndex(PatientContract.PatientEntry._ID)));
            }
        }
        cursor.close();

        PatientInfo pi = new PatientInfo(getApplicationContext());
        for (Long id : ids) {
            PatientModel pm = pi.getPatientInfo(id);
            if (pm != null)
                patientModels.add(pm);
        }

        Collections.sort(patientModels, new Comparator<PatientModel>() {
            @Override
            public int compare(PatientModel pm1, PatientModel pm2)
            {
                return  (pm2.get_startTime() > pm1.get_startTime()) ? 1 : -1;
            }
        });
    }

}
