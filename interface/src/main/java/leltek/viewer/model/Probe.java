package leltek.viewer.model;

import android.graphics.Bitmap;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Probe interface
 */
public interface Probe {

    final static Logger logger = LoggerFactory.getLogger(Probe.class);

    /**
     * 回傳目前的版本
     *
     * @return version 版本
     */
    String getVersion();

    /**
     * 設定InitializeListener
     *
     * @param systemListener systemListener
     */
    void setSystemListener(SystemListener systemListener);

    /**
     * 執行初始化：讀assets下cfg目錄的設定檔，目連結device。
     * SystemListener.onInitialized()表示初始成功
     * SystemListener.onInitializationError表示初始失敗
     */
    void initialize();

    /**
     * @return true 如果已經連上device
     */
    boolean isConnected();

    /**
     * @return true 如果已經正在向device下指令
     */
    boolean isRequesting();

    /**
     * 設定InfoListener
     *
     * @param infoListener infoListener
     */
    void setInfoListener(InfoListener infoListener);

    /**
     * @return 0~100 (%), null代表尚未取得device的值
     */
    Integer getBatteryLevel();

    /**
     * @return 0~100 (°C), null代表尚未取得device的值
     */
    Integer getTemperature();

    /**
     * 設定CineBufferListener
     *
     * @param cineBufferListener cineBufferListener
     */
    void setCineBufferListener(CineBufferListener cineBufferListener);

    /**
     * 從CineBuffer取得frame
     *
     * @param index 從0開始
     * @return 只有在非live時才有值，live時會回傳null
     */
    Frame getFrameFromCineBuffer(int index);

    /**
     * 從CineBuffer取得frame size
     *
     * @return cine buffer size
     */
    int getCineBufferSize();

    /**
     * 設定ScanListener
     *
     * @param scanListener scanListener
     */
    void setScanListener(ScanListener scanListener);

    /**
     * 試圖將scan mode切換為B mode
     * 由ScanListener.onModeSwitched(EnumMode mode), 且mode等於MODE_B表示切換成功
     * 由ScanListener.onModeSwitchingError()表示切換失敗
     */
    void switchToBMode();

    /**
     * 試圖將scan mode切換為M mode
     * 由ScanListener.onModeSwitched(EnumMode mode), 且mode等於MODE_M表示切換成功
     * 由ScanListener.onModeSwitchingError()表示切換失敗
     */
    void switchToMMode();

    /**
     * 試圖將scan mode切換為C mode
     * 由ScanListener.onModeSwitched(String mode), 且mode等於MODE_C表示切換成功
     * 由ScanListener.onModeSwitchingError()表示切換失敗
     */
    void switchToCMode();

    /**
     * 回傳目前的scan mode
     *
     * @return "B"表示B mode, "M"表示M mode, "C"表示color mode
     */
    EnumMode getMode();

    /**
     * 要求開始scan
     * 由ScanListener.onScanStarted()表示開始scan成功
     */
    void startScan();

    /**
     * 要求停止scan
     * 由ScanListener.onScanStopped()表示停止scan成功
     */
    void stopScan();

    /**
     * @return true 如果正在scan
     */
    boolean isLive();

    /**
     * 取得目前的frame rate, 單位為 Hz
     *
     * @return frame rate
     */
    int getFrameRate();

    /**
     * 取得所有可用的freq, 單位為 MHz
     *
     * @return the all of the possible freq
     */
    Float[] getAllFreq();

    /**
     * 取得目前的freq, 單位為 MHz
     *
     * @return Freq
     */
    float getFreq();

    /**
     * 要求設定freq
     * 由ScanListener.onFreqSet()表示設定成功
     * 由ScanListener.onFreqSetError表示設定失敗
     *
     * @param newFreq 合理值 2.6 ~ 12
     */
    void setFreq(Float newFreq);

    /**
     * 取得所有可用的B mode Tgc Delay
     *
     * @return the all of the possible B mode tgc delay
     */
    Integer[] getAllTgcDelayBmode();

    /**
     * 取得目前的B mode Tgc Delay
     *
     * @return B mode tgc delay
     */
    Integer getTgcDelayBmode();

