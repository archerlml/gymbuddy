package com.github.archerlml.gymbuddy.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by archerlml on 10/28/17.
 */

public abstract class TypedBaseView<Model> extends BaseView {
    private Model val;

    public TypedBaseView(@NonNull Context context) {
        super(context);
    }

    public TypedBaseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Model getData() {
        return val;
    }

    public TypedBaseView applyData(Model model) {
        val = model;
        return this;
    }
}
