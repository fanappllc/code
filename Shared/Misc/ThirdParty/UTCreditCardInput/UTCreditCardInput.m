//
//  UTCreditCardInput.m
//  CreditCardUtils
//
//  Created by utk@rsh on 27/09/15.
//  Copyright © 2015 Codiant. All rights reserved.
//

#import "UTCreditCardInput.h"

@interface UTCreditCardInput ()

@property (strong, nonatomic) UITextField *txfCreditCardNumber;
@property (strong, nonatomic) UITextField *txfValidThruDate;
@property (strong, nonatomic) UITextField *txfCVV;
@property (nonatomic, strong) UIImageView *cardImageView;

@property (nonatomic, strong) NSCharacterSet        *numberCharacterSet;
@property (nonatomic, strong) NSRegularExpression   *nonNumericRegularExpression;
@property (nonatomic, strong) NSSet                 *cardPatterns;
@property (nonatomic, strong) NSString              *cachedPrefix;
@property (nonatomic, strong) UTCardInfo            *cardPatternInfo;

@end

@implementation UTCreditCardInput

#pragma mark - 
#pragma mark Init
- (void)initWithCardNumberField:(UITextField *)numberField expDateField:(UITextField *)dateField cvvField:(UITextField *)cvvField cardLogo:(UIImageView *)logoCard {
    
    [numberField addTarget:self
                    action:@selector(didChangeCreditCardNumber:)
          forControlEvents:UIControlEventEditingChanged];
    
    [dateField addTarget:self
                  action:@selector(didChangeValidThruDate:)
        forControlEvents:UIControlEventEditingChanged];
    
    [cvvField addTarget:self
                 action:@selector(didChangeCVV:)
       forControlEvents:UIControlEventEditingChanged];
    
    [numberField setDelegate:self];
    [dateField setDelegate:self];
    [cvvField setDelegate:self];
    
    self.txfCreditCardNumber    = numberField;
    self.txfValidThruDate       = dateField;
    self.txfCVV                 = cvvField;
    self.cardImageView          = logoCard;
    
    self.numberCharacterSet             = [self numberCharacterSet];
    self.nonNumericRegularExpression    = [self nonNumericRegularExpression];
    
    self.cardPatternInfo = nil;
    
    [self prepareCreditCardPatterns];
    [self updateCardLogoImage];
    
    //[numberField becomeFirstResponder];
}

#pragma mark Notification Listner
//  Card Number Field.
- (void)didChangeCreditCardNumber:(UITextField *)textField {
    
}

//  Valid thru date Field.
- (void)didChangeValidThruDate:(UITextField *)textField {
    
}

//  CVV Field.
- (void)didChangeCVV:(UITextField *)textField {
    
}

