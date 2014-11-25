package org.Nordaggios.app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.Nordaggios.app.R;
import org.Nordaggios.app.adapter.RewardListAdapter;
import org.Nordaggios.app.interfaces.ActivityResultListener;
import org.Nordaggios.app.interfaces.RegisterActivityResultListener;
import org.Nordaggios.app.model.LocalUser;
import org.Nordaggios.app.model.Reward;
import org.Nordaggios.app.utility.Constants;
import org.Nordaggios.app.utility.FontUtils;
import org.Nordaggios.app.utility.Fonts;
import org.dm.zbar.android.scanner.ZBarConstants;
import org.dm.zbar.android.scanner.ZBarScannerActivity;

import com.parse.GetCallback;
import com.parse.LocationCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import net.sourceforge.zbar.Symbol;

import java.util.Date;

public class RewardsFragment extends Fragment implements OnItemClickListener, ActivityResultListener
{
    private static final int ZBAR_QR_SCANNER_REQUEST = 1;

    private RewardListAdapter rewardListadapter;
    private Button btnScanForStar;
    private ListView rewardsListView;
    private TextView userRewardPointsTextView;

    private ParseObject userData;

    public RewardsFragment()
    {
    }

    public static RewardsFragment newInstance()
    {
        return new RewardsFragment();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        ((RegisterActivityResultListener) activity).registerActivityResultListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_rewards, container, false);

        rewardListadapter = new RewardListAdapter(getActivity(), R.layout.row_list_reward);
        rewardsListView = (ListView) v.findViewById(R.id.rewards_list_view);
        rewardsListView.setAdapter(rewardListadapter);
        rewardsListView.setOnItemClickListener(this);

