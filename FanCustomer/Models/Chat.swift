//
//  Chat.swift
//  FanCustomer
//
//  Created by Urvashi Bhagat on 1/30/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import Foundation
import ObjectMapper

class Chat: Mappable {
    
    var message: String!
    var createdDate: Date!
    var fromId: String!
    var opponent = false

    init() {}
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        
        self.message <- map["message"]
        self.createdDate <- (map["created_at"], CustomDateFormatTransform(formatString: "yyyy-MM-dd HH:mm:ss"))
        self.fromId <- (map["from_id"],IntToStringTransform)

    }
    
    class func map(JSONObject: Any?, context: MapContext?) -> Chat {
        return Mapper(context: context).map(JSONObject: JSONObject, toObject: Chat())
    }
}
