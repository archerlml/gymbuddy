package com.github.archerlml.gymbuddy.application;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Created by archerlml on 10/10/17.
 */


@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ApplicationContext {
}