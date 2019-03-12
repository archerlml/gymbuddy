package com.github.archerlml.gymbuddy.util;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.github.archerlml.gymbuddy.application.GymBuddyApplication;
import com.github.archerlml.gymbuddy.model.ReportImage;
import com.google.firebase.storage.StorageReference;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ImageUtil {

    @Inject
    StorageReference storageReference;

    @Inject
    ImageUtil(StorageReference storageReference) {
        this.storageReference = storageReference;
    }

    public void loadImage(ReportImage image, ImageView imageView) {
        if (image.path != null) {
            Glide.with(imageView.getContext())
                    .load(image.path)
                    .error(image.resid)
                    .into(imageView);
        } else if (image.uri != null) {
            Glide.with(imageView.getContext())
                    .using(new FirebaseImageLoader())
                    .load(storageReference.child(image.uri))
                    .error(image.resid)
                    .into(imageView);
        } else {
            Glide.with(imageView.getContext())
                    .load(image.resid)
                    .into(imageView);
        }
    }
}
