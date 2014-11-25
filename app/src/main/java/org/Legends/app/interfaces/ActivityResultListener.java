package org.Legends.app.interfaces;

import android.content.Intent;

/**
 * Created by Usama on 10/4/14.
 */
public interface ActivityResultListener
{
    public void onActivityResult(int requestCode, int resultCode, Intent intent);
}