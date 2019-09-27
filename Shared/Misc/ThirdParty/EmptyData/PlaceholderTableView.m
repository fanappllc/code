//
//  PlaceholderTableView.m
//  Instafresh
//
//  Created by Codiant on 9/30/15.
//  Copyright © 2015 Neuron_Mac_Mini 7. All rights reserved.
//

#import "PlaceholderTableView.h"

@implementation PlaceholderTableView

- (void)awakeFromNib
{
    [super awakeFromNib];
    [self commonInit];
}

#pragma mark - Custom methods 
- (void)commonInit {
    
    self.emptyDataSetDelegate   = self;
    self.emptyDataSetSource     = self;
    self.tableFooterView        = [UIView new];
}

#pragma mark - DZNEmptyDataSetSource Methods
- (NSAttributedString *)titleForEmptyDataSet:(UIScrollView *)scrollView
{
    NSString *text      = nil;
    UIFont *font        = nil;
    UIColor *textColor  = nil;
    
    NSMutableDictionary *attributes = [NSMutableDictionary new];
    
    text = self.title;
    font = [UIFont fontWithName:@"Quicksand-Regular" size:22.0];;
    textColor = [UIColor colorWithRed:101.0/255.0 green:115.0/255.0 blue:126.0/255.0 alpha:1.0];
    [attributes setObject:font forKey:NSFontAttributeName];
    [attributes setObject:textColor forKey:NSForegroundColorAttributeName];
    
    return [[NSAttributedString alloc] initWithString:text attributes:attributes];
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
    font = [UIFont fontWithName:@"Quicksand-Regular" size:16.0];
    textColor = [UIColor colorWithRed:192.0/255.0 green:197.0/255.0 blue:206.0/255.0 alpha:1.0];
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
        UIFont *defaultFont = [UIFont systemFontOfSize:16];
        //UIFont *defaultFont         = [UIFont fontWithName:@"JosefinSans-Regular" size:18.0];
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
    if (self.tableHeaderView) {
        
        return self.tableHeaderView.frame.size.height/2.0f;
    }
    
    return -self.tableHeaderView.frame.size.height/2.0f;
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

@end
