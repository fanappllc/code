//
//  AppConstant.swift
//  TaxiAppUKCustomer
//
//  Created by Urvashi Bhagat on 8/14/17.
//  Copyright © 2017 Codiant Software Technologies Pvt ltd. All rights reserved.
//

struct Screen {
    static let width = UIScreen.main.bounds.size.width
    static let height = UIScreen.main.bounds.size.height
    static let maxLength = max(width, height)
    static let minLength = min(width, height)
}

struct Device {
    static let iPhone6 = UIDevice.current.userInterfaceIdiom == .phone && Screen.maxLength == 667.0
    static let iPhone6p = UIDevice.current.userInterfaceIdiom == .phone && Screen.maxLength == 736.0
    static let iPhoneX = UIDevice.current.userInterfaceIdiom == .phone && Screen.maxLength == 812.0
    static func isLessThen6() -> Bool { return Screen.height < 667.0 }
}

struct Color {
    
    static var celrianBlue: UIColor {
        get { return UIColor(red: 79.0/255.0, green: 241.0/255.0, blue: 252.0/255.0, alpha: 1.0) }
    }
    
    static var ultraBlue: UIColor {
        get { return UIColor(red: 90.0/255.0, green: 200.0/255.0, blue: 249/255.0, alpha: 1.0) }
    }
    
    static var white: UIColor {
        get { return UIColor(red: 255.0/255.0, green: 255.0/255.0, blue: 255.0/255.0, alpha: 1.0) }
    }
    
    static var orange: UIColor {
        get { return UIColor(red: 253.0/255.0, green: 103.0/255.0, blue: 0.0/255.0, alpha: 1.0) }
    }
    static var lightOrange: UIColor {
        get { return UIColor(red: 254.0/255.0, green: 141.0/255.0, blue: 1.0/255.0, alpha: 1.0) }
    }
    static var green: UIColor {
        get { return UIColor(red: 25.0/255.0, green: 222.0/255.0, blue: 177.0/255.0, alpha: 1.0) }
    }
    static var gray: UIColor {
        get { return UIColor(red: 132.0/255.0, green: 132.0/255.0, blue: 132.0/255.0, alpha: 1.0) }
    }
    static var purple: UIColor {
        get { return UIColor(red: 152.0/255.0, green: 167.0/255.0, blue: 252/255.0, alpha: 1.0) }
    }
}
