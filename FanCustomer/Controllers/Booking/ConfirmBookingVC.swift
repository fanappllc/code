//
//  ConfirmBookingVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/20/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
import GoogleMaps

class ConfirmBookingVC: UIViewController {
    
    @IBOutlet weak var mapView: GMSMapView!
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var lblArrivingDuration: UILabel!
    @IBOutlet weak var lblDateTime: UILabel!
    @IBOutlet weak var lblDuration: UILabel!
    @IBOutlet weak var lblHandset: UILabel!
    @IBOutlet weak var viewRating: HCSStarRatingView!
    @IBOutlet weak var imgUser: UIImageView!
    
    var markers : [GMSMarker]!
    private var polyline: GMSPolyline?
    private var requestTimer: Timer!
    var seconds = 60 //This variable will hold a starting value of seconds. It could be any amount above 0.
    var profileData : PhotographerProfile!
    var orderId : String!
    var photographerId : String!
    var backgroundTask : UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
    
    override func viewDidLoad() {
        super.viewDidLoad()
        NotificationCenter.default.addObserver(self, selector: #selector(updateTimer), name: NSNotification.Name.UIApplicationDidBecomeActive, object: nil)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        getProfile {
            self.displayData()
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        self.view.viewWithTag(200)?.gradientBackground(from: Color.orange, to: Color.lightOrange , direction: .leftToRight)
    }
   
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        stopTimer()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
  
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    //  MARK:- Notification observer
    @objc private func updateTimer() {
        if requestTimer != nil && (backgroundTask == UIBackgroundTaskInvalid) {
            registerBackgroundTask()
        }
    }
    
    //  MARK:- Private methods
    private func displayData() -> Void {
        if profileData.fname == nil {
            return
        }
        if let rating = NumberFormatter().number(from: profileData.rating ?? "0.0") {
            viewRating.value =  CGFloat(truncating: rating)
        }
        lblName.text = "\(profileData.fname!) \(profileData.lname!)"
        lblArrivingDuration.text = profileData.arrivingTime
        lblDateTime.text = profileData.date.toString(format: .custom("EEE dd, hh:mm a"))
        lblHandset.text = profileData.model
        lblDuration.text = TimeInterval(profileData.slotTime)?.getMinutesIntoHoursDay()
        imgUser.setImage(with: URL(string: profileData.profileUrl)!)
        
        let pgLocation = CLLocationCoordinate2DMake(profileData.photographerLatitude, profileData.photographerLongitude)
        let pgLocationMarker = self.markerWithCoordinate(pgLocation, markerImage: "pgpointer")
        
        let customerLocation = CLLocationCoordinate2DMake(profileData.customerLatitude, profileData.customerLongitude)
        let customerLocationMarker = self.markerWithCoordinate(customerLocation, markerImage: "pulse_location")
        
        self.markers =  [customerLocationMarker, pgLocationMarker]
        markers[0].map = mapView
        markers[1].map = mapView
        
        zoomToFitMarkers()
        parseRoute(source: markers[0].position, destination: markers[1].position)
        self.requestTimer = Timer.scheduledTimer(timeInterval: 1, target: self, selector: #selector(updateProgress), userInfo: nil, repeats: true)
        registerBackgroundTask()
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
    
    // MARK:- Timer operation
    @objc func updateProgress() {
        print(seconds)
        if seconds < 1 {
            self.stopTimer()
            self.navigationController?.popViewController(animated: true)
            getAcceptRequest(requestType: "cancel") {
            }
        }
        else {
            seconds -= 1
        }
    }
    
    func stopTimer() {
        NotificationCenter.default.removeObserver(self)
        if requestTimer != nil {
            requestTimer?.invalidate()
            requestTimer = nil
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
    @IBAction func btnProcess_Action(_ sender: UIButton) {
        if profileData == nil || profileData.photographerId == nil {
            return
        }
        getAcceptRequest(requestType: (sender.titleLabel?.text?.lowercased())!) {
            switch sender.tag {
            case 100:
                // Process
                if let trackingVC = self.storyboard?.viewController(withClass: TrackingVC.self) {
                    TimerOperation.bookingStatus = OrderStatus.Proceed.rawValue
                    PhotographerProfile.currentOrder = self.profileData
                    trackingVC.bookingInfo = self.profileData
                    self.push(trackingVC)
                }
                break
            case 200:
                // Go back
                self.navigationController?.popViewController(animated: true)
                break
            default:
                break
            }
            
        }
    }
    
    //  MARK:- API methods
    func getProfile(handler: @escaping() -> Void) {
        FanHUD.show()
        APIComponents.Customer.getPhotographerProfile(photographerID: photographerId, orderID: orderId) { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let data = object["data"] as? HTTPParameters {
                
                self?.profileData = PhotographerProfile.map(JSONObject: data, context: nil)
                handler()
            }
        }
    }
    
    func getAcceptRequest(requestType: String ,handler: @escaping() -> Void) {
        FanHUD.show()
        APIComponents.Customer.acceptRequestOperation(orderID: orderId , type : requestType ) { [weak self] (success, data, error) in
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
