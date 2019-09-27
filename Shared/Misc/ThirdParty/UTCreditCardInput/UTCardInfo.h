//
//  CardInfo.h
//  CreditCardUtils
//
//  Created by utk@rsh on 27/09/15.
//  Copyright © 2015 Codiant. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, CreditCardType) {
    DinersClub,
    JCB,
    AmericanExpress,
    LaserCard,
    Visa,
    UnionPay,
    MasterCard,
    Maestro,
    Discover
};

@interface UTCardInfo : NSObject

/**
 * The card company name. (e.g., Visa, Master, ...)
 */
@property (nonatomic, strong, readonly) NSString *companyName;

/**
 * Short card company name. (e.g., visa, master, ...)
 */
@property (nonatomic, strong, readonly) NSString *shortName;


@property (nonatomic, assign, readonly) NSUInteger cardType;

/**
 * Initialize card pattern info with dictionary object in CardPatterns.plist
 */
- (instancetype)initWithDictionary:(NSDictionary *)aDictionary;

/**
 * Check whether number string matches credit card number pattern.
 */
- (BOOL)patternMatchesWithNumberString:(NSString *)aNumberString;

/**
 * Returns formatted card number string. (e.g., 1234 1234 1234 1234)
 */
- (NSString *)groupedStringWithString:(NSString *)aString
                       groupSeparater:(NSString *)aGroupSeparater
                     maskingCharacter:(NSString *)aMaskingCharacter
                 maskingGroupIndexSet:(NSIndexSet *)aMaskingGroupIndexSet;

@end