    /**
     * 要求設定B mode Tgc Delay
     * 由ScanListener.onTgcDelayBmodeSet()表示設定成功
     * 由ScanListener.onTgcDelayBmodeSetError表示設定失敗
     *
     * @param newDelay 合理值 2.6 ~ 12
     */
    void setTgcDelayBmode(Integer newDelay);

    /**
     * 取得所有可用的C mode Tgc Delay
     *
     * @return the all of the possible C mode tgc delay
     */
    Integer[] getAllTgcDelayCmode();

    /**
     * 取得目前的C mode Tgc Delay
     *
     * @return C mode tgc delay
     */
    Integer getTgcDelayCmode();

    /**
     * 要求設定C mode Tgc Delay
     * 由ScanListener.onTgcDelayCmodeSet()表示設定成功
     * 由ScanListener.onTgcDelayCmodeSetError表示設定失敗
     *
     * @param newDelay 合理值 2.6 ~ 12
     */
    void setTgcDelayCmode(Integer newDelay);

    /**
     * 取得目前的B mode Tgc Table Count
     *
     * @return B mode tgc table count
     */
    int getTgcTableBmodeCount();

    /**
     * 取得目前的B mode Tgc Table Index
     *
     * @return B mode tgc table index
     */
    int getTgcTableBmodeIndex();

    /**
     * 要求設定B mode Tgc Table Index
     * 由ScanListener.onTgcTableBmodeSet()表示設定成功
     * 由ScanListener.onTgcTableBmodeSetError()表示設定失敗
     *
     * @param index 合理值 0 ~ TgcTableBmodeCount-1
     */
    void setTgcTableBmodeIndex(int index);

    /**
     * 取得目前的C mode Tgc Table Count
     *
     * @return C mode tgc table count
     */
    int getTgcTableCmodeCount();

    /**
     * 取得目前的C mode Tgc Table Index
     *
     * @return C mode tgc table index
     */
    int getTgcTableCmodeIndex();

    /**
     * 要求設定C mode Tgc Table Index
     * 由ScanListener.onTgcTableCmodeSet()表示設定成功
     * 由ScanListener.onTgcTableCmodeSetError()表示設定失敗
     *
     * @param index 合理值 0 ~ TgcTableCmodeCount-1
     */
    void setTgcTableCmodeIndex(int index);




    /**
     * 取得目前M mode的scanline: 0 ~ 127
     *
     * @return scanlineMmode
     */
    int getScanlineMmode();

    /**
     * 要求設定M mode的scanline
     * 由ScanListener.onScanlineMmodeSet()表示設定成功
     * 由ScanListener.onScanlineMmodeSetError表示設定失敗
     *
     * @param scanline 合理值 0 ~ 127
     */
    void setScanlineMmode(Integer scanline);

    /**
     * 取得目前的depth, 單位為 mm
     *
     * @return depth
     */
    EnumDepth getDepth();

    /**
     * 要求設定depth
     * 由ScanListener.onDepthSet()表示設定成功
     * 由ScanListener.onDepthSetError表示設定失敗
     *
     * @param newDepth 合理值 LinearDepth_32, LinearDepth_63 (mm) for linear,
     *                 ConvexDepth_126, ConvexDepth_189 (mm) for convex
     */
    void setDepth(EnumDepth newDepth);

    /**
     * 取得目前的gain
     *
     * @return gain
     */
    int getGain();

    /**
     * 設定gain
     *
     * @param gain 合理值 0 ~ 100
     */
    void setGain(int gain);

    /**
     * 取得目前的dynamic range
     *
     * @return dynamic range
     */
    int getDr();

    /**
     * 設定dynamic range
     *
     * @param dr 合理值 0 ~ 100
     */
    void setDr(int dr);

    /**
     * 取得目前的gray map編號
     *
     * @return gray map編號
     */
    int getGrayMap();

    /**
     * 設定gray map
     *
     * @param grayMap 合理值 0 ~ GrayMapMaxValue
     */
    void setGrayMap(int grayMap);

