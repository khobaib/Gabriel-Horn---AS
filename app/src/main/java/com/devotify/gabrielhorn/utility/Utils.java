package com.devotify.gabrielhorn.utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.view.View;
import android.view.WindowManager.BadTokenException;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.devotify.gabrielhorn.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils
{
    public static final String PREF_NAME = "HGHORN";

    public static void writeBoolean(Context context, String key, boolean value)
    {
        getEditor(context).putBoolean(key, value).commit();
    }

    public static boolean readBoolean(Context context, String key, boolean defValue)
    {
        return getPreferences(context).getBoolean(key, defValue);
    }

    public static void writeInteger(Context context, String key, int value)
    {
        getEditor(context).putInt(key, value).commit();

    }

    public static int readInteger(Context context, String key, int defValue)
    {
        return getPreferences(context).getInt(key, defValue);
    }

    public static void writeString(Context context, String key, String value)
    {
        getEditor(context).putString(key, value).commit();

    }

    public static String readString(Context context, String key, String defValue)
    {
        return getPreferences(context).getString(key, defValue);
    }

    public static void writeFloat(Context context, String key, float value)
    {
        getEditor(context).putFloat(key, value).commit();
    }

    public static float readFloat(Context context, String key, float defValue)
    {
        return getPreferences(context).getFloat(key, defValue);
    }

    public static void writeLong(Context context, String key, long value)
    {
        getEditor(context).putLong(key, value).commit();
    }

    public static long readLong(Context context, String key, long defValue)
    {
        return getPreferences(context).getLong(key, defValue);

    }

    public static SharedPreferences getPreferences(Context context)
    {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static Editor getEditor(Context context)
    {
        return getPreferences(context).edit();
    }

    public static void remove(Context context, String key)
    {
        getEditor(context).remove(key);

    }

    public static void toast(Context contxt, String str)
    {
        Toast.makeText(contxt, str, Toast.LENGTH_LONG).show();

    }

    public static ProgressDialog createProgressDialog(Context mContext)
    {
        ProgressDialog dialog = new ProgressDialog(mContext);
        try
        {
            dialog.show();
        }
        catch (BadTokenException e)
        {

        }
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.progress_dialog);
        return dialog;
    }


    public static void showKeyboard(Activity activity)
    {
        ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).
                toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static void hideKeyboard(Activity activity)
    {
        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = activity.getCurrentFocus();
        if (view == null)
            return;

        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static String readAssetsFile(Context context, String file)
    {
        AssetManager assetManager = context.getAssets();
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(file)));

            StringBuilder out = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null)
            {
                out.append(line);
            }

            reader.close();
            return out.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
