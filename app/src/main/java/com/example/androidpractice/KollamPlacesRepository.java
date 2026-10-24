package com.example.androidpractice;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class KollamPlacesRepository {

    public static class PlaceItem {
        public String id;
        public String name;
        public String category; // "Hidden Spot", "Restaurant", "Cafe", "Fuel"
        public double rating;
        public GeoPoint geoPoint;
        public String address;
        public String description;
        public boolean isVerified;
        public double distanceToRouteKm;
        public String locationTag; // "Along Route", "Near Destination", "District Highlight"

        public PlaceItem(String id, String name, String category, double rating, GeoPoint geoPoint, String address, String description, boolean isVerified) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.rating = rating;
            this.geoPoint = geoPoint;
            this.address = address;
            this.description = description;
            this.isVerified = isVerified;
            this.distanceToRouteKm = 0.0;
            this.locationTag = "Along Route";
        }
    }

    public static List<PlaceItem> getVerifiedKollamHiddenSpots() {
        List<PlaceItem> spots = new ArrayList<>();

        spots.add(new PlaceItem("spot_1", "Munroe Island Backwaters", "Hidden Spot", 4.9,
                new GeoPoint(8.9950, 76.6125), "Munroe Island, Kollam",
                "Quiet cluster of 8 islands in Ashtamudi Lake with narrow canal cruising.", true));

        spots.add(new PlaceItem("spot_2", "Sambranikodi Island", "Hidden Spot", 4.8,
                new GeoPoint(8.9320, 76.5780), "Prakkulam, Kollam",
                "Serene shallow mangrove island in Ashtamudi Lake reachable by local boats.", true));

        spots.add(new PlaceItem("spot_3", "Palaruvi Waterfalls", "Hidden Spot", 4.8,
                new GeoPoint(8.9514, 77.1565), "Aryankavu, Kollam",
                "300ft cascading waterfall surrounded by dense Shendurney forests.", true));

        spots.add(new PlaceItem("spot_4", "Jatayu Earth's Center", "Hidden Spot", 4.9,
                new GeoPoint(8.8683, 76.8686), "Chadayamangalam, Kollam",
                "World's largest bird sculpture atop Jatayu Rock with cable car rides.", true));

        spots.add(new PlaceItem("spot_5", "Rosemala Viewpoint", "Hidden Spot", 4.7,
                new GeoPoint(8.9667, 77.1333), "Aryankavu, Thenmala",
                "Secluded valley overlook deep inside Shendurney Wildlife Sanctuary.", true));

        spots.add(new PlaceItem("spot_6", "Thenmala Eco-Tourism & Dam", "Hidden Spot", 4.7,
                new GeoPoint(8.9622, 77.0628), "Thenmala, Kollam",
                "India's first planned eco-tourism site with suspension bridge and boating.", true));

        spots.add(new PlaceItem("spot_7", "Alumkadavu Houseboat Village", "Hidden Spot", 4.6,
                new GeoPoint(9.0553, 76.5401), "Karunagappally, Kollam",
                "Traditional origin site where Kerala Kettuvallam houseboats are handcrafted.", true));

        spots.add(new PlaceItem("spot_8", "Azheekal Beach & Estuary", "Hidden Spot", 4.8,
                new GeoPoint(9.1234, 76.5218), "Azheekal, Karunagappally",
                "Pristine white sand beach where backwaters meet the Arabian Sea.", true));

        spots.add(new PlaceItem("spot_9", "Tangasseri Lighthouse & Fort Ruins", "Hidden Spot", 4.6,
                new GeoPoint(8.8828, 76.5742), "Tangasseri, Kollam",
                "Historic 144ft lighthouse offering 360-degree panoramic ocean views.", true));

        spots.add(new PlaceItem("spot_10", "Thirumullavaram Beach & Rocks", "Hidden Spot", 4.6,
                new GeoPoint(8.9056, 76.5512), "Thirumullavaram, Kollam",
                "Tranquil beach featuring the ancient Sthanathupara rock formation.", true));

        spots.add(new PlaceItem("spot_11", "Kottukal Rock Cut Cave Temple", "Hidden Spot", 4.7,
                new GeoPoint(8.8872, 76.8833), "Kottukal, Anchal, Kollam",
                "Ancient 8th-century rock-cut cave temple carving in serene countryside.", true));

        spots.add(new PlaceItem("spot_12", "Sasthamcotta Fresh Water Lake", "Hidden Spot", 4.7,
                new GeoPoint(9.0416, 76.6272), "Sasthamcotta, Kollam",
                "Kerala's largest natural freshwater lake surrounded by rolling hills.", true));

        return spots;
    }

    public static List<PlaceItem> getVerifiedKollamRestaurants() {
        List<PlaceItem> restaurants = new ArrayList<>();

        restaurants.add(new PlaceItem("rest_1", "Ramees Restaurant Kollam", "Restaurant", 4.7,
                new GeoPoint(8.8890, 76.5820), "Beach Road, Kollam",
                "Authentic Malabar biryani, seafood karimeen fry & traditional Kerala meals.", true));

        restaurants.add(new PlaceItem("rest_2", "Fayalwan Hotel", "Restaurant", 4.6,
                new GeoPoint(8.8835, 76.5912), "Chinnakada, Kollam",
                "Famous historic Kollam eatery known for mutton soup and Kerala parotta.", true));

        restaurants.add(new PlaceItem("rest_3", "The Quilon Beach Seafood Grill", "Restaurant", 4.8,
                new GeoPoint(8.8812, 76.5765), "Kollam Beach Front",
                "Fresh Arabian Sea seafood delicacies served right along Kollam beach.", true));

        restaurants.add(new PlaceItem("rest_4", "Allspice Restaurant", "Restaurant", 4.5,
                new GeoPoint(8.8923, 76.6015), "Kottarakkara Highway Junction",
                "Multi-cuisine family restaurant on National Highway 66.", true));

        restaurants.add(new PlaceItem("rest_5", "Indian Coffee House Chinnakada", "Cafe", 4.5,
                new GeoPoint(8.8860, 76.5880), "Chinnakada Clock Tower, Kollam",
                "Iconic red-brick spiral cafe serving coffee, cutlets, and masala dosa.", true));

        restaurants.add(new PlaceItem("rest_6", "Supreme Bakers & Cafe", "Cafe", 4.7,
                new GeoPoint(8.8875, 76.5892), "RP Mall Road, Kollam",
                "Famous Kollam bakery, fresh pastries, snacks, and espresso coffee.", true));

        restaurants.add(new PlaceItem("rest_7", "Munroe Island Backwater Diner", "Restaurant", 4.8,
                new GeoPoint(8.9912, 76.6110), "Munroe Island Canal Side",
                "Traditional Kerala fish curry meals served on banana leaves by the lake.", true));

        restaurants.add(new PlaceItem("rest_8", "Thenmala Eco Rest House Diner", "Restaurant", 4.4,
                new GeoPoint(8.9610, 77.0615), "Thenmala Dam Road",
                "South Indian meals and refreshing drinks near Thenmala eco park.", true));

        restaurants.add(new PlaceItem("rest_9", "Shell Highway Mart & Diner", "Fuel", 4.4,
                new GeoPoint(9.0012, 76.5412), "NH 66 Karunagappally",
                "24/7 fuel station, quick snacks, coffee, and clean restrooms on NH66.", true));

        return restaurants;
    }

    // Helper: Compute minimum distance in km from a place point to any segment on the route polyline
    public static double computeMinDistanceToRouteKm(GeoPoint placePoint, List<GeoPoint> routePolyline) {
        if (placePoint == null || routePolyline == null || routePolyline.isEmpty()) {
            return 999.0;
        }

        double minDistanceKm = Double.MAX_VALUE;
        for (GeoPoint routePt : routePolyline) {
            double distKm = placePoint.distanceToAsDouble(routePt) / 1000.0;
            if (distKm < minDistanceKm) {
                minDistanceKm = distKm;
            }
        }
        return minDistanceKm;
    }
}