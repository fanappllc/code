//
//  LocationComponent.swift
//  FanCustomer
//
//  Created by Codiant on 1/9/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import UIKit
import CoreLocation

struct LocationComponent {
    static func updateLocation(location: CLLocation, closure: @escaping (Bool, Data?, Error?)->Void)  {
        
        let parameters = ["latitude": "\(location.coordinate.latitude)", "longitude": "\(location.coordinate.longitude)"]
        
        NSSession.shared.requestWith(path: "update-location", method: .post, parameters: parameters, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
}
