package com.manual;


import android.app.Activity;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import android.util.Range;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.eszdman.photoncamera.R;
import com.eszdman.photoncamera.app.PhotonCamera;
import com.eszdman.photoncamera.processing.parameters.IsoExpoSelector;
import com.eszdman.photoncamera.ui.camera.CameraFragment;
import com.eszdman.photoncamera.util.Timer;
import com.manual.model.*;

/**
 * Created by Vibhor on 10/08/2020
 */
public final class ManualModeImpl implements ManualMode {

    private static final String TAG = "ManualModeImpl";
    private final Activity activity;
    private final ManualModel.ValueChangedEvent evchanged = new ManualModel.ValueChangedEvent() {
        @Override
        public void onValueChanged(String value) {
            evTextview.post(() -> evTextview.setText(value));
        }
    };
    private final ManualModel.ValueChangedEvent expochanged = new ManualModel.ValueChangedEvent() {
        @Override
        public void onValueChanged(String value) {
            expoTextView.post(() -> expoTextView.setText(value));
        }
    };
    private final ManualModel.ValueChangedEvent isochanged = new ManualModel.ValueChangedEvent() {
        @Override
        public void onValueChanged(String value) {
            isoTextview.post(() -> isoTextview.setText(value));
        }
    };
    private KnobView defaultKnobView;
    private ImageButton exposureButton, isoButton, focusButton, evButton;
    private LinearLayout buttons_container;
    private FrameLayout knob_container;
    private ManualModel mfModel, isoModel, expotimeModel, evModel, selectedModel;
    private TextView mfTextView, isoTextview, expoTextView, evTextview;
    private final ManualModel.ValueChangedEvent mfchanged = new ManualModel.ValueChangedEvent() {
        @Override
        public void onValueChanged(String value) {
            mfTextView.post(() -> mfTextView.setText(value));

        }
    };

