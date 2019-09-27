//
//  LoggedInUser.swift
//  FanCustomer
//
//  Created by Codiant on 11/15/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
import ObjectMapper

enum KeychainSyncType {
    case fromKeychain, toKeychain
}

@objcMembers
class LoggedInUser: NSObject, Mappable {
    var accessToken: String!
    var dialCode: String!
    var phoneNumber: String!
    var accountStatus: String!
     
    private var keychainStorage: UICKeyChainStore!
    
    static var getUnreadCount: String {
        get {
            guard let deviceId = UserDefaults.standard.value(forKey: "unread_notification_count") else { return "0" }
            return deviceId as! String
        }
        set {
            UserDefaults.standard.set(newValue, forKey: "unread_notification_count")
        }
    }
    
    
    static var deviceToken: String {
        get {
            guard let deviceId = UserDefaults.standard.value(forKey: "device_id") else { return "" }
            return deviceId as! String
        }
        set {
            UserDefaults.standard.set(newValue, forKey: "device_id")
        }
    }
    
    static let shared: LoggedInUser = {
        let instance = LoggedInUser()
        instance.keychainStorage = UICKeyChainStore(service: Bundle.main.bundleIdentifier!)
        return instance
    }()
    
    // Can't init singleton
    private override init() {}
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        accessToken <- map["access-token"]
        UserDefaults.standard.set(accessToken, forKey: "AccessToken")
        dialCode    <- map["country_code"]
        phoneNumber <- map["mobile"]
        accountStatus   <- (map["is_user_registered"])
    }
    
    //  MARK:- Public methods
    func map(JSONObject: Any?, context: MapContext?) {
        _ = Mapper(context: context).map(JSONObject: JSONObject, toObject: self)
        syncUser(type: .toKeychain)
        UserDefaults.standard.set(true, forKey: "UserLoggedIn")
    }
    var isRegistered: Bool {
        return accountStatus == "1" ? true : false
    }
    
    static var PhotographerAvailable: Bool {
        get {
            guard UserDefaults.standard.value(forKey: "pgAvailable") != nil  else {
                return true
            }
            return UserDefaults.standard.value(forKey: "pgAvailable") as! Bool
        }
        set {
            UserDefaults.standard.set(newValue, forKey: "pgAvailable")
        }
    }
    
    func checkLastUserSession() -> Bool {
        let sessionAvailable = UserDefaults.standard.bool(forKey: "UserLoggedIn")
        if sessionAvailable {
            self.syncUser(type: .fromKeychain)
        }
        return sessionAvailable
    }
    
    func clear() {
        for children in Mirror(reflecting: self).children {
            guard let label = children.label,
                label != "keychainStorage" else {
                    continue
            }
            self.setValue("", forKey: label)
        }
        self.keychainStorage.removeAllItems()
         #if FANPHOTOGRAPHER
            UserDefaults.standard.set("", forKey: "VCIdentifier")
            UserDefaults.standard.removeObject(forKey: "pgAvailable")
         #endif
        
        UserDefaults.standard.set(false, forKey: "UserLoggedIn")
        UserDefaults.standard.removeObject(forKey: "unread_notification_count")
    }
    
    func syncUser(type: KeychainSyncType) {
        for children in Mirror(reflecting: self).children {
            guard let label = children.label,
                label != "keychainStorage" else {
                    continue
            }
            if type == .toKeychain {
                if let keychainValue = children.value as? String {
                    self.keychainStorage.setString(keychainValue, forKey: label)
                }
            }
            else {
                if let keychainValue = self.keychainStorage.string(forKey: label) {
                    self.setValue(keychainValue, forKey: label)
                }
            }
        }
    }
}
