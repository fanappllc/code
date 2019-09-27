//
//  BookingHistory.swift
//  FanPhotographer
//
//  Created by Codiant on 1/12/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import UIKit
import ObjectMapper

enum OrderStatus : String {
    case Cancelled  = "cancelled"
    case Pending    = "pending"
    case Proceed    = "proceed"
    case Accepted   = "accepted"
    case Running    = "running"
    case Completed  = "completed"
}

class BookingHistory: Mappable {
    
    var f_name: String!
    var l_name: String!
    var profileUrl: String!
    var amount: String!
    var bookingId: String!
    var bookingDate: Date!
    var orderStatus: OrderStatus = .Completed
    
    init() {}
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        self.f_name <- map["first_name"]
        self.l_name <- map["last_name"]
        self.profileUrl <- map["profile_image"]
        self.amount <- (map["total_amount"],DoubleToStringTransform)
        self.bookingId <- (map["order_id"],IntToStringTransform)
        self.bookingDate <- (map["order_created_at"], CustomDateFormatTransform(formatString: "yyyy-MM-dd HH:mm:ss"))
        self.orderStatus <- map["order_status"]
    }
    
    class func map(JSONObject: Any?, context: MapContext?) -> BookingHistory {
        return Mapper(context: context).map(JSONObject: JSONObject, toObject: BookingHistory())
    }
}
