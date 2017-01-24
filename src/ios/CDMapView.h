
#import <Cordova/CDV.h>
#import <CartoMobileSDK/CartoMobileSDK.h>

@interface CDMapView : CDVPlugin

@property (nonatomic,strong) NTMapView* mapView;

@property NTProjection *projection;
@property NTLocalVectorDataSource *source;

@property CDVInvokedUrlCommand *clickCommand;

- (void)registerLicense:(CDVInvokedUrlCommand*)command;
- (void)initialize:(CDVInvokedUrlCommand*)command;
- (void)showPopup:(CDVInvokedUrlCommand*)command;
- (void)setClickListener:(CDVInvokedUrlCommand*)command;

- (void)onMapClicked:(NTMapClickInfo *)mapClickInfo;

@end
