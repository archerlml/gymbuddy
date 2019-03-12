package com.github.archerlml.gymbuddy.model;

/**
 * Created by archerlml on 11/1/17.
 */

public class UserEntity extends  Entity{

    public String uid;

    public UserEntity() {

    }

    public UserEntity setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public UserEntity(String uid) {
        this.uid = uid;
    }

}
