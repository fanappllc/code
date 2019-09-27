//
//  A.swift
//  TaxiAppUKCustomer
//
//  Created by Ashish Shah on 04/09/17.
//  Copyright © 2017 Codiant Software Technologies Pvt ltd. All rights reserved.
//

import Foundation
extension NSMutableAttributedString {
    
    func setFont(_ font : UIFont, color : UIColor, onString : String){
        if let range = self.string.range(of: onString) {
            let nsRange = self.string.nsRangeFromRange(range: range)
            self.addAttributes([NSAttributedStringKey(rawValue: kCTForegroundColorAttributeName as String as String): color, NSAttributedStringKey.font: font], range: nsRange)
        }
    }
}
