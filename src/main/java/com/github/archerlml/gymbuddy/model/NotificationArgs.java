package com.github.archerlml.gymbuddy.model;

import com.github.archerlml.gymbuddy.activity.BaseActivity;

/**
 * Created by archerlml on 12/16/16.
 */

public class NotificationArgs {
    public String title;
    public String text;
    public Class<? extends BaseActivity> clz;
    public Object args;
}
