package io.tpourjalali.reflexgame;

import android.animation.Animator;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity implements Game.GameView {
    public static final String ASSET_SPOT = "SPOT";
    private AppCompatImageButton mSettingsImageButton;
    private TextView mScoreTextView, mHighScoreTextView;
    private LinearLayout mLivesLayout;
    private Drawable mHeartDrawable;
    private GradientDrawable mSpotDrawable;
    private FrameLayout mGameLayout;
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
        mGame = new Game(this, 6);
        mGameRunner = new GameRunner(this, mGame);
        mGame.setRunner(mGameRunner);

//        ImageView vv = (ImageView) getLayoutInflater().inflate(R.layout.image_view, null);
//        vv.setX(10);
//        vv.setY(10);
//        vv.setLayoutParams(
//                new ViewGroup.LayoutParams(200, 200)
//        );
//        mGameLayout.addView(vv);
//

        mGame.start();
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
        v.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        addView(v);
    }

    @Override
    public void addView(View view) {
        mGameLayout.addView(view);
    }

    @Override
    public void runAnimator(Animator animator) {

    }

    @Override
    public void setLives(int lives) {
        mLivesLayout.removeAllViews();
        Resources resources = getResources();
        int hearWidth = (int) resources.getDimension(R.dimen.heart_width), heartHeight = (int) resources.getDimension(R.dimen.heart_height);
        for (int i = 0; i < lives; ++i) {
            ImageView lifeView = new ImageView(this);
            lifeView.setImageDrawable(mHeartDrawable);
            lifeView.setLayoutParams(new LinearLayout.LayoutParams(
                    hearWidth, heartHeight
            ));
            mLivesLayout.addView(lifeView);
        }
    }

    @Override
    public void setScore(int score) {

    }

    @Override
    public int getHighScore() {
        return 0;
    }

    @Override
    public void setHighScore(int highScore) {

    }

    @Override
    public void clearViewPort() {

    }

    @Override
    public void displayHighScore() {

    }

    @Override
    public void resetSoundEffects() {

    }

}
