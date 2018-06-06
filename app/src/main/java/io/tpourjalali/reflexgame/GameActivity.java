package io.tpourjalali.reflexgame;

import android.animation.Animator;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

public class GameActivity extends AppCompatActivity implements Game.GameView {
    public static final String TAG = "GameActivity";
    public static int SOUND_HIT;
    public static int SOUND_MISS;
    public static int SOUND_DISAPPEAR;



    private static final String HIGH_SCORE_PREFERECE = "high score";
    private AppCompatImageButton mSettingsImageButton;
    private TextView mScoreTextView, mHighScoreTextView, mStartAndGameOverTV;
    private ConstraintLayout mGameOverLayout;
    private LinearLayout mLivesLayout;
    private Drawable mHeartDrawable;
    private StateListDrawable mSpotDrawable;
    private FrameLayout mGameLayout;
    //private Handler mHandler = new Handler();
    //End of view element declerations

    private SharedPreferences mPreferences; //we'll save a reference to default sharedpreferences.
    private SoundPool mSoundPool;
    private Game.Runner mGameRunner;
    private Game mGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mStartAndGameOverTV = findViewById(R.id.gameover_start_tv);
        mGameOverLayout = findViewById(R.id.gameOverLayout);
        mSettingsImageButton = findViewById(R.id.settingsimageButton);
        mScoreTextView = findViewById(R.id.scoreTextView);
        mHighScoreTextView = findViewById(R.id.highScoreTextView);
        mLivesLayout = findViewById(R.id.livesLayout);
        mHeartDrawable = getDrawable(R.drawable.ic_heart);
        mSpotDrawable = (StateListDrawable) getDrawable(R.drawable.spot);
        mGameLayout = findViewById(R.id.gameLayout);

        //Done with views
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mGame = new Game(this, 5);
        mGameRunner = new GameRunner(this, mGame);
        mGame.setRunner(mGameRunner);

        //Sound
        SoundPool.Builder sb = new SoundPool.Builder();
        AudioAttributes.Builder ab = new AudioAttributes.Builder();
        ab.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
        ab.setUsage(AudioAttributes.USAGE_GAME);
        sb.setMaxStreams(4);
        sb.setAudioAttributes(ab.build());
        mSoundPool = sb.build();
        SOUND_HIT = mSoundPool.load(this, R.raw.hit, 100);
        SOUND_DISAPPEAR = mSoundPool.load(this, R.raw.disappear, 100);
        SOUND_MISS = mSoundPool.load(this, R.raw.miss, 100);

//        ImageView vv = (ImageView) getLayoutInflater().inflate(R.layout.image_view, null);
//        vv.setX(10);
//        vv.setY(10);
//        vv.setLayoutParams(
//                new ViewGroup.LayoutParams(200, 200)
//        );
//        mGameLayout.addView(vv);
        mSettingsImageButton.setOnClickListener((View view) -> {
            PreferenceFragmentCompat pf = new PreferencesFragment();
            FragmentManager fm = getSupportFragmentManager();
            DialogFragment df = new DialogFragment();
            df.setTargetFragment(pf, 100);
            df.show(fm, "tag");
        });
//
        mGameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mGameOverLayout.setOnClickListener((x) -> {
                    x.setVisibility(View.GONE);
                    mGame.reset();
                    mGame.start();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        mSoundPool.release();
        super.onDestroy();
    }

    @Override
    public View createAssetView(Map<String, Object> asset_description) {
        ImageView v = new ImageView(this);
        String type = (String) asset_description.get(KEY_ASSET_TYPE);
//        Integer color = (Integer) asset_description.get(KEY_ASSET_COLOR);
        Integer width = (Integer) asset_description.get(KEY_ASSET_WIDTH);
        Integer height = (Integer) asset_description.get(KEY_ASSET_HEIGHT);
        switch (type){
            case ASSET_SPOT:
                v.setImageDrawable(mSpotDrawable);
                break;
        }
        if (width == null)
            width = 100;
        if (height == null)
            height = 100;
        v.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
        return v;
    }

    @Override
    public ViewGroup getViewPort() {
        return mGameLayout;
    }
    @Override
    public void addView(View v, int x, int y) {
        v.setX(x);
        v.setY(y);
        Log.d(TAG, "got x: " + x + " got y: " + y);
        addView(v);
    }

    @Override
    public void addView(View view) {
        runOnUiThread(()-> mGameLayout.addView(view));
    }

    @Override
    public void removeView(View v) {

        runOnUiThread(() -> {
            mGameLayout.removeView(v);
            v.clearAnimation();
//            ((GameAsset)v.getTag()).getAnimator().cancel();
        });
    }

    @Override
    public void startAnimation(@NonNull Animator animator) {
        runOnUiThread(animator::start);
    }

    @Override
    public void playSound(int sound_id) {
        runOnUiThread(
                () -> mSoundPool.play(sound_id, 1.0f, 1.0f, 100, 0, 1)
        );
    }

    @Override
    public void setLives(int lives) {
        runOnUiThread(() -> {
            mLivesLayout.removeAllViews();
            Resources resources = getResources();
            int hearWidth = (int) resources.getDimension(R.dimen.heart_width),
                    heartHeight = (int) resources.getDimension(R.dimen.heart_height),
                    heartMargin = (int) resources.getDimension(R.dimen.heart_margin);
            Log.d(TAG, "Heart height is: " + Integer.toString(heartHeight));
            for (int i = 0; i < lives; ++i) {
                ImageView lifeView = new ImageView(this);
                lifeView.setImageDrawable(mHeartDrawable);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(hearWidth, heartHeight);
                lp.setMargins(heartMargin, 0, heartMargin, 0);
                lifeView.setLayoutParams(lp);
                mLivesLayout.addView(lifeView);
            }
        });
    }

    @Override
    public void setScore(int score) {
        runOnUiThread(() -> {
            String scoreStr = getString(R.string.currentScore, score);
            mScoreTextView.setText(scoreStr);
        });
    }

    @Override
    public int getHighScore() {
        return mPreferences.getInt(HIGH_SCORE_PREFERECE, 0);
    }

    @Override
    public void setHighScore(int highScore) {
        mPreferences.edit()
                .putInt(HIGH_SCORE_PREFERECE, highScore)
                .apply();
    }

    @Override
    public void showGameOverScreen() {
        mStartAndGameOverTV.setText(getString(R.string.gameOverText));
        mGameOverLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void clearViewPort() {
        int c  =mGameLayout.getChildCount();
        for(int i = 0; i< c; ++i){
            View v = mGameLayout.getChildAt(i);
            if(v!= null){
                v.clearAnimation();
            }
        }
        mGameLayout.removeAllViews();
    }

    @Override
    public void displayHighScore() {
        int highscore = mPreferences.getInt(HIGH_SCORE_PREFERECE, 0);
        String highscoreStr = getString(R.string.highScore, highscore);
        mHighScoreTextView.setText(highscoreStr);
    }

    @Override
    public void resetSoundEffects() {
        //TODO:implement method.
    }

}
