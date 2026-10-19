package com.example.androidpractice;

import android.content.Context;
import android.content.SharedPreferences;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;

import java.io.File;

public class MapTileConfig {

    private static final String CUSTOM_USER_AGENT = "HiddenSpotLocator/1.0 (com.example.androidpractice.newversion; contact@hiddenspotlocator.org)";

    // 1. ESRI World Street Map (Documented, open-access ArcGIS tile service, NO API KEY REQUIRED)
    public static final OnlineTileSourceBase ESRI_WORLD_STREET = new OnlineTileSourceBase(
            "EsriWorldStreetMap",
            1, 19, 256, "",
            new String[]{
                    "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/"
            },
            "Tiles © Esri — Source: Esri, DeLorme, NAVTEQ, USGS, Intermap, iPC, NRCAN, Esri Japan, METI, Esri China, TomTom") {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() + MapTileIndex.getZoom(pMapTileIndex) + "/" +
                    MapTileIndex.getY(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex);
        }
    };

    // 2. OpenStreetMap Standard Mapnik (Policy-compliant, custom User-Agent, NO API KEY REQUIRED)
    public static final ITileSource OSM_STANDARD = TileSourceFactory.MAPNIK;

    // 3. Default OpenStreetMap Tile Source (NO API KEY REQUIRED)
    public static final ITileSource DEFAULT_OSM = TileSourceFactory.DEFAULT_TILE_SOURCE;

    public static void initializeMapConfiguration(Context context) {
        if (context == null) return;
        Context appContext = context.getApplicationContext();

        // Load OSMDroid Configuration with app-specific SharedPreferences
        SharedPreferences prefs = appContext.getSharedPreferences("hiddenspot_osmdroid_prefs", Context.MODE_PRIVATE);
        Configuration.getInstance().load(appContext, prefs);

        // Set unique, policy-compliant User-Agent
        Configuration.getInstance().setUserAgentValue(CUSTOM_USER_AGENT);

        // Configure persistent disk tile cache
        File tileCacheDir = new File(appContext.getCacheDir(), "osmdroid_tiles");
        if (!tileCacheDir.exists()) {
            tileCacheDir.mkdirs();
        }
        Configuration.getInstance().setOsmdroidTileCache(tileCacheDir);

        // Direct HTTP connection setup
        Configuration.getInstance().setHttpProxy(null);
    }

    public static void configureMapView(MapView mapView) {
        if (mapView == null) return;

        // Default to Esri World Street Map (Documented, reliable, NO API KEY REQUIRED)
        mapView.setTileSource(ESRI_WORLD_STREET);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);

        mapView.setDestroyMode(false);
    }

    public static void setCustomTileSource(MapView mapView, ITileSource tileSource) {
        if (mapView == null) return;
        mapView.setTileSource(tileSource != null ? tileSource : ESRI_WORLD_STREET);
        mapView.invalidate();
    }
}