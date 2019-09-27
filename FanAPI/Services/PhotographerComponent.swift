//
//  PhotographerComponent.swift
//  FanCustomer
//
//  Created by Darshan Mothreja on 12/29/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
import CoreLocation

struct PhotographerComponent {
    
    static func getRegisrationFee(closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "photographer-registration-fee", method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func photographerAvailablity(isAvailable : Bool? = nil, closure: @escaping (Bool, Data?, Error?)->Void) {
        var parameters  = [String:String]()
        
        if let isTrue = isAvailable {
            parameters = ["is_available": isTrue ? "1" : "0"]
        } else {
            parameters = ["is_available":""]
        }
        NSSession.shared.requestWith(path: "change-user-availability", method: .post, parameters: parameters, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func getOrderRequest(closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "order-request-list", method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func orderRequestOperation(orderID : String, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "accept-request/" + orderID , method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func canelRequestWithReason(orderID : String, reasonID : String, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "cancel-session-by-photographer/" + orderID + "/" + reasonID , method: .delete, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func startTimeApproved(orderID : String, closure: @escaping (Bool, Data?, Error?)->Void)  {
        
        let parameters = ["order_id": orderID]
        NSSession.shared.requestWith(path: "start-time-approve", method: .post, parameters: parameters, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func getBookingHistory(page: String, closure: @escaping (Bool, Data?, Error?)->Void) {
        let parameters = ["page": page]
        NSSession.shared.requestWith(path: "my-orders", method: .get, parameters: parameters, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func getNotifictionList(page: String, closure: @escaping (Bool, Data?, Error?)->Void) {
        let parameters = ["page": page]
        NSSession.shared.requestWith(path: "notifications", method: .get, parameters: parameters, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func deleteNotifiction(notificationID : String, closure: @escaping (Bool, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "notification/" + notificationID, method: .delete, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, error)
        }
    }
    static func deleteAllNotifiction(closure: @escaping (Bool, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "delete-all-notification", method: .delete, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, error)
        }
    }
    static func uploadPhoto(parameter: HTTPParameters, images: [String: UIImage]?, closure: @escaping (Bool, Data?, Error?)-> Void) {
        
        NSSession.shared.multipartRequestWith(path: "upload-photo", method: .post, parameters: parameter, images: images, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func getMessageList(orderID : String, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "messages/" + orderID, method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success,data, error)
        }
    }
    static func sendMessage(parameter: HTTPParameters, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "save-message" , method: .post, parameters: parameter, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
}