    /**
     * 取得最大的gray map編號
     *
     * @return 最大的gray map編號
     */
    int getGrayMapMaxValue();

    /**
     * 取得目前的persistence設定值
     *
     * @return persistence
     */
    int getPersistence();

    /**
     * 設定persistence
     *
     * @param persistence 合理值 0 ~ 4
     */
    void setPersistence(int persistence);

    /**
     * 取得目前的image enhance level count
     *
     * @return image enhance level count
     */
    int getEnhanceLevelCount();

    /**
     * 取得目前的image enhance level
     *
     * @return image enhance level
     */
    int getEnhanceLevel();

    /**
     * 設定image enhance level
     *
     * @param enhanceLevel 合理值0 ~ 4
     */
    void setEnhanceLevel(int enhanceLevel);

    /**
     * 取得目前第一組TGC的值
     *
     * @return 目前第一組TGC的值
     */
    int getTgc1();

    /**
     * 設定第一組TGC
     *
     * @param tgc1 合理值0 ~ 100
     */
    void setTgc1(int tgc1);

    /**
     * 取得目前第二組TGC的值
     *
     * @return 目前第二組TGC的值
     */
    int getTgc2();

    /**
     * 設定第二組TGC
     *
     * @param tgc2 合理值0 ~ 100
     */
    void setTgc2(int tgc2);

    /**
     * 取得目前第三組TGC的值
     *
     * @return 目前第三組TGC的值
     */
    int getTgc3();

    /**
     * 設定第三組TGC
     *
     * @param tgc3 合理值0 ~ 100
     */
    void setTgc3(int tgc3);

    /**
     * 取得目前第四組TGC的值
     *
     * @return 目前第四組TGC的值
     */
    int getTgc4();

    /**
     * 設定第四組TGC
     *
     * @param tgc4 合理值0 ~ 100
     */
    void setTgc4(int tgc4);

    /**
     * 將全部的TGC設為50
     */
    void resetAllTgc();

    /**
     * 取得目前的color gain
     *
     * @return gain
     */
    int getColorGain();

    /**
     * 設定color gain
     *
     * @param colorGain 合理值 0 ~ 100
     */
    void setColorGain(int colorGain);

    /**
     * 取得目前的color persistence設定值
     *
     * @return color persistence
     */
    int getColorPersistence();

    /**
     * 設定persistence
     *
     * @param colorPersistence 合理值 0 ~ 4
     */
    void setColorPersistence(int colorPersistence);

    /**
     * 取得目前的color PRF設定值
     *
     * @return color PRF
     */
    EnumColorPrf getColorPrf();

    /**
     * 設定color PRF
     *
     * @param newColorPrf 合理值 2.27, 2.78, 3.57 (kHz)
     */
    void setColorPrf(EnumColorPrf newColorPrf);

    /**
     * 取得目前的color Sensitivity設定值
     *
     * @return color Sensitivity
     */
    EnumColorSensitivity getColorSensitivity();

    /**
     * 設定color Sensitivity
     *
     * @param newColorSensitivity 合理值 1~8
     */
    void setColorSensitivity(EnumColorSensitivity newColorSensitivity);

    /**
     * 取得所有可用的color angle, 單位為degree
     *
     * @return the all of the possible color angle
     */
    Float[] getAllColorAngle();

    /**
     * 取得目前的color Angle
     *
     * @return color Angle
     */
    Float getColorAngle();

    /**
     * 設定color Angle
     *
     * @param newColorAngle 合理值以EnumColorAngle定義
     */
    void setColorAngle(Float newColorAngle);

    /**
     * 取得目前的color Angle tangent
     *
     * @return color Angle tan
     */
    float getColorAngleTan();

    /**
     * 回傳底層image bitmap的寬度
     *
     * @return image bitmap的寬度 (pixel)
     */
    int getImageWidthPx();

    /**
     * 回傳底層image bitmap的高度
     *
     * @return image bitmap的高度 (pixel)
     */
    int getImageHeightPx();

    /**
     * 用BModePreset來設定系統的B mode參數
     *
     * @param preset preset
     */
    void setByBModePreset(BModePreset preset);

