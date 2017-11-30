package leltek.viewer;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

import leltek.viewer.model.CurrentPatientInfo;
import leltek.viewer.model.PatientInfo;
import leltek.viewer.model.PatientModel;
import leltek.viewer.adapter.SavedImageAdapter;
import leltek.viewer.helper.PrefrenceData;

public class SavedExamDetailActivity extends AppCompatActivity implements View.OnClickListener, SavedImageAdapter.ICallBack {

    final static Logger logger = LoggerFactory.getLogger(SavedExamDetailActivity.class);

    public static final String EXTRA_PATIENT_ID = "patientId";

    @BindView(R.id.txvId) TextView txvId;
    @BindView(R.id.txvMrn) TextView txvMrn;
    @BindView(R.id.txvDOB) TextView txvDOB;
    @BindView(R.id.txvDate) TextView txvDate;
    @BindView(R.id.txvTime) TextView txvTime;
    @BindView(R.id.txvDuration) TextView txvDuration;

    private ImageView imgMenu;
    private ImageView imgFull;
    private RecyclerView lytImage;
    private LinearLayout lytinfo;
    TextView noImageText;
    SavedImageAdapter savedImageAdapter;
    ImageView selectedImage;
    long patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_exam_detail);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        patientId = intent.getLongExtra(EXTRA_PATIENT_ID, 0);

        loadLayout();
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
            case R.id.imgFull:

                if (lytImage.getVisibility() == View.VISIBLE) {

                    lytImage.setVisibility(View.GONE);
                    lytinfo.setVisibility(View.GONE);
                    imgFull.setImageResource(R.mipmap.svg_fullscreen_back);
                } else {
                    lytImage.setVisibility(View.VISIBLE);
                    lytinfo.setVisibility(View.VISIBLE);
                    imgFull.setImageDrawable(getDrawable(R.drawable.ic_svg_fullscreen));
                }

                break;
        }
    }

    private void loadLayout() {
        selectedImage = findViewById(R.id.selectedImage);
        imgMenu = (ImageView) findViewById(R.id.imgMenu);
        imgMenu.setOnClickListener(this);
        noImageText = findViewById(R.id.noImageText);
        imgFull = (ImageView) findViewById(R.id.imgFull);
        imgFull.setOnClickListener(this);
        lytinfo = (LinearLayout) findViewById(R.id.lytInfo);
        lytImage = (RecyclerView) findViewById(R.id.lytImage);
        
        PatientInfo pi = new PatientInfo(getApplicationContext());
        PatientModel pm = pi.getPatientInfo(patientId);
        if (pm ==null)
            return;

        String[] images =  pm.get_imagePath().split(",");
        savedImageAdapter = new SavedImageAdapter(images, this);
        lytImage.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        lytImage.setAdapter(savedImageAdapter);
        if (images.length > 0 && !images[0].isEmpty()) {
            noImageText.setVisibility(View.GONE);
            Picasso.with(this).load(new File(images[0])).into(selectedImage);
        }

        String id = pm.get_lastName();
        if (pm.get_firstName().length() > 0)
            id += ", " + pm.get_firstName();
        txvId.setText(id);
        txvMrn.setText(pm.get_MRN());
        txvDOB.setText(pm.get_birthday());
        txvDate.setText(pm.get_date());
        txvTime.setText(pm.get_time());
        if (pm.get_duration() == 0)
            txvDuration.setText("In Progress");
        else
            txvDuration.setText("" + pm.get_duration()/1000/60 + " minutes");
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

                startActivity(new Intent(SavedExamDetailActivity.this, PatientActivity.class));
                popupWindow.dismiss();
            }
        });

        txvSavedExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(SavedExamDetailActivity.this, SavedExamActivity.class));
                popupWindow.dismiss();
            }
        });

        txvCurrentExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(SavedExamDetailActivity.this, "Current Exam", Toast.LENGTH_LONG).show();
            }
        });

        txvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(SavedExamDetailActivity.this, "Setting", Toast.LENGTH_LONG).show();
            }
        });
        txvAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SavedExamDetailActivity.this, "About", Toast.LENGTH_LONG).show();
            }
        });

        txvAbdomen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
                gotoBImageActivity();
            }
        });
        txvLung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
                gotoBImageActivity();
            }
        });

        txvOb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
                gotoBImageActivity();
            }
        });

    }

    private void gotoBImageActivity() {

        startActivity(new Intent(this, ScanActivity.class));
        finish();
    }

    @Override
    public void showImage(String image) {
        noImageText.setVisibility(View.GONE);
        Picasso.with(this).load(new File(image)).into(selectedImage);
    }
}
