package leltek.viewer;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.corelibs.utils.ToastMgr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import leltek.viewer.model.CurrentPatientInfo;
import leltek.viewer.model.PatientContract;
import leltek.viewer.model.PatientInfo;
import leltek.viewer.model.PatientModel;
import leltek.viewer.model.SqliteHelper;
import leltek.viewer.helper.PrefrenceData;
import leltek.viewer.model.Probe;
import leltek.viewer.model.SimuProbe;
import leltek.viewer.model.SimuProbeLinear;
import leltek.viewer.model.WifiProbe;
import leltek.viewer.constants.AppConstant;
import leltek.viewer.customview.EllipseView;
import leltek.viewer.customview.MeasureView;
import leltek.viewer.customview.MovableEditText;
import leltek.viewer.customview.PopupwindowMeasure;
import leltek.viewer.helper.Utils;
import leltek.viewer.permission.CheckPermission;

public class ScanActivity extends AppCompatActivity
        implements Probe.ScanListener, Probe.CineBufferListener,
        Probe.InfoListener, PopupwindowMeasure.ICallback {
    final static Logger logger = LoggerFactory.getLogger(ScanActivity.class);
    FrameLayout annotateContainer;
    ArrayList<View> annotateEditText;
    FrameLayout capturedArea;
    private boolean mode_sim = false;
    private boolean mode_test = false;
    private Probe probe;
    private Button mToggleScan;
    private Button mBMode;
    private Button mCMode;
    private Button mMMode;
    private Button mFit;
    private UsImageView mImageView;
    private TextView mCineBufferCount;
    private Button mTestConnectionError;
    private Button mTestOverHeated;
    private Button mTestBatteryLow;
    private SeekBar mSeekBarGain;
    private SeekBar mSeekBarDr;
    private SeekBar mSeekBarTgc1;
    private SeekBar mSeekBarTgc2;
    private SeekBar mSeekBarTgc3;
    private SeekBar mSeekBarTgc4;
    private SeekBar mSeekBarPersistence;
    private SeekBar mSeekBarEnhancement;
    private Button mResetAllTgc;
    private NumberPicker mNumberPicker;
    private TextView mTextViewColorPrf;
    private Spinner mSpinnerColorPrf;
    private TextView mTextViewColorSensitivity;
    private SeekBar mSeekBarColorSensitivity;
    private TextView mTextViewColorAngle;
    private SeekBar mSeekBarColorAngle;
    private ProgressBar mProgressBarBattery;
    private Spinner mSpinnerFreq;
    private TextView txvAdv;
    private TextView txvCinebufcnt;
    private TextView txvGray;
    private TextView txvDr;
    private TextView txvTgc1;
    private TextView txvTgc2;
    private TextView txvTgc3;
    private TextView txvTgc4;
    private ImageView imgFull;
    private boolean mode_adv = false;
    private TextView mTextViewTgcBmode;
    private SeekBar mSeekBarTgcBmode;
    private TextView mTextViewTgcCmode;
    private SeekBar mSeekBarTgcCmode;

    private TextView txvPatientId;
    private SeekBar seekBarLoops;
    private TextView txvFrameNo;
    private RelativeLayout rytLoops;

    /**
     * Added by zhandong
     *
     * @param packageContext
     * @return
     */
    private ImageView imgMenu;
    private TextView txvEndExam;
    private LinearLayout lytMenu;
    private LinearLayout lytFreeze;
    private TextView txvAnnocate;
    private TextView txvSaveImage;
    private TextView txvMeasure;
    private NestedScrollView nestedScrollView;
    private Handler mHandler = new Handler();
    private View mView;
    private PopupMenu mPopupMenu;
    private Matrix mMatrix = null;

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, ScanActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.debug("onCreate() called");

        setContentView(R.layout.activity_scan);
        capturedArea = findViewById(R.id.capturedArea);
        annotateEditText = new ArrayList<>();
        annotateContainer = findViewById(R.id.annotateContainer);


        probe = ProbeSelection.simu ? (ProbeSelection.simuLinear ?
                SimuProbeLinear.getDefault() : SimuProbe.getDefault()) : WifiProbe.getDefault();
        probe.setScanListener(this);
        probe.setCineBufferListener(this);
        probe.setInfoListener(this);

        txvPatientId = findViewById(R.id.txvPatientId);
        seekBarLoops = findViewById(R.id.seekBarLoops);
        txvFrameNo = findViewById(R.id.txvFrameNo);
        rytLoops = findViewById(R.id.rytLoops);
        seekBarLoops.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                logger.debug("seekBarLoops.onProgressChanged {}", progress);
                txvFrameNo.setText("frame# " + Integer.toString(progress));
                Probe.Frame frame = probe.getFrameFromCineBuffer(progress);
                if (frame != null)
                    mImageView.setImageBitmap(frame.getBitmap());
                else
                    logger.debug("seekBarLoops.onProgressChanged seekBarLoopsnull frame");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        ////////////////////////////////////////////////////////////////////////
        // main image area for bmode/color in the center
        ////////////////////////////////////////////////////////////////////////
        mImageView = findViewById(R.id.image_view);

        mImageView.setImageListener(new UsImageView.ImageListener() {
            @Override
            public void onImageMatrixChanged() {
                float[] values = mImageView.getUsImageMatrixValues();
                float tranX = values[Matrix.MTRANS_X];
                float scaleX = values[Matrix.MSCALE_X];
                float tranY = values[Matrix.MTRANS_Y];
                float scaleY = values[Matrix.MSCALE_Y];
                for (View v : annotateEditText) {
                    if (v instanceof MovableEditText) {
                        MovableEditText met =  (MovableEditText)v;
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
                        params.leftMargin = (int)(met.scrollX * scaleX + tranX);
                        params.topMargin = (int)(met.scrollY * scaleY + tranY);
                        v.requestLayout();
                    }
                }
            }
        });

        ////////////////////////////////////////////////////////////////////////
        // information and full screen button area on the right
        ////////////////////////////////////////////////////////////////////////
        imgFull = findViewById(R.id.imgFull);


        ////////////////////////////////////////////////////////////////////////
        // control area on the left
        ////////////////////////////////////////////////////////////////////////
        mToggleScan = findViewById(R.id.toogle_scan);
        mToggleScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (probe.isLive()) {
                    probe.stopScan();
                    lytFreeze.setVisibility(View.VISIBLE);
                    nestedScrollView.setVisibility(View.GONE);
                } else {
                    annotateEditText.clear();
                    annotateContainer.removeAllViews();
                    probe.startScan();
                    lytFreeze.setVisibility(View.GONE);
                    nestedScrollView.setVisibility(View.VISIBLE);
                }
            }
        });

        mBMode = findViewById(R.id.bMode);
        mBMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                probe.switchToBMode();
            }
        });

        mCMode = findViewById(R.id.cMode);
        mCMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                probe.switchToCMode();
            }
        });

        mMMode = findViewById(R.id.mMode);
        mMMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                probe.switchToMMode();
            }
        });


        mSeekBarGain = findViewById(R.id.seekBarGain);
        mSeekBarGain.setProgress(probe.getGain());
        mSeekBarGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                probe.setGain(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mSeekBarPersistence = findViewById(R.id.seekBarPersistence);
        mSeekBarPersistence.setProgress(probe.getPersistence());
        //mSeekBarPersistence.setProgress(2);
        //probe.setPersistence(2);
        mSeekBarPersistence.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                probe.setPersistence(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarEnhancement = findViewById(R.id.seekBarEnhancement);
        mSeekBarEnhancement.setProgress(probe.getEnhanceLevel());
        //probe.setEnhanceLevel(2);
        mSeekBarEnhancement.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                probe.setEnhanceLevel(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mTextViewColorPrf = findViewById(R.id.textViewColorPrf);
        mSpinnerColorPrf = findViewById(R.id.spinnerColorPrf);
        mSpinnerColorPrf.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Probe.EnumColorPrf.values()));

        mSpinnerColorPrf.setSelection(((ArrayAdapter<Probe.EnumColorPrf>) mSpinnerColorPrf.getAdapter())
                .getPosition(probe.getColorPrf()));

        mSpinnerColorPrf.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Probe.EnumColorPrf newColorPrf = (Probe.EnumColorPrf) parent.getItemAtPosition(position);
                probe.setColorPrf(newColorPrf);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mTextViewColorPrf.setVisibility(View.GONE);
        mSpinnerColorPrf.setVisibility(View.GONE);

        mTextViewColorSensitivity = findViewById(R.id.textViewColorSensitivity);
        mTextViewColorSensitivity.setVisibility(View.GONE);


        mSeekBarColorSensitivity = findViewById(R.id.seekBarColorSensitivity);
        mSeekBarColorSensitivity.setVisibility(View.GONE);
        mSeekBarColorSensitivity.setProgress(probe.getColorSensitivity().getIntValue());
        mSeekBarColorSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                for (Probe.EnumColorSensitivity s : Probe.EnumColorSensitivity.values()) {
                    if (s.getIntValue() == progress) {
                        probe.setColorSensitivity(s);
                        logger.debug("Color Sensitivity: {}", s.toString());
                        break;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mTextViewColorAngle = findViewById(R.id.textViewColorAngle);
        mSeekBarColorAngle = findViewById(R.id.seekBarColorAngle);
        mSeekBarColorAngle.setMax(probe.getAllColorAngle().length-1);
        mSeekBarColorAngle.setProgress(Arrays.asList(probe.getAllColorAngle()).indexOf(probe.getColorAngle()));

        mSeekBarColorAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                probe.setColorAngle(probe.getAllColorAngle()[progress]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mTextViewColorAngle.setVisibility(View.GONE);
        mSeekBarColorAngle.setVisibility(View.GONE);


        mProgressBarBattery = findViewById(R.id.progressBarBattery);
        mSpinnerFreq = findViewById(R.id.spinnerFreq);
        mSpinnerFreq.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, probe.getAllFreq()));

        mSpinnerFreq.setSelection(((ArrayAdapter<Float>) mSpinnerFreq.getAdapter())
                .getPosition(probe.getFreq()));

        mSpinnerFreq.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                probe.setFreq((Float) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mTextViewTgcBmode = findViewById(R.id.textViewTgcBmode);
        mSeekBarTgcBmode = findViewById(R.id.seekBarTgcBmode);
        mSeekBarTgcBmode.setMax(probe.getTgcTableBmodeCount()-1);
        mSeekBarTgcBmode.setProgress(probe.getTgcTableBmodeIndex());
        mSeekBarTgcBmode.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                probe.setTgcTableBmodeIndex(progress);
                logger.debug("ScanActivity: TGC set to: {}", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mTextViewTgcCmode = findViewById(R.id.textViewTgcCmode);
        mSeekBarTgcCmode = findViewById(R.id.seekBarTgcCmode);
        mSeekBarTgcCmode.setMax(probe.getTgcTableCmodeCount()-1);
        mSeekBarTgcCmode.setProgress(probe.getTgcTableCmodeIndex());
        mSeekBarTgcCmode.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                probe.setTgcTableCmodeIndex(progress);
                logger.debug("ScanActivity: TGC set to: {}", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mTextViewTgcCmode.setVisibility(View.GONE);
        mSeekBarTgcCmode.setVisibility(View.GONE);


        ////////////////////////////////////////////////////////////////////////
        // txvAdv :
        // . when click, the following setting could be visible for internal test only
        // . toggle between GONE and visible for these setting for clean screen
        // . let txvAdv GONE for customer release
        // . default GONE for the advanced setting
        ////////////////////////////////////////////////////////////////////////
        txvAdv = findViewById(R.id.txvAdv);
        txvAdv.setVisibility(View.VISIBLE);
        txvAdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode_adv) {
                    mode_adv = false;
                    //txvAdv.setVisibility(View.VISIBLE);
                    txvCinebufcnt.setVisibility(View.VISIBLE);
                    mCineBufferCount.setVisibility(View.VISIBLE);
                    mNumberPicker.setVisibility(View.VISIBLE);
                    txvGray.setVisibility(View.VISIBLE);
                    txvDr.setVisibility(View.VISIBLE);
                    mSeekBarDr.setVisibility(View.VISIBLE);
                    txvTgc1.setVisibility(View.VISIBLE);
                    txvTgc2.setVisibility(View.VISIBLE);
                    txvTgc3.setVisibility(View.VISIBLE);
                    txvTgc4.setVisibility(View.VISIBLE);
                    mSeekBarTgc1.setVisibility(View.VISIBLE);
                    mSeekBarTgc2.setVisibility(View.VISIBLE);
                    mSeekBarTgc3.setVisibility(View.VISIBLE);
                    mSeekBarTgc4.setVisibility(View.VISIBLE);
                    mFit.setVisibility(View.VISIBLE);
                    mResetAllTgc.setVisibility(View.VISIBLE);
                    mTestConnectionError.setVisibility(View.VISIBLE);
                    mTestOverHeated.setVisibility(View.VISIBLE);
                    mTestBatteryLow.setVisibility(View.VISIBLE);
                } else {
                    mode_adv = true;
                    //txvAdv.setVisibility(View.VISIBLE);
                    txvCinebufcnt.setVisibility(View.GONE);
                    mCineBufferCount.setVisibility(View.GONE);
                    mNumberPicker.setVisibility(View.GONE);
                    txvGray.setVisibility(View.GONE);
                    txvDr.setVisibility(View.GONE);
                    mSeekBarDr.setVisibility(View.GONE);
                    txvTgc1.setVisibility(View.GONE);
                    txvTgc2.setVisibility(View.GONE);
                    txvTgc3.setVisibility(View.GONE);
                    txvTgc4.setVisibility(View.GONE);
                    mSeekBarTgc1.setVisibility(View.GONE);
                    mSeekBarTgc2.setVisibility(View.GONE);
                    mSeekBarTgc3.setVisibility(View.GONE);
                    mSeekBarTgc4.setVisibility(View.GONE);
                    mFit.setVisibility(View.GONE);
                    mResetAllTgc.setVisibility(View.GONE);
                    mTestConnectionError.setVisibility(View.GONE);
                    mTestOverHeated.setVisibility(View.GONE);
                    mTestBatteryLow.setVisibility(View.GONE);
                }
            }
        });

        // CineBufferCount display
        txvCinebufcnt = findViewById(R.id.txvCinebufcnt);
        txvCinebufcnt.setVisibility(View.GONE);
        mCineBufferCount = findViewById(R.id.cine_buffer_count);
        mCineBufferCount.setVisibility(View.GONE);

        // dynamice range
        txvDr = findViewById(R.id.txvDr);
        txvDr.setVisibility(View.GONE);
        mSeekBarDr = findViewById(R.id.seekBarDr);
        mSeekBarDr.setVisibility(View.GONE);
        mSeekBarDr.setProgress(probe.getDr());
        mSeekBarDr.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                probe.setDr(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        // gray map
        txvGray = findViewById(R.id.txvGray);
        txvGray.setVisibility(View.GONE);
        txvGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mNumberPicker = findViewById(R.id.np);
        mNumberPicker.setVisibility(View.GONE);
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(probe.getGrayMapMaxValue());
        mNumberPicker.setWrapSelectorWheel(false);
        mNumberPicker.setValue(probe.getGrayMap());
        mNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                probe.setGrayMap(newVal);
            }
        });

        ///////////////
        // digital tgc
        ///////////////
        txvTgc1 = findViewById(R.id.txvTgc1);
        txvTgc2 = findViewById(R.id.txvTgc2);
        txvTgc3 = findViewById(R.id.txvTgc3);
        txvTgc4 = findViewById(R.id.txvTgc4);

        mSeekBarTgc1 = findViewById(R.id.seekBarTgc1);
        mSeekBarTgc1.setProgress(probe.getTgc1());
        mSeekBarTgc1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                probe.setTgc1(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarTgc2 = findViewById(R.id.seekBarTgc2);
        mSeekBarTgc2.setProgress(probe.getTgc2());
        mSeekBarTgc2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                probe.setTgc2(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarTgc3 = findViewById(R.id.seekBarTgc3);
        mSeekBarTgc3.setProgress(probe.getTgc3());
        mSeekBarTgc3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                probe.setTgc3(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekBarTgc4 = findViewById(R.id.seekBarTgc4);
        mSeekBarTgc4.setProgress(probe.getTgc4());
        mSeekBarTgc4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                probe.setTgc4(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mResetAllTgc = findViewById(R.id.resetAllTgc);
        mResetAllTgc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                probe.resetAllTgc();
                mSeekBarTgc1.setProgress(probe.getTgc1());
                mSeekBarTgc2.setProgress(probe.getTgc2());
                mSeekBarTgc3.setProgress(probe.getTgc3());
                mSeekBarTgc4.setProgress(probe.getTgc4());
            }
        });

        // fit for width or height
//    if(mode_test==true) {
        mFit = findViewById(R.id.fit);
        mFit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFit();
            }
        });
