## CARTO Cordova Plugin

CARTO's Mobile SDK Cordova Plugin

## Getting Started

A general guide to installing cordova and creating a project can be found [here](https://cordova.apache.org/docs/en/latest/guide/cli/)

### Installation

* To install CartoMobileSDK Cordova plugin to your cordova app, enter `cordova plugin add https://github.com/CartoDB/mobile-sdk-cordova.git` in your project's root folder.
* It is advised to also install Device plugin via `cordova plugin add cordova-plugin-device`, as CARTO's licenses are device-specific
* This plugin also requires jQuery, download jQuery, place it in your `/js/` folder reference it via `<script type="text/javascript" src="js/jquery-3.1.1.min.js"></script>` in your project's `index.html` any time before `<script type="text/javascript" src="js/index.js"></script>`


### API
* `MapView.registerLicense(YOUR_LICENSE);` registers the license, which is required to show the map.
* `MapView.initialize(mapView);` initializes the map with a default base layer. 
 * `mapView` is a html element, e.g. `<div class="mapView"></div>` in `index.html`, that is a required parameter, as it provides the location and size of the map view.
* `MapView.setClickListener(onMapClicked);` sets a click listener to the map

 * Here's an example of onMapClicked and the data it provides: 
 
		```
		 function onMapClicked(clickInfo) {
		
		    if (device.platform == "iOS") {
		        // Android is already Json, iOS returns clickInfo as a string
		        clickInfo = $.parseJSON(clickInfo);
		    }
		    
		    var latitude = clickInfo["latitude"];
		    var longitude = clickInfo["longitude"];
		    
		    var x = clickInfo["x"];
		    var y = clickInfo["y"];
		
		    // Click type can be "SINGLE", "DOUBLE", "DUAL" or "LONG"
		    var clickType = clickInfo["click_type"];
		
		    var title = "Clicked (" + clickType + ")";
		    var description = "Coordinates: " + latitude + ", " + longitude;
		
		    MapView.showPopup(longitude, latitude, title, description);
		    
		}
		
		```

* `MapView.showPopup(longitude, latitude, title, description);` shows a **native Balloon popup** at coordinates with a title and a description.

### Sample project

We have a small sample project available [here](https://github.com/CartoDB/mobile-cordova-samples)

## Contributing

We're always looking for contributors for this plugin, as our main focus is providing a great SDK and we simply cannot find the time to offer full-time support for a Cordova Plugin.

### Overview

A general overview of Cordova Plugin creation can be found [here](https://cordova.apache.org/docs/en/latest/guide/hybrid/plugins/), but I'll also, in short, explain it here.

The heart and soul of a plugin is `plugin.xml` that defines all the platforms that are supported, the classes and libraries that it contains.

Additionally, a Cordova Plugin consts of **2** main layers:

#### A JavaScript module 

`www/mapview.js` in this plugin that in turn calls native code, e.g.
	
	module.exports = {
		registerLicense: function(license) {
				cordova.exec(null, null, "CDMapView", "registerLicense", [license]);
		}
	};
	
	
* The first two arguments, `null` in this example, are success/failure callbacks.


#### Native

* Java 
 * `src/android/CDMapView.java` contains the Java code that in turn calls our native SDK's functions
 * `public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException` is what receives all of the commands from the JavaScript module (args) and provides a callback context that can call `.success()` or `.error()`
 
* Objective-C
 * `src/ios/CDMapView.h` and `src/ios/CDMapView.mm` contain the Objective-C code that calls our native SDK's functions
 * Unlike Java, we do not need a special receiver method, the methods are called directly, however, they require a special `CDVInvokedUrlCommand parameter` that functions as both the argument list (`[command argumentAtIndex:0]`) and the callback context (`self.clickCommand = command;`)

### Debugging

There's no easy solution for debugging Cordova, that's why we've also included a small cheat-sheet in the README

#### Debugging JavaScript

##### Android

* Without a special plugin, only supported by Google Chrome
* Open the following page: chrome://inspect/#devices
* Click **Inspect** on the device that is currently running your Cordova application

NB! You must click **Inspect** each time you start a new session (`cordova run android`)

##### iOS

* Without a special plugin, only supported by Safari
* Enable developer mode in Safari:
 * Pull down the “Safari” menu and choose “Preferences”
 * Click on the “Advanced” tab
 * Check the box next to “Show Develop menu in menu bar”
 * Close Preferences, the Develop menu will now be visible between Bookmarks and Window menus
* `Develop > Simulator > index.html` opens the remote inspector
 
 
#### Debugging Native

##### Android

* Compile time
 * Cordova CLI will log any compile time errors you might have
* Runtime
 * Open **Android Device Monitor** and filter by tag `MapView`
 
 
##### iOS

* Compile time
 * Cordova CLI will log any compile time errors you might have
* Runtime
 * ?

