package edu.vt.ece.onaire;

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

public class SimpleSettingsActivity extends AppCompatActivity {

    private static SeekBar seekBar_B;
    private static TextView tv_B;
    private int B;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_settings);
        seekBar_B  = (SeekBar) findViewById(R.id.seekBar_B);
        B = getIntent().getIntExtra("B",0);
        /*if (savedInstanceState!=null && savedInstanceState.containsKey("B")) {
            B = savedInstanceState.getInt("B");
        }
        else{
            B =0; //assigning B as 0 to denote the incorrect behavior
        }*/
        seekBar_B.setProgress(B);
        tv_B = (TextView)findViewById(R.id.textView_settings_B);
        tv_B.setText(getString(R.string.break_even_def_text)+"\t\t"+Integer.toString(B));
        seekBar_B.setOnSeekBarChangeListener((new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                B = progress;
                tv_B.setText(getString(R.string.break_even_def_text)+"\t\t"+Integer.toString(B));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        }));
    }

    private int getBFromIntent() {
        return getIntent().getIntExtra("B",0);
    }


    @Override
    public void onBackPressed() {
        //TODO: Need to evaluate between sending result vs preferences. Currently pref is preferred.
        Intent result = new Intent();
        result.putExtra("B",B);
        setResult(Activity.RESULT_OK,result);
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt("B",B).apply();
        super.onBackPressed();
    }
}
