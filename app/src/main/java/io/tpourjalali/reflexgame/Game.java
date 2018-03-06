package io.tpourjalali.reflexgame;

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
    private AssetFactory mAssetFactory;
    private State mState;

    public Game(AssetFactory assetFactory, int lives) {
        mScore = mLevel = 0;
        ++mLevel;
        mLives = lives;
        mAssetFactory = assetFactory;
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

    public AssetFactory getAssetFactory() {
        return mAssetFactory;
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

    public interface AssetFactory {
        public View createAssetDrawable(String asset_id);
    }

    public interface Runner {
        void setGame(Game game);

        void pause();

        void end();
        void resume();
    }
}