    ManualModeImpl(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void init() {
        if (activity == null)
            return;
        initialiseDataMembers();
        addKnobs();
        setupOnClickListeners();
    }

    @Override
    public boolean isManualMode() {
        return !(getCurrentExposureValue() == -1.0 && getCurrentFocusValue() == -1.0 && getCurrentISOValue() == -1.0 && getCurrentEvValue() == 0);
    }

    @Override
    public double getCurrentExposureValue() {
        return expotimeModel.getCurrentInfo().value;
    }

    @Override
    public double getCurrentISOValue() {
        return isoModel.getCurrentInfo().value;
    }

    @Override
    public double getCurrentFocusValue() {
        return mfModel.getCurrentInfo().value;
    }

    @Override
    public double getCurrentEvValue() {
        return evModel.getCurrentInfo().value;
    }

    @Override
    public void retractAllKnobs() {
        defaultKnobView.setVisibility(View.GONE);
        knob_container.setVisibility(View.GONE);
        defaultKnobView.resetKnob();
        selectedModel = null;
        mfModel.resetModel();
        expotimeModel.resetModel();
        isoModel.resetModel();
        evModel.resetModel();
    }

    @Override
    public void rotate(int orientation, int duration) {
        if (defaultKnobView != null) {
            defaultKnobView.setKnobItemsRotation(Rotation.fromDeviceOrientation(orientation));
            for (int i = 0; i < buttons_container.getChildCount(); i++) {
                buttons_container.getChildAt(i).animate().rotation(orientation).setDuration(duration).start();
            }
        }
    }

    private void initialiseDataMembers() {
        knob_container = activity.findViewById(R.id.knob_container);
        buttons_container = activity.findViewById(R.id.buttons_container);
        focusButton = activity.findViewById(R.id.focus_option);
        exposureButton = activity.findViewById(R.id.exposure_option);
        isoButton = activity.findViewById(R.id.iso_option);
        evButton = activity.findViewById(R.id.ev_option);
        mfTextView = activity.findViewById(R.id.focus_option_tv);
        evTextview = activity.findViewById(R.id.ev_option_tv);
        expoTextView = activity.findViewById(R.id.exposure_option_tv);
        isoTextview = activity.findViewById(R.id.iso_option_tv);
    }

    private void addKnobs() {
        Timer timer = Timer.InitTimer(TAG,"addKnobs");
        knob_container.removeAllViews();
        defaultKnobView = new KnobView(activity);

        CameraCharactersticsOldWay aClass = new CameraCharactersticsOldWay();
        mfModel = new FocusModel(aClass.focusRange, mfchanged);
        evModel = new EvModel(aClass.evRange, evchanged);
        isoModel = new IsoModel(aClass.isoRange, isochanged);
        expotimeModel = new ShutterModel(aClass.expRange, expochanged);

        aClass.logIt();
        knob_container.addView(defaultKnobView);
        knob_container.setVisibility(View.GONE);
        timer.endTimer();
    }

    private void setupOnClickListeners() {

        focusButton.setOnClickListener(v -> {
            setModelToKnob(mfModel);
        });
        focusButton.setOnLongClickListener(v -> {
            if (selectedModel == mfModel)
                defaultKnobView.resetKnob();
            mfModel.resetModel();
            return true;
        });
        exposureButton.setOnClickListener(v -> {
            setModelToKnob(expotimeModel);
        });
        exposureButton.setOnLongClickListener(v -> {
            if (selectedModel == expotimeModel)
                defaultKnobView.resetKnob();
            expotimeModel.resetModel();
            return true;
        });
        isoButton.setOnClickListener(v -> {
            setModelToKnob(isoModel);
        });
        isoButton.setOnLongClickListener(v -> {
            if (selectedModel == isoModel)
                defaultKnobView.resetKnob();
            isoModel.resetModel();
            return true;
        });
        evButton.setOnClickListener(v -> {
            setModelToKnob(evModel);
        });
        evButton.setOnLongClickListener(v -> {
            if (selectedModel == evModel)
                defaultKnobView.resetKnob();
            evModel.resetModel();
            return true;
        });

        mfModel.fireValueChangedEvent(mfModel.getAutoModel().text);
        evModel.fireValueChangedEvent(evModel.getAutoModel().text);
        expotimeModel.fireValueChangedEvent(expotimeModel.getAutoModel().text);
        isoModel.fireValueChangedEvent(isoModel.getAutoModel().text);
    }

    private void setModelToKnob(ManualModel modelToKnob) {
        if (modelToKnob == selectedModel) {
//            defaultKnobView.resetKnob();
            defaultKnobView.setKnobViewChangedListener(null);
            defaultKnobView.setVisibility(View.GONE);
            knob_container.setVisibility(View.GONE);
            selectedModel = null;
        } else {
            //defaultKnobView.resetKnob();
            if (modelToKnob.getKnobInfoList().size() > 1) {
                defaultKnobView.setKnobViewChangedListener(modelToKnob);
                defaultKnobView.setKnobInfo(modelToKnob.getKnobInfo());
                defaultKnobView.setKnobItems(modelToKnob.getKnobInfoList());
                defaultKnobView.setTickByValue(modelToKnob.getCurrentInfo().value);
                defaultKnobView.setVisibility(View.VISIBLE);
                knob_container.setVisibility(View.VISIBLE);
                selectedModel = modelToKnob;
            }
        }
    }

    static class CameraCharactersticsOldWay {
        CameraCharacteristics cameraCharacteristics = CameraFragment.mCameraCharacteristics;
        Float minFocal = CameraFragment.mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        Float maxFocal = CameraFragment.mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE);
        Range<Float> focusRange = (!(minFocal == null || maxFocal == null || minFocal == 0.0f)) ? new Range<>(Math.min(minFocal, maxFocal), Math.max(minFocal, maxFocal)) : null;
        Range<Integer> isoRange = new Range<>(IsoExpoSelector.getISOLOWExt(), IsoExpoSelector.getISOHIGHExt());
        Range<Long> expRange = new Range<>(IsoExpoSelector.getEXPLOW(), IsoExpoSelector.getEXPHIGH());
        Range<Float> evRange = new Range<>(cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE).getLower().floatValue(), cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE).getUpper().floatValue());

        public void logIt() {
            String lens = PhotonCamera.getSettings().mCameraID;
            Log.d(TAG, "focusRange(" + lens + ") : " + (focusRange == null ? "Fixed [" + maxFocal + "]" : focusRange.toString()));
            Log.d(TAG, "isoRange(" + lens + ") : " + isoRange.toString());
            Log.d(TAG, "expRange(" + lens + ") : " + expRange.toString());
            Log.d(TAG, "evCompRange(" + lens + ") : " + evRange.toString());
        }

    }
}
