//
//  Custom+UITextField.swift
//  TaxiAppUKCustomer
//
//  Created by Urvashi Bhagat on 8/16/17.
//  Copyright © 2017 Codiant Software Technologies Pvt ltd. All rights reserved.
//

import Foundation

class CustomTextField: UITextField {
    
    override func textRect(forBounds bounds: CGRect) -> CGRect {
        return UIEdgeInsetsInsetRect(bounds, UIEdgeInsets(top: 0, left: 15, bottom: 0, right: 0))
    }
    
    override func editingRect(forBounds bounds: CGRect) -> CGRect {
        return UIEdgeInsetsInsetRect(bounds, UIEdgeInsets(top: 0, left: 15, bottom: 0, right: 0))
    }
    
}
