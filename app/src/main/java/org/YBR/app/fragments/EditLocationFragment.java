/**
 *
 */
package org.YBR.app.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;


import org.YBR.app.R;
import org.YBR.app.model.LocalUser;
import org.YBR.app.model.RetailLocation;
import org.YBR.app.utility.FontUtils;
import org.YBR.app.utility.Fonts;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LocationCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Touhid
 */
public class EditLocationFragment extends Fragment {
    private MapView mapView;
    private GoogleMap mapViewManager;

    private List<RetailLocation> locations = new ArrayList<>();
    private HashMap<RetailLocation, Marker> markerMap = new HashMap<>();

    public static EditLocationFragment newInstance() {
        return new EditLocationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rv = inflater.inflate(R.layout.frag_edit_location, container, false);
        rv.findViewById(R.id.btnsaveEditLocation).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRetailLocations();
            }
        });

        mapView = (MapView) rv.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        mapViewManager = mapView.getMap();
        mapViewManager.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                MapsInitializer.initialize(getActivity().getApplicationContext());
                getRetailLocations();
            }
        });

        FontUtils.getInstance().overrideFonts(rv, Fonts.LIGHT);
        return rv;
    }

    public void getRetailLocations() {
        ParseQuery<RetailLocation> query = ParseQuery.getQuery("RetailLocation");
        query.whereEqualTo("appCompany", LocalUser.getInstance().getParentCompany());
        query.findInBackground(new FindCallback<RetailLocation>() {
            @Override
            public void done(List<RetailLocation> retailLocations, ParseException e) {
                if (e == null) {
                    locations.clear();
                    locations.addAll(retailLocations);

                    LatLng centerLatLng = null;
                    for (RetailLocation location : locations) {
                        centerLatLng = new LatLng(location.getLocation().getLatitude(), location.getLocation().getLongitude());
                        MarkerOptions options = new MarkerOptions();
                        options.position(centerLatLng);
                        options.draggable(true);

                        Marker newMarker = mapViewManager.addMarker(options);
                        markerMap.put(location, newMarker);

                        int vicinityRadius = location.getVicinityRadius();
                        CircleOptions circleOptions = new CircleOptions();
                        circleOptions.center(centerLatLng).fillColor(Color.argb(150, 200, 45, 45))
                                .radius(vicinityRadius);
                        mapViewManager.addCircle(circleOptions);
                    }

                    if (centerLatLng != null) {
                        CameraUpdate centerUpdate = CameraUpdateFactory.newLatLngZoom(centerLatLng, 13.0f);
                        mapViewManager.animateCamera(centerUpdate);
                    } else {
                        ParseGeoPoint.getCurrentLocationInBackground(10000, new LocationCallback() {
                            @Override
                            public void done(ParseGeoPoint parseGeoPoint, ParseException e) {
                                if (e == null) {
                                    LatLng centerLocation = new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());
                                    CameraUpdate centerUpdate = CameraUpdateFactory.newLatLngZoom(centerLocation, 13.0f);
                                    mapViewManager.animateCamera(centerUpdate);
                                }
                            }
                        });
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    public void saveRetailLocations() {
        for (RetailLocation location : locations) {
            Marker attachedMarker = markerMap.get(location);
            if (attachedMarker != null) {
                LatLng position = attachedMarker.getPosition();
                location.setLocation(new ParseGeoPoint(position.latitude, position.longitude));
            }

            location.saveInBackground();
        }

        Toast.makeText(getActivity(), "Saved locations", Toast.LENGTH_SHORT).show();
    }
}
