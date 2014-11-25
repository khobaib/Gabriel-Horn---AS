package org.dogdish.app.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.dogdish.app.R;
import org.dogdish.app.adapter.PostViewListViewAdapter;
import org.dogdish.app.model.Post;
import com.parse.ParseQueryAdapter;

import java.util.List;

public class PostsFragment extends Fragment
{
    public interface FragmentPostItemClickedListener
    {
        public void onPostClicked(Post post);
    }

    private ParseQueryAdapter<Post> postAdapter;
    private ListView postsListView;
    private SwipeRefreshLayout postsRefreshLayout;
    private FragmentPostItemClickedListener postItemClickedListener;

    public static Fragment newInstance()
    {
        return new PostsFragment();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        postItemClickedListener = (FragmentPostItemClickedListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_post, container, false);
        postAdapter = new PostViewListViewAdapter(getActivity());
        postAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<Post>()
        {
            @Override
            public void onLoading()
            {
            }

            @Override
            public void onLoaded(List<Post> posts, Exception e)
            {
                postsRefreshLayout.setRefreshing(false);
            }
        });

        postsRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.post_pull_refresh_view);
        postsRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                postAdapter.loadObjects();
            }
        });

        postsListView = (ListView) rootView.findViewById(R.id.list_view_latest_offer);
        postsListView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                postItemClickedListener.onPostClicked(postAdapter.getItem(Math.max(0, position - 1))); // Account for the header by subtracting one from the index
            }
        });

        View headerView = getActivity().getLayoutInflater().inflate(R.layout.post_list_header_view, null, false);
        postsListView.addHeaderView(headerView);
        postsListView.setAdapter(postAdapter);
        return rootView;
    }
}
