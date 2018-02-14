package com.example.windows.mapfix;

/**
 * Created by benk on 2/13/2018.
 */

import java.util.List;

public interface RouteFinderListener {
    void onRouteFinderStart();
    void onRouteFinderSuccess(List<Rute> route);
}
