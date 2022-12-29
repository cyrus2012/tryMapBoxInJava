package com.example.trymapbox;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.trymapbox.databinding.FragmentRouteBinding;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.bindgen.Expected;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsUtils;
import com.mapbox.navigation.base.extensions.RouteOptionsExtensions;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver;
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer;
import com.mapbox.navigation.ui.maps.camera.NavigationCamera;
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource;
import com.mapbox.navigation.ui.maps.camera.transition.MapboxNavigationCameraStateTransition;
import com.mapbox.navigation.ui.maps.camera.transition.MapboxNavigationCameraTransition;
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraStateTransition;
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources;
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue;

import java.util.ArrayList;
import java.util.List;

//refer to example "Draw route lines on a map" at https://docs.mapbox.com/android/navigation/examples/render-route-line/
//refer to example "Add a complete turn-by-turn experience" at https://docs.mapbox.com/android/navigation/examples/turn-by-turn-experience/
public class RouteFragment extends Fragment {


    /**
     * RouteLine: Various route line related options can be customized here including applying
     * route line color customizations.
     */
    private RouteLineResources routeLineResources = new RouteLineResources.Builder()
            /**
             * Route line related colors can be customized via the [RouteLineColorResources]. If using the
             * default colors the [RouteLineColorResources] does not need to be set as seen here, the
             * defaults will be used internally by the builder.
             */
            .routeLineColorResources(new RouteLineColorResources.Builder().build())
            .build();

    /**
     * RouteLine: Additional route line options are available through the MapboxRouteLineOptions.
     * Notice here the withRouteLineBelowLayerId option. The map is made up of layers. In this
     * case the route line will be placed below the "road-label" layer which is a good default
     * for the most common Mapbox navigation related maps. You should consider if this should be
     * changed for your use case especially if you are using a custom map style.
     */
    private MapboxRouteLineOptions options;

    /**
     * RouteLine: This class is responsible for rendering route line related mutations generated
     * by the [routeLineApi]
     */
    private MapboxRouteLineView routeLineView;

    /**
     * RouteLine: This class is responsible for generating route line related data which must be
     * rendered by the [routeLineView] in order to visualize the route line on the map.
     */
    private MapboxRouteLineApi routeLineApi;

