package org.TheFurShop.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.TheFurShop.app.R;
import org.TheFurShop.app.utility.Utils;

public class TermsAndConditionsFragment extends Fragment
{
    private WebView tcWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.layout_tc_fragment, container, false);

        String appName = getString(R.string.app_name);
        String oldData = Utils.readAssetsFile(getActivity(), "terms_conditions.html");

        tcWebView = (WebView) v.findViewById(R.id.wv_tc);
        tcWebView.loadData(oldData.replaceAll("&&&", appName), "text/html", "UTF-8");
        return v;
    }
}
