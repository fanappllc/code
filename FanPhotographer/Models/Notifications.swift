//
//  Notifications.swift
//  FanPhotographer
//
//  Created by Codiant on 1/12/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import UIKit
import ObjectMapper

class Notifications: Mappable {
    var id: String!
    var message: String!
    var messageDate: Date!
    init() {}
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        self.id <- (map["id"],IntToStringTransform)
        self.message <- map["message"]
        self.messageDate <- (map["created_at"], CustomDateFormatTransform(formatString: "yyyy-MM-dd HH:mm:ss"))
    }
    
    class func map(JSONObject: Any?, context: MapContext?) -> Notifications {
        return Mapper(context: context).map(JSONObject: JSONObject, toObject: Notifications())
    }
}
