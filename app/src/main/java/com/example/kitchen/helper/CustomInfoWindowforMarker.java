package com.example.kitchen.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.kitchen.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowforMarker implements GoogleMap.InfoWindowAdapter {

    View infoView;

    public CustomInfoWindowforMarker(Context context) {

        infoView = LayoutInflater.from(context).inflate(R.layout.rider_info_marker, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {

        TextView tvPickupInfo = (TextView)infoView.findViewById(R.id.tvPickupInfo);
        tvPickupInfo.setText(marker.getTitle());

        TextView tvPickupSnippet = ((TextView)infoView.findViewById(R.id.tvPickupSnippet));
        tvPickupSnippet.setText(marker.getSnippet());

        return infoView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
