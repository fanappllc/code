//
//  ComplainData.swift
//  FanCustomer
//
//  Created by Urvashi Bhagat on 1/18/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import UIKit
import ObjectMapper

class ComplainData: Mappable {
    
    var title: String!
    var id: String!
    
    init() {}
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        
        self.title <- map["title"]
        self.id <- (map["id"],IntToStringTransform)

    }
    
    class func map(JSONObject: Any?, context: MapContext?) -> ComplainData {
        return Mapper(context: context).map(JSONObject: JSONObject, toObject: ComplainData())
    }
}
