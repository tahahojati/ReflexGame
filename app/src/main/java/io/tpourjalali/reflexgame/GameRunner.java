package io.tpourjalali.reflexgame;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import io.tpourjalali.reflexgame.Game.GameView;

import static io.tpourjalali.reflexgame.Game.GameView.*;
import static io.tpourjalali.reflexgame.Game.GameView.KEY_ASSET_TYPE;

/**
 * Created by ProfessorTaha on 3/6/2018.
 */

public class GameRunner implements Game.Runner, Runnable, View.OnClickListener, Animator.AnimatorListener {
    public static final String TAG = "GameRunner";
    public static final String MESSAGE_DATA_ASSET = "asset";
    private final GameView mGameView;
    private final HashMap<Animator, View> mAnimatorMap = new HashMap<>();
    AtomicInteger mCurrentViewTag = new AtomicInteger(0);
    private AtomicInteger mHighScore = new AtomicInteger(0);
    private SingleTaskThread mThread;
    private final LinearInterpolator mLinearInterpolator = new LinearInterpolator();
    private Game mGame;


    public GameRunner(@NonNull GameView gameView, @NonNull Game game) {
        Objects.requireNonNull(gameView);
        mGameView = gameView;
        Objects.requireNonNull(game);
        mGame = game;
        mThread = new SingleTaskThread();
        mThread.start();  //TODO: uncomment this when everything works.
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
        mThread.enqueRunnable(this);
//        mThread.run(); //TODO: comment this
    }

    private void clear() {
        //empty for now!
    }

    private void setup() { //#1
        mGame.reset();
        mGameView.setLives(mGame.getLives());
        mGameView.setScore(mGame.getScore());
        mGameView.displayHighScore();
        mHighScore.set(mGameView.getHighScore());
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

    //runs on 2nd thread.
    @Override
    public void run() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        View viewport = mGameView.getViewPort();
        int viewportMaxY = viewport.getHeight();
        int viewportMaxX = viewport.getWidth();
        int level = 1;
        Log.d(TAG, "in run method");
        while (mThread.mRunning.get()&&!Thread.interrupted()) //TODO: uncomment this!
        {
            level = mGame.getLevel();
            Map<String, Object> assetDescription = new ArrayMap<>(3);
            assetDescription.put(KEY_ASSET_TYPE, ASSET_SPOT);
//            assetDescription.put(KEY_ASSET_COLOR, Color.BLUE);
//            assetDescription.put(KEY_ASSET_HEIGHT, 60);

            View assetView = mGameView.createAssetView(assetDescription);
            GameAsset asset = new GameAsset(assetView);
            assetView.setTag(asset); // we tag the view with the asset so we can find the asset when view is clicked.
            assetView.setOnClickListener(this);
            int y = (random.nextInt(viewportMaxY - 170) + 35);
            int x = (random.nextInt(viewportMaxX - 80) + 40);
            mGame.addAsset(asset);
            mGameView.addView(asset.getView(), x, y);
            float speed = getRandomSpeed(random, level);
            float duration = getRandomAnimationDuration(random, level);
            asset.setAnimator(generateAnimator(speed, duration, asset.getView()));
            mGameView.startAnimation(asset.getAnimator());
            Log.d(TAG, "added point: "+x+", "+y);
            int delayDuration = getRandomDelayDuration(random, mGame.getLevel());
            delayMillis(delayDuration);
        }
        Thread.currentThread().interrupt();
    }

