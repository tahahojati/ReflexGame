package io.tpourjalali.reflexgame;

import android.animation.Animator;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity implements Game.GameView {
    public static final String ASSET_SPOT = "SPOT";
    public static final String TAG = "GameActivity";
    private static final String HIGH_SCORE_PREFERECE = "high score";
    public static int SOUND_HIT;
    public static int SOUND_MISS;
    public static int SOUND_DISAPPEAR;
    private AppCompatImageButton mSettingsImageButton;
    private TextView mScoreTextView, mHighScoreTextView;
    private LinearLayout mLivesLayout;
    private Drawable mHeartDrawable;
    private GradientDrawable mSpotDrawable;
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

        mSettingsImageButton = findViewById(R.id.settingsimageButton);
        mScoreTextView = findViewById(R.id.scoreTextView);
        mHighScoreTextView = findViewById(R.id.highScoreTextView);
        mLivesLayout = findViewById(R.id.livesLayout);
        mHeartDrawable = getDrawable(R.drawable.ic_heart);
        mSpotDrawable = (GradientDrawable) getDrawable(R.drawable.spot);
        mGameLayout = findViewById(R.id.gameLayout);

        //Done with views
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mGame = new Game(this, 6);
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
//
        mGameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mGame.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        mSoundPool.release();
        super.onDestroy();
    }

    @Override
    public View createAssetView(String asset_id) {
        ImageView v = new ImageView(this);
        mSpotDrawable.setColor(Color.BLACK);
        v.setImageDrawable(mSpotDrawable);
        return v;
    }

    @Override
    public View getViewPort() {
        return mGameLayout;
    }
    @Override
    public void addView(View v, int x, int y, int width, int height) {
        v.setX(x);
        v.setY(y);
        Log.d(TAG, "got x: " + x + " got y: " + y);
        v.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
        addView(v);
    }

    @Override
    public void addView(View view) {
        mGameLayout.addView(view);
    }

    @Override
    public void removeView(View v) {
        mGameLayout.removeView(v);
    }

    @Override
    public void startAnimation(@NonNull Animator animator) {
        runOnUiThread(animator::start);
    }

    @Override
    public void playSound(int sound_id) {
        mSoundPool.play(sound_id, 1.0f, 1.0f, 100, 0, 1);
    }

    @Override
    public void setLives(int lives) {
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
    }

    @Override
    public void setScore(int score) {
        String scoreStr = getString(R.string.currentScore, score);
        mScoreTextView.setText(scoreStr);
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
    public void clearViewPort() {

    }

    @Override
    public void displayHighScore() {
        int highscore = mPreferences.getInt(HIGH_SCORE_PREFERECE, 0);
        String highscoreStr = getString(R.string.highScore, highscore);
        mHighScoreTextView.setText(highscoreStr);
    }

    @Override
    public void resetSoundEffects() {

    }

}
