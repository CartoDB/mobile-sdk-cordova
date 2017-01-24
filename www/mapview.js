
var exec = require('cordova/exec');

module.exports = {

	registerLicense: function(license) {
		cordova.exec(null, null, "CDMapView", "registerLicense", [license]);
	},

	initialize: function(element) {
		element.css("background-color", "blue");
		// Combine both offset and margin when calculating position,
		// as in this case they combine into the true X and Y we are looking for
		var x = element.offset().left + parseInt(element.css("margin-left").replace("px", ""));
		var y = element.offset().top + parseInt(element.css("margin-top").replace("px", ""));
		var width = element.width();
		var height = element.height();

		// Convert to percentages of screen, since measurement units are different. 
		// Calculate width from percentage in CDMapView.java/.m
		x = x / $(window).width();
		y = y / $(window).height();
		width = width / $(window).width();
		height = height / $(window).height();

		cordova.exec(null, null, "CDMapView", "initialize", [x, y, width, height]);
	},

	setClickListener: function(listener) {
		cordova.exec(listener, null, "CDMapView", "setClickListener", null);
	},

	showPopup: function(longitude, latitude, title, description) {
		cordova.exec(null, null, "CDMapView", "showPopup", [longitude, latitude, title, description]);
	}
};
