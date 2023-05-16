package com.github.msemitkin.financie.localizatitonapi;

import com.github.msemitkin.financie.domain.Location;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.TimeZone;

public interface GetTimezoneByLocationPort {

    @Nullable
    TimeZone getTimezoneByLocation(@NonNull Location location);
}
