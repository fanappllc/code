//
//  A.swift
//  TaxiAppUKCustomer
//
//  Created by Ashish Shah on 04/09/17.
//  Copyright © 2017 Codiant Software Technologies Pvt ltd. All rights reserved.
//

import Foundation

extension UIFont{
    
    enum FontSize {
        case short
        case small
        case medium
        case large
        case mediumLarge
        case extraLarge
        case custom(CGFloat)
        
        func value() -> CGFloat {
            switch self {
            case .short:
                return 10
            case .small:
                return 12
            case .medium:
                return 14
            case .large:
                return 16
            case .mediumLarge:
                return 18
            case .extraLarge:
                return (Device.iPhone6 || Device.iPhone6p) ? 32 : 29
            case .custom(let custom):
                return custom
            }
        }
    }
    
    enum StyleAttribute : String {
        case regular  = "Regular"
        case medium   = "Medium"
    }
    
    class func josefinSans(_ style : StyleAttribute, size : FontSize) -> UIFont {
        
        if let font = UIFont(name:"JosefinSans-\(style.rawValue)", size: size.value()) {
            return font
        }
        return UIFont(name:"JosefinSans", size: size.value())!
    }
    
    class func quicksand(_ style : StyleAttribute, size : FontSize) -> UIFont {
        if let font = UIFont(name:"Quicksand-\(style.rawValue)", size: size.value()) {
            return font
        }
        return UIFont(name:"Quicksand-Regular", size: size.value())!
    }
}
