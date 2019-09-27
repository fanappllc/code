//
//  Card.swift
//  FanCustomer
//
//  Created by Codiant on 12/19/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import Foundation
import ObjectMapper

class Card: Mappable {
    
    var id: String!
    var card_id: String!
    var last4: String!
    var brand: String!
    var isDefault = false
    
    init() {}
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        id <- (map["id"], IntToStringTransform)
        last4 <- (map["last_4_digit"], IntToStringTransform)
        brand <- map["brand"]
        card_id <- map["card_id"]
        isDefault <- map["is_default"]
    }
    
    class func map(JSONObject: Any?, context: MapContext?) -> Card {
        return Mapper(context: context).map(JSONObject: JSONObject, toObject: Card())
    }
    
}
