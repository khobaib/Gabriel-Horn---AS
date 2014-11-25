package org.chimeracafe.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.chimeracafe.app.R;
import org.chimeracafe.app.model.LocalUser;
import org.chimeracafe.app.model.Post;
import org.chimeracafe.app.utility.FontUtils;
import org.chimeracafe.app.utility.Fonts;
import org.chimeracafe.app.viewHelpers.ParseImageCacheAdapter;
import org.chimeracafe.app.viewHelpers.ViewHolder;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PostViewListViewAdapter extends ParseImageCacheAdapter<Post>
{
    public PostViewListViewAdapter(final Context context)
    {
        super(context, new ParseQueryAdapter.QueryFactory()
        {
            @Override
            public ParseQuery create()
            {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
                query.whereEqualTo("appCompany", LocalUser.getInstance().getParentCompany());
                query.addDescendingOrder("expiration");
                return query;
            }
        }, R.layout.row_post);
    }

    private SimpleDateFormat eventFormat = new SimpleDateFormat("MM/d/yyyy hh:mm a");

    @Override
    protected void initView(ViewHolder holder, final Post parsePost)
    {
        final ParseImageView postImageView = (ParseImageView) holder.getView(R.id.iv_post_image);

        Bitmap savedImage = getBitmapCache().get(parsePost.getObjectId());
        if (savedImage == null)
        {
            ParseFile imageFile = parsePost.getParseFile("image");
            if (imageFile != null)
            {
                postImageView.setVisibility(View.VISIBLE);
                postImageView.setParseFile(imageFile);
                postImageView.loadInBackground(new GetDataCallback()
                {
                    @Override
                    public void done(byte[] bytes, ParseException e)
                    {
                        if (bytes != null)
                        {
                            Bitmap fetchedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            getBitmapCache().put(parsePost.getObjectId(), fetchedImage);
                        }
                    }
                });
            }
            else
            {
                postImageView.setVisibility(View.GONE);
            }
        }
        else
        {
            postImageView.setImageBitmap(savedImage);
        }

        TextView tvTitle = (TextView) holder.getView(R.id.tv_title);
        TextView tvContent = (TextView) holder.getView(R.id.tv_content);
        tvTitle.setText(parsePost.getTitle());
        tvContent.setText(parsePost.getContents());

        ImageView postCategoryImageView = (ImageView) holder.getView(R.id.post_category_image_view);
        TextView expiredTextView = (TextView) holder.getView(R.id.iv_expired);
        expiredTextView.setVisibility(View.VISIBLE);

        String postCategory = parsePost.getCategory();
        if (parsePost.getCategory() != null)
        {
            if (postCategory.equals(Post.OFFER_CATEGORY))
            {
                postCategoryImageView.setImageResource(R.drawable.offer_icon);
                if (parsePost.getPostExpiration() != null)
                {
                    long dt = parsePost.getPostExpiration().getTime() - System.currentTimeMillis();
                    if (dt < 0)
                    {
                        int daysBeforeExpiration = (int) Math.abs(dt / (1000.0 * 3600.0 * 24.0));
                        int unit = daysBeforeExpiration;
                        String formattedUnit = "";
                        if (daysBeforeExpiration > 0)
                        {
                            String formattedDay = daysBeforeExpiration == 1 ? "day" : "days";
                            formattedUnit = formattedDay;
                        }
                        else
                        {
                            int hoursBeforeExpiration = (int) Math.abs(dt / (1000.0 * 3600.0));
                            if (hoursBeforeExpiration > 0)
                            {
                                unit = hoursBeforeExpiration;
                                formattedUnit = hoursBeforeExpiration == 1 ? "hour" : "hours";
                            }
                            else
                            {
                                int minutesBeforeExpiration = (int) Math.abs(dt / (1000.0 * 60));
                                unit = minutesBeforeExpiration;
                                formattedUnit = minutesBeforeExpiration == 1 ? "minute" : "minutes";
                            }
                        }

                        expiredTextView.setVisibility(View.VISIBLE);
                        expiredTextView.setText("Expires in " + unit + " " + formattedUnit);
                        expiredTextView.setBackgroundResource(R.drawable.near_expired_background);
                        expiredTextView.setTextColor(Color.BLACK);
                        expiredTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.time_icon, 0, 0, 0);
                    }
                    else
                    {
                        expiredTextView.setVisibility(View.VISIBLE);
                        expiredTextView.setText(R.string.expired);
                        expiredTextView.setBackgroundResource(R.drawable.expired_background);
                        expiredTextView.setTextColor(Color.WHITE);
                        expiredTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.time_icon_white, 0, 0, 0);
                    }
                }
            }
            else if (postCategory.equals(Post.EVENT_CATEGORY))
            {
                postCategoryImageView.setImageResource(R.drawable.large_calendar_icon);
                Date eventDate = parsePost.getPostExpiration();
                if (eventDate != null)
                {
                    expiredTextView.setText(eventFormat.format(eventDate));
                    expiredTextView.setBackgroundResource(R.color.transparent);
                    expiredTextView.setTextColor(Color.BLACK);
                    expiredTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.small_calendar_icon, 0, 0, 0);
                }
            }
            else if (postCategory.equals(Post.POST_CATEGORY))
            {
                postCategoryImageView.setImageResource(R.drawable.post_icon);
                expiredTextView.setVisibility(View.GONE);
            }
        }
        else
        {
            expiredTextView.setVisibility(View.GONE);
        }

        FontUtils.getInstance().overrideFonts((View) tvTitle.getParent(), Fonts.LIGHT);
    }
}
