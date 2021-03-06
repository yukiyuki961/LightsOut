package com.valkyrie.nabeshimamac.lightsout.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.valkyrie.nabeshimamac.lightsout.R;
import com.valkyrie.nabeshimamac.lightsout.manager.FireBaseManager;
import com.valkyrie.nabeshimamac.lightsout.manager.PreferencesManager;
import com.valkyrie.nabeshimamac.lightsout.manager.ShareManager;
import com.valkyrie.nabeshimamac.lightsout.model.Question;
import com.valkyrie.nabeshimamac.lightsout.model.SharedQuestion;
import com.valkyrie.nabeshimamac.lightsout.view.LightsOutView;

import java.util.Date;
import java.util.Locale;

/**
 * Createモードの新規作成・編集時のActivity
 */
public class MakeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static Intent createIntent(Context context) {
        return new Intent(context, MakeActivity.class);
    }

    private LightsOutView lightsOutEachView;
    private EditText editText;
    private TextView detailText, shareTitle, shareContents;
    private Spinner widthSpinner, heightSpinner;
    private RelativeLayout shareCompleteLayout;
    private Button shareTwitterButton, shareBackButton;
    private Locale locale = Locale.getDefault();

    @NonNull
    private Question question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("New and Edited");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //ツールバーのプロパティ

        detailText = (TextView) findViewById(R.id.detaleTextView);
        //盤面の情報のテキスト

        shareTitle = (TextView) findViewById(R.id.totalShare);
        shareContents = (TextView) findViewById(R.id.countResult);
        shareTwitterButton = (Button) findViewById(R.id.shareTwitterButton);
        shareBackButton = (Button) findViewById(R.id.retryButton);
        widthSpinner = (Spinner) findViewById(R.id.spinnerWidth);
        heightSpinner = (Spinner) findViewById(R.id.spinnerHeight);
        editText = (EditText) findViewById(R.id.titleEditText);
        //IDの関連付け

        final ArrayAdapter widthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.size_spinner));
        widthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        widthSpinner.setAdapter(widthAdapter);
        //spinnerの横部分


        final ArrayAdapter heightAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.size_spinner));
        heightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        heightSpinner.setAdapter(heightAdapter);
        //spinnerの縦部分


        shareCompleteLayout = (RelativeLayout) findViewById(R.id.shareCompleteLayout);
        //盤面入れ替えのスピナーの部分

        lightsOutEachView = (LightsOutView) findViewById(R.id.lightsOutView2);
        lightsOutEachView.setMode(LightsOutView.MODE_MAKE);
        lightsOutEachView.setOnLightsOutListener(new LightsOutView.LightsOutListener() {
            @Override
            public void onButtonTapped(int line, int row, int tapCount) {
                updateDetailsText();
            }

            @Override
            public void onClearListener() {
            }
        });

        long questionId = getIntent().getLongExtra("question_id", -1);
        if (questionId == -1) {
            question = new Question();
            widthSpinner.setEnabled(true);
            // 新規作成
            widthSpinner.setOnItemSelectedListener(this);
            heightSpinner.setEnabled(true);
            heightSpinner.setOnItemSelectedListener(this);
        } else {
            // 編集（新規じゃない）
            question = new Select().from(Question.class).where("id = ?", questionId).executeSingle();
            if (question == null) {
                // 見つからなかった時は、何も処理を行わずに画面を閉じる
                finish();
                return;
            }
            editText.setText("" + question.title);
            widthSpinner.setEnabled(false);
            heightSpinner.setEnabled(false);
            // 4から8の値が入るようにする
            widthSpinner.setSelection(question.width - 4, false);
            heightSpinner.setSelection(question.height - 4, false);

            lightsOutEachView.setBoardWidth(question.width);
            lightsOutEachView.setBoardHeight(question.height);
            lightsOutEachView.setFlagsFromString(question.board);
            lightsOutEachView.updateFlags();
        }
        updateDetailsText();

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                }
                return false;
            }
        });

        // SharedPreferencesからMuteかどうかの設定を読み込む
        lightsOutEachView.setSound(PreferencesManager.getInstance(this).isSound());
        // SharedPreferencesからColorかどうかの設定を読み込む
        lightsOutEachView.setColor(PreferencesManager.getInstance(this).getThemeColor());

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (locale.equals(Locale.JAPAN)) {
                new AlertDialog.Builder(this)
                        .setTitle("注意")
                        .setMessage("戻ってしまうとパズルの編集が消えてしまいます！")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 自動生成されたメソッド・スタブ
                                MakeActivity.this.finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 自動生成されたメソッド・スタブ
                            }
                        })
                        .show();
                return true;
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Caution")
                        .setMessage("It will disappear editing of puzzle!!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 自動生成されたメソッド・スタブ
                                MakeActivity.this.finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 自動生成されたメソッド・スタブ
                            }
                        })
                        .show();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.menu_make, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //toolbarの右側のアイコン

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                save();
                return true;
            case R.id.menu_share:
                share();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //toolbar右側のアイコンの処理

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinnerWidth:
                lightsOutEachView.setBoardWidth(position + 4);
                break;
            case R.id.spinnerHeight:
                lightsOutEachView.setBoardHeight(position + 4);
                break;
        }
        updateDetailsText();
    }
    //spinnerのマス目選択部分

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void shareTwitter(View view) {
        if (locale.equals(Locale.JAPAN)) {
            final String questionUrl = ShareManager.createShareQuestionUrl(question.sharedKey);
            ShareManager.shareTwitter(this, "シンプルパズルゲーム【LightsOut】で問題を共有しました！！" +
                    "\nURLをクリックして問題をとこう！！ \n#LightsOut \n" + questionUrl);
        } else {
            final String questionUrl = ShareManager.createShareQuestionUrl(question.sharedKey);
            ShareManager.shareTwitter(this, "Shared question in LightsOut." +
                    "\nTrying to play down the URL. \n#LightsOut \n" + questionUrl);
        }
    }
    //share時のtwitter共有文章

    public void closeShareComplete(View view) {
        shareCompleteLayout.setVisibility(View.INVISIBLE);
        lightsOutEachView.setButtonEnabled(true);
        editText.setEnabled(true);
        widthSpinner.setEnabled(true);
        heightSpinner.setEnabled(true);
    }
    //share画面を閉じる部分

    //save部分
    private void save() {
        updateQuestion();
        question.save();
        setResult(RESULT_OK);
        finish();
    }

    private void share() {
        //共有部分
        updateQuestion();
        SharedQuestion sharedQuestion = SharedQuestion.valueOf(question);

        if (locale.equals(Locale.JAPAN)) {
            Typeface gothicApple = Typeface.createFromAsset(getAssets(), "AppleSDGothicNeo.ttc");
            shareTitle.setTypeface(gothicApple);
            shareContents.setTypeface(gothicApple);
            shareTwitterButton.setTypeface(gothicApple);
            shareBackButton.setTypeface(gothicApple);
        } else {
            Typeface gothicAdobe = Typeface.createFromAsset(getAssets(), "AdobeGothicStd-Bold.otf");
            shareTitle.setTypeface(gothicAdobe);
            shareContents.setTypeface(gothicAdobe);
            shareTwitterButton.setTypeface(gothicAdobe);
            shareBackButton.setTypeface(gothicAdobe);
        }
        //フォントの設定

        if (locale.equals(Locale.JAPAN)) {
            shareTitle.setText("シェアが完了しました！");
            shareContents.setText("作った問題をSNSでシェアしよう！");
            shareTwitterButton.setText("Twitterでシェア");

        } else {
            shareTitle.setText("Share was completed!!");
            shareContents.setText("Trying to share the question created by the SNS!");
            shareTwitterButton.setText("Share on Twitter");
        }

        if (TextUtils.isEmpty(question.sharedKey)) {
            final String key = FireBaseManager.pushObject("questions", sharedQuestion, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    shareCompleteLayout.setVisibility(View.VISIBLE);
                    lightsOutEachView.setButtonEnabled(false);
                    editText.setEnabled(false);
                    widthSpinner.setEnabled(false);
                    heightSpinner.setEnabled(false);
                }
            });
            // ShareしたことがわかるようにKeyを入れておく
            question.sharedKey = key;
            question.save();
        } else {
            FireBaseManager.updateObject("questions", question.sharedKey, sharedQuestion);
        }
    }

    private void updateQuestion() {
        if (TextUtils.isEmpty(editText.getText())) {
            question.title = "No Name";
            //もしtitleに何も入力しないでセーブしたら『No Name』と入る
        } else {
            question.title = editText.getText().toString();
            //titleに何か入っていたらString型で配置
        }
        // 現在日時の取得
        question.board = lightsOutEachView.getFlagsToString();
        question.width = lightsOutEachView.getBoardWidth();
        question.height = lightsOutEachView.getBoardHeight();
        question.createdAt = new Date();
    }

    private void updateDetailsText() {
        String board = lightsOutEachView.getFlagsToString();
        int emptyCount = 0;
        for (int i = 0; i < board.length(); i++) {
            if (board.charAt(i) == '0') {
                emptyCount++;
            }
        }
        if (locale.equals(Locale.JAPAN)) {
            detailText.setText("盤面のサイズ : " + lightsOutEachView.getBoardWidth() + "×"
                    + lightsOutEachView.getBoardHeight() + "  空のマス : " + emptyCount);
        } else {
            detailText.setText("Size : " + lightsOutEachView.getBoardWidth() + "×"
                    + lightsOutEachView.getBoardHeight() + "  Empty Board : " + emptyCount);
        }
    }
    //ShareからListViewに表示させる内容
}
