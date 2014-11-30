package org.mochabutterfly.app.activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.mochabutterfly.app.R;
import org.mochabutterfly.app.adapter.HomePageSwipeAdapter;
import org.mochabutterfly.app.fragments.AboutFragment;
import org.mochabutterfly.app.fragments.AddPostFragment;
import org.mochabutterfly.app.fragments.EditLocationFragment;
import org.mochabutterfly.app.fragments.FragmentMore;
import org.mochabutterfly.app.fragments.PostDetailsFragment;
import org.mochabutterfly.app.fragments.PostsFragment;
import org.mochabutterfly.app.fragments.PrivacyPolicyFragment;
import org.mochabutterfly.app.fragments.TabContainerFragment;
import org.mochabutterfly.app.fragments.TermsAndConditionsFragment;
import org.mochabutterfly.app.fragments.VisitSiteFragment;
import org.mochabutterfly.app.interfaces.ActivityResultListener;
import org.mochabutterfly.app.interfaces.LogInStateListener;
import org.mochabutterfly.app.interfaces.RegisterActivityResultListener;
import org.mochabutterfly.app.model.LocalUser;
import org.mochabutterfly.app.model.Post;
import org.mochabutterfly.app.utility.AsyncCallback;
import org.mochabutterfly.app.utility.FontUtils;
import org.mochabutterfly.app.utility.Fonts;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements PostsFragment.FragmentPostItemClickedListener,
        FragmentMore.MoreItemClickedListener, LogInStateListener, RegisterActivityResultListener, PostDetailsFragment.SpecificShareListener

