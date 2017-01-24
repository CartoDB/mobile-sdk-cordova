package com.carto.plugin.sdk;

import android.view.Window;
import android.view.Display;

import android.os.Looper;
import android.os.Handler;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.util.Log;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Point;
import android.content.DialogInterface;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.carto.core.MapPos;
import com.carto.core.Variant;

import com.carto.ui.MapView;
import com.carto.ui.MapClickInfo;
import com.carto.ui.MapEventListener;
import com.carto.ui.ClickType;

import com.carto.layers.CartoBaseMapStyle;
import com.carto.layers.CartoOnlineVectorTileLayer;

import com.carto.vectorelements.Text;
import com.carto.vectorelements.BalloonPopup;

import com.carto.layers.VectorLayer;
import com.carto.datasources.LocalVectorDataSource;
import com.carto.projections.Projection;

import com.carto.styles.TextStyleBuilder;
import com.carto.styles.BillboardOrientation;
import com.carto.styles.TextStyleBuilder;
import com.carto.styles.BalloonPopupStyleBuilder;

public class CDMapView extends CordovaPlugin {

    private static final String TAG = "MapView!";

    FrameLayout layout;
    Activity activity;
    Context context;
    MapView mapView;

    CallbackContext clickCallback;

    private void print(String message) {
        Log.d(TAG, message);
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView)
    {
        layout = (FrameLayout) webView.getView().getParent();
        activity = this.cordova.getActivity();
        context = activity.getApplicationContext();
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException 
    {
        if (action.equals("registerLicense")) {
            registerLicense(args.getString(0));
            return true;

        } else if (action.equals("initialize")) {

            double xPercentage = Double.parseDouble(args.getString(0));
            double yPercentage = Double.parseDouble(args.getString(1));
            double widthPercent = Double.parseDouble(args.getString(2));
            double heightPercent = Double.parseDouble(args.getString(3));
    
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point screenSize = new Point();
            display.getSize(screenSize);

            print(screenSize.y + " - " + yPercentage);

            int x = (int)(screenSize.x * xPercentage);
            int y = (int)(screenSize.y * yPercentage);
            int width = (int)(screenSize.x * widthPercent);
            int height = (int)(screenSize.y * heightPercent);

            // TODO: What's this? Why is the native element just a bit too low? Decrease by a few pixels
            y -= (int)(screenSize.y * 0.005);

            initialize(x, y, width, height);
            
            return true;

        } else if (action.equals("setClickListener")) {
            this.clickCallback = callbackContext;
            setClickListener();
            return true;
        } else if (action.equals("showPopup")) {

            double longitude = Double.parseDouble(args.getString(0));
            double latitude = Double.parseDouble(args.getString(1));
            String title = args.getString(2);
            String description = args.getString(3);

            showPopup(longitude, latitude, title, description);
        } else {
            print("Unknown action o_o");
        }

        return false;
    }

    private synchronized boolean registerLicense(String license)
    {
        return MapView.registerLicense(license, cordova.getActivity());
    }

    private synchronized void initialize(final int x, final int y, final int width, final int height)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                print("Frame (java): " + x + ", " + y + ", " + width + ", " + height);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
                params.setMargins(x, y, 0, 0);

                mapView = new MapView(context);

                CartoOnlineVectorTileLayer layer = new CartoOnlineVectorTileLayer(CartoBaseMapStyle.CARTO_BASEMAP_STYLE_DEFAULT);
                
                mapView.getLayers().add(layer);
                mapView.setLayoutParams(params);

                layout.addView(mapView);
            }
        });
    }

    Projection projection;
    LocalVectorDataSource source;

    private synchronized void showPopup(final double longitude, final double latitude, final String title, final String description)
    {
        print("showPopup");
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (mapView == null) {
                    return;
                }

                if (projection == null) {

                    print("Initializing Billboard data source and layer");
                    projection = mapView.getOptions().getBaseProjection();

                    // Initialize an local vector data source
                    source = new LocalVectorDataSource(projection);

                    // Initialize a vector layer with the previous data source
                    VectorLayer vectorLayer = new VectorLayer(source);

                    // Add the previous vector layer to the map
                    mapView.getLayers().add(vectorLayer);
                } else {
                    // After it's initialized, there'll be one element
                    // Remove it and then add another
                    source.remove(source.getAll().get(0));
                }

                // Add popup
                BalloonPopupStyleBuilder builder = new BalloonPopupStyleBuilder();
                builder.setDescriptionWrap(false);
                builder.setPlacementPriority(1);

                MapPos position = projection.fromWgs84(new MapPos(latitude, longitude));

                BalloonPopup popup = new BalloonPopup(position, builder.buildStyle(), title, description);

                source.add(popup);
            }
        });
    }
    
    private synchronized void setClickListener() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapView.setMapEventListener(new MyMapEventListener());

            }
        });
    }

    private class MyMapEventListener extends MapEventListener {

        @Override
        public void onMapClicked(MapClickInfo mapClickInfo) {
            super.onMapClicked(mapClickInfo);
            
            print("onMapClicked");

            if (clickCallback != null) {
                
                try {
                    
                    double x = mapClickInfo.getClickPos().getX();
                    double y = mapClickInfo.getClickPos().getY();

                    JSONObject clickInfo = new JSONObject();
                    clickInfo.put("x", x);
                    clickInfo.put("y", y);

                    MapPos latLon = mapView.getOptions().getBaseProjection().toLatLong(x, y);
                    clickInfo.put("latitude", latLon.getY());
                    clickInfo.put("longitude", latLon.getX());

                    ClickType type = mapClickInfo.getClickType();

                    if (type == ClickType.CLICK_TYPE_LONG) {
                        clickInfo.put("click_type", "LONG");
                    } else if (type == ClickType.CLICK_TYPE_DOUBLE) {
                        clickInfo.put("click_type", "DOUBLE");
                    } else if (type == ClickType.CLICK_TYPE_DUAL) {
                        clickInfo.put("click_type", "DUAL");
                    } else if (type == ClickType.CLICK_TYPE_SINGLE) {
                        clickInfo.put("click_type", "SINGLE");
                    }

                    PluginResult result = new PluginResult(PluginResult.Status.OK, clickInfo);
                    result.setKeepCallback(true);
                    CDMapView.this.clickCallback.sendPluginResult(result);

                } catch (JSONException e) {
                    print("JsonException: " + e.getMessage());
                }
            }
        }
    }

}




