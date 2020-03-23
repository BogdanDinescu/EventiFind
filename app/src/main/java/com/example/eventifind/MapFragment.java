package com.example.eventifind;

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap gMap;
    private MapView mapView;

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
        googleMap.setOnMapLongClickListener(this);

        // obtine locatia curenta
        Location location = getCurrentLocation();
        // se centreaza camera pe locatia curenta
        if (location != null) {
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(15)
                    .build();
            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        //LatLng sydney = new LatLng(-34, 151);
        //gMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //gMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onMapLongClick(LatLng point) {
        showDialog(point,getAddressLocation(point));
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

    public void showDialog(LatLng point,String address){
        DialogFragment newFragment = new EventDialog(point,address);
        newFragment.show(getFragmentManager(), "eventDialog");
    }
}
