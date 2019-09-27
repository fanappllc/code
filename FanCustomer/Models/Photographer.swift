//
//  Photographer.swift
//  FanCustomer
//
//  Created by Urvashi Bhagat on 1/17/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import Foundation
import ObjectMapper

class Photographer: Mappable {
    
    var orderId: String!
    var photographerName: String!
    var photos =  [Photos]()
    var createdAt: Date!
    
    init() {}
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        
        self.orderId <- (map["order_id"],IntToStringTransform)
        self.photographerName <- map["photographer_name"]
        self.photos <- map["photos"]
        self.createdAt <- (map["created"], CustomDateFormatTransform(formatString: "yyyy-MM-dd HH:mm:ss"))

    }
    
    class func map(JSONObject: Any?, context: MapContext?) -> Photographer {
        return Mapper(context: context).map(JSONObject: JSONObject, toObject: Photographer())
    }
}

class Photos: Mappable {
    
    var imageURLString: String!
    var imageURL: String!
    var isSelected: Bool!
    
    init() {}
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        
        self.imageURLString <- map["thumb_picture"]
        self.imageURL  <- map["picture"]
        self.isSelected =  false
        
    }
    
    class func map(JSONObject: Any?, context: MapContext?) -> Photos {
        return Mapper(context: context).map(JSONObject: JSONObject, toObject: Photos())
    }
}
