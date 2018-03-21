package world.strangers.nomorejam;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mhealth.core.mvp.BaseTiActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import world.strangers.nomorejam.data.MapsActivityView;

public class MapsActivity extends BaseTiActivity<MapsActivityPresenter, MapsActivityView> implements OnMapReadyCallback, MapsActivityView {

    private GoogleMap mMap;

    ArrayList markerPoints= new ArrayList();

    @BindView(R.id.tv_address_1)
    AppCompatEditText tvAddress1;

    @BindView(R.id.tv_address_2)
    AppCompatEditText tvAddress2;

    @BindView(R.id.adView)
    AdView adView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ButterKnife.bind(this);
        MobileAds.initialize(this, getString(R.string.admob_app_id));

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                getPresenter().pickAddress(place.getLatLng(), MapsActivityPresenter.LocationType.FROM);
            }

            @Override
            public void onError(Status status) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(21.0225932,105.8043822);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16));
        mMap.setOnMapClickListener(latLng -> {

            if (markerPoints.size() > 1) {
                markerPoints.clear();
                mMap.clear();
                tvAddress1.setText("");
                tvAddress2.setText("");
            }

            // Adding new item to the ArrayList
            markerPoints.add(latLng);

            // Creating MarkerOptions
            MarkerOptions options = new MarkerOptions();

            // Setting the position of the marker
            options.position(latLng);

            if (markerPoints.size() == 1) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                getPresenter().pickAddress(latLng, MapsActivityPresenter.LocationType.FROM);
            } else if (markerPoints.size() == 2) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                getPresenter().pickAddress(latLng, MapsActivityPresenter.LocationType.TO);
            }

            // Add new marker to the Google Map Android API V2
            mMap.addMarker(options);

            // Checks, whether start and end locations are captured
            if (markerPoints.size() >= 2) {
                LatLng origin = (LatLng) markerPoints.get(0);
                LatLng dest = (LatLng) markerPoints.get(1);

                getPresenter().calculateDirection(origin, dest);
            }

        });

    }

    @NonNull
    @Override
    public MapsActivityPresenter providePresenter() {
        return new MapsActivityPresenter(new Geocoder(this, Locale.getDefault()));
    }


    @Override
    public void displayDirection(List<PolylineOptions> polylineOptions) {
        if (mMap != null) {
            for (PolylineOptions options : polylineOptions) {
                mMap.addPolyline(options);
            }
        }
    }

    @Override
    public void displayAddress(Address address, MapsActivityPresenter.LocationType type, LatLng location) {
        if (type == MapsActivityPresenter.LocationType.FROM) {
            tvAddress1.setText(address.getAddressLine(0));
        } else if (type == MapsActivityPresenter.LocationType.TO) {
            tvAddress2.setText(address.getAddressLine(0));
        }
    }
}