    /**
     * 取得convex半徑
     *
     * @return r convex半徑 (pixel), 如果是linear則回傳0
     */
    float getRPx();

    /**
     * 取得convex角度
     *
     * @return theta convex角度 (degree), 如果是linear則回傳0
     */
    float getTheta();

    /**
     * 取得convex 原點x軸坐標
     *
     * @return originXPx convex 原點x軸坐標 (pixel), 如果是linear則回傳0
     */
    float getOriginXPx();

    /**
     * 取得convex 原點y軸坐標
     *
     * @return originYPx convex 原點y軸坐標 (pixel), 如果是linear則回傳0
     */
    float getOriginYPx();

    /**
     * 設定linear ROI data
     */
    void setLinearRoiData(float roiXPx, float roiYPx, float roiX2Px, float roiY2Px, float roiAngleTan);

    /**
     * 設定convex ROI data
     */
    void setConvexRoiData(float roiStartRPx, float roiEndRPx, float roiStartTheta, float roiEndTheta);


    enum EnumMode {
        MODE_B,
        MODE_M,
        MODE_C
    }

    enum EnumDepth {
        //LinearDepth_32,
        LinearDepth_63,
        ConvexDepth_126,
        ConvexDepth_189
    }

    /**
     * 定義Color PRF的合理值
     */
    enum EnumColorPrf {
        ColorPrf_1000("1"),
        ColorPrf_2000("2"),
        ColorPrf_2270("2.27"),
        ColorPrf_2780("2.78"),
        ColorPrf_3570("3.57");

        private final String strValue;

        EnumColorPrf(String strValue) {
            this.strValue = strValue;
        }

        @Override
        public String toString() {
            return strValue;
        }
    }

    /**
     * 定義Color Sensitivity的合理值
     */
    enum EnumColorSensitivity {
        ColorSensitivity_1(0),
        ColorSensitivity_2(1),
        ColorSensitivity_3(2),
        ColorSensitivity_4(3),
        ColorSensitivity_5(4),
        ColorSensitivity_6(5),
        ColorSensitivity_7(6),
        ColorSensitivity_8(7);

        private final int intValue;

        EnumColorSensitivity(int intValue) {
            this.intValue = intValue;
        }

        public int getIntValue() {
            return intValue;
        }
    }

    /**
     * 定義Color Angle的合理值
     */
    enum EnumColorAngle {
        ColorAngle_Minus8("-8"),
        ColorAngle_Minus4("-4"),
        ColorAngle_0("0"),
        ColorAngle_4("4"),
        ColorAngle_8("8");

        private final String strValue;

        EnumColorAngle(String strValue) {
            this.strValue = strValue;
        }

        @Override
        public String toString() {
            return strValue;
        }
    }

    /**
     * SystemListener
     */
    interface SystemListener {

        /**
         * 初始成功
         */
        void onInitialized();

        /**
         * 初始失敗
         */
        void onInitializationError(String message);

        void onInitialingLowVoltageError(String message);

        /**
         * 系統錯誤
         */
        void onSystemError(String message);
    }

    /**
     * InfoListener
     */
    interface InfoListener {

        /**
         * device battery level發生改變，可用來更新UI的battery level
         *
         * @param newBatteryLevel newBatteryLevel
         */
        void onBatteryLevelChanged(int newBatteryLevel);

        /**
         * battery level太低，可用來提醒user
         *
         * @param BatteryLevel BatteryLevel
         */
        void onBatteryLevelTooLow(int BatteryLevel);

        /**
         * device溫度發生改變，可用來更新UI的temperature
         *
         * @param newTemperature newTemperature
         */
        void onTemperatureChanged(int newTemperature);

        /**
         * device溫度太高，可用來提醒user
         *
         * @param temperature temperature
         */
        void onTemperatureOverHeated(int temperature);

        /**
         * the freeze/unfreeze button pressed
         *
         * @param button button
         */
        void onButtonPressed(int button);

        /**
         * the freeze/unfreeze button released
         *
         * @param button button
         */
        void onButtonReleased(int button);

    }

    /**
     * CineBufferListener
     */
    interface CineBufferListener {

