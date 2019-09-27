//
//  TrackingVC.swift
//  FanPhotographer
//
//  Created by Codiant on 11/30/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
import GoogleMaps
import UserNotifications
private let kAddButtonTag = 10

class TrackingVC: UIViewController {
    
    @IBOutlet weak var viewInfo: UIView!
    @IBOutlet weak var mapView: GMSMapView!
    @IBOutlet weak var lblETA: UILabel!
    @IBOutlet weak var btnStartTimeApprove: UIButton!
    @IBOutlet weak var btnChat: UIButton!
    private var proceedTimer: Timer!
    var seconds = 60 //This variable will hold a starting value of seconds. It could be any amount above 0.
    var order   = Order()
    private var markers = [GMSMarker]()
    private var polyline: GMSPolyline?
    fileprivate var alertController       : UIAlertController?
    private var locationManager = NSLocationManager.shared
    private var locationTracker = LocationTracker()
    private var photographerMarker: GMSMarker?
    var backgroundTask : UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //self.mapView.isMyLocationEnabled = true
        if TimerOperation.bookingStatus == OrderStatus.Completed.rawValue {
            // Start the timer if old booking is completed
            self.proceedTimer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(updateProgress), userInfo: nil, repeats: true)
            registerBackgroundTask()
        } else {
            updateLocation()
            if !self.viewInfo.isHidden {
                self.viewInfo.isHidden = true
            }
        }
        NotificationCenter.default.addObserver(self, selector: #selector(updateTimer), name: NSNotification.Name.UIApplicationDidBecomeActive, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(self.startTracking), name: NSNotification.Name(rawValue: "ORDER_REQUEST_PROCEED"), object: nil)
        checkNotifications()
        
        self.btnStartTimeApprove.isHidden = !(TimerOperation.bookingStatus == OrderStatus.Pending.rawValue)
        self.btnChat.isHidden = TimerOperation.bookingStatus == OrderStatus.Pending.rawValue
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.lblETA.text = self.order.arrivingTime + " to reach..."
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        focusCurrentLocation()
        self.view.viewWithTag(kAddButtonTag)?.gradientBackground(from: Color.orange, to: Color.lightOrange , direction: .leftToRight)
        if Device.iPhoneX || Device.iPhone6 || Device.iPhone6p {
            self.view.viewWithTag(kAddButtonTag)?.constraints.filter({ $0.firstAttribute == .height }).first?.constant = 45
        }
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        if TimerOperation.bookingStatus == OrderStatus.Completed.rawValue {
            NotificationCenter.default.removeObserver(self)
            stopTimer()
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    deinit {
        stopTimer()
        locationTracker.stop()
        NotificationCenter.default.removeObserver(self)
    }
    
    //  MARK:- Notification observer
    @objc private func updateTimer() {
        if TimerOperation.bookingStatus == OrderStatus.Completed.rawValue {
            if proceedTimer != nil && (backgroundTask == UIBackgroundTaskInvalid) {
                registerBackgroundTask()
            }
        } else if TimerOperation.bookingStatus == OrderStatus.Proceed.rawValue {
            checkNotifications()
            locationTracker.reconnectIfNeeded()
        } else if TimerOperation.bookingStatus == OrderStatus.Pending.rawValue {
            locationTracker.reconnectIfNeeded()
        }
    }
    
    @objc func startTracking(_ notification: NSNotification) {
        if !self.viewInfo.isHidden {
            self.viewInfo.isHidden = true
        }
        stopTimer()
        if let userInfo = notification.userInfo {
            if let notificationType = userInfo["type"] as? String {
                switch notificationType {
                case "request_proceed" :
                    Order.currentOrder = order
                    TimerOperation.bookingStatus = OrderStatus.Proceed.rawValue
                    updateLocation()
                    break
                case "request_cancel", "cancel_session_by_customer" :
                    TimerOperation.bookingStatus = OrderStatus.Completed.rawValue
                    self.locationTracker.stop()
                    self.navigationController?.popViewController(animated: true)
                    break
                case "start_time" :
                    let closure = { (action: UIAlertAction!) -> Void in
                        let title = action.style == .cancel
                        if !title {
                            self.startTimeApproved {
                                TimerOperation.bookingStatus = OrderStatus.Running.rawValue
                                self.locationTracker.stop()
                                NotificationCenter.default.removeObserver(self)
                                if let startShootVC = self.storyboard?.viewController(withClass: StartShootVC.self) {
                                    TimerOperation.shared.saveTimeOperation(isTimerRunning: true, remainingTime: Int(TimeInterval(self.order.duration)! * 60))
                                    TimerOperation.totalTime = Int(TimeInterval(self.order.duration)! * 60)
                                    startShootVC.order = self.order
                                    self.push(startShootVC)
                                }
                            }
                        } else {
                            TimerOperation.bookingStatus = OrderStatus.Pending.rawValue
                           // Show start button
                            self.btnStartTimeApprove.isHidden = !(TimerOperation.bookingStatus == OrderStatus.Pending.rawValue)
                           // Hide chat button
                            self.btnChat.isHidden = TimerOperation.bookingStatus == OrderStatus.Pending.rawValue
                        }
                    }
                    self.alertController = UIAlertController(title: "FAN", message: "Please confirm start time request.", preferredStyle: .alert)
                    self.alertController!.addAction(UIAlertAction(title: "Yes", style: .default, handler: closure))
                    self.alertController!.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: closure))
                    self.present(self.alertController!, animated: true, completion: nil)
                    break
                case "new_message":
                    guard let topController = UIViewController.topViewController(), !(topController is UIAlertController) else {
                            return
                    }
                    if (topController is ChatVC) {
                        return
                    }
                    if let chatVC = self.storyboard?.viewController(withClass: ChatVC.self) {
                        chatVC.order = self.order
                        self.push(chatVC)
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                            NotificationCenter.default.post(name: NSNotification.Name(rawValue: "NEW_MESSAGE"), object: nil, userInfo: userInfo)
                        }
                    }
                default:
                    break
                }
            }
        }
    }
    
    func checkNotifications()  {
        if TimerOperation.bookingStatus == OrderStatus.Proceed.rawValue {
            UNUserNotificationCenter.current().getDeliveredNotifications { notifications in
                DispatchQueue.main.sync { /* or .async {} */
                    // update UI
                    for notification in notifications {
                        let userInfo = notification.request.content.userInfo
                        if let currentOrder = userInfo["order_id"] as? String, self.order.id == currentOrder {
                            if userInfo["type"] as? String == "request_cancel" || userInfo["type"] as? String == "cancel_session_by_customer" || userInfo["type"] as? String == "start_time" || userInfo["type"] as? String == "new_message" {
                                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "ORDER_REQUEST_PROCEED"), object: nil, userInfo: userInfo)
                                break
                            }
                        }
                    }
                    let center = UNUserNotificationCenter.current()
                    center.removeAllDeliveredNotifications()
                }
            }
        }
    }

    //  MARK:- Private methods
    private func focusCurrentLocation() {
        
        self.mapView.clear()
        self.markers.removeAll()
        
        // Add custom marker on current location
        let pgLocation = CLLocationCoordinate2DMake(self.order.pgLat, self.order.pgLong)
        photographerMarker = self.markerWithCoordinate(pgLocation , markerImage: "current_location")
        photographerMarker!.title = "Current Location"
        photographerMarker!.map = self.mapView
        self.markers.append(photographerMarker!)
        
        // Customer Marker
        let custLocation = CLLocationCoordinate2DMake(self.order.lat, self.order.long)
        let pgLocationMarker = self.markerWithCoordinate(custLocation, markerImage: "userpointer")
        pgLocationMarker.title = "Customer Location"
        pgLocationMarker.map = self.mapView
        self.markers.append(pgLocationMarker)
        self.displayData()
    }
    
    private func updateLocation() {
        locationTracker = LocationTracker()
        locationManager.startUpdatingLocationWith(type: .periodic)
        locationManager.onDidUpdateLocation = ({ [weak self] (location) in
            guard let strongSelf = self else {
                return
        }
            strongSelf.updateMarkerLocation(location.coordinate)
            strongSelf.sendLocation(location.coordinate)
            strongSelf.locationTracker.reconnectIfNeeded()
        })
        locationTracker.startWithOrder(id: order.id)
    }
    
    private func sendLocation(_ coordinates:CLLocationCoordinate2D) {
        let parameters = ["latitude": "\(coordinates.latitude)", "longitude": "\(coordinates.longitude)", "orderId": "\(order.id!)"]
        print(parameters)
        locationTracker.sendLocation(args: [parameters])
    }
    
    private func updateMarkerLocation(_ location: CLLocationCoordinate2D) {
        guard photographerMarker != nil else {
            photographerMarker = markerWithCoordinate(location, markerImage: "current_location")
            photographerMarker!.groundAnchor = CGPoint(x: 0.5, y: 0.5)
            photographerMarker!.map = self.mapView
            // Movement
          //  self.mapView.animate(toLocation: location)
            return
        }
        // Keep Rotation Short
        CATransaction.begin()
        CATransaction.setAnimationDuration(0.5)
        CATransaction.commit()
        photographerMarker!.position = location
    }
    
    private func markerWithCoordinate(_ coordinate: CLLocationCoordinate2D, markerImage:String) -> GMSMarker {
        let marker = GMSMarker()
        marker.position = coordinate
        marker.icon = UIImage(named: markerImage)
        return marker
    }
    
    private func displayData() -> Void {
        zoomToFitMarkers()
        parseRoute(source: markers[0].position, destination: markers[1].position)
    }
    
    private func zoomToFitMarkers() -> Void {
        var bounds = GMSCoordinateBounds()
        bounds = bounds.includingCoordinate(markers[0].position)
        for marker in self.markers {
            bounds = bounds.includingCoordinate(marker.position)
        }
        mapView.animate(with: GMSCameraUpdate.fit(bounds, withPadding: 40))
    }
    
    private func parseRoute(source:CLLocationCoordinate2D, destination:CLLocationCoordinate2D) -> Void {
        DispatchQueue.main.async {
        let sourceStr = "\(source.latitude),\(source.longitude)"
        let destinationStr = "\(destination.latitude),\(destination.longitude)"
        
        let directionsApi = "https://maps.googleapis.com/maps/api/directions/json?&origin=\(sourceStr)&destination=\(destinationStr)&mode=driving"
        let url = URL(string: directionsApi)!
        
        
        let directionsTask = URLSession.shared.dataTask(with: url) { [weak self] (data, response, error) in
            
            guard let strongSelf = self,
                error == nil,
                let data = data else {
                    return
            }
            
            do {
                let object = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
                if let routes = object?["routes"] as? [[String: Any]] {
                    strongSelf.polyline?.map = nil
                    strongSelf.polyline = nil
                    
                    guard routes.count > 0 else {
                        return
                    }
                    
                    let route = routes[0]
                    if let overviewPolyline = route["overview_polyline"] as? [String: Any], let points = overviewPolyline["points"] as? String {
                        let path = GMSPath(fromEncodedPath: points)
                        strongSelf.polyline = GMSPolyline(path: path)
                        
                        DispatchQueue.main.async {
                            strongSelf.polyline?.strokeWidth = 5
                            strongSelf.polyline?.strokeColor = Color.orange
                            strongSelf.polyline?.map = strongSelf.mapView
                        }
                    }
                }
            }
            catch let error {
                DDLogError(error.localizedDescription)
            }
        }
        directionsTask.resume()
        }
    }
    
    
    // MARK:- Timer operation
    @objc func updateProgress() {
        print(seconds)
        if seconds < 1 {
            stopTimer()
            self.navigationController?.popViewController(animated: true)
            return
        }
        seconds -= 1
    }
    
    func stopTimer() {
        if self.proceedTimer != nil {
            self.proceedTimer.invalidate()
            self.proceedTimer = nil
            if backgroundTask != UIBackgroundTaskInvalid {
                endBackgroundTak()
            }
        }
    }
    
    //  MARK:- Background Task
    private func endBackgroundTak() {
        UIApplication.shared.endBackgroundTask(backgroundTask)
        backgroundTask = UIBackgroundTaskInvalid
    }
    
    private func registerBackgroundTask() {
        backgroundTask = UIApplication.shared.beginBackgroundTask(expirationHandler: { [weak self] in
            self?.endBackgroundTak()
        })
    }

    //  MARK:- Action methods
    @IBAction func btnCurrentLocation(_ sender: UIButton) {
        mapView.animate(to: GMSCameraPosition.init(target: markers[0].position, zoom: 16, bearing: 0, viewingAngle: 0))
    }
    
    @IBAction func btnChat_Action(_ sender: UIButton) {
        if let chatVC = self.storyboard?.viewController(withClass: ChatVC.self) {
            chatVC.order = self.order
            self.push(chatVC)
        }
    }
    
    @IBAction func btnCancel_Action(_ sender: UIButton) {
        
        let closure = { (action: UIAlertAction!) -> Void in
            
            let title = action.style == .cancel
            if !title {
                self.cancelAcceptedOrderRequest {
                    TimerOperation.bookingStatus = OrderStatus.Completed.rawValue
                    self.locationTracker.stop()
                    self.navigationController?.popViewController(animated: true)
                }
            }
        }
        self.alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        
        self.alertController!.addAction(UIAlertAction(title: "Unable to reach on location", style: .default, handler: closure))
        self.alertController!.addAction(UIAlertAction(title: "Location is too far", style: .default, handler: closure))
        self.alertController!.addAction(UIAlertAction(title: "GPS is not locating", style: .default, handler: closure))
        self.alertController!.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        
        self.present(self.alertController!, animated: true, completion: nil)
    }
    @IBAction func btnStartTimeApprove_Action(_ sender: UIButton) {
        self.startTimeApproved {
            TimerOperation.bookingStatus = OrderStatus.Running.rawValue
            self.locationTracker.stop()
            NotificationCenter.default.removeObserver(self)
            if let startShootVC = self.storyboard?.viewController(withClass: StartShootVC.self) {
                TimerOperation.shared.saveTimeOperation(isTimerRunning: true, remainingTime: Int(TimeInterval(self.order.duration)! * 60))
                TimerOperation.totalTime = Int(TimeInterval(self.order.duration)! * 60)
                startShootVC.order = self.order
                self.push(startShootVC)
            }
        }
    }
    
    //  MARK:- API methods
    func cancelAcceptedOrderRequest(handler: @escaping() -> Void) {
        FanHUD.show()
        APIComponents.Photographer.canelRequestWithReason(orderID : self.order.id!, reasonID : "1") { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            handler()
        }
    }
    
    func startTimeApproved(handler: @escaping() -> Void) {
        FanHUD.show()
        APIComponents.Photographer.startTimeApproved(orderID : self.order.id!) { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            handler()
        }
    }
}
