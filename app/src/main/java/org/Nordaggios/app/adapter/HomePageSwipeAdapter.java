package org.Nordaggios.app.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.Nordaggios.app.fragments.FragmentMore;
import org.Nordaggios.app.fragments.LoginFragment;
import org.Nordaggios.app.fragments.PostsFragment;
import org.Nordaggios.app.fragments.RewardsFragment;
import com.parse.ParseUser;

/**
 * Created by Usama on 10/2/14.
 */
public class HomePageSwipeAdapter extends FragmentStatePagerAdapter
{
    public static final int NUM_SWIPE_VIEWS = 3;
    public static final int POS_NEWS_FEED = 0, POS_REWARDS = 1, POS_MORE = 2;

    public HomePageSwipeAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case POS_NEWS_FEED:
                return "News";
            case POS_REWARDS:
                return "Rewards";
            case POS_MORE:
                return "More";
        }

        return null;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case POS_NEWS_FEED:
                return PostsFragment.newInstance();
            case POS_REWARDS:
                return ParseUser.getCurrentUser() != null ? RewardsFragment.newInstance() : LoginFragment.newInstance();
            case POS_MORE:
                return FragmentMore.newInstance();
        }

        return null;
    }

    @Override
    public int getItemPosition(Object object)
    {
        return POSITION_NONE;
    }

    @Override
    public int getCount()
    {
        return NUM_SWIPE_VIEWS;
    }
}