        /**
         * Cine buffer的個數增加，可用來更新UI的buffer個數
         *
         * @param newCineBufferCount newCineBufferCount
         */
        void onCineBufferCountIncreased(int newCineBufferCount);

        /**
         * Cine buffer的個數清除為0，可用來更新UI的buffer個數
         */
        void onCineBufferCleared();
    }

    /**
     * ScanListener
     */
    interface ScanListener {

        /**
         * scan mode切換成功
         *
         * @param mode Mode_B表示切換到B mode, Mode_C表示切換到color mode
         */
        void onModeSwitched(EnumMode mode);

        /**
         * scan mode切換失敗
         */
        void onModeSwitchingError();

        /**
         * 跟device之間的connection發生error
         */
        void onConnectionError();

        /**
         * device開始scan
         */
        void onScanStarted();

        /**
         * device停止scan
         */
        void onScanStopped();

        /**
         * scan進行中，傳回接收到的frame
         *
         * @param frame 接收到的frame
         */
        void onNewFrameReady(Frame frame, Bitmap bitmap);

        /**
         * scan進行中，傳回接收到的M mode scanline
         *
         * @param line 接收到的scanline的內容：byte[512]
         */
        void onNewMmodeReady(byte[] line);

        /**
         * 設定depth成功
         *
         * @param newDepth newDepth
         */
        void onDepthSet(EnumDepth newDepth);

        /**
         * 設定depth失敗
         *
         * @param oldDepth oldDepth
         */
        void onDepthSetError(EnumDepth oldDepth);

        /**
         * 設定freq成功
         *
         * @param newFreq newFreq
         */
        void onFreqSet(Float newFreq);

        /**
         * 設定freq失敗
         *
         * @param oldFreq oldFreq
         */
        void onFreqSetError(Float oldFreq);

//        /**
//         * 設定tgcDelayBmode成功
//         *
//         * @param newTgcDelayBmode newTgcDelayBmode
//         */
//        void onTgcDelayBmodeSet(Float newTgcDelayBmode);
//
//        /**
//         * 設定tgcDelayBmode失敗
//         *
//         * @param oldTgcDelayBmode oldTgcDelayBmode
//         */
//        void onTgcDelayBmodeSetError(Float oldTgcDelayBmode);
//
//        /**
//         * 設定tgcDelayCmode成功
//         *
//         * @param newTgcDelayCmode newTgcDelayCmode
//         */
//        void onTgcDelayCmodeSet(Float newTgcDelayCmode);
//
//        /**
//         * 設定tgcDelayCmode失敗
//         *
//         * @param oldTgcDelayCmode oldTgcDelayCmode
//         */
//        void onTgcDelayCmodeSetError(Float oldTgcDelayCmode);
        /**
         * 設定tgcTableBmode成功
         *
         * @param newTgcTableBmode newTgcTableBmode
         */
        default void onTgcTableBmodeSet(Integer newTgcTableBmode) {
	    // do nothing
	    logger.debug("onTgcTableBmodeSet: "+newTgcTableBmode);
	}

        /**
         * 設定tgcTableBmode失敗
         *
         * @param oldTgcTableBmode oldTgcTableBmode
         */
        default void onTgcTableBmodeSetError(Integer oldTgcTableBmode) {
	    // do nothing
	    logger.debug("onTgcTableBmodeSetError: "+oldTgcTableBmode);
	}

        /**
         * 設定tgcTableCmode成功
         *
         * @param newTgcTableCmode newTgcTableCmode
         */
        default void onTgcTableCmodeSet(Integer newTgcTableCmode) {
	    // do nothing
	    logger.debug("onTgcTableCmodeSet: "+newTgcTableCmode);
	}

        /**
         * 設定tgcTableCmode失敗
         *
         * @param oldTgcTableCmode oldTgcTableCmode
         */
        default void onTgcTableCmodeSetError(Integer oldTgcTableCmode) {
	    // do nothing
	    logger.debug("onTgcTableCmodeSetError: "+oldTgcTableCmode);
	}


        /**
         * 設定M mode scanline成功
         *
         * @param newScanlineMmode
         */
        void onScanlineMmodeSet(Integer newScanlineMmode);

