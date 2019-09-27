//
//  PlaceholderCollectionView.h
//  Instafresh
//
//  Created by Neuron_Mac_Mini 7 on 11/10/15.
//  Copyright © 2015 Neuron_Mac_Mini 7. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UIScrollView+EmptyDataSet.h"

@protocol PlaceholderCollectionViewDelegate <NSObject>

@required
- (void)didPressedPlaceholderViewButton;

@end

IB_DESIGNABLE

@interface PlaceholderCollectionView : UICollectionView <DZNEmptyDataSetSource, DZNEmptyDataSetDelegate>

@property (nonatomic) IBInspectable UIImage     *placeholderImage;
@property (nonatomic) IBInspectable NSString    *title;
@property (nonatomic) IBInspectable NSString    *subtitle;
@property (nonatomic) IBInspectable BOOL        enableButton;
@property (nonatomic) IBInspectable NSString    *buttonTitle;
@property (nonatomic) IBInspectable float       headerSize;

@property (nonatomic, assign) BOOL isLoading;
@property (nonatomic, assign) id <PlaceholderCollectionViewDelegate> buttonDelegate;

- (void)showLoader;
- (void)hideLoader;

@end
