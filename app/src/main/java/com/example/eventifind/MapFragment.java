package com.example.eventifind;

import android.content.Context;
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

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener{

    private static GoogleMap gMap;
    private MapView mapView;
    private List<Marker> markers;
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
        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setInfoWindowAdapter(this);
        googleMap.setMyLocationEnabled(true);

        Location location = activity.getLocationService().getCurrentLocation();
        if (location != null)
            this.centreOnPoint(location.getLatitude(),location.getLongitude());
    }

    public void addMarkers(){
        markers = new ArrayList<Marker>();
        for(Map.Entry<String,Event> e: activity.getDatabase().eventMap.entrySet()) {
            LatLng point = new LatLng(e.getValue().getLatitude(), e.getValue().getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().
                    position(point)
                    .title(e.getKey());
            markers.add(gMap.addMarker(markerOptions));
        }
    }
    // titlul markerului reprezinta in implementarea asta id-ul eventului
    public void deleteMarker(String title){
        for(Marker marker: markers) {
            if (marker.getTitle().equals(title)) {
                marker.remove();
                Log.e("removed","removed");
                return;
            }
        }
    }

    public void colorMarkers(){
        if (markers == null) return;
        for (Marker m: markers){
            if(activity.getDatabase().joinedEvents.contains(m.getTitle())){
                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
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
        activity.getDatabase().joinEvent(activity.getUserId(),marker.getTitle());
        toast.show();
    }

    // la click prelung pe map
    @Override
    public void onMapLongClick(LatLng point) {
        if(activity.getDatabase().admin)
            showDialog(point);
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

    public String getCityLocation(LatLng point) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            if (null != listAddresses && listAddresses.size() > 0) {
                return listAddresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return getActivity().getResources().getString(R.string.Unnamed_location);

    }

    public void centreOnPoint(double latitude, double longitude){
        LatLng latLng = new LatLng(latitude,longitude);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(15)
                .build();
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void showDialog(LatLng point){
        DialogFragment newFragment = new EventDialog(point.latitude,
                point.longitude,
                activity.getUserId(),
                activity.getUserName(),
                getAddressLocation(point));
        newFragment.show(getFragmentManager(), "eventDialog");
    }

}
