package com.example.walletwizard.Utils;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import com.example.walletwizard.R;

import java.util.Objects;

// Class for displaying a loading screen dialog
public class LoadingScreen {

    // Dialog instance for the loading screen
    private final Dialog dialog;

    // Constructor
    public LoadingScreen(Context context) {
        // Initialize the dialog
        dialog = new Dialog(context);

        // Set dialog properties
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.loading_screen);

        // Set a background, animations and full-screen layout
        setFullScreenLayout();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.background_footer); // Set background drawable
        dialog.getWindow().getAttributes().windowAnimations = R.style.LoadingScreenAnimation;
    }

    // Method to show the loading screen
    public void show() {
        dialog.show();
    }

    // Method to dismiss the loading screen
    public void dismiss() {
        dialog.dismiss();
    }

    // Method to set full-screen layout for the dialog
    private void setFullScreenLayout() {
        Window window = dialog.getWindow();
        if (window != null) {
            // Get window parameters
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());

            // Set fullScreen
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
        }
    }
}
