package io.tpourjalali.reflexgame;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity implements Game.AssetFactory {
    private AppCompatImageButton mSettingsImageButton;
    private TextView mScoreTextView, mHighScoreTextView;
    private LinearLayout mLivesLayout;
    private Drawable mHeartDrawable;
    private Drawable mSpotDrawable;
    private FrameLayout mGameLayout;
    private StyleRes mHeartStyle;
    //End of view element declerations

    private SharedPreferences mPreferences; //we'll save a reference to default sharedpreferences.
    private SoundPool mSoundPool;
    private Game mGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    @Override
    public Drawable createAssetDrawable(String asset_id) {
        return null;
    }
}
