package com.kewenc.dynamicsprocessing;

import android.media.MediaPlayer;
import android.media.audiofx.DynamicsProcessing;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static android.os.Build.ID;

public class MainActivity extends AppCompatActivity {
    private static final String[] NEW_HZ_NAME = {"31HZ", "62HZ","125HZ", "250HZ",  "500HZ", "1KHZ", "2KHZ", "4KHZ", "8KHZ", "16KHZ"};
//    private static final String[] NEW_HZ_NAME = {"100HZ", "200HZ","400HZ", "600HZ",  "1KHZ", "3KHZ", "6KHZ", "12KHZ", "14KHZ", "16KHZ"};
    private MediaPlayer mMediaPlayer;
    private int sessionId = 0;
    private static final int EQ_MAX_VALUE = 10;
    private static final int HALF_EQ_MAX_VALUE = EQ_MAX_VALUE/2;
    private AppCompatSeekBar seekBars[];
    private TextView textViews[];
    private LinearLayout eqLayout;
    private AppCompatCheckBox eqEnableCheckBox;

    private boolean totalEnable = true;
    private static final int PRIORITY = Integer.MAX_VALUE;
    private DynamicsProcessing dp;
    private DynamicsProcessing.Eq eq;
    private static final int mVariant = 0;
    private static final int mChannelCount = 1;
    private static final int[] bandVal = {31, 62, 125, 250,  500, 1000, 2000, 4000, 8000, 16000};
//    private static final int[] bandVal = {220, 2220, 4220, 6220,  8220, 10220, 12220, 14220, 16220, 20000};
//    private static final int[] bandVal = {100, 200, 400, 600,  1000, 3000, 6000, 12000, 14000, 16000};
    private static final int maxBandCount = bandVal.length;
//    private static final int maxBandCount = 6;
    private int[] currentEqualizerValues;
    private TextView tvInputGain;
    private AppCompatSeekBar sbInputGain;
    private DynamicsProcessing.Mbc mbc;
    private DynamicsProcessing.Limiter limiter;
    private Equalizer equalizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMediaPlayer = MediaPlayer.create(this, R.raw.aa);
        sessionId = mMediaPlayer.getAudioSessionId();
        mMediaPlayer.start();

        addTenEqCount();

        equalizer = new Equalizer(PRIORITY,0);
        equalizer.setEnabled(false);

//        initDynamicsProcessing();
        setEffectEnable(totalEnable);

