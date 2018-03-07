package io.tpourjalali.reflexgame;

import android.animation.Animator;
import android.view.View;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by ProfessorTaha on 3/6/2018.
 */

public class Game {
    private int mScore, mLevel, mLives;
    private ConcurrentLinkedDeque<GameAsset> mAssets;
    private Runner mRunner;
    private GameView mGameView;
    private State mState;

    public Game(GameView gameView, int lives) {
        mScore = mLevel = 0;
        mAssets = new ConcurrentLinkedDeque<>();
        ++mLevel;
        mLives = lives;
        mGameView = gameView;
        mState = State.NOTSTARTED;
    }

    public void start() throws IllegalStateException {

        if (mState == State.RUNNING) return;
        if (Objects.isNull(mRunner))
            throw new IllegalStateException("No runner specified");
        else {
            mState = State.RUNNING;
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
        mRunner.setGame(this);
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

    public enum State {
        RUNNING, PAUSED, NOTSTARTED, FINISHED
    }

    public interface GameView {
        View createAssetView(String asset_id);

        void addView(View v, int x, int y, int width, int height);

        void runAnimator(Animator animator);

        void setLives(int lives);

        void setScore(int score);

        void setHighScore(int highScore);

        void clearViewPort();
    }

    public interface Runner {
        void setGame(Game game);

        void pause();

        void end();
        void resume();
    }
}
