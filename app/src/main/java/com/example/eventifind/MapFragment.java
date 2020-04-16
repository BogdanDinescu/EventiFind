package com.example.eventifind;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    private static GoogleMap gMap;
    private MapView mapView;
    private List<Marker> markers;
    private GoogleSignInAccount account;
    private Location currentLocation = null;
    private MainActivity activity;

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
        activity = (MainActivity) getActivity();
    }

    // atunci cand harta a fost creata se pot efectua actiuni
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        account = GoogleSignIn.getLastSignedInAccount(getActivity());
        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setInfoWindowAdapter(this);
        googleMap.setMyLocationEnabled(true);

        // centreaza pe locatia curenta
        if(currentLocation != null) {
            centreOnPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
        }

        markers = new ArrayList<Marker>();
        addMarkers();
        colorMarkers();
    }

    public void addMarkers(){
        for(Map.Entry<String,Event> e: activity.getDatabase().eventMap.entrySet()) {
            LatLng point = new LatLng(e.getValue().getLatitude(), e.getValue().getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().
                    position(point)
                    .title(e.getKey());
            markers.add(gMap.addMarker(markerOptions));
        }
    }

    public void colorMarkers(){
        for (Marker m: markers){
            if(activity.getDatabase().joinedEvents.contains(m.getTitle())){
                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
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
        Event event = activity.getDatabase().eventMap.get(key);

        View markerView = getLayoutInflater().inflate(R.layout.custom_marker_window, null);
        TextView join = markerView.findViewById(R.id.join);
        if(activity.getDatabase().joinedEvents.contains(marker.getTitle())){
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
        //marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        marker.hideInfoWindow();
        Toast toast;
        if(activity.getDatabase().joinedEvents.contains(marker.getTitle())) {
            toast = Toast.makeText(getContext(), getResources().getString(R.string.Unjoined), Toast.LENGTH_LONG);
        } else {
            toast = Toast.makeText(getContext(), getResources().getString(R.string.Joined), Toast.LENGTH_LONG);
        }
        activity.getDatabase().joinEvent(account.getId(),marker.getTitle());
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

    public Location getCurrentLocation(Context context) throws ConnectException{

        LocationManager service = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        // daca GPS sau Conexiune nu sunt pornite arunca exceptie
        if(!service.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
        !service.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ) {
            throw new ConnectException("GPS or Connection unavailable");
        }

        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        try {
            currentLocation = service.getLastKnownLocation(provider);
        } catch (SecurityException e){
            e.printStackTrace();
        }
        return currentLocation;
    }

    public void centreOnPoint(double latitude, double longitude){
        LatLng latLng = new LatLng(latitude,longitude);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(15)
                .build();
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void showDialog(double latitude, double longitude,String address){
        DialogFragment newFragment = new EventDialog(latitude,longitude,address);
        newFragment.show(getFragmentManager(), "eventDialog");
    }

}
