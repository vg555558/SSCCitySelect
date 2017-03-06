/********* GXPlugin.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "CityViewController.h"
#import <MapKit/MapKit.h>
#import "locationGPS.h"
//#import "Myanotation.h"
#import "LocationManager.h"
@interface CitySelectPlugin : CDVPlugin {
    // Member variables go here.
}
- (void)startSelectCity:(CDVInvokedUrlCommand*)command;
@property(nonatomic)locationGPS *loc;
@end

@implementation CitySelectPlugin


- (void)pluginInitialize {
    
    
}

- (void)startSelectCity:(CDVInvokedUrlCommand*)command{
    
    NSDictionary* paramters = [command.arguments objectAtIndex:0];
    NSLog(@"传来的参数：%@",paramters);
    
    
    //    self.loc = [locationGPS sharedlocationGPS];
    //    [self.loc getAuthorization];//授权
    //    [self.loc startLocation];//开始定位
    //    self.loc.address = ^(NSString * address) {
    //
    //        NSDictionary * dic = [NSDictionary dictionaryWithObject:address forKey:@"poiName"];
    //
    //
    //    };
    
    
    
    
    
    CityViewController *cityListVC = [[CityViewController alloc] init];
    cityListVC.currentCityString = paramters[@"city"];
    
    if ([cityListVC.currentCityString  isEqual: @""] || cityListVC.currentCityString == nil ) {
        cityListVC.currentCityString = @"没有选择";
    }
    
    [self.viewController presentViewController:cityListVC animated:true completion:^{
        
    }];
    
    [self.commandDelegate runInBackground:^{
        
        cityListVC.selectString = ^(NSString* citySting){
            
            NSLog(@"%@",citySting);
            NSDictionary * dic = [NSDictionary dictionaryWithObject:citySting forKey:@"city"];
            
            CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dic];
            //            NSLog(@"sdfghgfdssdfghgfds%@,%@",commandResult,address);
            
            if (commandResult != NULL) {
                [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
            }
            
        };
        
    }];
    
    
}




@end


