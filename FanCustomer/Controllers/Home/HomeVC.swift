//
//  HomeVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/15/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
import GoogleMaps

class HomeVC: UIViewController {
    
    struct Slot {
        var duration : String!
        var time : Int
        var charge : Int
    }
    
    @IBOutlet weak var cnstrntRequestViewBottom: NSLayoutConstraint! // Default = 130
    @IBOutlet weak var mapView: GMSMapView!
    @IBOutlet weak var progressBarView : UIProgressView!
    @IBOutlet weak var viewRequesting  : UIView!
    @IBOutlet weak var btnCurrentLocation: UIButton!
    @IBOutlet weak var btnRefresh: UIButton!
    @IBOutlet weak var collectionSlot: UICollectionView!
    @IBOutlet weak var btnRequestNow: MRGradintButton!
    
    private var requestTimer: Timer!
    private var allMarkers = [GMSMarker]()
    var selectedIndex   = IndexPath(row: 0, section: 0)
    var pulseLayer: PulsingHaloLayer?
    var arrPhotographers = [PhotographerInfo]()
    var arrSlots: [Slot]!
    var currentLocation: CLLocation!
    var backgroundTask : UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid

    override func viewDidLoad() {
        super.viewDidLoad()
        //mapView.isMyLocationEnabled = true
        NotificationCenter.default.addObserver(self, selector: #selector(self.acceptRequest), name: NSNotification.Name(rawValue: "ORDER_REQUEST_ACCEPTED"), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(updateTimer), name: NSNotification.Name.UIApplicationDidBecomeActive, object: nil)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        focusCurrentLocation(isLoading: false)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        stopTimer()
        if !self.viewRequesting.isHidden  {
            self.viewRequesting.isHidden = true
            self.mapView.clear()
            self.allMarkers.removeAll()
        }
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
    
    @objc private func acceptRequest(notification: Notification) {
        if self.requestTimer != nil {
            self.requestTimer.invalidate()
            self.requestTimer = nil
            if backgroundTask != UIBackgroundTaskInvalid {
                endBackgroundTak()
            }
            if let userInfo = notification.userInfo , let orderId = userInfo["order_id"], let photographerId = userInfo["photographer_id"]  {
                redirectToConfirmBooking("\(orderId)" , photographerId: "\(photographerId)")
            }
            setMapContent(isIdeal: true)
        }
    }
    
    func redirectToConfirmBooking(_ orderId : String , photographerId : String) {
        if let bookingConfirmVC = self.storyboard?.viewController(withClass: ConfirmBookingVC.self) {
            bookingConfirmVC.photographerId = "\(photographerId)"
            bookingConfirmVC.orderId = "\(orderId)"
            self.push(bookingConfirmVC)
        }
    }
    
    //  MARK:- Private methods
    private func focusCurrentLocation(isLoading: Bool) {
        if let lastSent = NSLocationManager.shared.bestFitLocation,  !isLoading{
            self.showFetchedLocation(location: lastSent)
        }
        
        if isLoading {
            FanHUD.show()
        }
        var locationFetched = false
        NSLocationManager.shared.startUpdatingLocationWith(type: .once)
        NSLocationManager.shared.onDidUpdateLocation = ({ [weak self] (location) in
            NSLocationManager.shared.onDidUpdateLocation = nil
            NSLocationManager.shared.stopUpdatingLocation()
            guard let strongSelf = self,
                !locationFetched else {
                    if isLoading {
                        FanHUD.hide()
                    }
                    return
            }
            
            locationFetched = true
            
            strongSelf.showFetchedLocation(location: location)
            strongSelf.updateLocation(showLoading: isLoading, handler: {
                strongSelf.getTimeSlotsToBook(showLoading: isLoading, handler: {
                    strongSelf.getPhotographers(showLoading: isLoading, currentLocation: CLLocationCoordinate2D(latitude: location.coordinate.latitude, longitude: location.coordinate.longitude)) {
                        strongSelf.addPhotographerMarkers {
                            strongSelf.updateCamera(with: location.coordinate)
                        }
                    }
                })
            })
        })
    }
    
    func showFetchedLocation(location : CLLocation) {

        self.currentLocation = location
        self.mapView.clear()
        self.allMarkers.removeAll()
        
        // Add custom marker on current location
        let currentLocationMarker = self.markerWithCoordinate(location.coordinate, markerImage: "current_location")
        currentLocationMarker.title = "Current Location"
        currentLocationMarker.map = self.mapView
        self.allMarkers.append(currentLocationMarker)
        self.mapView.animate(to: GMSCameraPosition.init(target: location.coordinate, zoom: 16, bearing: 0, viewingAngle: 0))
    }
    
    fileprivate func updateCamera(with coordinates: CLLocationCoordinate2D) {
        mapView.animate(to: GMSCameraPosition.init(target: coordinates, zoom: 16, bearing: 0, viewingAngle: 0))
        if self.cnstrntRequestViewBottom.constant != 0 {
            // Show Bottom action view
            self.viewRequesting.layoutIfNeeded()
            self.cnstrntRequestViewBottom.constant = 0
            self.btnCurrentLocation.isHidden = false
            self.btnRequestNow.isEnabled = true
            self.btnRequestNow.backgroundColor = Color.celrianBlue
            UIView.animate(withDuration: 0.5, animations: {
                self.view.layoutIfNeeded()
            })
        }
    }
    
    fileprivate func addPhotographerMarkers(handler: @escaping() -> Void) {
        //CLStruct.structForCurrentPosition()
        for CLMarker in arrPhotographers {
            if CLMarker.lat == nil {
                return
            }
            let pgLocation = CLLocationCoordinate2DMake(CLMarker.lat, CLMarker.long)
            let pgLocationMarker = markerWithCoordinate(pgLocation, markerImage: "pgpointer")
            pgLocationMarker.map = self.mapView
            allMarkers.append(pgLocationMarker)
        }
        handler()
    }
    
    private func markerWithCoordinate(_ coordinate: CLLocationCoordinate2D, markerImage:String) -> GMSMarker {
        let marker = GMSMarker()
        marker.position = coordinate
        marker.icon = UIImage(named: markerImage)
        return marker
    }
    
    func setMapContent(isIdeal: Bool) {
        self.viewRequesting.isHidden = isIdeal
        self.mapView.clear()
        if isIdeal {
            let currentLocationMarker = self.markerWithCoordinate(self.currentLocation.coordinate, markerImage: "current_location")
            currentLocationMarker.title = "Current Location"
            currentLocationMarker.map = self.mapView
            self.allMarkers[0] = currentLocationMarker
            self.addPhotographerMarkers {
                self.updateCamera(with: self.currentLocation.coordinate)
            }
            selectedIndex   = IndexPath(row: 0, section: 0)
            self.view.isUserInteractionEnabled = true
        } else {
            haloAnimationPulse()
        }
    }
    
    func haloAnimationPulse() {
        
        // Clear previous current loaction marker
        self.mapView.clear()
        updateCamera(with: currentLocation.coordinate)
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
            if self.pulseLayer != nil {
                self.pulseLayer?.removeAllAnimations()
                self.pulseLayer = nil
            }
            
            self.pulseLayer = PulsingHaloLayer()
            
            // Creat custom imageView and add pulse layer animation
            let pulseRingImg = UIImageView(frame: CGRect(x: 0, y: 0, width: self.mapView.frame.size.width, height: self.mapView.frame.size.height))
            pulseRingImg.image = #imageLiteral(resourceName: "pulse_location")
            pulseRingImg.isUserInteractionEnabled = false
            pulseRingImg.contentMode = .center
            self.pulseLayer!.position = pulseRingImg.center
            pulseRingImg.layer.addSublayer(self.pulseLayer!)
            
            // Creat custom marker on current location
            let marker = GMSMarker(position: self.mapView.camera.target)
            marker.iconView = pulseRingImg
            marker.layer.addSublayer(pulseRingImg.layer)
            marker.map = self.mapView
            marker.groundAnchor = CGPoint(x: 0.5, y: 0.5)
            self.pulseLayer!.start()
            
            // Update marker
            self.allMarkers[0] = marker
            self.addPhotographerMarkers {
                self.view.isUserInteractionEnabled = false
            }
        }
    }
    
    // MARK:- Timer operation
    private func startRequestTimer() {
        setMapContent(isIdeal: false)
        self.progressBarView.progress = 0
        self.requestTimer = nil
        self.requestTimer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(updateProgress), userInfo: nil, repeats: true)
        registerBackgroundTask()
    }
    
