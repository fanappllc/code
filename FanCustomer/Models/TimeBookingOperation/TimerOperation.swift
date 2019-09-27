//
//  TimerOperation.swift
//  Fan
//
//  Created by Codiant on 1/16/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import UIKit

class TimerOperation: NSObject {
  
    
    static let shared: TimerOperation = {
        let instance = TimerOperation()
        return instance
    }()
    
    static var bookingStatus: OrderStatus.RawValue {
        
        get {
            guard UserDefaults.standard.value(forKey: "bookingStatus") != nil  else {
                PhotographerProfile.resetCurrentOrder()
                return OrderStatus.Completed.rawValue
            }
            let status = UserDefaults.standard.value(forKey: "bookingStatus") as! OrderStatus.RawValue
            if status == OrderStatus.Completed.rawValue {
                PhotographerProfile.resetCurrentOrder()
            }
            return status
        }
        set {
            if newValue == OrderStatus.Completed.rawValue {
                PhotographerProfile.resetCurrentOrder()
            }
            UserDefaults.standard.set(newValue, forKey: "bookingStatus")
        }
    }
    
    static var isRenewAvailable: Bool {
        get {
            guard UserDefaults.standard.value(forKey: "renewAvailable") != nil  else {
                return true
            }
            return UserDefaults.standard.value(forKey: "renewAvailable") as! Bool
        }
        set {
             UserDefaults.standard.set(newValue, forKey: "renewAvailable")
        }
    }
    
    static var isTimerRunning: Bool {
        get {
            guard UserDefaults.standard.value(forKey: "timerState") != nil  else {
                return false
            }
            return UserDefaults.standard.value(forKey: "timerState") as! Bool
        }
    }
    
    static var elapsedTime : Double {
        //Background or terminated time
        get {
            guard UserDefaults.standard.value(forKey: "savedTimeStamp") != nil else {
                return 0
            }
            let savedTimeInterval:Double = UserDefaults.standard.value(forKey: "savedTimeStamp")  as! Double
            let currentTimeInterval = NSDate().timeIntervalSince1970
            let timeDifference =  currentTimeInterval - savedTimeInterval
            print("Elapsed Time  =   \(timeDifference)")
            return timeDifference
        }
    }
    static var totalTime: Int {
        get {
            return Int(UserDefaults.standard.value(forKey: "totalTime") as! TimeInterval)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: "totalTime")
        }
    }
    static var remainingTime: Int {
        
        // Remaining seconds - Background or terminated time
        get {
            guard UserDefaults.standard.value(forKey: "savedTimeCount") != nil else {
                return 0
            }
            if TimerOperation.isTimerRunning {
                let savedTimeInterval:Double = UserDefaults.standard.value(forKey: "savedTimeStamp")  as! Double
                let currentTimeInterval = NSDate().timeIntervalSince1970
                let timeDifference =  currentTimeInterval - savedTimeInterval
                var seconds = Int(UserDefaults.standard.value(forKey: "savedTimeCount") as! TimeInterval)
                seconds = seconds - Int(timeDifference)
                return seconds
            } else {
                return Int(UserDefaults.standard.value(forKey: "savedTimeCount") as! TimeInterval)
            }
        }
        set {
            UserDefaults.standard.set(newValue, forKey: "savedTimeCount")
        }
    }
    
    func saveTimeOperation(isTimerRunning : Bool, remainingTime: Int? = 0) {
        UserDefaults.standard.set(NSDate().timeIntervalSince1970, forKey: "savedTimeStamp")
        UserDefaults.standard.set(isTimerRunning, forKey: "timerState")
        UserDefaults.standard.set(remainingTime, forKey: "savedTimeCount")
    }
    
    func resetTime()  {
        UserDefaults.standard.removeObject(forKey: "bookingStatus")
        UserDefaults.standard.removeObject(forKey: "savedTimeStamp")
        UserDefaults.standard.removeObject(forKey: "timerState")
        UserDefaults.standard.removeObject(forKey: "savedTimeCount")
        UserDefaults.standard.removeObject(forKey: "totalTime")
        UserDefaults.standard.removeObject(forKey: "renewAvailable")
    }
}