{
    public static final long LOCATION_ALARM_DURATION = (long) (30 * 10 * 1000), SPLASH_SCREEN_DURATION = 5 * 1000;
    private TabContainerFragment mTabContainerFragment;
    private ArrayList<ActivityResultListener> mActivityResultListeners = new ArrayList<>();
    private long startSplashTime;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.splash_screen);

        startSplashTime = System.currentTimeMillis();
        FontUtils.initialize(this, new String[]{Fonts.LIGHT});
        ParseAnalytics.trackAppOpened(getIntent());
        getAppCompanyInfo(new AsyncCallback<Boolean>()
        {
            @Override
            public void onOperationCompleted(Boolean result)
            {
                if (result)
                {
                    initLocationAlarm(MainActivity.this);

                    long dt = System.currentTimeMillis() - startSplashTime;
                    long timeToWait = SPLASH_SCREEN_DURATION - dt;
                    timeToWait = timeToWait > 0 ? timeToWait : 0;

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            initUI();
                        }
                    }, timeToWait);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Error. Please check your network connection.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }


    public void initUI()
    {
        getSupportActionBar().show();
        setContentView(R.layout.activity_main);

        mTabContainerFragment = new TabContainerFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mTabContainerFragment).commit();

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitleTextView = (TextView) findViewById(titleId);
        FontUtils.getInstance().overrideFonts(actionBarTitleTextView, Fonts.LIGHT);
    }

    public static void initLocationAlarm(Context context)
    {
        Intent locationAlarmIntent = new Intent(context, BackgroundNotificationService.class);
        PendingIntent pendingLocationAlarmIntent = PendingIntent.getService(context, 0,
                locationAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), LOCATION_ALARM_DURATION, pendingLocationAlarmIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        updateAddPostButton(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        updateAddPostButton(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAddPostButton(Menu menu)
    {
        MenuItem addPostItem = menu.findItem(R.id.action_add_post);
        if (addPostItem != null)
        {
            ParseUser currentUser = ParseUser.getCurrentUser();
            addPostItem.setVisible(currentUser != null && currentUser.getBoolean("isAdmin"));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_share:
                String shareUrl = LocalUser.getInstance().getParentCompany().getString("appShareUrl");
                onShareAppMenuClicked(shareUrl);
                break;
            case R.id.action_add_post:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, AddPostFragment.newInstance()).
                        addToBackStack(null).commit();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getAppCompanyInfo(final AsyncCallback<Boolean> onCompanyInitialized)
    {
        LocalUser.initialize(this, new AsyncCallback<Boolean>()
        {
            @Override
            public void onOperationCompleted(Boolean result)
            {
                onCompanyInitialized.onOperationCompleted(result);
            }
        });
    }

    @Override
    public void onPostClicked(Post post)
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, PostDetailsFragment.newInstance(post)).addToBackStack(null)
                .commit();
    }

    @Override
    public void onVisitWebMenuClicked()
    {
        VisitSiteFragment visitSiteFragment = VisitSiteFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, visitSiteFragment).addToBackStack(null)
                .commit();
    }

    @Override
    public void onSpecificShare(String shareUrl)
    {
        onShareAppMenuClicked(shareUrl);
    }

    public void shareToFacebook(final String shareUrl)
    {
        String urlToShare = shareUrl;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, urlToShare);

        boolean facebookAppFound = false;
        List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches)
        {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana"))
            {
                intent.setPackage(info.activityInfo.packageName);
                facebookAppFound = true;
                break;
            }
        }

        if (!facebookAppFound)
        {
            String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + urlToShare;
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
        }

        startActivity(intent);
    }

    @Override
    public void onShareAppMenuClicked(final String shareUrl)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Share");
        builder.setItems(new String[]{
                "Facebook", "Email", "Text"
        }, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int clickedPosition)
            {
                switch (clickedPosition)
                {
                    case 0:
                        shareToFacebook(shareUrl);
                        break;
                    case 1:
                        shareToEmail(shareUrl);
                        break;
                    case 2:
                        shareToSms(shareUrl);
                        break;
                }
            }
        });

        builder.show();
    }


    public void shareToEmail(String shareUrl)
    {
        Intent emailIntent = new Intent(Intent.ACTION_SEND, Uri.fromParts("mailto", "", null));
        emailIntent.setType("image/png");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "From the " + getString(R.string.app_name) + " app");
        emailIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
        startActivity(Intent.createChooser(emailIntent, "Email"));
    }

    public void shareToSms(String shareUrl)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) //At least KitKat
        {
            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(this); //Need to change the build to API 19

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);

            if (defaultSmsPackageName != null) //Can be null in case that there is no default, then the user would be able to choose any app that support this intent.
            {
                sendIntent.setPackage(defaultSmsPackageName);
            }

            startActivity(sendIntent);
        }
        else
        {
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.setData(Uri.parse("sms:"));
            sendIntent.putExtra("sms_body", shareUrl);
            startActivity(sendIntent);
        }
    }

    @Override
    public void onTermsConditionMenuClicked()
    {
        TermsAndConditionsFragment termsAndConditionsFragment = new TermsAndConditionsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, termsAndConditionsFragment).addToBackStack(null).commit();
    }

    @Override
    public void onPrivacyPolicyMenuClicked()
    {
        PrivacyPolicyFragment policyFragment = PrivacyPolicyFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, policyFragment).addToBackStack(null).commit();
    }

    @Override
    public void onRewardsClicked()
    {
        mTabContainerFragment.getHomePager().setCurrentItem(HomePageSwipeAdapter.POS_REWARDS);
    }

    @Override
    public void onEditStoreLocationClicked()
    {
        EditLocationFragment editLocationFragment = EditLocationFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, editLocationFragment).addToBackStack(null).commit();
    }

    @Override
    public void onAboutAppMenuClicked()
    {
        AboutFragment aboutFragment = AboutFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, aboutFragment).addToBackStack(null).commit();
    }

    @Override
    public void onCallUsMenuClicked()
    {
        String phn = LocalUser.getInstance().getParentCompany().getString("phoneNumber");
        if (!phn.equals(""))
        {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phn));
            startActivity(callIntent);
        }
    }

    @Override
    public void onEmailUsMenuClicked()
    {
        String email = LocalUser.getInstance().getParentCompany().getString("email");
        if (!email.equals(""))
        {
            Intent intentMail = new Intent(Intent.ACTION_SEND);
            intentMail.putExtra(Intent.EXTRA_EMAIL, new String[]{email});

            intentMail.setType("message/rfc822");
            startActivity(Intent.createChooser(intentMail, "Choose an Email client :"));
        }
    }

    @Override
    public void onLogInToggled(boolean requestLogin)
    {
        supportInvalidateOptionsMenu();
        mTabContainerFragment.getHomePageSwipeAdapter().notifyDataSetChanged();

        if (requestLogin)
            mTabContainerFragment.getHomePager().setCurrentItem(HomePageSwipeAdapter.POS_REWARDS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        for(ActivityResultListener listener : mActivityResultListeners)
        {
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void registerActivityResultListener(ActivityResultListener listener)
    {
        mActivityResultListeners.add(listener);
    }
}