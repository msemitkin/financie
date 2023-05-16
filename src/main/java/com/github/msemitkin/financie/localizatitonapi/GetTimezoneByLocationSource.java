package com.github.msemitkin.financie.localizatitonapi;

import com.github.msemitkin.financie.domain.Location;
import com.google.maps.GeoApiContext;
import com.google.maps.TimeZoneApi;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.TimeZone;

@Service
class GetTimezoneByLocationSource implements GetTimezoneByLocationPort {
    private final GeoApiContext context;

    GetTimezoneByLocationSource(@Value("${com.github.msemitkin.financie.googlemapsplatform.api-key}") String apiKey) {
        this.context = new GeoApiContext.Builder()
            .apiKey(apiKey)
            .build();
    }

    @Override
    public TimeZone getTimezoneByLocation(Location location) {
        return TimeZoneApi
            .getTimeZone(context, new LatLng(location.latitude(), location.longitude()))
            .awaitIgnoreError();
    }
}
