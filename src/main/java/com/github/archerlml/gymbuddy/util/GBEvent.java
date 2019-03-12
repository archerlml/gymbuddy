package com.github.archerlml.gymbuddy.util;


public class GBEvent {
    public enum Type {
        REFRESH_SCHEDULE,
        REFRESH_EVENT,
        SHOW_APP_TOAST,
    }

    public Type what;
    public Class<?> receiver;
    public Class<?> sender;
    public int arg1;
    public int arg2;
    public int arg3;
    private Object obj;

    public GBEvent(Type event) {
        this.what = event;
    }

    public GBEvent(Type event, Object obj) {
        this.what = event;
        this.obj = obj;
    }

    public GBEvent setObj(Object obj) {
        this.obj = obj;
        return this;
    }

    public GBEvent setReceiver(Class<?> receiver) {
        this.receiver = receiver;
        return this;
    }

    public boolean isFrom(Object sender) {
        return this.sender == null || this.sender.equals(this.sender.getClass());
    }

    public boolean isFor(Object receiver) {
        return this.receiver == null || this.receiver.equals(this.receiver.getClass());
    }

    public <E> E obj(Class<E> eClass) {
        return (E) obj;
    }
}

