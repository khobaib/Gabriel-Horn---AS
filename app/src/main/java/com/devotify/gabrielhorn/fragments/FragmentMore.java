package com.devotify.gabrielhorn.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devotify.gabrielhorn.R;
import com.devotify.gabrielhorn.interfaces.LogInStateListener;
import com.devotify.gabrielhorn.model.LocalUser;
import com.devotify.gabrielhorn.utility.FontUtils;
import com.devotify.gabrielhorn.utility.Fonts;
import com.parse.ParseUser;

public class FragmentMore extends Fragment implements OnClickListener, LogInStateListener
{
    public interface MoreItemClickedListener
    {
        public void onCallUsMenuClicked();

        public void onEmailUsMenuClicked();

        public void onVisitWebMenuClicked();

        public void onShareAppMenuClicked(String shareUrl);

        public void onAboutAppMenuClicked();

        public void onTermsConditionMenuClicked();

        public void onPrivacyPolicyMenuClicked();

        public void onRewardsClicked();

        public void onEditStoreLocationClicked();
    }

    private MoreItemClickedListener moreItemClickedListener;
    private LogInStateListener logInStateListener;
    private TextView loginTextView;

    public static Fragment newInstance()
    {
        return new FragmentMore();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        moreItemClickedListener = (MoreItemClickedListener) activity;
        logInStateListener = (LogInStateListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rv = inflater.inflate(R.layout.frag_more_static, container, false);
        rv.findViewById(R.id.tvMyRewardsMore).setOnClickListener(this);

        loginTextView = (TextView) rv.findViewById(R.id.tvLogOutMore);
        loginTextView.setOnClickListener(this);
        updateLoginState();

        rv.findViewById(R.id.tvEditLocationMore).setOnClickListener(this);

        rv.findViewById(R.id.tvCallUsMore).setOnClickListener(this);
        rv.findViewById(R.id.tvEmailUsMore).setOnClickListener(this);
        rv.findViewById(R.id.tvVisitWebMore).setOnClickListener(this);
        rv.findViewById(R.id.tvShareAppMore).setOnClickListener(this);

        rv.findViewById(R.id.tvAboutAppMore).setOnClickListener(this);
        rv.findViewById(R.id.tvTermsConditionMore).setOnClickListener(this);
        rv.findViewById(R.id.tvPrivacyPolicyMore).setOnClickListener(this);

        View editStoreLocationsView = rv.findViewById(R.id.tvEditLocationMore);
        if (ParseUser.getCurrentUser() != null)
            editStoreLocationsView.setVisibility(ParseUser.getCurrentUser().getBoolean("isAdmin") ? View.VISIBLE : View.INVISIBLE);
        else
            editStoreLocationsView.setVisibility(View.GONE);

        FontUtils.getInstance().overrideFonts(rv, Fonts.LIGHT);
        return rv;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.tvMyRewardsMore:
                moreItemClickedListener.onRewardsClicked();
                return;
            case R.id.tvLogOutMore:
                if (ParseUser.getCurrentUser() != null)
                {
                    ParseUser.getCurrentUser().logOut();
                    logInStateListener.onLogInToggled(false);
                }
                else
                {
                    logInStateListener.onLogInToggled(true);
                }
                return;
            case R.id.tvEditLocationMore:
                moreItemClickedListener.onEditStoreLocationClicked();
                return;
            case R.id.tvCallUsMore:
                moreItemClickedListener.onCallUsMenuClicked();
                return;
            case R.id.tvEmailUsMore:
                moreItemClickedListener.onEmailUsMenuClicked();
                return;
            case R.id.tvVisitWebMore:
                moreItemClickedListener.onVisitWebMenuClicked();
                return;
            case R.id.tvShareAppMore:
                String appShareUrl = LocalUser.getInstance().getParentCompany().getString("appShareUrl");
                moreItemClickedListener.onShareAppMenuClicked(appShareUrl);
                return;
            case R.id.tvAboutAppMore:
                moreItemClickedListener.onAboutAppMenuClicked();
                return;
            case R.id.tvTermsConditionMore:
                moreItemClickedListener.onTermsConditionMenuClicked();
                return;
            case R.id.tvPrivacyPolicyMore:
                moreItemClickedListener.onPrivacyPolicyMenuClicked();
                return;
        }
    }

    @Override
    public void onLogInToggled(boolean isLoggedIn)
    {
        updateLoginState();
    }

    public void updateLoginState()
    {
        if (ParseUser.getCurrentUser() == null)
            loginTextView.setText("Log in");
        else
            loginTextView.setText("Log out");
    }
}
