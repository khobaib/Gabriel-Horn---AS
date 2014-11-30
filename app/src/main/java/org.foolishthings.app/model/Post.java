package org.foolishthings.app.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.io.Serializable;
import java.util.Date;

@ParseClassName("Post")
public class Post extends ParseObject implements Serializable
{
    public final static String EVENT_CATEGORY = "Event", POST_CATEGORY = "Post", OFFER_CATEGORY = "Offer";

    public String getCategory()
    {
        return getString("category");
    }

    public void setCategory(String category)
    {
        put("category", category);
    }

    public String getContents()
    {
        return getString("contents");
    }

    public void setContents(String contents)
    {
        put("contents", contents);
    }

    public String getLink()
    {
        return getString("link");
    }

    public void setLink(String link)
    {
        put("link", link);
    }

    public String getTitle()
    {
        return getString("title");
    }

    public void setTitle(String title)
    {
        put("title", title);
    }

    public Date getPostExpiration()
    {
        return getDate("expiration");
    }

    public void setPostExpiration(Date postExpiration)
    {
        put("expiration", postExpiration);
    }
}
