package io.tpourjalali.reflexgame;

import android.animation.Animator;
import android.view.View;

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

    public void addAsset(GameAsset asset) {
        mAssets.add(asset);
    }

    public int getScore() {
        return mScore;
    }

    public void setScore(int score) {
        mScore = score;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = level;
    }

    public int getLives() {
        return mLives;
    }

    public void setLives(int lives) {
        mLives = lives;
    }

    public ConcurrentLinkedDeque<GameAsset> getAssets() {
        return mAssets;
    }

    public Runner getRunner() {
        return mRunner;
    }

    public void setRunner(Runner runner) {
        mRunner = runner;
    }

    public GameView getGameView() {
        return mGameView;
    }

    public State getState() {
        return mState;
    }

    public void setState(State state) {
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
        View createAssetView(String asset_id);

        void addView(View v, int x, int y, int width, int height);

        View getViewPort();

        void runAnimator(Animator animator);

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
    }

    public interface Runner {
        void pause();

        void end();
        void resume();
    }
}
