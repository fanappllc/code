//
//  UIImageView+Decorative.swift
//  TaxiAppUK
//
//  Created by Codiant on 9/13/17.
//  Copyright © 2017 Codiant Software Technologies Pvt Ltd. All rights reserved.
//

import Foundation

// Extention to render image in a given color
extension UIImageView {
    
    @IBInspectable var imageColor: UIColor {
        get {
            return self.tintColor
        }
        set {
            
            if let givenImage = self.image {
                self.image = givenImage.withRenderingMode(.alwaysTemplate)
            }
            
            self.tintColor = newValue
        }
    }
}