//    }  // if(mode_test==true) {


        ///////////////
        // test mode
        ///////////////
        mTestConnectionError = findViewById(R.id.test_conn_error);
        mTestConnectionError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ProbeSelection.simu) {
                    if (ProbeSelection.simuLinear) {
                        ((SimuProbeLinear) probe).testConnectionClosed();
                    } else {
                        ((SimuProbe) probe).testConnectionClosed();
                    }
                } else {
                    ((WifiProbe) probe).testConnectionClosed();
                }
            }
        });

        mTestOverHeated = findViewById(R.id.test_over_heated);
        mTestOverHeated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ProbeSelection.simu) {
                    if (ProbeSelection.simuLinear) {
                        ((SimuProbeLinear) probe).testOverHeated();
                    } else {
                        ((SimuProbe) probe).testOverHeated();
                    }
                } else {
                    ((WifiProbe) probe).testOverHeated();
                }
            }
        });


        mTestBatteryLow = findViewById(R.id.test_battery_low);
        mTestBatteryLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ProbeSelection.simu) {
                    if (ProbeSelection.simuLinear) {
                        ((SimuProbeLinear) probe).testBatteryLevelTooLow();
                    } else {
                        ((SimuProbe) probe).testBatteryLevelTooLow();
                    }
                } else {
                    ((WifiProbe) probe).testBatteryLevelTooLow();
                }
            }
        });

        // initial gone
        txvTgc1.setVisibility(View.GONE);
        txvTgc2.setVisibility(View.GONE);
        txvTgc3.setVisibility(View.GONE);
        txvTgc4.setVisibility(View.GONE);
        mSeekBarTgc1.setVisibility(View.GONE);
        mSeekBarTgc2.setVisibility(View.GONE);
        mSeekBarTgc3.setVisibility(View.GONE);
        mSeekBarTgc4.setVisibility(View.GONE);
        mFit.setVisibility(View.GONE);
        mResetAllTgc.setVisibility(View.GONE);
        mTestConnectionError.setVisibility(View.GONE);
        mTestOverHeated.setVisibility(View.GONE);
        mTestBatteryLow.setVisibility(View.GONE);

        imgMenu = (ImageView) findViewById(R.id.imgMenu);
        txvEndExam = (TextView) findViewById(R.id.txvEndExam);

        imgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int[] location = new int[2];

                imgMenu.getLocationOnScreen(location);
                Point point = new Point();
                point.x = location[0];
                point.y = location[1];
                showPopup(point);
            }
        });

        txvEndExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PatientInfo pi = new PatientInfo(getApplicationContext());
                long id =  CurrentPatientInfo.getPatientId();
                PatientModel pm = pi.getPatientInfo(id);
                long endTime = System.currentTimeMillis();
                long startTime = pm.get_startTime();
                long duration = endTime - startTime;
                pm.set_duration(duration);
                boolean success = pi.updatePatientInfo(id, pm);
                if (success) {
                    ToastMgr.show("Successful to end exam.");
                    if (!pi.deletePatientIndex())
                        logger.debug("deletePatientIndex failed");
                    finish();
                } else
                    ToastMgr.show("Failed to end exam.");
            }
        });

        lytMenu = (LinearLayout) findViewById(R.id.lytMenu);
        imgFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (lytMenu.getVisibility() == View.GONE) {

                    lytMenu.setVisibility(View.VISIBLE);
                    imgFull.setImageDrawable(getDrawable(R.drawable.ic_svg_fullscreen));
                } else {
                    lytMenu.setVisibility(View.GONE);
                    imgFull.setImageResource(R.mipmap.svg_fullscreen_back);

                }

