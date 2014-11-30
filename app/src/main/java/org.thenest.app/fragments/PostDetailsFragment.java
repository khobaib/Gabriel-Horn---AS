package org.thenest.app.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.thenest.app.R;
import org.thenest.app.model.LocalUser;
import org.thenest.app.model.Post;
import org.thenest.app.utility.FontUtils;
import org.thenest.app.utility.Fonts;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;

public class PostDetailsFragment extends Fragment
{
    public interface SpecificShareListener
    {
        public void onSpecificShare(String shareUrl);
    }

    private Post singleofferDetails;
    private TextView tv_title, tv_message, postLinkTextView;
    private SpecificShareListener specificShareListener;

    public static PostDetailsFragment newInstance(Post singleOfferDetails)
    {
        PostDetailsFragment f = new PostDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable("post", singleOfferDetails);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        specificShareListener = (SpecificShareListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.singleofferDetails = (Post) getArguments().get("post");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.fragment_post_details, container, false);
        tv_title = (TextView) v.findViewById(R.id.tv_title);
        tv_message = (TextView) v.findViewById(R.id.tv_message);
        postLinkTextView = (TextView) v.findViewById(R.id.post_link_text_view);

        tv_title.setText(singleofferDetails.getTitle());
        tv_message.setText(singleofferDetails.getContents());
        postLinkTextView.setText(singleofferDetails.getLink());

        ParseImageView todoImage = (ParseImageView) v.findViewById(R.id.img_pic);
        ParseFile imageFile = singleofferDetails.getParseFile("image");
        if (imageFile != null)
        {
            todoImage.setParseFile(imageFile);
            todoImage.loadInBackground(new GetDataCallback()
            {
                @Override
                public void done(byte[] data, ParseException e)
                {
                }
            });
        }

        FontUtils.getInstance().overrideFonts(v, Fonts.LIGHT);
        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_share:
                String specificShareUrl = LocalUser.getInstance().getParentCompany().getString("postShareBaseUrl") + singleofferDetails.getObjectId();
                specificShareListener.onSpecificShare(specificShareUrl);
                break;
        }

        return false;
    }
}