        btnScanForStar = (Button) v.findViewById(R.id.btnScanForStar);
        btnScanForStar.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                scanAndGetStar();
            }
        });

        userRewardPointsTextView = (TextView) v.findViewById(R.id.user_points_text_view);
        updateRewardsTextView();
        FontUtils.getInstance().overrideFonts(v, Fonts.LIGHT);

        initUserData();
        return v;
    }

    private void scanAndGetStar()
    {
        if (userData != null)
        {
            if (isCameraAvailable())
            {
                Date lastScanDate = userData.getDate("qrScanDate");
                long lastScanTime = lastScanDate != null ? lastScanDate.getTime() : 0;
                long dt = System.currentTimeMillis() - lastScanTime;
                long neededScanDuration = LocalUser.getInstance().getParentCompany().getInt("scanInterval");
                if (dt > neededScanDuration * 1000 || ParseUser.getCurrentUser().getBoolean("isSuperAdmin"))
                {
                    Intent intent = new Intent(getActivity(), ZBarScannerActivity.class);
                    intent.putExtra(ZBarConstants.SCAN_MODES, new int[]{Symbol.QRCODE});
                    getActivity().startActivityForResult(intent, ZBAR_QR_SCANNER_REQUEST);
                }
                else
                {
                    long minutes = (neededScanDuration * 1000 - dt) / (1000 * 60);
                    Toast.makeText(getActivity(), "You must wait " + minutes + " minutes before scanning again", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(getActivity(), "Camera Unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isCameraAvailable()
    {
        PackageManager pm = getActivity().getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == ZBAR_QR_SCANNER_REQUEST)
            {
                final ProgressDialog dialog = ProgressDialog.show(getActivity(), "Loading", "Checking reward...");

                String contents = data.getStringExtra(ZBarConstants.SCAN_RESULT);

                ParseQuery<ParseObject> locationParentCompanyCheckQuery = new ParseQuery<ParseObject>("LocationPin");
                locationParentCompanyCheckQuery.whereEqualTo("appCompany", LocalUser.getInstance().getParentCompany());

                ParseQuery<ParseObject> qrCodequery = ParseQuery.getQuery(Constants.OBJECT_QRCODE);
                qrCodequery.whereMatchesQuery("location", locationParentCompanyCheckQuery);
                qrCodequery.include("location");
                qrCodequery.getInBackground(contents, new GetCallback<ParseObject>()
                {
                    @Override
                    public void done(final ParseObject qrCodeObject, ParseException e)
                    {
                        if (getActivity() != null)
                        {
                            dialog.cancel();
                            if (e == null)
                            {
                                final ProgressDialog locationCheckDialog = ProgressDialog.show(getActivity(), "Loading", "Checking your location...");
                                final ParseObject locationObject = qrCodeObject.getParseObject("location");
                                ParseGeoPoint.getCurrentLocationInBackground(10000, new LocationCallback()
                                {
                                    @Override
                                    public void done(ParseGeoPoint parseGeoPoint, ParseException e)
                                    {
                                        if (getActivity() != null && locationCheckDialog != null)
                                        {
                                            locationCheckDialog.dismiss();
                                        }

                                        if (e == null)
                                        {
                                            if (parseGeoPoint != null)
                                            {
                                                double distanceKilometers = parseGeoPoint.distanceInKilometersTo(locationObject.
                                                        getParseGeoPoint("location"));
                                                double distanceMeters = distanceKilometers * 1000.0;

                                                if (distanceMeters <= locationObject.getDouble("vicinityRadius"))
                                                {
                                                    int pointsToAward = qrCodeObject.getInt(Constants.POINTS_TO_AWARD);
                                                    userData.put("rewardPoints", userData.getInt("rewardPoints") + pointsToAward);
                                                    userData.put("qrScanDate", new Date());
                                                    userData.saveInBackground(new SaveCallback()
                                                    {
                                                        @Override
                                                        public void done(ParseException paramParseException)
                                                        {
                                                            updateRewardsTextView();
                                                        }
                                                    });
                                                }
                                                else
                                                {
                                                    Toast.makeText(getActivity(), "You must scan this code in a store to win loyalty points", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            else
                                            {
                                                Toast.makeText(getActivity(), "Error - couldn't fetch your location", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(getActivity(), "Invalid QR code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        }
    }

    protected void alert(String message)
    {
        try
        {
            AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
            bld.setMessage(message);
            bld.setNeutralButton("Close", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            bld.create().show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
    {
        Reward reward = rewardListadapter.getItem(position);
        if (userData.getInt("rewardPoints") < reward.getPointsNeeded())
        {
            Toast.makeText(getActivity(), "Sorry! You don't have enough stars.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            showRewardOptionDialog(reward);
        }
    }

    private void updateWonReward(final Reward reward)
    {
        ParseObject rewardWon = new ParseObject("RewardsWon");
        rewardWon.put("wonReward", reward);
        rewardWon.put("winner", ParseUser.getCurrentUser());
        rewardWon.put("hasUserClaimed", true);
        rewardWon.saveInBackground(null);

        userData.put("rewardPoints", userData.getInt("rewardPoints") - reward.getPointsNeeded());
        userData.saveInBackground(null);
        updateRewardsTextView();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(reward.getName()).setMessage("Show to employee to redeem").setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void showRewardOptionDialog(final Reward reward)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("" + reward.get("name"));
        alert.setMessage("Do you want to use this reward?");
        alert.setPositiveButton("Redeem", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                updateWonReward(reward);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    public void updateRewardsTextView()
    {
        if (userData != null)
            userRewardPointsTextView.setText(Integer.toString(userData.getInt("rewardPoints")));
    }

    public void initUserData()
    {
        ParseQuery<ParseObject> userDataQuery = new ParseQuery<ParseObject>("UserData");
        userDataQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        userDataQuery.whereEqualTo("appParentCompany", LocalUser.getInstance().getParentCompany());
        userDataQuery.getFirstInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(ParseObject parseObject, ParseException e)
            {
                if (getActivity() != null)
                {
                    userData = parseObject;
                    updateRewardsTextView();
                }
            }
        });
    }
}