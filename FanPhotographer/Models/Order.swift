//
//  Order.swift
//  FanPhotographer
//
//  Created by Codiant on 1/12/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import UIKit
import ObjectMapper

class Order: Mappable {
    var f_name: String!
    var l_name: String!
    var id: String!
    var amount: String!
    var duration: String!
    var address: String!
    var lat: Double!
    var long: Double!
    var pgLat: Double!
    var pgLong: Double!
    var profileUrl: String!
    var arrivingTime: String!
    var slotId: String!
    var userId: String!

    init() {}
    required init?(map: Map) {}
    func mapping(map: Map) {
        self.f_name <- map["get_customer.first_name"]
        self.l_name <- map["get_customer.last_name"]
        self.id <- (map["id"], IntToStringTransform)
        self.amount <- (map["order_slot.price"], IntToStringTransform)
        self.duration <- (map["order_slot.slot"], IntToStringTransform)
        self.address <- map["location"]
        self.lat <- map["latitude"]
        self.long <- map["longitude"]
        self.pgLat <- map["photographer_latitude"]
        self.pgLong <- map["photographer_longitude"]
        self.profileUrl <- map["get_customer.profile_img"]
        self.arrivingTime <- map["arriving_time"]
        self.slotId <- (map["order_slot.id"], IntToStringTransform)
        self.userId <- (map["get_customer.id"], IntToStringTransform)

    }
    
    class func map(JSONObject: Any?, context: MapContext?) -> Order {
        return Mapper(context: context).map(JSONObject: JSONObject, toObject: Order())
    }
    class func mapString(JSONObject: String, context: MapContext?) -> Order {
        return Mapper(context: context).map(JSONString: JSONObject, toObject: Order())
    }
    
    class func resetCurrentOrder() {
        UserDefaults.standard.removeObject(forKey: "CurrentOrder")
    }
    
    static var currentOrder: Order {
        get {
            guard UserDefaults.standard.value(forKey: "CurrentOrder") != nil  else {
                return Order()
            }
            let decoded  = UserDefaults.standard.object(forKey: "CurrentOrder") as! String
            return self.mapString(JSONObject: decoded, context: nil)
        }
        set {
            UserDefaults.standard.set(newValue.toJSONString(), forKey: "CurrentOrder")
        }
    }
}
