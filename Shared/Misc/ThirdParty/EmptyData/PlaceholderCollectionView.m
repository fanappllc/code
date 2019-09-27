//
//  PlaceholderCollectionView.m
//  Instafresh
//
//  Created by Neuron_Mac_Mini 7 on 11/10/15.
//  Copyright © 2015 Neuron_Mac_Mini 7. All rights reserved.
//

#import "PlaceholderCollectionView.h"

@implementation PlaceholderCollectionView

- (void)awakeFromNib
{
    [super awakeFromNib];
    [self commonInit];
}

#pragma mark - Custom methods
- (void)commonInit {
    
    self.emptyDataSetDelegate   = self;
    self.emptyDataSetSource     = self;
}

#pragma mark - DZNEmptyDataSetSource Methods
- (NSAttributedString *)titleForEmptyDataSet:(UIScrollView *)scrollView
{
    NSString *text      = nil;
    UIFont *font        = nil;
    UIColor *textColor  = nil;
    
    NSMutableDictionary *attributes = [NSMutableDictionary new];
    
    text = self.title;
    font = [UIFont systemFontOfSize: 26.0];
    textColor = [UIColor grayColor];
    
    [attributes setObject:font forKey:NSFontAttributeName];
    [attributes setObject:textColor forKey:NSForegroundColorAttributeName];
    
    return [[NSAttributedString alloc] initWithString:text attributes:attributes];
}

- (UIView *)customViewForEmptyDataSet:(UIScrollView *)scrollView
{
    UIActivityIndicatorView *activityView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
    [activityView startAnimating];
    return self.isLoading ? activityView : nil;
}

- (NSAttributedString *)descriptionForEmptyDataSet:(UIScrollView *)scrollView
{
    NSString *text      = nil;
    UIFont *font        = nil;
    UIColor *textColor  = nil;
    
    NSMutableDictionary *attributes = [NSMutableDictionary new];
    
    NSMutableParagraphStyle *paragraph  = [NSMutableParagraphStyle new];
    paragraph.lineBreakMode             = NSLineBreakByWordWrapping;
    paragraph.alignment                 = NSTextAlignmentCenter;
    
    text = self.subtitle;
    font = [UIFont systemFontOfSize:16.0];
    textColor = [UIColor grayColor];
    paragraph.lineSpacing = 4.0;
    
    [attributes setObject:font forKey:NSFontAttributeName];
    [attributes setObject:textColor forKey:NSForegroundColorAttributeName];
    [attributes setObject:paragraph forKey:NSParagraphStyleAttributeName];
    
    NSMutableAttributedString *attributedString = [[NSMutableAttributedString alloc] initWithString:text attributes:attributes];
    return attributedString;
}

- (UIImage *)imageForEmptyDataSet:(UIScrollView *)scrollView {
    
    return self.placeholderImage;
}

- (UIColor *)backgroundColorForEmptyDataSet:(UIScrollView *)scrollView {
    
    return [UIColor whiteColor];
}

- (NSAttributedString *)buttonTitleForEmptyDataSet:(UIScrollView *)scrollView forState:(UIControlState)state
{
    if (self.enableButton) {
        
        UIFont *defaultFont         = [UIFont systemFontOfSize:18.0];
        UIColor *defaultTextColor   = [UIColor whiteColor];
        
        NSMutableDictionary *attributes = [NSMutableDictionary new];
        [attributes setObject:defaultFont forKey:NSFontAttributeName];
        [attributes setObject:defaultTextColor forKey:NSForegroundColorAttributeName];
        
        return [[NSAttributedString alloc] initWithString:self.buttonTitle ? self.buttonTitle : @"Button" attributes:attributes];
    }
    
    return nil;
}

- (UIImage *)buttonBackgroundImageForEmptyDataSet:(UIScrollView *)scrollView forState:(UIControlState)state
{
    if (self.enableButton) {
        
        UIEdgeInsets capInsets  = UIEdgeInsetsMake(22.0, 22.0, 22.0, 22.0);
        UIEdgeInsets rectInsets = UIEdgeInsetsMake(0.0, -20, 0.0, -20);;
        
        return [[[UIImage imageNamed:@"emptyTableButtonBack"] resizableImageWithCapInsets:capInsets resizingMode:UIImageResizingModeStretch] imageWithAlignmentRectInsets:rectInsets];
    }
    
    return nil;
}

- (CGFloat)spaceHeightForEmptyDataSet:(UIScrollView *)scrollView {
    
    return 20.0f;
}

- (CGFloat)verticalOffsetForEmptyDataSet:(UIScrollView *)scrollView
{
    if (self.headerSize != 0) {
        
        return self.headerSize / 2.0f;
    }
    
    return -self.headerSize / 2.0f;
}

#pragma mark - DZNEmptyDataSetDelegate Methods
- (BOOL)emptyDataSetShouldDisplay:(UIScrollView *)scrollView {
    
    return YES;
}

- (BOOL)emptyDataSetShouldAllowScroll:(UIScrollView *)scrollView {
    
    return YES;
}

- (void)emptyDataSet:(UIScrollView *)scrollView didTapButton:(UIButton *)button {
    
    if (self.buttonDelegate && [self.buttonDelegate respondsToSelector:@selector(didPressedPlaceholderViewButton)]) {
        
        [self.buttonDelegate didPressedPlaceholderViewButton];
    }
}

- (void)showLoader {
    self.isLoading = YES;
    [self reloadEmptyDataSet];
}

- (void)hideLoader {
    self.isLoading = NO;
    [self reloadEmptyDataSet];
}

@end
