# ⚠️ ARCHIVED - This repository is no longer maintained

**This repository has been archived and is no longer actively maintained.**

This project was last updated on 2017-03-29 and is preserved for historical reference only.

- 🔒 **Read-only**: No new issues, pull requests, or changes will be accepted
- 📦 **No support**: This code is provided as-is with no support or updates
- 🔍 **For reference only**: You may fork this repository if you wish to continue development

For current CARTO projects and actively maintained repositories, please visit: https://github.com/CartoDB

---

## CARTO SDK Cordova Plugin

[CARTO Mobile maps SDK](https://carto.com/engine/mobile/) Plugin for Cordova/PhoneGAP platorm

**NB! This a proof of concept solution. It only features a small percentage of CARTO Mobile SDK's features. It is provided in 'as is' basis - CARTO does not intend to support, document or sell it, and there are no development plans for it. Feel free to use it, and if you extend then please post Pull Requests, so also others can make use of it.**

## Getting Started

A general guide to installing cordova and creating a project can be found [here](https://cordova.apache.org/docs/en/latest/guide/cli/)

### Installation

* To install CartoMobileSDK Cordova plugin to your cordova app, enter `cordova plugin add https://github.com/CartoDB/mobile-sdk-cordova.git` in your project's root folder.
* It is advised to also install Device plugin via `cordova plugin add cordova-plugin-device`, as CARTO's licenses are device-specific
* This plugin also requires jQuery, download jQuery, place it in your `/js/` folder reference it via `<script type="text/javascript" src="js/jquery-3.1.1.min.js"></script>` in your project's `index.html` any time before `<script type="text/javascript" src="js/index.js"></script>`

##### NB! If installing iOS plugin for the first time, you need to change compiler flags the Cordova project's XCode project

* Open the workspace file (XCode) under `platforms/ios/HelloCordova/`, open build settings and make the following changes:

  * Set `C++ Language Dialect` to `C++11`
  * Set `C++ Standard Library` to `libc++`

### API

**NB! This a proof of concept solution. It implements only a small percentage of CARTO Mobile SDK's features. Other features can be added by you (please submit Pull Request!) or by us upon request**

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

* `MapView.showPopup(longitude, latitude, title, description);` shows a **native SDK Balloon popup** at given map coordinates with a title and a description.

### Sample project

* See [https://github.com/CartoDB/mobile-cordova-samples](https://github.com/CartoDB/mobile-cordova-samples)

## Contributing

We're always looking for contributors for this plugin, please add your enhancements as Pull Request in github and feel free to post issues. Contact CARTO if you need full coverage of CARTO Mobile SDK for Cordova.

### Overview

A general overview of Cordova Plugin creation can be found [here](https://cordova.apache.org/docs/en/latest/guide/hybrid/plugins/), but I'll also, in short, explain it here.

The heart and soul of a plugin is `plugin.xml` that defines all the platforms that are supported, the classes and libraries that it contains.

A method definition in Cordova Plugin consts of **two main layers**:

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

