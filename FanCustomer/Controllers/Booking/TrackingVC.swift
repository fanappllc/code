//
//  TrackingVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/20/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
import GoogleMaps
import UserNotifications
private let kAddButtonTag = 10

class TrackingVC: UIViewController {
    @IBOutlet weak var mapView: GMSMapView!
    @IBOutlet weak var lblETA: UILabel!
    private var locationTracker: LocationTracker?
    var markers = [GMSMarker]()
    private var polyline: GMSPolyline?
    private var photographerMarker: GMSMarker?
    var bookingInfo : PhotographerProfile!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        NotificationCenter.default.addObserver(self, selector: #selector(self.startTracking), name: NSNotification.Name(rawValue: "ORDER_REQUEST_CANCEL"), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(self.applicationDidBecomeActive), name: NSNotification.Name.UIApplicationDidBecomeActive, object: nil)
        checkNotifications()
        //#Setup To track Photographer location
        self.setupLocationUpdate()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        lblETA.text = bookingInfo.arrivingTime + " to reach..."
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        displayData()
        self.view.viewWithTag(kAddButtonTag)?.gradientBackground(from: Color.orange, to: Color.lightOrange , direction: .leftToRight)
        if Device.iPhoneX || Device.iPhone6 || Device.iPhone6p {
            self.view.viewWithTag(kAddButtonTag)?.constraints.filter({ $0.firstAttribute == .height }).first?.constant = 45
        }
 }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    deinit {
        locationTracker?.stop()
        NotificationCenter.default.removeObserver(self)
    }
    
    //  MARK:- Notification observer

    @objc func startTracking(_ notification : NSNotification){
        if let userInfo = notification.userInfo {
            if let notificationType = userInfo["type"] as? String {
                switch notificationType {
                case "cancel_session_by_photographer" :
                    stopActiveOperation()
                    if let array = self.navigationController?.viewControllers.filter(({$0 is HomeVC})), array.count > 0 {
                        TimerOperation.bookingStatus = OrderStatus.Completed.rawValue
                        self.navigationController?.popToViewController(array[0], animated: true)
                    }
                    break
                    case "new_message":
                        guard let topController = UIViewController.topViewController(), !(topController is UIAlertController) else {
                                return
                        }
                        if (topController is ChatVC) {
                            return
                        }
                    if let chatVC = self.storyboard?.viewController(withClass: ChatVC.self) {
                        chatVC.bookingInfo = self.bookingInfo
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
    
    @objc func applicationDidBecomeActive() {
        if TimerOperation.bookingStatus == OrderStatus.Proceed.rawValue {
            checkNotifications()
            locationTracker?.reconnectIfNeeded()
        }
    }
    
    func checkNotifications()  {
        if TimerOperation.bookingStatus == OrderStatus.Proceed.rawValue {
            UNUserNotificationCenter.current().getDeliveredNotifications { notifications in
                DispatchQueue.main.sync { /* or .async {} */
                    // update UI
                    for notification in notifications {
                        let userInfo = notification.request.content.userInfo
                        if let currentOrder = userInfo["order_id"] as? String, self.bookingInfo.orderId! == currentOrder {
                            if userInfo["type"] as? String == "cancel_session_by_photographer" || userInfo["type"] as? String == "new_message" {
                                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "ORDER_REQUEST_CANCEL"), object: nil, userInfo: userInfo)
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
    func setupLocationUpdate() {
        locationTracker = LocationTracker()
        locationTracker!.startWithOrder(id: bookingInfo.orderId!)
        locationTracker!.onNewLocation = ({ [weak self] (args) in
            guard let strongSelf = self else {
                return
            }
            if let data = args, let locationData = data[0] as? HTTPParameters {
                let trackedLocation = TrackLocation.map(JSONObject: locationData, context: nil)
                strongSelf.updateMarkerLocation(trackedLocation)
            }
        })
    }
    private func updateMarkerLocation(_ location: TrackLocation) {
        let coordinates = CLLocationCoordinate2DMake(location.latitude, location.longitude)
        markers[1].map = nil
        guard photographerMarker != nil else {
            photographerMarker = markerWithCoordinate(coordinates, markerImage: "pgpointer")
            photographerMarker!.groundAnchor = CGPoint(x: 0.5, y: 0.5)
            photographerMarker!.map = self.mapView
            // Movement
            //self.mapView.animate(toLocation: coordinates)
            return
        }
        // Keep Rotation Short
        CATransaction.begin()
        CATransaction.setAnimationDuration(0.5)
        CATransaction.commit()
        self.photographerMarker!.position = coordinates
    }
    
    private func displayData() -> Void {
        let selfCoordinates = CLLocationCoordinate2DMake(bookingInfo.customerLatitude, bookingInfo.customerLongitude)
        let pgCoordinates = CLLocationCoordinate2DMake(bookingInfo.photographerLatitude, bookingInfo.photographerLongitude)
        markers.append(markerWithCoordinate(selfCoordinates, markerImage: "pulse_location"))
        markers.append(markerWithCoordinate(pgCoordinates, markerImage: "pgpointer"))
        markers[0].map = mapView
        markers[1].map = mapView
        zoomToFitMarkers()
        self.parseRoute(source: self.markers[0].position, destination: self.markers[1].position)
    }
        
    private func markerWithCoordinate(_ coordinate: CLLocationCoordinate2D, markerImage:String) -> GMSMarker {
        let marker = GMSMarker()
        marker.position = coordinate
        marker.icon = UIImage(named: markerImage)
        return marker
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
 
    func stopActiveOperation() {
        self.locationTracker?.stop()
        NotificationCenter.default.removeObserver(self)
    }
    
    //  MARK:- Action methods
    @IBAction func btnBack_Action(_ sender: UIButton) {
        // Go back
        self.navigationController?.popViewController(animated: true)
    }
    
    @IBAction func btnStartShoot_Action(_ sender: UIButton) {
        // Start Shoot
        if let startShootVC = self.storyboard?.viewController(withClass: StartShootVC.self) {
            startShootVC.bookingInfo = self.bookingInfo
            startShootVC.locationTracker = self.locationTracker
            self.push(startShootVC)
        }
    }
    
    @IBAction func btnCurrentLocation(_ sender: UIButton) {
        mapView.animate(to: GMSCameraPosition.init(target: markers[0].position, zoom: 16, bearing: 0, viewingAngle: 0))
    }
    
    @IBAction func btnChat_Action(_ sender: UIButton) {
        if let chatVC = self.storyboard?.viewController(withClass: ChatVC.self) {
            chatVC.bookingInfo = self.bookingInfo
            self.push(chatVC)
        }
    }
    
    @IBAction func btnCancel_Action(_ sender: UIButton) {
        self.cancelRequest {
            self.stopActiveOperation()
            if let array = self.navigationController?.viewControllers.filter(({$0 is HomeVC})), array.count > 0 {
                TimerOperation.bookingStatus = OrderStatus.Completed.rawValue
                self.navigationController?.popToViewController(array[0], animated: true)
            }
        }
    }
    
    //  MARK:- API methods
    func cancelRequest(handler : @escaping() -> Void) {
        FanHUD.show()
        APIComponents.Customer.canelRequest(orderID: bookingInfo.orderId!) { [weak self] (success, data, error) in
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
    
    func startRequest(handler : @escaping() -> Void){
        FanHUD.show()
        let param: HTTPParameters = ["order_id"  : bookingInfo.orderId!]
        
        APIComponents.Customer.startRequest(parameter: param) { [weak self] (success, data, error) in
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
