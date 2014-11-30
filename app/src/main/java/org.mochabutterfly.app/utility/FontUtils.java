package org.mochabutterfly.app.utility;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

public class FontUtils
{
    private HashMap<String, Typeface> fontHolder = new HashMap<>();
    private static FontUtils singleton;

    private FontUtils()
    {

    }

    public static void initialize(Context context, String[] fonts)
    {
        if (singleton == null)
        {
            singleton = new FontUtils();
        }

        for (String font : fonts)
        {
            Typeface loadedFont = Typeface.createFromAsset(context.getAssets(), font);
            singleton.fontHolder.put(font, loadedFont);
        }
    }

    public static FontUtils getInstance()
    {
        return singleton;
    }

    public void overrideFonts(final View v, String fontName)
    {
        Typeface font = fontHolder.get(fontName);

        try
        {
            if (v instanceof ViewGroup)
            {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++)
                {
                    View child = vg.getChildAt(i);
                    overrideFonts(child, fontName);
                }
            }
            else if (v instanceof TextView)
            {
                TextView textView = (TextView) v;
                textView.setTypeface(font);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
