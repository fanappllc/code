//
//  SettingComponent.swift
//  FanCustomer
//
//  Created by Codiant on 1/3/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import UIKit

struct SettingComponent {
    static func getTerms(closure: @escaping (Bool, Data?, Error?)->Void) {
        var path = "photographer-terms-and-condition"
        #if FANCUSTOMER
            path = "customer-terms-and-condition"
        #endif
        
        NSSession.shared.requestWith(path: path, method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func getFAQ(closure: @escaping (Bool, Data?, Error?)->Void) {
        var path = "photographer-faq"
        #if FANCUSTOMER
            path = "customer-faq"
        #endif
        NSSession.shared.requestWith(path: path, method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func getContact(closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "settings", method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func getComplainReasons(closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "complain-header" , method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func addComplain(parameter: HTTPParameters, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "complain" , method: .post, parameters: parameter, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
}
