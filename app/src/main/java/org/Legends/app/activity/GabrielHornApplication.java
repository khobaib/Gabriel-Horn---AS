package org.Legends.app.activity;

import android.app.Application;

import org.Legends.app.model.Post;
import org.Legends.app.model.RetailLocation;
import org.Legends.app.model.Reward;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

public class GabrielHornApplication extends Application
{
    public static final String CHANNEL_NAME = "PushLegends";
    private static final String APP_ID = "b0iV7zeWFXFN0BcAd5gv3OjAYjWQXtbI5rsdJmU3", CLIENT_KEY = "UKSHdFci5UMlEmawuAfSPVEbBKGfVsy35B4K34C8";

    @Override
    public void onCreate()
    {
        super.onCreate();

        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Reward.class);
        ParseObject.registerSubclass(RetailLocation.class);

        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(this, APP_ID, CLIENT_KEY);
        ParsePush.subscribeInBackground(CHANNEL_NAME, new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e != null)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