        /**
         * 設定M mode scanline失敗
         *
         * @param oldScanlineMmode
         */
        void onScanlineMmodeSetError(Integer oldScanlineMmode);

        /**
         * 設定color PRF成功
         *
         * @param newColorPrf newColorPrf
         */
        void onColorPrfSet(EnumColorPrf newColorPrf);

        /**
         * 設定color PRF失敗
         *
         * @param oldColorPrf oldColorPrf
         */
        void onColorPrfSetError(EnumColorPrf oldColorPrf);

        /**
         * 設定color Sensitivity成功
         *
         * @param newColorSensitivity newColorSensitivity
         */
        void onColorSensitivitySet(EnumColorSensitivity newColorSensitivity);

        /**
         * 設定color Sensitivity失敗
         *
         * @param oldColorSensitivity oldColorSensitivity
         */
        void onColorSensitivitySetError(EnumColorSensitivity oldColorSensitivity);

        /**
         * 設定color Angle成功
         *
         * @param newColorAngle newColorAngle
         */
        void onColorAngleSet(Float newColorAngle);

        /**
         * 設定color Angle失敗
         *
         * @param oldColorAngle oldColorAngle
         */
        void onColorAngleSetError(Float oldColorAngle);

        /**
         * 當發生此事件時，代表此硬體來不及做影像後處理，底層會將image丟掉
         */
        void onImageBufferOverflow();
    }

    /**
     * B Mode Frame Data
     */
    class BModeFrameData {
        public int gain;
        public int dr;
        public int grayMap;
        public int persistence;
        public int enhanceLevel;
    }

    /**
     * C Mode Frame Data
     */
    class CModeFrameData {
        public int colorGain;
        public int colorPersistence;
        public EnumColorPrf colorPrf;
        public EnumColorSensitivity colorSensitivity;
        public Float colorAngle;
        public float originXPx;
        public float originYPx;
        public float rPx;
    }

    /**
     * Frame class
     */
    abstract class Frame {

        public EnumMode mode;
        public Date date;
        public int frameRate;

        public Float freq;
        public EnumDepth depth;

        public BModeFrameData bModeFrameData;

        public CModeFrameData cModeFrameData;

        public byte[] rawImage;

        public Frame(Probe probe, byte[] rawImage, EnumMode mode) {
            this.mode = mode;
            this.date = new Date();
            this.frameRate = probe.getFrameRate();
            this.freq = probe.getFreq();
            this.depth = probe.getDepth();
            this.bModeFrameData = new BModeFrameData();

            bModeFrameData.gain = probe.getGain();
            bModeFrameData.dr = probe.getDr();
            bModeFrameData.grayMap = probe.getGrayMap();
            bModeFrameData.persistence = probe.getPersistence();
            bModeFrameData.enhanceLevel = probe.getEnhanceLevel();

            if (this.mode == EnumMode.MODE_C) {
                this.cModeFrameData = new CModeFrameData();
                cModeFrameData.colorGain = probe.getColorGain();
                cModeFrameData.colorPersistence = probe.getColorPersistence();
                cModeFrameData.colorPrf = probe.getColorPrf();
                cModeFrameData.colorSensitivity = probe.getColorSensitivity();
                cModeFrameData.colorAngle = probe.getColorAngle();
                cModeFrameData.originXPx = probe.getOriginXPx();
                cModeFrameData.originYPx = probe.getOriginYPx();
                cModeFrameData.rPx = probe.getRPx();
            }

            this.rawImage = rawImage;
        }

        public abstract Bitmap getBitmap();
    }

    class BModePreset {
        public EnumDepth depth;
        public int gain;
        public int dr;
        public int grayMap;
        public int persistence;
        public int enhanceLevel;

        public BModePreset(EnumDepth depth, int gain, int dr, int grayMap, int persistence, int enhanceLevel) {
            this.depth = depth;
            this.gain = gain;
            this.dr = dr;
            this.grayMap = grayMap;
            this.persistence = persistence;
            this.enhanceLevel = enhanceLevel;
        }
    }
}
