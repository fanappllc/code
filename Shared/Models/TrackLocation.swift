//
//  TrackLocation.swift
//  FanCustomer
//
//  Created by Urvashi Bhagat on 1/27/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import Foundation
import ObjectMapper

struct TrackLocation : Mappable {
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    init() {}
    init?(map: Map) {}
    
    mutating func mapping(map: Map) {
        latitude <- (map["latitude"], StringToDoubleTransform)
        longitude <- (map["longitude"], StringToDoubleTransform)
    }
    
    static func map(JSONObject: Any?, context: MapContext?) -> TrackLocation {
        return Mapper(context: context).map(JSONObject: JSONObject, toObject: TrackLocation())
    }
}
