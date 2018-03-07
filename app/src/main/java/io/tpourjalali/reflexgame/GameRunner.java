package io.tpourjalali.reflexgame;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by ProfessorTaha on 3/6/2018.
 */

public class GameRunner implements Game.Runner {
    public static final String MESSAGE_DATA_ASSET = "asset";
    private final Context mContext;
    private final ViewGroup mViewGroup;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //we only handle a single type of msg. we animate a view!
            Bundle data = msg.getData();
            GameAsset asset = (GameAsset) data.getSerializable(MESSAGE_DATA_ASSET);
            asset.getAnimator().start();
        }
    };
    private final ExecutorService mSingleTreadExecutor = Executors.newSingleThreadExecutor();
    private Future mRunningLogicTask;
    private Game mGame;


    public GameRunner(Context context, ViewGroup parentView) {
        mContext = context;
        mViewGroup = parentView;
    }

    private RunningLogic createRunningLogic() {
        return new RunningLogic();
    }

    @Override
    public void setGame(Game game) {
        mGame = game;
    }

    @Override
    public void resume() {
        //do the setup, start all animators.
        //mSingleTreadExecutor.submit(new SetUpLogic());
        Log.d("GameRunner", "Inside resume");
//        mRunningLogicTask = mSingleTreadExecutor.submit(new RunningLogic());
        new RunningLogic().run();
    }

    @Override
    public void pause() {
        //stop all animators
    }

    @Override
    public void end() {
        //clear everything and be ready for a new game.
    }


    private class SetUpLogic implements Runnable {
        @Override
        public void run() {

        }
    }

    private class RunningLogic implements Runnable {
        public static final long ORIGITAL_WAIT = 2000;

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                Log.d("RunningLogic", "just thinking");
                GameAsset asset = generateAsset();
                ViewGroup.LayoutParams lp = new ConstraintLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT
                        100, 100
                );
                asset.getView().setX(250);
                asset.getView().setY(250);
                mViewGroup.addView(asset.getView(), lp);
                Bundle b = new Bundle();
                b.putSerializable(MESSAGE_DATA_ASSET, asset);
                //have the activity animate the asset.
//                Message m = mHandler.obtainMessage();
//                m.setData(b);
//                m.sendToTarget();
//                synchronized (this) {
//                    try {
//                        this.wait(calclateWait());
//                    } catch (InterruptedException e) {
//                        return;
//                    }
//                }
                break;
            }
        }

        private long calclateWait() {
            int level = mGame.getLevel();
            return ORIGITAL_WAIT / level;
        }

        private GameAsset generateAsset() {
            View assetView = mGame.getGameView().createAssetView(GameActivity.ASSET_SPOT);
            GameAsset asset = new GameAsset(assetView, null);
            mGame.addAsset(asset);
            return asset;
        }
    }

    ;
}