    func stopTimer(){
        if self.requestTimer != nil {
            self.requestTimer.invalidate()
            self.requestTimer = nil
            if self.backgroundTask != UIBackgroundTaskInvalid {
                self.endBackgroundTak()
            }
        }
    }
    
    @objc func updateProgress() {
        /*
         let progress = self.progressBarView.progress + ( 1/60)
         
         UIView.animate(withDuration: 1.0, animations: {
         self.progressBarView.setProgress(progress, animated: true)
         }) { (isCompleted) in
         if self.progressBarView.progress == 1.0 {
         if !self.viewRequesting.isHidden  {
         self.viewRequesting.isHidden = true
         self.mapView.clear()
         self.allMarkers.removeAll()
         }
         self.setMapIdle()
         } else {
         self.updateProgress()
         }
         }
         */
        let progress = self.progressBarView.progress + ( 1/60)
        UIView.animate(withDuration: 1.0, animations: {
            self.progressBarView.setProgress(progress, animated:true)
        }) { (isFinished) in
            if self.progressBarView.progress == 1.0 {
                self.stopTimer()
                self.setMapContent(isIdeal: true)
                self.showAlertWith(message: "No photographer available, please try again later.")
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
    @IBAction func btnRequestNow_Action(_ sender: UIButton) {
        if currentLocation != nil {
            checkDiscountStatus(handler: { (data) in
                if let isDiscount = data["is_discount"] as? Bool, isDiscount == true  {
                    let alertController = UIAlertController(title: "Discount", message: ("You have discount of \(String(describing: data["discount_percent"]!))% on this booking."), preferredStyle: .alert)
                    alertController.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
                    alertController.addAction(UIAlertAction(title: "Proceed", style: .default, handler: { (action) in
                        self.sendRequestToPhotographers(promoId: "", handler: {
                            self.startRequestTimer()
                        })
                    }))
                    self.present(alertController, animated: true, completion: nil)
                } else {
                    // Show alert for promocode
                    let alert = UIAlertController(title: "Promocode", message: "Do you have any Promocode?", preferredStyle: UIAlertControllerStyle.alert)
                    let action = UIAlertAction(title: "Apply", style: .default) { (alertAction) in
                        let textField = alert.textFields![0] as UITextField
                        if let code = textField.text, code.count > 0 {
                            print("Code is " + code)
                            self.verifyPromocode(code: code, handler: { (data) in
                                if let promoId = data["id"] as? Int  {
                                    let alertController = UIAlertController(title: "Promocode applied successfully", message: ("You have discount of \(String(describing: data["discount_percent"]!))% on this booking."), preferredStyle: .alert)
                                    alertController.addAction(UIAlertAction(title: "Proceed", style: .default, handler: { (action) in
                                        // TODO: Send ID in below API
                                        self.sendRequestToPhotographers(promoId: String(promoId), handler: {
                                           self.startRequestTimer()
                                        })
                                    }))
                                    alertController.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
                                    self.present(alertController, animated: true, completion: nil)
                                }
                            })
                        }
                    }
                    alert.addTextField { (textField) in
                        textField.placeholder = "Enter promocode"
                    }
                    alert.addAction(action)
                    alert.addAction(UIAlertAction(title: "Don't have Promocode", style: .default, handler: { (action) in
                        self.sendRequestToPhotographers(promoId: "", handler: {
                            self.startRequestTimer()
                        })
                    }))
                    alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
                    self.present(alert, animated: true, completion: nil)
                }
            })
        }
    }
    
    @IBAction func btnCurrentLocation_Action(_ sender: UIButton) {
        focusCurrentLocation(isLoading: true)
    }
    
    @IBAction func btnRefreshPhotographer_Action(_ sender: UIButton) {
        focusCurrentLocation(isLoading: true)
    }
    
    //  MARK:- API methods
    func getPhotographers(showLoading: Bool, currentLocation: CLLocationCoordinate2D, handler: @escaping() -> Void) {
        
        APIComponents.Customer.getPhotographersWith(location: currentLocation) { [weak self] (success, data, error) in
            if showLoading {
                FanHUD.hide()
            }
            self?.btnRefresh.isHidden = false
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                return
            }
            
            if let object = data!.deserialize(), let data = object["data"] as? [HTTPParameters] {
            //let json = data?.deserialize()!["data"] as! [HTTPParameters]
            strongSelf.arrPhotographers.removeAll()
            strongSelf.arrPhotographers.append(contentsOf: data.map({ PhotographerInfo.map(JSONObject: $0, context: nil) }))
            if strongSelf.arrPhotographers.count > 0 {
                handler()
              }
           }
        }
    }
    
    func updateLocation(showLoading: Bool, handler: @escaping() -> Void) {
        APIComponents.Location.updateLocation(location: currentLocation) { [weak self] (success, data, error) in
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if showLoading {
                    FanHUD.hide()
                }
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            handler()
        }
    }
    
    func getTimeSlotsToBook(showLoading: Bool, handler: @escaping() -> Void) {
        
        APIComponents.Customer.getTimeSlots { [weak self] (success, data, error) in
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if showLoading {
                    FanHUD.hide()
                }
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            let json = data?.deserialize()!["data"] as! [HTTPParameters]
            strongSelf.arrSlots?.removeAll()
            strongSelf.arrSlots = json.map({ Slot(duration: ($0["slot_minutes"] as! TimeInterval).getMinutesIntoHoursDay(), time: $0["slot_minutes"] as! Int, charge: $0["price"] as! Int ) })
            
            if strongSelf.arrSlots.count > 0 {
                strongSelf.collectionSlot.reloadData()
                handler()
            }
            if showLoading {
                FanHUD.hide()
            }
        }
    }
    
    func sendRequestToPhotographers(promoId: String, handler: @escaping() -> Void) {
        
        let param: HTTPParameters = ["slot"  : arrSlots[selectedIndex.row].time,
                                     "price" : arrSlots[selectedIndex.row].charge,
                                     "latitude"  : "\(currentLocation.coordinate.latitude)",
                                     "longitude" : "\(currentLocation.coordinate.longitude)",
                                     "location": "",
                                     "promo_code_id": promoId]
        FanHUD.show()
        APIComponents.Customer.sendRequestToPhotographer(parameter: param) { [weak self] (success, data, error) in
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
    
    func checkDiscountStatus(handler: @escaping(_ datas: HTTPParameters) -> Void) {
        FanHUD.show()
        let price = String(arrSlots[selectedIndex.row].charge)
        APIComponents.Customer.checkDiscountStatus(slotPrice: price) { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let data = object["data"] as? HTTPParameters {
               handler(data)
            }
        }
    }
    
    
    func verifyPromocode(code:String, handler: @escaping(_ datas: HTTPParameters) -> Void) {
        let param: HTTPParameters = ["promocode"  : code]
        FanHUD.show()
        APIComponents.Customer.verifyPromoCode(parameter: param)  { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let data = object["data"] as? HTTPParameters {
                handler(data)
            }
        }
    }
}

extension HomeVC : UICollectionViewDelegate, UICollectionViewDataSource {
    // MARK: UICollectionView datasource
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        
        return (arrSlots ?? []).count
    }
    
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "SlotCollectionCell", for: indexPath) as! SlotCollectionCell
        let slot = arrSlots[indexPath.row]
        cell.lblSlotDuration.text = slot.duration
        let price = String(slot.charge).currencyInputFormatting()
        cell.lblSlotCharge.text   = "Price:" + price
        cell.viewSlot.backgroundColor   = indexPath  == selectedIndex ? Color.celrianBlue : UIColor.clear
        cell.lblSlotDuration.textColor  =  UIColor.white
        cell.lblSlotCharge.textColor    =  UIColor.white
        return cell
    }
    
    // MARK: UICollectionView delegate
    public func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        selectedIndex = indexPath
        collectionView.reloadData()
    }
}
