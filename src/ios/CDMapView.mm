#import "CDMapView.h"

@interface MapListener : NTMapEventListener

@property CDMapView *mainView;

@end

@implementation CDMapView

- (void)pluginInitialize
{
}

- (void)registerLicense:(CDVInvokedUrlCommand*)command
{
    [NTMapView registerLicense:[command argumentAtIndex:0]];
}

- (void)initialize:(CDVInvokedUrlCommand*)command
{  
    CGRect bounds = [[UIScreen mainScreen] bounds];
    
    CGFloat x = [[command argumentAtIndex:0] floatValue] * bounds.size.width;
    CGFloat y = [[command argumentAtIndex:1] floatValue] * bounds.size.height;
    CGFloat width = [[command argumentAtIndex:2] floatValue] * bounds.size.width;
    CGFloat height = [[command argumentAtIndex:3] floatValue] * bounds.size.height;

    CGRect frame = CGRectMake(x, y, width, height);
    self.mapView = [[NTMapView alloc] initWithFrame:frame];

    [self.webView.superview addSubview:self.mapView];
    
    NTCartoOnlineVectorTileLayer* layer = [[NTCartoOnlineVectorTileLayer alloc] initWithStyle:NT_CARTO_BASEMAP_STYLE_DEFAULT];
    [[self.mapView getLayers] add:layer];
}

- (void)showPopup:(CDVInvokedUrlCommand*)command
{   
    if (self.projection == nil) {
        self.projection = [[self.mapView getOptions]getBaseProjection];
        self.source = [[NTLocalVectorDataSource alloc]initWithProjection:self.projection];
        
        NTVectorLayer *layer = [[NTVectorLayer alloc]initWithDataSource:self.source];
        
        [[self.mapView getLayers]add:layer];
    } else {
        NTVectorElement *popup = [[self.source getAll]get:0];
        [self.source remove:popup];
    }

    CGFloat longitude = [[command argumentAtIndex:0] floatValue];
    CGFloat latitude = [[command argumentAtIndex:1] floatValue];

    NSString *title = [command argumentAtIndex:2];
    NSString *description = [command argumentAtIndex:3];

    NTBalloonPopupStyleBuilder *builder = [[NTBalloonPopupStyleBuilder alloc]init];
    [builder setDescriptionWrap:NO];
    [builder setPlacementPriority:1];

    NTMapPos *position = [self.projection fromWgs84:[[NTMapPos alloc]initWithX:latitude y:longitude]];
    
    NTBalloonPopup *popup = [[NTBalloonPopup alloc]initWithPos:position style:[builder buildStyle] title:title desc:description];

    [self.source add:popup];
}

- (void)setClickListener:(CDVInvokedUrlCommand*)command
{
    self.clickCommand = command;

    MapListener* listener = [[MapListener alloc]init];
    listener.mainView = self;
    [self.mapView setMapEventListener: listener];
}

- (void)onMapClicked:(NTMapClickInfo *)mapClickInfo
{
    [self.commandDelegate runInBackground:^{

        NSString *clickType = @"";

        if ([mapClickInfo getClickType] == NT_CLICK_TYPE_SINGLE) {
            clickType = @"SINGLE";
        } else if ([mapClickInfo getClickType] == NT_CLICK_TYPE_DOUBLE) {
            clickType = @"DOUBLE";
        } else if ([mapClickInfo getClickType] == NT_CLICK_TYPE_LONG) {
            clickType = @"LONG";
        } else if ([mapClickInfo getClickType] == NT_CLICK_TYPE_DUAL) {
            clickType = @"DUAL";
        }

        double x = [[mapClickInfo getClickPos] getX];
        double y = [[mapClickInfo getClickPos] getY];

        NTProjection *projection = [[self.mapView getOptions]getBaseProjection];
        NTMapPos *latLon = [projection toLatLong:x y:y];
    
        NSDictionary *dict = @{
            @"x": @(x),
            @"y": @(y),
            @"latitude": @([latLon getY]),
            @"longitude": @([latLon getX]),
            @"click_type": clickType
        };
        NSError *error;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict
                                                           options:NSJSONWritingPrettyPrinted 
                                                             error:&error];
        NSString *payload = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];

        // Some blocking logic...
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:payload];
        [pluginResult setKeepCallback:[NSNumber numberWithBool:YES]];

        // The sendPluginResult method is thread-safe.
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.clickCommand.callbackId];
    }];  
}

@end

@implementation MapListener

-(void) onMapClicked:(NTMapClickInfo *)mapClickInfo
{
    [self.mainView onMapClicked:mapClickInfo];
}

@end


