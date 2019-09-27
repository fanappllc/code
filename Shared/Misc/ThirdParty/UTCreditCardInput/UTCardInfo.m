//
//  CardInfo.m
//  CreditCardUtils
//
//  Created by utk@rsh on 27/09/15.
//  Copyright © 2015 Codiant. All rights reserved.
//

#import "UTCardInfo.h"

@interface UTCardInfo ()

@property (nonatomic, strong) NSString              *companyName;
@property (nonatomic, strong) NSString              *shortName;
@property (nonatomic, assign) NSUInteger            cardType;
@property (nonatomic, strong) NSRegularExpression   *patternRegularExpression;
@property (nonatomic, strong) NSArray               *numberGrouping;
@property (nonatomic, strong) NSArray               *lengths;
@property (nonatomic) NSInteger                     maxLength;

@end

@implementation UTCardInfo

- (instancetype)initWithDictionary:(NSDictionary *)aDictionary
{
    self = [super init];
    
    if (self) {
        
        NSString *pattern               = aDictionary[@"pattern"];
        self.patternRegularExpression   = [[NSRegularExpression alloc] initWithPattern:pattern options:0 error:nil];
        self.companyName                = aDictionary[@"companyName"];
        self.shortName                  = aDictionary[@"shortName"];
        self.cardType                   = [aDictionary[@"type"] integerValue];
        self.numberGrouping             = [[self class] numberArrayWithCommaSeparatedString:aDictionary[@"numberGrouping"] maxValue:NULL];
        
        NSInteger maxLength;
        self.lengths    = [[self class] numberArrayWithCommaSeparatedString:aDictionary[@"length"] maxValue:&maxLength];
        self.maxLength  = maxLength;
    }
    return self;
}

+ (NSArray *)numberArrayWithCommaSeparatedString:(NSString *)aCommaSeparatedString maxValue:(NSInteger *)outMaxValue
{
    NSArray *components                 = [aCommaSeparatedString componentsSeparatedByString:@","];
    NSMutableArray *mutableResultArray  = [NSMutableArray arrayWithCapacity:components.count];
    
    __block NSInteger maxValue = 0;
    
    [components enumerateObjectsUsingBlock:^(NSString *component, NSUInteger idx, BOOL * _Nonnull stop) {
        
        NSInteger integer = component.integerValue;
        if (integer > 0) {
            [mutableResultArray addObject:@(integer)];
            maxValue = MAX(maxValue, integer);
        }
    }];
    
    if (outMaxValue)
        *outMaxValue = maxValue;
    
    return [NSArray arrayWithArray:mutableResultArray];
}

- (BOOL)patternMatchesWithNumberString:(NSString *)aNumberString
{
    NSUInteger numberOfMatches = [self.patternRegularExpression numberOfMatchesInString:aNumberString options:0 range:NSMakeRange(0, aNumberString.length)];
    return numberOfMatches > 0;
}

- (NSString *)groupedStringWithString:(NSString *)aString groupSeparater:(NSString *)aGroupSeparater maskingCharacter:(NSString *)aMaskingCharacter maskingGroupIndexSet:(NSIndexSet *)aMaskingGroupIndexSet
{
    if (aString.length == 0)
        return @"";
    
    if (aString.length > self.maxLength)
        aString = [aString substringWithRange:NSMakeRange(0, self.maxLength)];
    
    NSMutableString *mutableString = [NSMutableString stringWithCapacity:aString.length + self.numberGrouping.count - 1];
    
    NSInteger location = 0;
    
    for (NSInteger i = 0; i < self.numberGrouping.count; i++) {
        
        NSNumber *digitCountNumber  = self.numberGrouping[i];
        NSInteger digitCount        = digitCountNumber.integerValue;
        
        NSRange substringRange = NSMakeRange(location, MIN(digitCount, aString.length - location));
        
        if (aMaskingCharacter && [aMaskingGroupIndexSet containsIndex:i]) {
            
            NSString *maskString = [@"" stringByPaddingToLength:substringRange.length withString:aMaskingCharacter startingAtIndex:0];
            [mutableString appendString:maskString];
            
        } else {
            
            [mutableString appendString:[aString substringWithRange:substringRange]];
        }
        
        location += substringRange.length;
        
        if (substringRange.length < digitCount)
            break;
        
        if (i < self.numberGrouping.count - 1)
            [mutableString appendString:aGroupSeparater];
    }
    
    return [NSString stringWithString:mutableString];
}

- (NSString *)description {
    
    return [NSString stringWithFormat:@"companyName=%@, shortName=%@, pattern=%@, numberGrouping=%@, lengths=%@",
            self.companyName, self.shortName, self.patternRegularExpression, self.numberGrouping, self.lengths];
}

@end
