package org.phoenix.app.model;

import android.content.Context;

import org.phoenix.app.utility.AsyncCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Usama on 9/24/14.
 */
public class LocalUser
{
    private ParseObject parentCompany;
    private static LocalUser singleton;

    private LocalUser()
    {

    }

    public static void initialize(Context context, final AsyncCallback<Boolean> onFinishedCallback)
    {
        singleton = new LocalUser();

        ParseQuery<ParseObject> companyQuery = ParseQuery.getQuery("AppParentCompany");
        companyQuery.whereEqualTo("appIdentifier", context.getPackageName());
        companyQuery.getFirstInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(ParseObject parseObject, ParseException e)
            {
                singleton.parentCompany = parseObject;

                if (onFinishedCallback != null)
                    onFinishedCallback.onOperationCompleted(e == null);
            }
        });
    }

    public static LocalUser getInstance()
    {
        return singleton;
    }

    public ParseObject getParentCompany()
    {
        return parentCompany;
    }

    public void setParentCompany(ParseObject parentCompany)
    {
        this.parentCompany = parentCompany;
    }
}
