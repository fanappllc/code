//
//  PlaceholderTableView.h
//  Instafresh
//
//  Created by Codiant on 9/30/15.
//  Copyright © 2015 Neuron_Mac_Mini 7. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UIScrollView+EmptyDataSet.h"

@protocol PlaceholderTableviewDelegate <NSObject>

@required
- (void)didPressedPlaceholderViewButton;

@end

IB_DESIGNABLE

@interface PlaceholderTableView : UITableView <DZNEmptyDataSetSource, DZNEmptyDataSetDelegate>

@property (nonatomic) IBInspectable UIImage     *placeholderImage;
@property (nonatomic) IBInspectable NSString    *title;
@property (nonatomic) IBInspectable NSString    *subtitle;
@property (nonatomic) IBInspectable BOOL        enableButton;
@property (nonatomic) IBInspectable NSString    *buttonTitle;
@property (nonatomic, assign) id <PlaceholderTableviewDelegate> buttonDelegate;

@end