//                mImageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        });

        lytFreeze = findViewById(R.id.lytFreeze);
        txvAnnocate = findViewById(R.id.txvAnnotate);
        txvMeasure = findViewById(R.id.txvMeasure);
        txvSaveImage = findViewById(R.id.txvSaveImage);
        nestedScrollView = findViewById(R.id.scrolLive);

        txvAnnocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MovableEditText movableEditText = new MovableEditText(ScanActivity.this);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                float[] values = mImageView.getUsImageMatrixValues();
                float tranX = values[Matrix.MTRANS_X];
                float scaleX = values[Matrix.MSCALE_X];
                float tranY = values[Matrix.MTRANS_Y];
                float scaleY = values[Matrix.MSCALE_Y];
                layoutParams.leftMargin = 100;
                layoutParams.topMargin = 50;
                movableEditText.scrollX = (layoutParams.leftMargin - tranX)/scaleX;
                movableEditText.scrollY = (layoutParams.topMargin - tranY)/scaleY;
                movableEditText.setLayoutParams(layoutParams);
                movableEditText.setHint(getString(R.string.annotate));
                movableEditText.initializeCallback(new MovableEditText.ICallback() {
                    @Override
                    public void onLongPress(View view) {
                        mView = view;
                        mPopupMenu = new PopupMenu(ScanActivity.this, view);
                        mPopupMenu.getMenuInflater().inflate(R.menu.annotation, mPopupMenu.getMenu());
                        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch ( item.getItemId()) {
                                    case R.id.delete:
                                        annotateEditText.remove(mView);
                                        annotateContainer.removeView(mView);
                                        break;
                                }
                                mPopupMenu.dismiss();
                                return true;
                            }
                        });
                        mPopupMenu.show();
                    }
                    @Override
                    public void onBack(View view) {
                        view.setFocusable(false);
                        view.setBackgroundResource(R.color.transparent);
                    }
                    @Override
                    public void onSingleTap(View view) {
                        view.setFocusable(true);
                        view.setFocusableInTouchMode(true);
                        view.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(view, 0);
                        mView = view;
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                mView.requestLayout();
                            }
                        }, 10);
                    }
                    @Override
                    public float[] getUsImageMatrixValues() {
                        return mImageView.getUsImageMatrixValues();
                    }
                });
                movableEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            view.setFocusable(false);
                            view.setBackgroundResource(R.color.transparent);
                        }
                        return false;
                    }
                });
                annotateEditText.add(movableEditText);
                annotateContainer.addView(annotateEditText.get(annotateEditText.size() - 1));
                movableEditText.setSingleLine();
                movableEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(movableEditText, 0);
            }
        });

        txvMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PopupwindowMeasure().init(ScanActivity.this, view, ScanActivity.this);
            }
        });
        txvSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMatrix = new Matrix(mImageView.getUsImageMatrix());
                mImageView.fitHeight();
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (CheckPermission.isPermissionGranted(ScanActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE) && CheckPermission.isPermissionGranted(ScanActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Utils.saveImage(capturedArea, ScanActivity.this);
                            String newImage = PrefrenceData.getInstance().getNewImage();
                            PatientInfo pi = new PatientInfo(getApplicationContext());
                            long id =  CurrentPatientInfo.getPatientId();
                            PatientModel pm = pi.getPatientInfo(id);
                            pm.addImage(newImage);
                            pi.updatePatientInfo(id, pm);
                        } else {
                            Intent intent = new Intent(ScanActivity.this, CheckPermission.class);
                            intent.putExtra(CheckPermission.KEY, AppConstant.PERMISSION_SAVE_IMAGE);
                            startActivityForResult(intent, AppConstant.RESULT_CALLBACK);
                        }
                        mImageView.setUsImageMatrix(mMatrix);
                    }
                }, 10);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstant.RESULT_CALLBACK && resultCode == Activity.RESULT_OK && data != null) {
            if (!data.getBooleanExtra(AppConstant.IS_PERMISSION_GRANTED, false)) {
                Utils.saveImage(capturedArea, ScanActivity.this);
            }
        }
    }

    private void switchFit() {
        mImageView.switchFit();
        if (mImageView.isFitWidth()) {
            mFit.setText(R.string.fit_height);
        } else {
            mFit.setText(R.string.fit_width);
        }
    }

    void updatePatientId() {
        PatientInfo pi = new PatientInfo(getApplicationContext());
        if (!pi.updateCurrentPatientIndex()) {
            logger.debug("updatePatientId failed");
            return;
        }
        PatientModel pm = pi.getPatientInfo(CurrentPatientInfo.getPatientId());
        if (pm == null) {
            logger.debug("getCurrentPatientInfo failed");
            return;
        }
        txvPatientId.setText(pm.get_lastName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        logger.debug("onStart() called");
        updatePatientId();
    }

    @Override
    protected void onStop() {
        super.onStop();
        logger.debug("onStop() called");
        if (lytFreeze.getVisibility() == View.VISIBLE)
            return;
        probe.stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logger.debug("onDestroy() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        logger.debug("onPause() called");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        logger.debug("onResume() called");
        if (lytFreeze.getVisibility() == View.VISIBLE)
            return;
        if (probe.getMode() != Probe.EnumMode.MODE_B) {
            probe.switchToBMode();
        }
        if (probe.getMode() == Probe.EnumMode.MODE_B && !probe.isLive()) {
            probe.startScan();
        }
    }

    @Override
    public void onModeSwitched(Probe.EnumMode mode) {
        if (mode == Probe.EnumMode.MODE_B) {
            ToastMgr.show("switched to B mode");
            mTextViewTgcCmode.setVisibility(View.GONE);
            mTextViewTgcBmode.setVisibility(View.VISIBLE);
            mSeekBarTgcCmode.setVisibility(View.GONE);
            mSeekBarTgcBmode.setVisibility(View.VISIBLE);
            mTextViewColorPrf.setVisibility(View.GONE);
            mSpinnerColorPrf.setVisibility(View.GONE);
            mTextViewColorSensitivity.setVisibility(View.GONE);
            mSeekBarColorSensitivity.setVisibility(View.GONE);
            if(probe.getRPx() == 0 ) {
                mTextViewColorAngle.setVisibility(View.GONE);
                mSeekBarColorAngle.setVisibility(View.GONE);
            }
        } else if (mode == Probe.EnumMode.MODE_C) {
            ToastMgr.show("switched to Color mode");
            mTextViewTgcBmode.setVisibility(View.GONE);
            mTextViewTgcCmode.setVisibility(View.VISIBLE);
            mSeekBarTgcBmode.setVisibility(View.GONE);
            mSeekBarTgcCmode.setVisibility(View.VISIBLE);
            mTextViewColorPrf.setVisibility(View.VISIBLE);
            mSpinnerColorPrf.setVisibility(View.VISIBLE);
            mTextViewColorSensitivity.setVisibility(View.VISIBLE);
            mSeekBarColorSensitivity.setVisibility(View.VISIBLE);
            if(probe.getRPx() == 0 ) {
                mTextViewColorAngle.setVisibility(View.VISIBLE);
                mSeekBarColorAngle.setVisibility(View.VISIBLE);
            }
        } else if (mode == Probe.EnumMode.MODE_M) {
            ToastMgr.show("switched to M mode");
            mTextViewTgcCmode.setVisibility(View.GONE);
            mTextViewTgcBmode.setVisibility(View.VISIBLE);
            mSeekBarTgcCmode.setVisibility(View.GONE);
            mSeekBarTgcBmode.setVisibility(View.VISIBLE);
            mTextViewColorPrf.setVisibility(View.GONE);
            mSpinnerColorPrf.setVisibility(View.GONE);
            mTextViewColorSensitivity.setVisibility(View.GONE);
            mSeekBarColorSensitivity.setVisibility(View.GONE);
            if(probe.getRPx() == 0 ) {
                mTextViewColorAngle.setVisibility(View.GONE);
                mSeekBarColorAngle.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onModeSwitchingError() {
        ToastMgr.show("Switch mode failed");
    }

    @Override
    public void onConnectionError() {
        ToastMgr.show("Connection Closed");
        Intent intent = MainActivity.newIntent(this);
        startActivity(intent);
        finish();
    }

    @Override
    public void onScanStarted() {
        mToggleScan.setText(R.string.freeze);
        rytLoops.setVisibility(View.INVISIBLE);
        ToastMgr.show("Scan Started");
    }

    @Override
    public void onScanStopped() {
        mToggleScan.setText(R.string.live);
        int frameIndex = probe.getCineBufferSize()-1;
        seekBarLoops.setMax(frameIndex);
        seekBarLoops.setProgress(frameIndex);
        txvFrameNo.setText("frame# " + Integer.toString(frameIndex));
        rytLoops.setVisibility(View.VISIBLE);
        ToastMgr.show("Scan Stopped");
    }

    @Override
    public void onNewFrameReady(Probe.Frame frame, Bitmap bitmap) {
        if (frame.mode == probe.getMode()) {
            mImageView.setImageBitmap(bitmap);
        }

        Probe.CModeFrameData cModeFrameData = frame.cModeFrameData;
        if (cModeFrameData != null) {
            mImageView.setParams(cModeFrameData.originXPx, cModeFrameData.originYPx,
                    cModeFrameData.rPx);
        }
    }

    @Override
    public void onNewMmodeReady(byte[] line) {
        logger.debug("ScanActivity.onNewMmodeReady M mode");
    }

    @Override
    public void onScanlineMmodeSet(Integer newScanlineMmode) {

    }

    @Override
    public void onScanlineMmodeSetError(Integer oldScanlineMmode) {

    }

    @Override
    public void onDepthSet(Probe.EnumDepth newDepth) {

    }

    @Override
    public void onDepthSetError(Probe.EnumDepth oldDepth) {

    }

    @Override
    public void onFreqSet(Float newFreq) {

    }

    @Override
    public void onFreqSetError(Float oldFreq) {

    }

    @Override
    public void onColorPrfSet(Probe.EnumColorPrf newColorPrf) {

    }

    @Override
    public void onColorPrfSetError(Probe.EnumColorPrf oldColorPrf) {
        mSpinnerColorPrf.setSelection(((ArrayAdapter<Probe.EnumColorPrf>) mSpinnerColorPrf.getAdapter())
                .getPosition(oldColorPrf));
    }

    @Override
    public void onColorSensitivitySet(Probe.EnumColorSensitivity newColorSensitivity) {

    }

    @Override
    public void onColorSensitivitySetError(Probe.EnumColorSensitivity oldColorSensitivity) {
        mSeekBarColorSensitivity.setProgress(oldColorSensitivity.getIntValue());
    }

    @Override
    public void onColorAngleSet(Float newColorAngle) {
        mImageView.initRoi();
    }

    @Override
    public void onColorAngleSetError(Float oldColorAngle) {
        mSeekBarColorAngle.setProgress(Arrays.asList(probe.getAllColorAngle()).indexOf(oldColorAngle));
    }

    @Override
    public void onImageBufferOverflow() {
        ToastMgr.show("This hardware is not capable of image processing.");
    }

    @Override
    public void onCineBufferCountIncreased(int newCineBufferCount) {
        mCineBufferCount.setText(String.valueOf(newCineBufferCount));
    }

    @Override
    public void onCineBufferCleared() {
        mCineBufferCount.setText(String.valueOf(0));
    }

    @Override
    public void onBatteryLevelChanged(int newBatteryLevel) {
        // update battey level displayed on UI
        logger.debug("Battery level  is " + newBatteryLevel + "%");
        mProgressBarBattery.setProgress(newBatteryLevel > 100 ? 100 : newBatteryLevel);
    }

    @Override
    public void onBatteryLevelTooLow(int BatteryLevel) {
        ToastMgr.show("Battery level too low, now is " + BatteryLevel + "%");
    }

    @Override
    public void onTemperatureChanged(int newTemperature) {
        // update temperature displayed on UI
        //ToastMgr.show("Temperature  is " + newTemperature + "簞C");
        logger.debug("Temperature  is " + newTemperature + "簞C");
    }

    @Override
    public void onTemperatureOverHeated(int temperature) {
        ToastMgr.show("Temperature over heated, now is " + temperature + " 簞");
    }

    @Override
    public void onButtonPressed(int button) {
        //ToastMgr.show("Button pressed.");
        logger.debug("Button pressed {}", button);
        mToggleScan.performClick();
    }

    @Override
    public void onButtonReleased(int button) {
        //ToastMgr.show("Button released.");
        logger.debug("Button released {}", button);
    }


    /**
     * Add by zhandong
     */

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

                startActivity(new Intent(ScanActivity.this, PatientActivity.class));
                popupWindow.dismiss();
            }
        });

        txvSavedExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(ScanActivity.this, SavedExamActivity.class));
            }
        });

        txvCurrentExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(ScanActivity.this, "Current Exam", Toast.LENGTH_LONG).show();
            }
        });

        txvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(ScanActivity.this, "Setting", Toast.LENGTH_LONG).show();
            }
        });
        txvAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ScanActivity.this, "About", Toast.LENGTH_LONG).show();
            }
        });

        txvAbdomen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
            }
        });
        txvLung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
            }
        });

        txvOb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.dismiss();
            }
        });

    }

    @Override
    public void onPositionClick(int position) {
        switch (position) {
            case AppConstant.DISTANCE:
                ArrayList<View> removedView = new ArrayList<>();
                for (View view : annotateEditText) {
                    if (view instanceof EllipseView) {
                        removedView.add(view);
                        annotateContainer.removeView(view);
                    }
                }
                annotateEditText.removeAll(removedView);
                MeasureView measureView = new MeasureView(this);
                annotateEditText.add(measureView);
                annotateContainer.addView(annotateEditText.get(annotateEditText.size() - 1));
                break;
            case AppConstant.CLEAR_ALL:
                annotateEditText.clear();
                annotateContainer.removeAllViews();
                break;
            case AppConstant.ELLIPSE:
                if (annotateEditText.size() > 0) {
                    if (annotateEditText.get(annotateEditText.size() - 1) instanceof EllipseView)
                        return;
                }
                ArrayList<View> removedView1 = new ArrayList<>();
                for (View view : annotateEditText) {
                    if (view instanceof MeasureView) {
                        removedView1.add(view);
                        annotateContainer.removeView(view);
                    }
                }
                if (removedView1.size() > 0) {
                    annotateEditText.removeAll(removedView1);
                }
                EllipseView ellipseView = new EllipseView(this);
                annotateEditText.add(ellipseView);
                annotateContainer.addView(annotateEditText.get(annotateEditText.size() - 1));
        }
    }
}