#pragma mark UITextField delegates
-(BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    if ([textField isEqual:self.txfCreditCardNumber]) {
        
        NSString *currentText = textField.text;
        
        if ([string isEqualToString:@""]) {
            
            [self.txfValidThruDate setText:@""];
            self.dateComponents = nil;
            [self.txfCVV setText:@""];
        }
        
        NSCharacterSet *nonNumberCharacterSet = [self.numberCharacterSet invertedSet];
        
        if (string.length == 0 && [[currentText substringWithRange:range] stringByTrimmingCharactersInSet:nonNumberCharacterSet].length == 0) {
            
            NSRange numberCharacterRange = [currentText rangeOfCharacterFromSet:self.numberCharacterSet
                                                                        options:NSBackwardsSearch
                                                                          range:NSMakeRange(0, range.location)];
            
            if (numberCharacterRange.location != NSNotFound)
                range = NSUnionRange(range, numberCharacterRange);
        }
        
        NSString *newString = [currentText stringByReplacingCharactersInRange:range withString:string];
        textField.text      = [self formattedStringFromRawString:newString];
        
        [self updateCardLogoImage];
        return NO;
    }
    else if ([textField isEqual:self.txfValidThruDate]) {
        
        if (!self.cardPatternInfo) {
            //[Utils showAlertMessage:@"First enter your credit card number" withTitle:@""];
            [self showAlert:@"Please first enter your credit card number"];
            return NO;
        }
        
        NSString *currentText = textField.text;
        NSCharacterSet *nonNumberCharacterSet = [self.numberCharacterSet invertedSet];
        
        if (string.length == 0 && [[currentText substringWithRange:range] stringByTrimmingCharactersInSet:nonNumberCharacterSet].length == 0) {
            
            NSRange numberCharacterRange = [currentText rangeOfCharacterFromSet:self.numberCharacterSet
                                                                        options:NSBackwardsSearch
                                                                          range:NSMakeRange(0, range.location)];
            
            if (numberCharacterRange.location != NSNotFound) {
                range = NSUnionRange(range, numberCharacterRange);
            }
        }
        
        NSString *replacedString    = [currentText stringByReplacingCharactersInRange:range withString:string];
        NSString *numberOnlyString  = [self numberOnlyStringWithString:replacedString];
        
        if (numberOnlyString.length > 4) {
            return NO;
        }
        
        if (numberOnlyString.length == 1 && [numberOnlyString substringToIndex:1].integerValue > 1) {
            numberOnlyString = [@"0" stringByAppendingString:numberOnlyString];
        }
        
        NSMutableString *formattedString = [NSMutableString string];
        
        if (numberOnlyString.length > 0) {
            
            NSString *monthString = [numberOnlyString substringToIndex:MIN(2, numberOnlyString.length)];
            
            if (monthString.length == 2) {
                NSInteger monthInteger = monthString.integerValue;
                if (monthInteger < 1 || monthInteger > 12) {
                    return NO;
                }
            }
            [formattedString appendString:monthString];
        }
        
        if (numberOnlyString.length > 1) {
            [formattedString appendString:@" / "];
        }
        
        if (numberOnlyString.length > 2) {
            NSString *yearString = [numberOnlyString substringFromIndex:2];
            [formattedString appendString:yearString];
        }
        
        [self.txfValidThruDate setText:formattedString];
        [textField sendActionsForControlEvents:UIControlEventEditingChanged];

        return NO;
    }
    else if ([textField isEqual:self.txfCVV]) {
        
        if (!self.dateComponents.month && !self.dateComponents.year) {
            //[Utils showAlertMessage:@"First enter above info correctly" withTitle:@""];
            [self showAlert:@"Please first enter correct credit card number or expiry date"];
            return NO;
        }
        
        int validLength = 0;
        
        validLength = self.cardPatternInfo.cardType == AmericanExpress ? 4 : 3;
        
        if(range.length + range.location > textField.text.length)
            return NO;
        
        NSUInteger newLength = [textField.text length] + [string length] - range.length;
        return newLength <= validLength;
    }
    
    return YES;
}

#pragma mark -
#pragma mark Utils
- (void)prepareCreditCardPatterns {
    
    NSString *filePath              = [[NSBundle bundleForClass:[self class]] pathForResource:@"UTCardInput.bundle/CardPatterns" ofType:@"plist"];
    NSArray *array                  = [NSArray arrayWithContentsOfFile:filePath];
    NSMutableArray *mutableArray    = [NSMutableArray arrayWithCapacity:array.count];
    
    [array enumerateObjectsUsingBlock:^(NSDictionary *dictionary, NSUInteger idx, BOOL * _Nonnull stop) {
        
        UTCardInfo *pattern = [[UTCardInfo alloc] initWithDictionary:dictionary];
        if (pattern)
            [mutableArray addObject:pattern];
    }];
    
    self.cardPatterns = [NSSet setWithArray:mutableArray];
}

- (NSCharacterSet *)numberCharacterSet {
    
    return [NSCharacterSet characterSetWithCharactersInString:@"0123456789"];
}

- (NSRegularExpression *)nonNumericRegularExpression {
    
    return [NSRegularExpression regularExpressionWithPattern:@"[^0-9]+" options:0 error:nil];
}

- (NSString *)formattedStringFromRawString:(NSString *)rawString {
    
    NSString *numberString = [self.nonNumericRegularExpression stringByReplacingMatchesInString:rawString
                                                                                        options:0
                                                                                          range:NSMakeRange(0, [rawString length])
                                                                                   withTemplate:@""];
    
    UTCardInfo *patternInfo = [self cardPatternInfoWithNumberString:numberString];
    
    if (patternInfo) {
        return [patternInfo groupedStringWithString:numberString groupSeparater:self.groupSeparater maskingCharacter:self.maskingCharacter maskingGroupIndexSet:self.maskingGroupIndexSet];
    } else {
        return numberString;
    }

}

- (UTCardInfo *)cardPatternInfoWithNumberString:(NSString *)aNumberString {
    
    if (self.cachedPrefix && [aNumberString hasPrefix:self.cachedPrefix] && self.cardPatternInfo)
        return self.cardPatternInfo;
    
    for (UTCardInfo *patternInfo in self.cardPatterns) {
        
        if ([patternInfo patternMatchesWithNumberString:aNumberString]) {
            
            self.cardPatternInfo    = patternInfo;
            self.cachedPrefix       = aNumberString;
            return patternInfo;
        }
    }
    
    self.cachedPrefix       = nil;
    self.cardPatternInfo    = nil;
    
    return nil;
}

