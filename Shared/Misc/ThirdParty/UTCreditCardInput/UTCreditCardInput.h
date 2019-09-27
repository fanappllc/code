//
//  UTCreditCardInput.h
//  CreditCardUtils
//
//  Created by utk@rsh on 27/09/15.
//  Copyright © 2015 Codiant. All rights reserved.
//

@import UIKit;

#import "UTCardInfo.h"

@interface UTCreditCardInput : NSObject<UITextFieldDelegate>

/**
 * A Boolean indicating whether shows card logo left side or not.
 */
@property (nonatomic) BOOL showsCardLogo;

/**
 * The card number without blank space. (e.g., 1234123412341234)
 * Use this property to set or get card number instead of text property.
 */
@property (nonatomic, strong) NSString *cardNumber;

/**
 * The card company name. (e.g., Visa, Master, ...)
 */
@property (nonatomic, readonly) NSString *cardCompanyName;

/**
 * The card pattern info that is used last time.
 */
@property (nonatomic, strong, readonly) UTCardInfo *cardPatternInfo;

/**
 * The masking character. If this property is nil, entire card number will be shown.
 */
@property (nonatomic, strong) NSString *maskingCharacter;

/**
 * Card number group indexes to mask.
 * For example you can mask second, third and fourth group by setting [NSIndexSet indexSetWithIndexesInRange:NSMakeRange(1, 3)] to this property.
 * As a result, '1234 1234 1234 1234' will be '1234 **** **** ****'.
 */
@property (nonatomic, strong) NSIndexSet *maskingGroupIndexSet;

/**
 * Card number group separater. By default ' '(one space character) will be used.
 * For example, if you set '-', '1234-1234-1234-1234' will be returned.
 */
@property (nonatomic, strong) NSString *groupSeparater;

/**
 * Date component of Credit card Expiry Date.
 */
@property (nonatomic, strong) NSDateComponents *dateComponents;


- (void)initWithCardNumberField:(UITextField *)numberField expDateField:(UITextField *)dateField cvvField:(UITextField *)cvvField cardLogo:(UIImageView *)logoCard;

- (BOOL)isValid;

@end
UTCreditCardInput *creditCardInput(void);
