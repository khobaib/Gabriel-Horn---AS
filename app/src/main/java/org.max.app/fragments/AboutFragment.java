/**
 *
 */
package org.max.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.max.app.R;

/**
 * @author Touhid
 */
public class AboutFragment extends Fragment
{
    public static AboutFragment newInstance()
    {
        return new AboutFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.frag_about_app, container, false);
        return v;
    }

}
