package com.github.archerlml.gymbuddy.util;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

/**
 * Created by archerlml on 11/8/17.
 */

public class MyEventBus {
    EventBus mEvenBus;

    @Inject
    public MyEventBus(EventBus eventBus) {
        mEvenBus = eventBus;
    }

    public void register(Object object) {
        mEvenBus.register(object);
    }

    public void unregister(Object object) {
        mEvenBus.unregister(object);
    }

    public void post(GBEvent.Type event) {
        mEvenBus.post(new GBEvent(event));
    }

    public void post(GBEvent.Type event, Object obj) {
        mEvenBus.post(new GBEvent(event).setObj(obj));
    }

    public void post(GBEvent.Type event, Class<?> receiver) {
        mEvenBus.post(new GBEvent(event).setReceiver(receiver));
    }

    public void post(GBEvent.Type event, Object obj, Class<?> receiver) {
        mEvenBus.post(new GBEvent(event).setReceiver(receiver).setObj(obj));
    }

    public void post(GBEvent msg) {
        mEvenBus.post(msg);
    }

    public void onEvent(Callback callback, GBEvent GBEvent) {
        if (callback != null
                && GBEvent.isFor(callback)
                && callback.acceptEvent(GBEvent)) {
            callback.onAcceptedEvent(GBEvent);
        }
    }

    public interface Callback {
        boolean acceptEvent(GBEvent gbEvent);

        void onAcceptedEvent(GBEvent gbEvent);
    }
}
