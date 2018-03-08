package io.tpourjalali.reflexgame;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ProfessorTaha on 3/6/2018.
 */

public class GameRunner implements Game.Runner, Runnable, View.OnClickListener {
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
    AtomicInteger mCurrentViewTag = new AtomicInteger(0);
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
        //empty for now!
    }

    private void setup() { //#1
        mGame.reset();
        mGameView.setLives(mGame.getLives());
        mGameView.setScore(mGame.getScore());
        mGameView.displayHighScore();
        mHighScore = mGameView.getHighScore();
        mGameView.resetSoundEffects();
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
        int viewportMaxX = 400;//viewport.getHeight();
        int viewportMaxY = 600;//viewport.getWidth();
        Log.d(TAG, "in run method");
//        while (!Thread.interrupted()) //TODO: uncomment this!
        {
            View assetView = mGameView.createAssetView("spot");
            GameAsset asset = new GameAsset(assetView, null);
            assetView.setTag(asset); // we tag the view with the asset so we can find the asset when view is clicked.
            assetView.setOnClickListener(this);
            int y = (random.nextInt(viewportMaxY - 70) + 35);
            int x = (random.nextInt(viewportMaxX - 80) + 40);
            mGame.addAsset(asset);
            mGameView.addView(asset.getView(), x, y, 20, 20);
        }
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag == null || !(tag instanceof GameAsset)) {
            return;
        }
        GameAsset asset = (GameAsset) tag;
        mGame.removeAsset(asset);
        mGameView.removeView(v);
        mGame.incrementScore(10);
        int score = mGame.getScore();
        mGameView.setScore(score);
        if (score > mGameView.getHighScore())
            mGameView.setHighScore(score);
    }
}

