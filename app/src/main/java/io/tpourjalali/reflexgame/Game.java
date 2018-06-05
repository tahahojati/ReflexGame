package io.tpourjalali.reflexgame;

import android.animation.Animator;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by ProfessorTaha on 3/6/2018.
 */

public class Game {
    private int mScore, mLevel, mLives, mLivesInitial;
    private ConcurrentLinkedDeque<GameAsset> mAssets;
    private Runner mRunner;
    private GameView mGameView;
    private State mState;

    public Game(GameView gameView, int lives) {
        mAssets = new ConcurrentLinkedDeque<>();
        mLivesInitial = lives;
        mGameView = gameView;
        reset();
    }

    public void start() throws IllegalStateException {

        if (mState == State.RUNNING) return;
        if (Objects.isNull(mRunner))
            throw new IllegalStateException("No runner specified");
        else {
            mState = State.NOTSTARTED;
            mRunner.resume();
        }
    }

    synchronized public void addAsset(GameAsset asset) {
        mAssets.add(asset);
    }

    public int getScore() {
        return mScore;
    }

    synchronized public void setScore(int score) {
        mScore = score;
    }

    public int getLevel() {
        return mLevel;
    }

    synchronized public void setLevel(int level) {
        mLevel = level;
    }

    public int getLives() {
        return mLives;
    }

    synchronized public void setLives(int lives) {
        mLives = lives;
    }

    public ConcurrentLinkedDeque<GameAsset> getAssets() {
        return mAssets;
    }

    public Runner getRunner() {
        return mRunner;
    }

    synchronized public void setRunner(Runner runner) {
        mRunner = runner;
    }

    public GameView getGameView() {
        return mGameView;
    }

    public State getState() {
        return mState;
    }

    synchronized public void setState(State state) {
        mState = state;
    }

    public void reset() {
        mAssets.clear();
        mScore = 0;
        mLevel = 1;
        mLives = mLivesInitial;
        mState = State.NOTSTARTED;
    }

    public void incrementScore(int i) {
        mScore += i;
    }

    public void removeAsset(GameAsset asset) {
        mAssets.remove(asset);
    }

    public enum State {
        RUNNING, PAUSED, NOTSTARTED, FINISHED
    }

    public interface GameView {
        String ASSET_SPOT = "SPOT";
        String KEY_ASSET_TYPE = "asset_type";
        String KEY_ASSET_COLOR = "asset_color";
        String KEY_ASSET_WIDTH = "asset_width";
        String KEY_ASSET_HEIGHT = "asset_height";

        View createAssetView(Map<String, Object> asset_description);

        void addView(View v, int x, int y);

        ViewGroup getViewPort();

        void setLives(int lives);

        void setScore(int score);

        int getHighScore();

        void setHighScore(int highScore);

        void clearViewPort();

        void displayHighScore();

        void resetSoundEffects();

        void addView(View view);

        void removeView(View v);

        void startAnimation(Animator animator);

        void playSound(int sound_id);

        void showGameOverScreen();
    }

    public interface Runner {
        void pause();

        void end();
        void resume();
    }
}