    private void delayMillis(int delayDuration) {
        if(delayDuration < 500) {
            long time = System.currentTimeMillis();
            while ((time + delayDuration) < System.currentTimeMillis()) ;
        } else {
            synchronized (this){
                try {
                    this.wait(delayDuration);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private float getRandomAnimationDuration(ThreadLocalRandom random, int level) {
        double g = random.nextGaussian();
        g *= level / 2;
        g += 10 - Math.min(level * 0.7, 8);
        float res =  (float)Math.max(2, g);
        Log.d(TAG, "life in seconds: "+res);
        return res;
    }
    private int getRandomDelayDuration(ThreadLocalRandom random, int level) {
        double g = random.nextGaussian();
        int mean = (int) (5000 * Math.pow(0.66667, level ));
        return Math.max(1, (int)(0.5*mean + 0.5*g*mean));
    }

    private float getRandomSpeed(ThreadLocalRandom random, int level) {
        double g = random.nextGaussian();
//        g/=2;
        g *= level * 50;
        g += level * 100;
        return (float) Math.max(g, 100);
    }

    private Animator generateAnimator(float speed, float duration, @NonNull View v) {
        float final_distance = speed * duration;
        Objects.requireNonNull(v);
        ObjectAnimator animation;
        Path path = new Path();
        float current_x = v.getX(), current_y = v.getY();
        float target_x, target_y;
        View viewport = mGameView.getViewPort();
        int viewportMaxX = viewport.getMeasuredWidth();
        int viewportMaxY = viewport.getMeasuredHeight()-100;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        float distX = (int) (((random.nextFloat() * 0.8 + 0.1) * 2 - 0.9) * final_distance);
        float distY = (int) Math.sqrt(final_distance * final_distance - distX * distX) * (random.nextInt(2) * 2 - 1);
        //calculate all the times where direction change will be necessary:
        float xSpeed = (distX) / final_distance * speed;
        float ySpeed = (distY) / final_distance * speed;
        ArrayList<Float> times_of_interest = calculateTimesOfInterest(0, viewportMaxX, (int) v.getX(), duration, xSpeed);
        times_of_interest.addAll(calculateTimesOfInterest(0, viewportMaxY, (int) v.getY(), duration, ySpeed));
        times_of_interest.add((float) duration);
        times_of_interest.add(0.0f);

        Collections.sort(times_of_interest);
        //now we know all the times of collusion with the walls. So we want to generate the animations.

        path.moveTo(current_x, current_y);

        for (int i = 1; i < times_of_interest.size(); ++i) {
            float this_duration = times_of_interest.get(i) - times_of_interest.get(i - 1);
            target_x = current_x + this_duration * xSpeed;
            target_y = current_y + this_duration * ySpeed;
            if (isCloseTo(target_x, 0.0)) {
                xSpeed = Math.abs(xSpeed);
            } else if (isCloseTo(target_x, viewportMaxX)) {
                xSpeed = -Math.abs(xSpeed);
            }
            if (isCloseTo(target_y, 0.0)) {
                ySpeed = Math.abs(ySpeed);
            } else if (isCloseTo(target_y, viewportMaxY)) {
                ySpeed = -Math.abs(ySpeed);
            }
            path.lineTo(target_x, target_y);
            current_x = target_x;
            current_y = target_y;
        }
        animation = ObjectAnimator.ofFloat(v, "x", "y", path);

        animation.setDuration((long) duration * 1000);
        animation.setInterpolator(mLinearInterpolator);
        animation.addListener(this);
//        animation.setInterpolator(new LinearInterpolator());
        return animation;
    }

    private boolean isCloseTo(float v1, double v2) {
        return (Math.abs(v1 - v2) < 0.01);
    }


    private ArrayList<Float> calculateTimesOfInterest(int minVal, int maxVal, float currentVal, float duration, float speed) {
        ArrayList<Float> res = new ArrayList<>();
        float target;
        //calculate the first encounter with the wall.
        if (isCloseTo(currentVal, minVal))
            speed = Math.abs(speed);
        else if (isCloseTo(currentVal, maxVal))
            speed = -Math.abs(speed);
        float time = 0;
        while (time < duration) {
            target = minVal;
            if (speed > 0)
                target = maxVal;
            float current_duration = (target - currentVal) / speed;
            time += current_duration;
            currentVal = target;
            if (time < duration)
                res.add(time);
            speed = -speed;
        }
        return res;
    }

    @Override
    public void onClick(View v) {
        mGameView.playSound(GameActivity.SOUND_HIT);
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

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(@NonNull Animator animation) {
        View v = (View) ((ObjectAnimator) animation).getTarget();
        if(v == null)
            return;
        GameAsset asset = (GameAsset) v.getTag();
        mGame.removeAsset(asset);
        if(mGameView.getViewPort().indexOfChild(v) != -1){
            Log.d(TAG, "missed");
            mGameView.playSound(GameActivity.SOUND_MISS);
            mGameView.removeView(v);
            decrementLives();
        }
    }


    private void decrementLives() {
        int lives = mGame.getLives()-1;
        if(lives>=0) {
            mGame.setLives(lives);
            mGameView.setLives(lives);
        } else {
            //finish game
            gameOver();
        }
    }

    private void gameOver() {
        mThread.stopTask();
        mGameView.clearViewPort();
        mGameView.showGameOverScreen();
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}

