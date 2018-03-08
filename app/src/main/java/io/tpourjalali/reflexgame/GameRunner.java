package io.tpourjalali.reflexgame;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by ProfessorTaha on 3/6/2018.
 */

public class GameRunner implements Game.Runner, Runnable {
    public static final String TAG = "GameRunner";
    public static final String MESSAGE_DATA_ASSET = "asset";
    private final Game.GameView mGameView;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //we only handle a single type of msg. we animate a view!
            Bundle data = msg.getData();
            GameAsset asset = (GameAsset) data.getSerializable(MESSAGE_DATA_ASSET);
            asset.getAnimator().start();
        }
    };
    private int mHighScore = 0;
    private SingleTaskThread mThread;
    private Game mGame;


    public GameRunner(@NonNull Game.GameView gameView, @NonNull Game game) {
        Objects.requireNonNull(gameView);
        mGameView = gameView;
        Objects.requireNonNull(game);
        mGame = game;
        mThread = new SingleTaskThread();
//        mThread.start();  //TODO: uncomment this when everything works.
    }

    public void shutdown() {
        mThread.shutdown();
        mGame = null;
        mThread = null;
    }
    @Override
    public void resume() {
        Log.d("GameRunner", "Inside resume");
        if (mGame.getState() == Game.State.RUNNING) {
            return;
        }
        if (mGame.getState() == Game.State.FINISHED) {
            clear();
            mGame.setState(Game.State.NOTSTARTED);
        }
        if (mGame.getState() == Game.State.NOTSTARTED) {
            setup();
        } else if (mGame.getState() == Game.State.PAUSED) {
            recoverFromPause();
        }
        mGame.setState(Game.State.RUNNING);
        mThread.enqueRunnable(this::run);
        mThread.run(); //TODO: comment this
    }

    private void clear() {
        mGame.reset();
        mGameView.setLives(mGame.getLives());
        mGameView.setScore(mGame.getScore());
        mGameView.displayHighScore();
        mHighScore = mGameView.getHighScore();
        mGameView.resetSoundEffects();
    }

    private void setup() { //#1
        //empty for now.
    }

    private void recoverFromPause() {
    }

    @Override
    public void pause() {
        //stop all animators
    }

    @Override
    public void end() {
        //clear everything and be ready for a new game.
    }

    @Override
    public void run() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        View viewport = mGameView.getViewPort();
        int viewportMaxX = viewport.getMeasuredWidth();
        int viewportMaxY = viewport.getMeasuredHeight();
        Log.d(TAG, "in run method");
//        while (!Thread.interrupted()) //TODO: uncomment this!
        {
            View assetView = mGameView.createAssetView("spot");
            assetView.setY(random.nextInt(viewportMaxY - 70) + 35);
            assetView.setX(random.nextInt(viewportMaxX - 80) + 40);
            GameAsset asset = new GameAsset(assetView, null);
            ViewGroup.LayoutParams lp = new ConstraintLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT
                    100, 100
            );
            mGameView.addView(asset.getView());
        }
    }
}

