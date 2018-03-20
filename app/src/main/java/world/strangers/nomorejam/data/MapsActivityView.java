package world.strangers.nomorejam.data;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mhealth.core.mvp.BaseTiView;

import net.grandcentrix.thirtyinch.callonmainthread.CallOnMainThread;

import java.util.List;

import world.strangers.nomorejam.MapsActivityPresenter;

/**
 * Created by luhonghai on 3/19/18.
 */

public interface MapsActivityView extends BaseTiView {

    @CallOnMainThread
    void displayDirection(List<PolylineOptions> polylineOptions);

    @CallOnMainThread
    void displayAddress(Address address, MapsActivityPresenter.LocationType type, LatLng location);
}
