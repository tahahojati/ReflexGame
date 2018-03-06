package io.tpourjalali.reflexgame;

import android.graphics.drawable.Drawable;

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

    public void setRunner(Runner runner) {
        mRunner = runner;
        mRunner.setGame(this);
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

    public enum State {
        RUNNING, PAUSED, NOTSTARTED, FINISHED
    }

    public interface GameAsset {
        public void draw();
    }

    public interface AssetFactory {
        public Drawable createAssetDrawable(String asset_id);
    }

    public interface Runner {
        void setGame(Game game);

        void resume();
    }
}
