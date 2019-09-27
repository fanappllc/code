//
//  Layout.swift
//  TaxiAppUKCustomer
//
//  Created by Codiant on 7/10/17.
//  Copyright © 2017 Codiant Software Technologies Pvt Ltd. All rights reserved.
//

import Foundation

extension UIView {
    
    typealias Attributes = (firstAttribute: NSLayoutAttribute, constant: CGFloat, toItem: Any?, toAttribute: NSLayoutAttribute)
    
    func constructConstraints(attributes: [Attributes], setActive: Bool = false) -> [NSLayoutConstraint] {
        
        guard attributes.count > 0 else {
            return []
        }
        
        var constraints = [NSLayoutConstraint]()
        
        for (firstAttribute, constant, toItem, toAttribute) in attributes {
            
            let constraint = NSLayoutConstraint(item: self, attribute: firstAttribute, relatedBy: NSLayoutRelation.equal, toItem: toItem, attribute: toAttribute, multiplier: 1.0, constant: constant)
            constraint.isActive = setActive
            
            constraints.append(constraint)
        }
        
        return constraints
    }
}
