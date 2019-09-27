//
//  NSLocationManager.swift
//
//  - Copyright (c) 2017 Codiant. All rights reserved.
//

import UIKit
import CoreLocation

public enum NSLocationFetchType {
    case continuous, once, periodic
}

fileprivate typealias AlwaysAuthorizationHandler = (Bool) -> Void

class NSLocationManager: NSObject, CLLocationManagerDelegate {
    
    private var cl_locationManager: CLLocationManager!
    private var authorizationHandler: AlwaysAuthorizationHandler?
    private var bgTask = UIBackgroundTaskInvalid
    private var fetchType: NSLocationFetchType = .once
    private var locations = [CLLocation]()
     var lastSentLocation: CLLocation?
    private var currentHeading: CLLocationDirection = 0.0
    private var sendLocationTimer: Timer?
    var onDidUpdateLocation: ((CLLocation) -> Void)?
    static var shared: NSLocationManager = {
        //instance.cl_locationManager.allowsBackgroundLocationUpdates = false
        //instance.cl_locationManager.pausesLocationUpdatesAutomatically = true
        
        let instance = NSLocationManager()
        instance.cl_locationManager = CLLocationManager()
        instance.cl_locationManager.desiredAccuracy = kCLLocationAccuracyBestForNavigation
        instance.cl_locationManager.distanceFilter = 0
        instance.cl_locationManager.delegate = instance
        return instance
    }()
    
    var bestFitLocation: CLLocation? {
        get {
            return self.locations.min(by: {$0.horizontalAccuracy < $1.horizontalAccuracy})
        }
    }
    
    public func startUpdatingLocationWith(type: NSLocationFetchType) -> Void {
        requestAlwaysAuthorization { (authorized) in
            if authorized {
                self.fetchType = type
                self.startManager()
            }
        }
    }
    
    public func stopUpdatingLocation() -> Void {
        bgTask = UIBackgroundTaskInvalid
        stopManager()
        DDLogDebug("Location manager stopped")
    }
    
    public func authorized() -> Bool {
        switch CLLocationManager.authorizationStatus() {
        case .authorizedAlways, .authorizedWhenInUse :
            return true
        default:
            return false
        }
    }
    
    private func startManager() {
        if self.fetchType == .once {
            cl_locationManager.requestLocation()
            return
        }
        
        self.clearTimer()
        cl_locationManager.startUpdatingHeading()
        cl_locationManager.startUpdatingLocation()
    }
    
    private func stopManager() {
        self.clearTimer()
        cl_locationManager.stopUpdatingHeading()
        cl_locationManager.stopUpdatingLocation()
    }
    
    private func clearTimer() {
        if self.sendLocationTimer != nil {
            self.sendLocationTimer?.invalidate()
            self.sendLocationTimer = nil
        }
    }
    
    private func requestAlwaysAuthorization(_ handler: @escaping (Bool) -> Void) -> Void {
        if authorized() {
            handler(authorized())
            return
        }
        
        if authorizationHandler != nil {
            authorizationHandler = nil
        }
        
        authorizationHandler = handler
        cl_locationManager.requestWhenInUseAuthorization()
    }
    
    internal func applicationDidEnterBackground() -> Void {
        beginBackgroundTask()
    }
    
    internal func applicationDidBecomeActive() -> Void {
        endBackgroundTask()
    }
    
    private func beginBackgroundTask() -> Void {
        let state = UIApplication.shared.applicationState
        if (state == .background || state == .inactive), bgTask == UIBackgroundTaskInvalid {
            bgTask = UIApplication.shared.beginBackgroundTask(expirationHandler: {
                self.endBackgroundTask()
            })
        }
    }
    
    private func endBackgroundTask() {
        guard bgTask != UIBackgroundTaskInvalid else { return }
        UIApplication.shared.endBackgroundTask(bgTask)
        bgTask = UIBackgroundTaskInvalid
    }
    
    @objc func sendLocation() {
        if let bestFit = self.bestFitLocation, let lastSent = self.lastSentLocation, self.currentHeading > 0 {
            
            guard bestFit.coordinate.latitude != lastSent.coordinate.latitude || bestFit.coordinate.longitude != lastSent.coordinate.longitude else {
                return
            }
            
            self.lastSentLocation = bestFit
            self.locations.removeAll()
            
            APIComponents.Location.updateLocation(location: bestFit, closure: { (success, data, error) in
            })
        }
    }
    
    // MARK: CLLocationManager delegate
    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        var locationServicesEnabled = false
        
        switch status {
        case CLAuthorizationStatus.restricted: break
        case CLAuthorizationStatus.denied: break
        case CLAuthorizationStatus.notDetermined: break
        default:
            locationServicesEnabled = true
        }
        
        authorizationHandler?(locationServicesEnabled)
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        #if FANCUSTOMER
            return
        #else
        if self.currentHeading == 0, let bestFit = self.bestFitLocation {
            APIComponents.Location.updateLocation(location: bestFit, closure: { (success, data, error) in
                
            })
            self.lastSentLocation = bestFit
        }
        
        self.currentHeading = newHeading.trueHeading
        #endif
    }
    
    func locationManagerDidPauseLocationUpdates(_ manager: CLLocationManager) {
        DDLogDebug("Location manager paused location updates")
    }
    
    func locationManagerDidResumeLocationUpdates(_ manager: CLLocationManager) {
        DDLogDebug("Location manager resumed location updates")
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        
        for location in locations {
            // test the age of the location measurement to determine if the measurement is cached
            // in most cases you will not want to rely on cached measurements
//            let locationAge = -location.timestamp.timeIntervalSinceNow
//            if locationAge > 10 {
//                continue
//            }
            
            // test that the horizontal accuracy does not indicate an invalid measurement
//            if location.horizontalAccuracy < 0 || location.horizontalAccuracy > 30000 {
//                continue
//            }
            
            if let locationObserver = onDidUpdateLocation {
                locationObserver(location)
            }
            self.locations.append(location)
        }
        #if FANCUSTOMER
            return
        #else
            if let bestFit = self.bestFitLocation {
                if self.sendLocationTimer == nil {
                    self.sendLocationTimer = Timer.scheduledTimer(timeInterval: 60, target: self, selector: #selector(self.sendLocation), userInfo: nil, repeats: true)
                }
                DDLogDebug("Best Fit location:- latitude \(bestFit.coordinate.latitude) longitude \(bestFit.coordinate.longitude) accuracy \(bestFit.horizontalAccuracy)")
            }
        #endif
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        DDLogError("location manager failed to retrieve location with error: \(error.localizedDescription)")
    }
    
}

