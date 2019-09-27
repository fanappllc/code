//
//  PhotographerInfo.swift
//  FanCustomer
//
//  Created by Darshan Mothreja on 1/2/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import Foundation
import ObjectMapper

class PhotographerInfo: Mappable {
    
    
    
    var f_name: String!
    var l_name: String!
    var id: String!
    var email: String!
    var address: String!
    var mobileNo: String!
    var lat: Double!
    var long: Double!
    var profileUrl: String!
    
    init() {}
    required init?(map: Map) {}
    
    func mapping(map: Map) {
        self.f_name <- map["first_name"]
        self.l_name <- map["last_name"]
        self.id <- (map["id"], IntToStringTransform)
        self.email <- map["email"]
        self.address <- map["address"]
        self.mobileNo <- map["mobileNo"]
        self.lat <- map["latitude"]
        self.long <- map["longitude"]
        self.profileUrl <- map["profile_image"]
    }
    
    class func map(JSONObject: Any?, context: MapContext?) -> PhotographerInfo {
        return Mapper(context: context).map(JSONObject: JSONObject, toObject: PhotographerInfo())
    }
}
