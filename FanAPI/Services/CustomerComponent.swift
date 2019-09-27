//
//  CustomerComponent.swift
//  FanCustomer
//
//  Created by Urvashi Bhagat on 1/10/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import UIKit
import CoreLocation
struct CustomerComponent {
    
    static func getPhotographersWith(location: CLLocationCoordinate2D, closure: @escaping (Bool, Data?, Error?)->Void)  {
        
        let parameters = ["latitude": "\(location.latitude)", "longitude": "\(location.longitude)"]
        NSSession.shared.requestWith(path: "photographers", method: .post, parameters: parameters, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func getTimeSlots(closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "slots", method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func sendRequestToPhotographer(parameter: HTTPParameters, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "send-request", method: .post, parameters: parameter, retryCount: 1) { (success, data, error) in
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
    
    static func getPhotographerProfile(photographerID : String, orderID : String, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "photographer-profile/" + photographerID + "/" + orderID , method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func acceptRequestOperation(orderID : String, type : String, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "proceed-or-cancel-request/" + orderID + "/" + type , method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func canelRequest(orderID : String, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "cancel-photography-session/" + orderID , method: .delete, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func startRequest(parameter: HTTPParameters, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "start-time-request" , method: .post, parameters: parameter, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func rating(parameter: HTTPParameters, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "rating-user" , method: .post, parameters: parameter, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func endSession(orderID : String, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "end-sessoin/" + orderID , method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func getPhotos(closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "get-photos" , method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    static func renewTime(parameter: HTTPParameters, closure: @escaping(Bool, Data?, Error?)-> Void) {
        
        NSSession.shared.requestWith(path: "renew-start-time-request", method: .post, parameters: parameter, retryCount: 1) { (success, data, error) in
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
    
    
    static func checkDiscountStatus(slotPrice : String, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "discount/" + slotPrice , method: .get, parameters: nil, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func verifyPromoCode(parameter: HTTPParameters, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        NSSession.shared.requestWith(path: "use-promocode", method: .post, parameters: parameter, retryCount: 1) { (success, data, error) in
            closure(success, data, error)
        }
    }
}
