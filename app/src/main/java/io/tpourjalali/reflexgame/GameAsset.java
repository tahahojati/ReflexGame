package io.tpourjalali.reflexgame;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.io.Serializable;

/**
 * Created by ProfessorTaha on 3/6/2018.
 */

public class GameAsset implements Serializable {
    private View mView;
    private Animator mAnimator;

    public GameAsset(@NonNull View drawable, @Nullable Animator an) {
        mView = drawable;
        mAnimator = an;
    }

    public void draw() {
    }

    public Animator getAnimator() {
        return mAnimator;
    }

    public View getView() {
        return mView;
    }

}
