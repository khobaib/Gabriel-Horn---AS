package org.chimeracafe.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.chimeracafe.app.R;
import org.chimeracafe.app.model.LocalUser;

public class VisitSiteFragment extends Fragment
{
    private WebView vsWebView;

    public static VisitSiteFragment newInstance()
    {
        return new VisitSiteFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.layout_tc_fragment, container, false);
        vsWebView = (WebView) v.findViewById(R.id.wv_tc);
        String url = LocalUser.getInstance().getParentCompany().getString("websiteUrl");
        if (!url.equals(""))
            vsWebView.loadUrl(url);
        return v;
    }
}