        eqEnableCheckBox = findViewById(R.id.eqEnableCheckBox);
        eqEnableCheckBox.setChecked(totalEnable);
        eqEnableCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setEqEnable(isChecked);
            }
        });

        tvInputGain = findViewById(R.id.tvInputGain);
        sbInputGain = findViewById(R.id.sbInputGain);
        sbInputGain.setMax(100);
        sbInputGain.setProgress(50);
        sbInputGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvInputGain.setText("InputGain:"+(progress-50));
                setInputGain(progress-50);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setEqEnable(boolean b){
        totalEnable = b;
        Log.d("TAGF","totalEnable="+totalEnable);
//        if (Build.VERSION.SDK_INT >= 28) {
//            if (dp != null){
//                dp.setEnabled(totalEnable);
//            }
//        }
        setEffectEnable(totalEnable);
//        if (dp != null) {
//            dp.setEnabled(totalEnable);
//            dp.release();
//            dp = null;
//        }
//        initDynamicsProcessing();
    }

    //Limiter常量
    private static final boolean LIMITER_DEFAULT_ENABLED = true;
    private static final int LIMITER_DEFAULT_LINK_GROUP = 0;//;
    private static final float LIMITER_DEFAULT_ATTACK_TIME = 1; // ms
    private static final float LIMITER_DEFAULT_RELEASE_TIME = 60; // ms
    private static final float LIMITER_DEFAULT_RATIO = 10; // N:1
    private static final float LIMITER_DEFAULT_THRESHOLD = -2; // dB
    private static final float LIMITER_DEFAULT_POST_GAIN = 0; // dB

    private void setEffectEnable(boolean isEnable) {
        if (Build.VERSION.SDK_INT >= 28) {
            if (dp != null) {
                dp.setEnabled(isEnable);//isEnable
                dp.release();
                dp = null;
            }
            if (isEnable) {
                DynamicsProcessing.Config.Builder builder = new DynamicsProcessing.Config.Builder(mVariant, mChannelCount, true, maxBandCount, true, maxBandCount, true, maxBandCount, true);
                dp = new DynamicsProcessing(PRIORITY, sessionId, builder.build());
                dp.setEnabled(true);//true

                setEffectEnableBase(true);//true
            }
        }
    }

    private void setEffectEnableBase(boolean isEnable) {
        if (Build.VERSION.SDK_INT >= 28) {
            if (dp != null) {
                dp.setEnabled(isEnable);//isEnable
                dp.release();
                dp = null;
            }
            if (isEnable) {
                DynamicsProcessing.Config.Builder builder = new DynamicsProcessing.Config.Builder(mVariant, mChannelCount, true, maxBandCount, true, maxBandCount, true, maxBandCount, true);
                dp = new DynamicsProcessing(PRIORITY, sessionId, builder.build());
                dp.setEnabled(true);//true

                if (dp != null) {
                    dp.setEnabled(false);//false
                    dp.release();
                    dp = null;
                }
                initDynamicsProcessing();
            }
        }
    }

    private void initDynamicsProcessing() {
        if (Build.VERSION.SDK_INT >= 28) {
            if (dp == null){
                DynamicsProcessing.Config.Builder builder = new DynamicsProcessing.Config.Builder(mVariant, mChannelCount, true, maxBandCount, true, maxBandCount, true, maxBandCount, true);
                dp = new DynamicsProcessing(PRIORITY, sessionId, builder.build());
                dp.setEnabled(totalEnable);

                eq = new DynamicsProcessing.Eq(true, true, maxBandCount);
                eq.setEnabled(totalEnable);

                mbc = new DynamicsProcessing.Mbc(true,true, maxBandCount);
                mbc.setEnabled(totalEnable);

                limiter = new DynamicsProcessing.Limiter(true,LIMITER_DEFAULT_ENABLED, LIMITER_DEFAULT_LINK_GROUP, LIMITER_DEFAULT_ATTACK_TIME, LIMITER_DEFAULT_RELEASE_TIME, LIMITER_DEFAULT_RATIO, LIMITER_DEFAULT_THRESHOLD, LIMITER_DEFAULT_POST_GAIN);
                limiter.setEnabled(totalEnable);
            }
            try {
                for (int i=0;i<maxBandCount;i++){
                    eq.getBand(i).setCutoffFrequency(bandVal[i]);
                    setBandGain(i,currentEqualizerValues[i]);

                    mbc.getBand(i).setCutoffFrequency(bandVal[i]);

                }
                dp.setPreEqAllChannelsTo(eq);
                dp.setMbcAllChannelsTo(mbc);
                dp.setPostEqAllChannelsTo(eq);
                dp.setLimiterAllChannelsTo(limiter);
            } catch (Exception e){
                Toast.makeText(this,"initDynamicsProcessing Exception",Toast.LENGTH_SHORT).show();
                Log.e("TAGF","initDynamicsProcessing Exception");
                e.printStackTrace();
            }
        }
    }

    private void addTenEqCount() {
        eqLayout = findViewById(R.id.eqLayout);
        seekBars = new AppCompatSeekBar[10];
        textViews = new TextView[10];
        currentEqualizerValues = new int[10];
        for (int i=0;i<10;i++){
            seekBars[i] = new AppCompatSeekBar(this);
            seekBars[i].setMax(EQ_MAX_VALUE);
            seekBars[i].setProgress(HALF_EQ_MAX_VALUE);
            final int finalI = i;
            seekBars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    currentEqualizerValues[finalI] = progress - HALF_EQ_MAX_VALUE;
                    textViews[finalI].setText(NEW_HZ_NAME[finalI] + "-DB:"+currentEqualizerValues[finalI]);
                    setBandGain(finalI,currentEqualizerValues[finalI]);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            currentEqualizerValues[i] = 0;
            textViews[i] = new TextView(this);
            textViews[i].setText(NEW_HZ_NAME[i] + "-DB:"+currentEqualizerValues[i]);
            linearLayout.addView(textViews[i]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = 50;
            linearLayout.addView(seekBars[i],params);
            eqLayout.addView(linearLayout);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null){
            mMediaPlayer.pause();
            mMediaPlayer.release();
        }
        if (dp != null){
            dp.setEnabled(false);
            dp.release();
            dp = null;
        }
        if (equalizer != null) {
            equalizer.setEnabled(false);
            equalizer.release();
            equalizer = null;
        }
    }

    public boolean setBandGain(int band, int level) {
        if (currentEqualizerValues == null) return false;
        if (Build.VERSION.SDK_INT >= 28) {
            currentEqualizerValues[band] = level;
            if (dp != null && eq != null){
                try {
                    eq.getBand(band).setEnabled(true);
                    eq.getBand(band).setGain(currentEqualizerValues[band]);
                    dp.setPreEqBandAllChannelsTo(band,eq.getBand(band));
                    dp.setPostEqBandAllChannelsTo(band,eq.getBand(band));
                    return true;
                } catch (UnsupportedOperationException e){
                    Log.e("TAGF","setBandGain_Exception2!");
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private void setInputGain(float val){
        if (Build.VERSION.SDK_INT >= 28) {
            if (dp != null)
                dp.setInputGainAllChannelsTo(val);
        }
    }
}
