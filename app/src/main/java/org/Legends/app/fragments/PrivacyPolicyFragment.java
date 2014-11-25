/**
 *
 */
package org.Legends.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.Legends.app.R;
import org.Legends.app.utility.Utils;

/**
 * @author Touhid
 */
public class PrivacyPolicyFragment extends Fragment
{
    public static PrivacyPolicyFragment newInstance()
    {
        return new PrivacyPolicyFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.frag_privacy_terms, container, false);

        String appName = getString(R.string.app_name);
        String oldData = Utils.readAssetsFile(getActivity(), "privacy_policy.html");

        WebView privacyPolicyWebView = (WebView) v.findViewById(R.id.privacy_policy_web_view);
        privacyPolicyWebView.loadData(oldData.replaceAll("&&&", appName), "text/html", "UTF-8");
        return v;
    }

}
