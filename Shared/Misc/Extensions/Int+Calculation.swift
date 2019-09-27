//
//  Int+Calculation.swift
//  FanCustomer
//
//  Created by Darshan Mothreja on 1/2/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import Foundation


extension TimeInterval {
    
    func getMinutesIntoHoursDay() -> String {
        
        let hours = Int(self) / 60
        let minutes = Int(self) % 60
        if hours > 0 {
            return String(format:"%02i:%02i Hour", hours, minutes)
        }
        if minutes > 0 {
            return String(format:"%02i Min", minutes)
        }
        return String(format:"%02i:%02i", hours, minutes)
    }

}
