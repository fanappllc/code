//
//  Profile.swift
//  FanCustomer
//
//  Created by Codiant on 12/18/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
import ObjectMapper

@objcMembers
class Profile: NSObject, Mappable {
    var id: String!
    var dialCode: String!
    var phoneNumber: String!
    var firstName: String!
    var lastName: String!
    var email: String!
    var address: String!
    var zipcode: String!
    var photo: String!
    var mobileModel: String!
    private var keychainStorage: UICKeyChainStore!
    
    static let shared: Profile = {
        let instance = Profile()
        instance.keychainStorage = UICKeyChainStore(service: Bundle.main.bundleIdentifier!)
        return instance
    }()
    
    // Can't init singleton
    private override init() {}
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        id <- (map["id"], IntToStringTransform)
        dialCode    <- map["country_code"]
        phoneNumber <- map["number"]
        firstName   <- map["first_name"]
        lastName    <- map["last_name"]
        email       <- map["email"]
        photo       <- map["profile_image"]
        address     <- map["address"]
        zipcode     <- map["zip_code"]
        #if FANPHOTOGRAPHER
            mobileModel  <- map["mobile_model"]
        #endif
        
    }
    
    //  MARK:- Public methods
    func map(JSONObject: Any?, context: MapContext?) {
        _ = Mapper(context: context).map(JSONObject: JSONObject, toObject: self)
        syncUser(type: .toKeychain)
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
    }
    
    
    func syncUserInfo() {
        self.syncUser(type: .fromKeychain)
    }
    
    func syncUser(type: KeychainSyncType) {
        for children in Mirror(reflecting: self).children {
            guard let label = children.label,
                label != "keychainStorage",
                label != "onShift" else {
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
