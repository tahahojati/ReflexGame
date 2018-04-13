package io.tpourjalali.reflexgame;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ProfessorTaha on 3/6/2018.
 */

public class GameRunner implements Game.Runner, Runnable, View.OnClickListener, Animator.AnimatorListener {
    public static final String TAG = "GameRunner";
    public static final String MESSAGE_DATA_ASSET = "asset";
    private final Game.GameView mGameView;
    private final HashMap<Animator, View> mAnimatorMap = new HashMap<>();
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
    private AtomicInteger mHighScore = new AtomicInteger(0);
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
        mThread.enqueRunnable(this);
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
        int viewportMaxX = vicd ewport.getHeight();
        int viewportMaxY = viewport.getWidth();
        int level = mGame.getLevel();
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
            float speed = getRandomSpeed(random, level);
            float duration = getRandomAnimationDuration(random, level);
            mGameView.startAnimation(generateAnimator(speed, duration, asset.getView()));
            int delayDuration = getRandomDelayDuration(random, mGame.getLevel());
            delayMillis(delayDuration);

        }
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
        return (float) Math.max(2, g);
    }
    private int getRandomDelayDuration(ThreadLocalRandom random, int level) {
        double g = random.nextGaussian();
        int mean = (int) (5000 * Math.pow(0.66667, level ));
        return Math.max(1, (int)(mean + g*mean));
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
        ObjectAnimator animation;
        Path path = new Path();
        float current_x = v.getX(), current_y = v.getY();
        float target_x, target_y;
        View viewport = mGameView.getViewPort();
        int viewportMaxX = viewport.getMeasuredWidth();
        int viewportMaxY = viewport.getMeasuredHeight();
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
            } else if (isCloseTo(target_x, viewport.getMeasuredWidth())) {
                xSpeed = -Math.abs(xSpeed);
            }
            if (isCloseTo(target_y, 0.0)) {
                ySpeed = Math.abs(ySpeed);
            } else if (isCloseTo(target_y, viewport.getMeasuredHeight())) {
                ySpeed = -Math.abs(ySpeed);
            }
            path.lineTo(target_x, target_y);
            current_x = target_x;
            current_y = target_y;
        }
        animation = ObjectAnimator.ofFloat(v, "x", "y", path);
        animation.setDuration((long) duration * 1000);
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
        mGameView.playSound(GameActivity.SOUND_MISS);
        View v = (View) ((ObjectAnimator) animation).getTarget();
        mGameView.removeView(v);
        mGame.removeAsset((GameAsset) v.getTag());
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}

