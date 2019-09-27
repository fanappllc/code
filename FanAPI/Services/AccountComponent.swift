//
//  AccountComponent.swift
//  FanCustomer
//
//  Created by Codiant on 12/15/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

struct AccountComponent {
    
    static func requestCode(phoneNumber: String, country: String, dialCode: String, closure: @escaping (Bool, Data?, Error?)->Void) {
        var role = "phtographer"
        #if FANCUSTOMER
            role = "customer"
        #else
            role = "photographer"
        #endif
    
        let parameters = ["mobile": phoneNumber, "country_code": dialCode,  "role": role]
        NSSession.shared.requestWith(path: "send-otp", method: .post, parameters: parameters, retryCount: 2) { (success, data, error) in
        closure(success, data, error)
       }
    }
    
    static func verify(phoneNumber: String, dialCode: String, otp: String, device: String, deviceToken: String, closure: @escaping (Bool, Data?, Error?)->Void) {
        let parameters = ["mobile": phoneNumber, "otp": otp, "device_type": device, "device_id": deviceToken]
        
        NSSession.shared.requestWith(path: "otp-verify", method: .post, parameters: parameters, retryCount: 2) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func getProfile(closure: @escaping (Bool, Data?, Error?)->Void) {
        NSSession.shared.requestWith(path: "user", method: .get, parameters: nil, retryCount: 2) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func updateProfile(parameters: HTTPParameters?, images: [String: UIImage]?, closure: @escaping (Bool, Data?, Error?)->Void) {
        NSSession.shared.multipartRequestWith(path: "update-profile", method: .post, parameters: parameters, images: images, retryCount: 2) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func updateSSN(ssnNumber: String, closure: @escaping (Bool, Data?, Error?)->Void) {
        let parameters = ["ssn_no":ssnNumber]
        NSSession.shared.requestWith(path: "update-ssn", method: .post, parameters: parameters, retryCount: 2) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func getAccountStatus(closure: @escaping (Bool, Data?, Error?)->Void) {
        NSSession.shared.requestWith(path: "account-status", method: .get, parameters: nil, retryCount: 2) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func updateLicenseImage(images: [String: UIImage]?, closure: @escaping (Bool, Data?, Error?)->Void) {
        NSSession.shared.multipartRequestWith(path: "update-driving-licence-image", method: .post, parameters: nil, images: images, retryCount: 2) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func logout(_ closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "logout", method: .post, parameters: nil, retryCount: 2) { (success, data, error) in
            closure(success, data, error)
        }
    }
}
