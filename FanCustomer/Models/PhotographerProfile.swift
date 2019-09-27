//
//  PhotographerProfile.swift
//  FanCustomer
//
//  Created by Urvashi Bhagat on 1/11/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import Foundation
import ObjectMapper

class PhotographerProfile: Mappable {
    
    var orderId: String!
    var photographerId: String!
    var fname: String!
    var lname: String!
    var profileUrl: String!
    var rating: String!
    var photographerLatitude: Double!
    var photographerLongitude: Double!
    var customerLatitude: Double!
    var customerLongitude: Double!
    var arrivingTime: String!
    var cancelCharge: String!
    var date: Date!
    var model: String!
    var slotTime: String!
    var price: String!
    var lastdigit: String!
    
    init() {}
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        
        self.orderId <- (map["order_id"],IntToStringTransform)
        self.photographerId <- (map["photographer_id"],IntToStringTransform)
        self.fname <- map["first_name"]
        self.lname <- map["last_name"]
        self.profileUrl <- map["profile_image"]
        self.photographerLatitude <- map["photographer_latitude"]
        self.photographerLongitude <- map["photographer_longitude"]
        self.date <- (map["created_at"], CustomDateFormatTransform(formatString: "yyyy-MM-dd HH:mm:ss"))
        self.customerLatitude <- map["customer_latitude"]
        self.customerLongitude <- map["customer_longitude"]
        self.arrivingTime <- map["arriving_time"]
        self.cancelCharge <- map["order_cancel_charge"]
        self.model <- map["mobile_model"]
        self.slotTime <- (map["slot_time"],IntToStringTransform)
        self.rating <- map["rating_avg"]
        self.price <- (map["price"],DoubleToStringTransform)
        self.lastdigit <- map["last_4_digit"]
    }
    
    class func map(JSONObject: Any?, context: MapContext?) -> PhotographerProfile {
        return Mapper(context: context).map(JSONObject: JSONObject, toObject: PhotographerProfile())
    }
    
    class func mapString(JSONObject: String, context: MapContext?) -> PhotographerProfile {
        return Mapper(context: context).map(JSONString: JSONObject, toObject: PhotographerProfile())
    }
    
    class func resetCurrentOrder() {
        UserDefaults.standard.removeObject(forKey: "CurrentOrder")
    }
    
    static var currentOrder: PhotographerProfile {
        get {
            guard UserDefaults.standard.value(forKey: "CurrentOrder") != nil  else {
                return PhotographerProfile()
            }
            let decoded  = UserDefaults.standard.object(forKey: "CurrentOrder") as! String
            return self.mapString(JSONObject: decoded, context: nil)
        }
        set {
            UserDefaults.standard.set(newValue.toJSONString(), forKey: "CurrentOrder")
        }
    }
}