- (void)updateCardLogoImage {
    
    if (!self.showsCardLogo)
        return;
    
    UTCardInfo *patternInfo     = self.cardPatternInfo;
    self.cardImageView.image    = [self cardLogoImageWithShortName:patternInfo.companyName];
}

- (UIImage *)cardLogoImageWithShortName:(NSString *)shortName {
    
    return shortName ? [UIImage imageNamed:[NSString stringWithFormat:@"UTCardInput.bundle/CardLogo/%@@2x", shortName]] : [UIImage imageNamed:@"UTCardInput.bundle/CardLogo/Default@2x"];
}

- (NSString *)cardNumber {
    
    return [self rawStringFromFormattedString:self.txfCreditCardNumber.text];
}

- (NSString *)cardCompanyName {
    
    return self.cardPatternInfo.companyName;
}

- (BOOL)getObjectValue:(out __autoreleasing id *)obj forString:(NSString *)string errorDescription:(out NSString *__autoreleasing *)error {
    
    if (obj) {
        *obj = [self.nonNumericRegularExpression stringByReplacingMatchesInString:string options:0 range:NSMakeRange(0, string.length) withTemplate:@""];
    }
    
    return YES;
}

- (NSString *)rawStringFromFormattedString:(NSString *)string {
    
    NSString *result = nil;
    NSString *errorDescription = nil;
    if ([self getObjectValue:&result forString:string errorDescription:&errorDescription]) {
        return result;
    } else {
        return nil;
    }
}

- (NSString *)numberOnlyStringWithString:(NSString *)string {
    
    return [self.nonNumericRegularExpression stringByReplacingMatchesInString:string
                                                                      options:0
                                                                        range:NSMakeRange(0, string.length)
                                                                 withTemplate:@""];
}

- (NSDateComponents *)dateComponents {
    
    return [self dateComponentsWithString:self.txfValidThruDate.text];
}

- (NSDateComponents *)dateComponentsWithString:(NSString *)string {
    
    NSString *numberString = [self numberOnlyStringWithString:string];
    
    NSDateComponents *result = [[NSDateComponents alloc] init];
    result.year = 0;
    result.month = 0;
    
    if (numberString.length > 1) {
        result.month = [[numberString substringToIndex:2] integerValue];
    }
    
    if (numberString.length > 3) {
        NSInteger currentYear = [self currentYear];
        result.year = [[numberString substringFromIndex:2] integerValue] + (currentYear / 100 * 100);
    }
    
    return result;
}

- (NSInteger)currentYear {
    
    NSDateComponents *currentDateComponents = [[NSCalendar currentCalendar] components:NSCalendarUnitYear fromDate:[NSDate date]];
    return currentDateComponents.year;
}

- (void)showAlert:(NSString *)message {
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"FAN" message:message preferredStyle:UIAlertControllerStyleAlert];
    [alertController addAction:[UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleCancel handler:nil]];
    [[UIApplication sharedApplication].keyWindow.rootViewController presentViewController:alertController animated:true completion:nil];
}

#pragma mark -
#pragma mark Validation Algo
- (BOOL)isValid {
    
    return (self.cardNumber.length == [self lengthForCardType]) && [self isValidLuhn];
}

- (BOOL)isValidLuhn
{
    BOOL odd = true;
    int sum = 0;
    NSMutableArray *digits = [NSMutableArray arrayWithCapacity:self.cardNumber.length];
    
    for (int i = 0; i < self.cardNumber.length; i++) {
        [digits addObject:[self.cardNumber substringWithRange:NSMakeRange(i, 1)]];
    }
    
    for (NSString *digitStr in [digits reverseObjectEnumerator]) {
        int digit = [digitStr intValue];
        if ((odd = !odd)) digit *= 2;
        if (digit > 9) digit -= 9;
        sum += digit;
    }
    
    return sum % 10 == 0;
}

- (NSInteger)lengthForCardType
{
    NSInteger length;
    
    if (self.cardPatternInfo.cardType == AmericanExpress) {
        length = 15;
    }
    else if (self.cardPatternInfo.cardType == DinersClub) {
        length = 14;
    }
    else {
        length = 16;
    }
    return length;
}

@end

#pragma mark - Constructor
UTCreditCardInput *creditCardInput(void)
{
    static id sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[UTCreditCardInput alloc] init];
    });
    return sharedInstance;
}
