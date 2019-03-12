package com.github.archerlml.gymbuddy.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.archerlml.gymbuddy.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by archerlml on 11/3/17.
 */

public class TitlebarView extends RelativeLayout {
    @BindView(R.id.left)
    public ImageView mTitleLeftIcon;

    @BindView(R.id.middle)
    public TextView mTitleMiddleText;

    @BindView(R.id.right)
    public ImageView mTitleRightIcon;

    @BindView(R.id.title_bar)
    public View mTitlebar;


    public TitlebarView(Context context) {
        this(context, null);
    }

    public TitlebarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.title_bar, this);
        ButterKnife.bind(this);

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.TypedBaseView,
                    0, 0);
            try {
                int theme = a.getResourceId(R.styleable.Titlebar_titlebar_theme, 0);
                switch (theme) {
                    case 0:
                        mTitleLeftIcon.setImageResource(R.drawable.ic_chevron_left_white_48px);
                        mTitlebar.setBackgroundResource(R.color.contentWhite);
                        break;
                    case 1:
                        mTitleLeftIcon.setImageResource(R.drawable.ic_keyboard_arrow_left_black_48px);
                        mTitlebar.setBackgroundResource(R.color.contentGray);
                        break;
                }
            } finally {
                a.recycle();
            }
        }
    }
}
