package org.Legends.app.activity;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.IntentCompat;

import org.Legends.app.R;
import org.Legends.app.model.LocalUser;
import org.Legends.app.utility.AsyncCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Usama on 10/1/14.
 */
public class BackgroundNotificationService extends IntentService implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener
{
    private LocationClient mLocationClient;

    public BackgroundNotificationService()
    {
        super(BackgroundNotificationService.class.toString());
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        final Location currentUserLocation = mLocationClient.getLastLocation();
        if (currentUserLocation != null)
        {
            final LocalUser localUser = LocalUser.getInstance();
            if (localUser == null)
            {
                LocalUser.initialize(BackgroundNotificationService.this, new AsyncCallback<Boolean>()
                {
                    @Override
                    public void onOperationCompleted(Boolean result)
                    {
                        if (result)
                            onLocalUserReady(LocalUser.getInstance(), currentUserLocation);
                    }
                });
            }
            else
            {
                onLocalUserReady(localUser, currentUserLocation);
            }
        }

        mLocationClient.disconnect();
    }

    @Override
    public void onDisconnected()
    {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {

    }

    public void onLocalUserReady(LocalUser localUser, final Location currentUserLocation)
    {
        if (localUser.getParentCompany() != null)
        {
            ParseQuery<ParseObject> notificationObjectQuery = ParseQuery.getQuery("LocationPin");
            notificationObjectQuery.whereEqualTo("appCompany", localUser.getParentCompany());
            notificationObjectQuery.fromLocalDatastore();
            notificationObjectQuery.findInBackground(new FindCallback<ParseObject>()
            {
                @Override
                public void done(final List<ParseObject> parseObjects, ParseException e)
                {
                    ParseObject.unpinAllInBackground("location_pins", parseObjects, new DeleteCallback()
                    {
                        @Override
                        public void done(ParseException e)
                        {
                            if (e == null)
                            {
                                ParseObject.pinAllInBackground("location_pins", parseObjects);
                            }
                            else
                            {
                                e.printStackTrace();
                            }
                        }
                    });

                    for (ParseObject locationPinCandidate : parseObjects)
                    {
                        ParseGeoPoint candidateLocation = locationPinCandidate.getParseGeoPoint("location");
                        double candidateDistanceMeters = candidateLocation.distanceInKilometersTo(new ParseGeoPoint(currentUserLocation.getLatitude(),
                                currentUserLocation.getLongitude())) * 1000;
                        if (candidateDistanceMeters <= locationPinCandidate.getInt("vicinityRadius"))
                        {
                            sendUserNotification(locationPinCandidate);
                        }
                    }
                }
            });
        }
    }

    public void sendUserNotification(ParseObject notificationObject)
    {
        String title = getString(R.string.app_name);
        String message = notificationObject.getString("notificationText");

        Intent notificationIntent = new Intent(this, MainActivity.class);

        // Don't start a new activity if the app is already running in the foreground
        notificationIntent.setFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.app_icon).setContentText(message).setContentIntent(intent).setContentTitle(title).setAutoCancel(true);
        builder.setVibrate(new long[]
                {
                        0, 250
                });

        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) notificationObject.getCreatedAt().getTime(), builder.build());
    }

}