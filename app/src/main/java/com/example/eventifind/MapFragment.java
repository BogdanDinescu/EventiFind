package com.example.eventifind;

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.LOCATION_SERVICE;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    private static GoogleMap gMap;
    private MapView mapView;
    private static List<Marker> markers;
    private GoogleSignInAccount account;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    // atunci cand harta a fost creata se pot efectua actiuni
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        account = GoogleSignIn.getLastSignedInAccount(getActivity());
        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setInfoWindowAdapter(this);

        // obtine locatia curenta
        Location location = getCurrentLocation();

        // se centreaza camera pe locatia curenta
        if (location != null) {
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(15)
                    .build();
            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        markers = new ArrayList<Marker>();
        // obtine lista cu event-urile din imprejur si tot aceasta functie apeleaza addMarkers
        Database.queryClosestEvents(location,10);
        // obtine evenimentele la care participa userul cu id-ul respectiv
        Database.getJoinedEvents(account.getId());
    }

    public static void addMarkers(){
        for(Map.Entry<String,Event> e: Database.eventMap.entrySet()) {
            LatLng point = new LatLng(e.getValue().getLatitude(), e.getValue().getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().
                    position(point)
                    .title(e.getKey());
            markers.add(gMap.addMarker(markerOptions));
        }
    }

    public static void ColorMarkers(){
        for (Marker m: markers){
            if(Database.joinedEvents.containsValue(m.getTitle())){
                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
            }else {
                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            }
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }
    // se seteaza aspectul pentru "mini-fereastra" care apare cand dai click pe un marker
    @Override
    public View getInfoContents(Marker marker) {
        String key = marker.getTitle();
        Event event = Database.eventMap.get(key);

        View markerView = getLayoutInflater().inflate(R.layout.custom_marker_window, null);
        TextView join = markerView.findViewById(R.id.join);
        if(Database.joinedEvents.containsValue(marker.getTitle())){
            join.setText(getResources().getString(R.string.Unjoin));
        }else {
            join.setText(getResources().getString(R.string.Join));
        }
        TextView title = markerView.findViewById(R.id.title);
        title.setText(event.getName());
        TextView date = markerView.findViewById(R.id.date);
        date.setText(new SimpleDateFormat("dd/MM/yyyy").format(event.getDate() ) );
        TextView description = markerView.findViewById(R.id.description);
        description.setText(event.getDescription());
        return markerView;
    }

    // la click pe "mini-fereastra" care apare cand dai click pe un marker (join/unjoin)
    @Override
    public void onInfoWindowClick(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        marker.hideInfoWindow();
        Database.JoinEvent(account.getId(),marker.getTitle());
        Toast toast;
        if(Database.joinedEvents.containsValue(marker.getTitle())) {
             toast = Toast.makeText(getContext(), getResources().getString(R.string.Unjoined), Toast.LENGTH_LONG);
        } else {
            toast = Toast.makeText(getContext(), getResources().getString(R.string.Joined), Toast.LENGTH_LONG);
        }
        toast.show();
    }

    // la click prelung pe map
    @Override
    public void onMapLongClick(LatLng point) {
        showDialog(point.latitude,point.longitude,getAddressLocation(point));
    }

    // aceasta functie primeste Latitudine si Longitudine si returneaza stringul cu adresa
    private String getAddressLocation(LatLng point) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            if (null != listAddresses && listAddresses.size() > 0) {
                return listAddresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return getActivity().getResources().getString(R.string.Unnamed_location);
    }

    private Location getCurrentLocation() {
        LocationManager service = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        Location location = null;
        try {
            location = service.getLastKnownLocation(provider);
        } catch (SecurityException e){
            e.printStackTrace();
        }
        return location;
    }

    public void showDialog(double latitude, double longitude,String address){
        DialogFragment newFragment = new EventDialog(latitude,longitude,address);
        newFragment.show(getFragmentManager(), "eventDialog");
    }

}