    /**
     * RouteLine: This is one way to keep the route(s) appearing on the map in sync with
     * MapboxNavigation. When this observer is called the route data is used to draw route(s)
     * on the map.
     */
    private RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NonNull RoutesUpdatedResult routesUpdatedResult) {

            if(routesUpdatedResult.getNavigationRoutes().isEmpty()){
                // remove the route line and route arrow from the map
                Style style = viewBinding.mapView.getMapboxMap().getStyle();
                if(style != null){
                    routeLineApi.clearRouteLine(routeLineErrorRouteLineClearValueExpected -> {
                        routeLineView.renderClearRouteLineValue(viewBinding.mapView.getMapboxMap().getStyle(),
                                routeLineErrorRouteLineClearValueExpected);
                    });
                }
                // remove the route reference from camera position evaluations
                viewportDataSource.clearRouteData();
                viewportDataSource.evaluate();
            }else {
                // RouteLine: wrap the NavigationRoute objects and pass them
                // to the MapboxRouteLineApi to generate the data necessary to draw the route(s)
                // on the map.
                routeLineApi.setNavigationRoutes(routesUpdatedResult.getNavigationRoutes(),
                        new MapboxNavigationConsumer<Expected<RouteLineError, RouteSetValue>>() {
                            @Override
                            public void accept(Expected<RouteLineError, RouteSetValue> routeLineErrorRouteSetValueExpected) {
                                // RouteLine: The MapboxRouteLineView expects a non-null reference to the map style.
                                // the data generated by the call to the MapboxRouteLineApi above must be rendered
                                // by the MapboxRouteLineView in order to visualize the changes on the map.
                                routeLineView.renderRouteDrawData(viewBinding.mapView.getMapboxMap().getStyle(),
                                        routeLineErrorRouteSetValueExpected);
                            }
                        });
                // update the camera position to account for the new route
                viewportDataSource.onRouteChanged(routesUpdatedResult.getNavigationRoutes().get(0));
                viewportDataSource.evaluate();
                navigationCamera.requestNavigationCameraToOverview();
            }
        }
    };

    private MapboxNavigationObserver myMapboxNavigationObserver = new MapboxNavigationObserver() {
        @Override
        public void onAttached(@NonNull MapboxNavigation mapboxNavigation) {
            mapboxNavigation.registerRoutesObserver(routesObserver);
        }

        @Override
        public void onDetached(@NonNull MapboxNavigation mapboxNavigation) {
            mapboxNavigation.unregisterRoutesObserver(routesObserver);
        }
    };

    private EdgeInsets cameraOverviewPadding;

    private Point start = Point.fromLngLat(114.1756, 22.336);
    private Point destination = Point.fromLngLat(114.1731, 22.2832);
    private FragmentRouteBinding viewBinding;
    private NavigationCamera navigationCamera;
    private MapboxNavigationViewportDataSource viewportDataSource;
    private MapboxNavigation mapboxNavigation;
    private List<NavigationRoute> navigationRoutesList;

    public RouteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialize();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(!MapboxNavigationApp.isSetup()){
            Log.d("onCreated", "MapboxNavigationApp is not set up");
            NavigationOptions navigationOptions = new NavigationOptions.Builder(requireContext())
                    .accessToken(getString(R.string.mapbox_access_token))
                    .build();

            MapboxNavigationApp.attach(this);
            MapboxNavigationApp.setup(navigationOptions);
        }
        viewBinding = FragmentRouteBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState ){
        super.onViewCreated(v, savedInstanceState);
        MapboxNavigationApp.registerObserver(myMapboxNavigationObserver);

        // initialize Navigation Camera
        MapboxMap mapboxMap = viewBinding.mapView.getMapboxMap();
        CameraAnimationsPlugin cameraPlugin = CameraAnimationsUtils.getCamera(viewBinding.mapView);
        viewportDataSource = new MapboxNavigationViewportDataSource(mapboxMap);
        MapboxNavigationCameraTransition navigationCameraTransition = new MapboxNavigationCameraTransition(mapboxMap, cameraPlugin);
        navigationCamera = new NavigationCamera(mapboxMap, cameraPlugin, viewportDataSource,
        new MapboxNavigationCameraStateTransition(mapboxMap, cameraPlugin, navigationCameraTransition));

        viewportDataSource.setOverviewPadding(cameraOverviewPadding);

        viewBinding.route1Button.setOnClickListener(buttonClickListener);
        viewBinding.route2Button.setOnClickListener(buttonClickListener);
        viewBinding.route3Button.setOnClickListener(buttonClickListener);

        findRoutes();
    }

    @Override
    public void onDestroyView(){
        MapboxNavigationApp.unregisterObserver(myMapboxNavigationObserver);
        super.onDestroyView();
    }

    @Override
    public void onDestroy(){
        MapboxNavigationApp.detach(this);
        MapboxNavigationApp.disable();
        super.onDestroy();
    }

    private void initialize(){
         options = new MapboxRouteLineOptions.Builder(requireContext())
                        /**
                         * Remove this line and [onPositionChangedListener] if you don't wish to show the
                         * vanishing route line feature
                         */
                        .withVanishingRouteLineEnabled(true)
                        .withRouteLineResources(routeLineResources)
                        .withRouteLineBelowLayerId("road-label-navigation")
                        .build();

        routeLineView = new MapboxRouteLineView(options);
        routeLineApi = new MapboxRouteLineApi(options);

        cameraOverviewPadding = new EdgeInsets(getResources().getDimension(R.dimen.navigation_camera_overview_top),
                getResources().getDimension(R.dimen.navigation_camera_overview_left),
                getResources().getDimension(R.dimen.navigation_camera_overview_bottom),
                getResources().getDimension(R.dimen.navigation_camera_overview_right));
    }

    private void findRoutes(){
        //for the setting, refer to the Mapbox Directions API https://docs.mapbox.com/api/navigation/directions/
        RouteOptions routeOptions = RouteOptions.builder()
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .coordinatesList(List.of(start, destination))
                .alternatives(true)     //return up to 2 alternatives path if available
                .steps(true)            //return steps and turn-by-turn instructions
                .bannerInstructions(true)   //
                //.language() // language of returned turn-bt-turn instruction
                .build();

        mapboxNavigation = MapboxNavigationApp.current();
        if(mapboxNavigation == null)
            Log.w("RequestRoutes", "mapboxNavigation is empty");
        else
            mapboxNavigation.requestRoutes(routeOptions, routerCallback);
    }

    private NavigationRouterCallback routerCallback = new NavigationRouterCallback(){

        @Override
        public void onRoutesReady(@NonNull List<NavigationRoute> list, @NonNull RouterOrigin routerOrigin) {
            mapboxNavigation.setNavigationRoutes(list);
            navigationRoutesList = list;
            switch(list.size()){
                case 3: viewBinding.route3Button.setVisibility(View.VISIBLE);
                case 2: viewBinding.route2Button.setVisibility(View.VISIBLE);
                case 1: viewBinding.route1Button.setVisibility(View.VISIBLE);
                        break;
            }
        }

        @Override
        public void onFailure(@NonNull List<RouterFailure> list, @NonNull RouteOptions routeOptions) {
            Log.d("RequestRoutes", "onFailure");
        }

        @Override
        public void onCanceled(@NonNull RouteOptions routeOptions, @NonNull RouterOrigin routerOrigin) {
            Log.d("RequestRoutes", "onCanceled");
        }
    };

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ArrayList<NavigationRoute> newList = new ArrayList<>();
            newList.addAll(navigationRoutesList);
            NavigationRoute selectedRoute = null;
            if(view.getId() == viewBinding.route1Button.getId()){
                selectedRoute = newList.remove(0);
            }else if(view.getId() == viewBinding.route2Button.getId()){
                selectedRoute = newList.remove(1);
            }else if(view.getId() == viewBinding.route3Button.getId()){
                selectedRoute = newList.remove(2);
            }
            newList.add(0, selectedRoute);
            mapboxNavigation.setNavigationRoutes(newList);
        }
    };


}