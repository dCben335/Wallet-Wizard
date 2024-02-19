package com.example.walletwizard.Utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import com.example.walletwizard.R;
import java.util.Objects;

public class LoadingScreen{

    private final Dialog dialog;

    public LoadingScreen(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.loading_screen);
        setFullScreenLayout();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.background_footer);
        dialog.getWindow().getAttributes().windowAnimations = R.style.LoadingScreenAnimation;
    }


    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }



    private void setFullScreenLayout() {
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
        }
    }
}




