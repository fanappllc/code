//
//  APIComponents.swift
//  FanCustomer
//
//  Created by Codiant on 12/15/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

struct APIComponents {
    
    static let Account: AccountComponent.Type = {
        return AccountComponent.self
    }()
    
    static let Payment: PaymentComponent.Type = {
        return PaymentComponent.self
    }()
    
    static let Photographer: PhotographerComponent.Type = {
        return PhotographerComponent.self
    }()
    
    static let Setting: SettingComponent.Type = {
        return SettingComponent.self
    }()
    
    static let Location: LocationComponent.Type = {
        return LocationComponent.self
    }()
    
    static let Customer: CustomerComponent.Type = {
        return CustomerComponent.self
    }()
}
