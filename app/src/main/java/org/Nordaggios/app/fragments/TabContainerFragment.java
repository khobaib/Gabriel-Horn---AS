package org.Nordaggios.app.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import org.Nordaggios.app.R;
import org.Nordaggios.app.adapter.HomePageSwipeAdapter;

/**
 * Created by Usama on 10/4/14.
 */
public class TabContainerFragment extends Fragment
{
    private HomePageSwipeAdapter homePageSwipeAdapter;
    private ViewPager homePager;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_tab_container, container, false);
        initUI(rootView);
        return rootView;
    }

    public void initUI(View rootView)
    {
        homePageSwipeAdapter = new HomePageSwipeAdapter(getChildFragmentManager());
        homePager = (ViewPager) rootView.findViewById(R.id.main_pager);
        homePager.setAdapter(homePageSwipeAdapter);
        homePager.setOffscreenPageLimit(HomePageSwipeAdapter.NUM_SWIPE_VIEWS);

        final PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.main_tabs);
        tabs.setShouldExpand(true);
        tabs.setBackgroundColor(getResources().getColor(R.color.tab_color));
        tabs.setViewPager(homePager);
        tabs.setTextColor(getResources().getColor(R.color.primary_color));
        tabs.setIndicatorColor(getResources().getColor(R.color.primary_color));
    }

    public HomePageSwipeAdapter getHomePageSwipeAdapter()
    {
        return homePageSwipeAdapter;
    }

    public void setHomePageSwipeAdapter(HomePageSwipeAdapter homePageSwipeAdapter)
    {
        this.homePageSwipeAdapter = homePageSwipeAdapter;
    }

    public ViewPager getHomePager()
    {
        return homePager;
    }

    public void setHomePager(ViewPager homePager)
    {
        this.homePager = homePager;
    }
}
