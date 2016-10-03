package com.valkyrie.nabeshimamac.lightsout.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ViewFlipper;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.valkyrie.nabeshimamac.lightsout.MyApplication;
import com.valkyrie.nabeshimamac.lightsout.R;
import com.valkyrie.nabeshimamac.lightsout.manager.PreferencesManager;

/**
 * tutorial画面のActivity
 */
public class TutorialActivity extends AppCompatActivity {
    private ViewFlipper viewFlipper;
    private Button buttonTutorial;


    public static Intent createIntent(Context context) {
        return new Intent(context, TitleActivity.class);
    }

    //チュートリアル部分

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        buttonTutorial = (Button) findViewById(R.id.buttonTutorial);

        Bundle bundle = new Bundle();
        ((MyApplication) getApplication()).analytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN, bundle);
    }

    public void next(View v) {
        if (viewFlipper.getDisplayedChild() < 3) {
            viewFlipper.showNext();
        } else {
            finish();
        }
        if (viewFlipper.getDisplayedChild() == 3) {
            PreferencesManager.getInstance(this).checkTutorialEnd();

        }
    }
}
