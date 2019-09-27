//
//  StartShootVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/20/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
import UserNotifications
private let kRenewSessionTime = 300

class StartShootVC: UIViewController {
    var countdownTimer : Timer?
    @IBOutlet weak var lblTimer : UILabel!
    @IBOutlet weak var lblInfo  : UILabel!
    @IBOutlet weak var btnEndShoot: UIButton!
    @IBOutlet weak var btnBack    : UIButton!
    @IBOutlet weak var viewRenewSession: UIView!
    @IBOutlet weak var tblRenewSession: UITableView!
    @IBOutlet weak var countdownTimerView: SRCountdownTimer!
    @IBOutlet weak var btnStart    : UIButton!
    @IBOutlet weak var btnCancel    : UIButton!

    var bookingInfo    : PhotographerProfile!
    var selectedIndex = IndexPath(row: 0, section: 0)
    var arrSlots: [Slot]!
    var seconds = 0
    var totalSeconds : Int = 0
    var locationTracker: LocationTracker?
    struct Slot {
        var duration : String!
        var time : Int
        var charge : Double
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        self.setUpButtonUI()
        if TimerOperation.bookingStatus == OrderStatus.Running.rawValue {
            if TimerOperation.remainingTime > 0 {
                startTimerOperation()
            } else {
                // Session Completed
                self.endSession {
                    self.sessionCompleted()
                }
            }
        } else  {
           NotificationCenter.default.addObserver(self, selector: #selector(self.startTimer), name: NSNotification.Name(rawValue: "ORDER_REQUEST_PROCEED"), object: nil)
        }
        registerNotifications()
        checkNotifications()
        lblTimer.text = timeString(time: TimeInterval((Int(self.bookingInfo.slotTime)! * 60)))
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        getTimeSlotsToBook {
            
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        self.view.viewWithTag(200)?.gradientBackground(from: Color.orange, to: Color.lightOrange , direction: .leftToRight)
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        NotificationCenter.default.removeObserver(self)
        stopTimer()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    //  MARK:- Register Notification
    func registerNotifications() {
        NotificationCenter .default .addObserver(self, selector: #selector(willResignActive), name: NSNotification.Name.UIApplicationWillResignActive, object: nil)
        NotificationCenter .default .addObserver(self, selector: #selector(willTerminate), name: NSNotification.Name.UIApplicationWillTerminate, object: nil)
        NotificationCenter .default .addObserver(self, selector: #selector(didBecomeActive), name: NSNotification.Name.UIApplicationDidBecomeActive, object: nil)
        NotificationCenter .default .addObserver(self, selector: #selector(didEnterBackground), name: NSNotification.Name.UIApplicationDidEnterBackground, object: nil)
    }
    
    //  MARK:- Notification observer
    @objc func willResignActive() {
        if TimerOperation.bookingStatus == OrderStatus.Running.rawValue {
            // Save your settings
            TimerOperation.shared.saveTimeOperation(isTimerRunning: TimerOperation.bookingStatus == OrderStatus.Running.rawValue, remainingTime: seconds)
        }
    }
    
    @objc func didEnterBackground() {
        if TimerOperation.bookingStatus == OrderStatus.Running.rawValue {
            // Save your settings
            TimerOperation.shared.saveTimeOperation(isTimerRunning: TimerOperation.bookingStatus == OrderStatus.Running.rawValue, remainingTime: seconds)
        }
    }
    
    @objc func willTerminate() {
        if TimerOperation.bookingStatus == OrderStatus.Running.rawValue {
            // Save your settings
            TimerOperation.shared.saveTimeOperation(isTimerRunning: TimerOperation.bookingStatus == OrderStatus.Running.rawValue, remainingTime: seconds)
        }
    }

    func checkNotifications()  {
        UNUserNotificationCenter.current().getDeliveredNotifications { notifications in
            DispatchQueue.main.sync { /* or .async {} */
                // update UI
                for notification in notifications {
                    let userInfo = notification.request.content.userInfo
                    if let currentOrder = userInfo["order_id"] as? String, self.bookingInfo.orderId! == currentOrder {
                        if userInfo["type"] as? String == "start_time_request_approve", TimerOperation.bookingStatus == OrderStatus.Pending.rawValue  {
                            NotificationCenter.default.post(name: NSNotification.Name(rawValue: "ORDER_REQUEST_PROCEED"), object: nil, userInfo: userInfo)
                            break
                        } else if userInfo["type"] as? String == "cancel_session_by_photographer", (TimerOperation.bookingStatus == OrderStatus.Proceed.rawValue || TimerOperation.bookingStatus == OrderStatus.Pending.rawValue) {
                           self.navigationController?.popViewController(animated: true)
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "ORDER_REQUEST_CANCEL"), object: nil, userInfo: userInfo)
                            }
                        } else if userInfo["type"] as? String == "new_message", TimerOperation.bookingStatus == OrderStatus.Proceed.rawValue {
                            self.navigationController?.popViewController(animated: true)
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "ORDER_REQUEST_CANCEL"), object: nil, userInfo: userInfo)
                            }
                        }
                    }
                }
                let center = UNUserNotificationCenter.current()
                center.removeAllDeliveredNotifications()
            }
        }
    }
    
    @objc func didBecomeActive() {
        if TimerOperation.bookingStatus == OrderStatus.Proceed.rawValue || TimerOperation.bookingStatus == OrderStatus.Pending.rawValue {
            checkNotifications()
        } else {
            if TimerOperation.remainingTime > 0 {
                startTimerOperation()
            } else {
                stopTimer()
                self.endSession {
                    self.sessionCompleted()
                }
            }
        }
    }
    
    @objc func startTimer(_ notification : NSNotification) {
        if let userInfo = notification.userInfo {
            if let notificationType = userInfo["type"] as? String {
                switch notificationType {
                case "start_time_request_approve" :
                    self.locationTracker?.stop()
                    LocationTracker.shared.stop()
                    TimerOperation.bookingStatus = OrderStatus.Running.rawValue
                    //TODO  : Need to stop tracking ON
                    TimerOperation.shared.saveTimeOperation(isTimerRunning: true, remainingTime: Int(TimeInterval(self.bookingInfo.slotTime)! * 60))
                    TimerOperation.totalTime = Int(TimeInterval(self.bookingInfo.slotTime)! * 60)
                    startTimerOperation()
                    self.setUpButtonUI()
                    break
                default:
                    break
                }
            }
        }
    }
    
    private func sessionCompleted() {
        if let billDetailVC = self.storyboard?.viewController(withClass: BillDetailVC.self) {
            billDetailVC.bookingInfo = self.bookingInfo
            self.push(billDetailVC)
        }
    }
    
    //  MARK:- Private methods
    func setUpButtonUI() {
        switch TimerOperation.bookingStatus {
        case OrderStatus.Proceed.rawValue:
            self.btnEndShoot.isEnabled = false
            self.btnEndShoot.isHidden = true
            self.btnEndShoot.backgroundColor = UIColor(red: 214.0/255.0, green: 214.0/255.0, blue: 214.0/255.0, alpha: 1.0)
            self.btnStart.isHidden = false
            self.btnCancel.isHidden = false
            break
        case OrderStatus.Pending.rawValue:
            self.btnEndShoot.isEnabled = false
            self.btnEndShoot.isHidden = false
            self.btnEndShoot.backgroundColor = UIColor(red: 214.0/255.0, green: 214.0/255.0, blue: 214.0/255.0, alpha: 1.0)
            self.btnStart.isHidden = true
            self.btnCancel.isHidden = true
            break
        case OrderStatus.Running.rawValue:
            self.btnEndShoot.isEnabled = true
            self.btnEndShoot.isHidden = false
            self.btnEndShoot.backgroundColor = Color.orange
            self.btnStart.isHidden = true
            self.btnCancel.isHidden = true
            break
        default:
            break
        }
        self.btnBack.isHidden = !self.btnEndShoot.isHidden
        self.lblInfo.text = "Session will end in \(TimeInterval(self.bookingInfo.slotTime)!.getMinutesIntoHoursDay()), or you can end it early by clicking “END” button"
    }
    
    func renewStartTimeRequest() {
        renewStartTime {
            
            let session = self.arrSlots[self.selectedIndex.row]
            let elapsedSec = TimerOperation.totalTime - self.seconds
            self.totalSeconds = TimerOperation.totalTime + (Int(session.time) * 60 )
            TimerOperation.totalTime = self.totalSeconds
            self.countdownTimerView.end()
            self.countdownTimerView.start(beginingValue: self.totalSeconds, interval: 1, elapsedInterval: TimeInterval(elapsedSec))
            self.seconds = self.totalSeconds - elapsedSec
            self.selectedIndex = IndexPath(row: 0, section: 0)
            self.lblInfo.text = "Session will end in \(TimeInterval(self.bookingInfo.slotTime)!.getMinutesIntoHoursDay()), or you can end it early by clicking “END” button"
        }
    }
    
    func startTimerOperation () {
        if TimerOperation.bookingStatus == OrderStatus.Running.rawValue {
            totalSeconds = TimerOperation.totalTime
            seconds = TimerOperation.remainingTime
            if self.countdownTimer == nil {
                self.countdownTimer = Timer.scheduledTimer(timeInterval: 1, target: self, selector: #selector(self.updateTimer), userInfo: nil, repeats: true)
            }
            self.countdownTimerView.start(beginingValue: TimerOperation.totalTime, interval: 1, elapsedInterval: TimeInterval(TimerOperation.totalTime - seconds))
            if TimerOperation.isRenewAvailable, TimerOperation.remainingTime < kRenewSessionTime {
                if arrSlots != nil, arrSlots.count > 0, self.viewRenewSession.isHidden {
                    self.viewRenewSession.isHidden = false
                    TimerOperation.isRenewAvailable = false
                } else {
                    getTimeSlotsToBook {
                        self.viewRenewSession.isHidden = false
                        TimerOperation.isRenewAvailable = false
                    }
                }
            }
        }
    }

    // MARK:- Timer operation
    @objc func updateTimer() {
        if seconds < 1 {
            self.stopTimer()
            self.endSession {
                self.sessionCompleted()
            }
        } else {
            seconds -= 1
            lblTimer.text = timeString(time: TimeInterval(seconds))
            // Show renew session screen
            if seconds == kRenewSessionTime {
                if TimerOperation.isRenewAvailable {
                    if arrSlots.count > 0, self.viewRenewSession.isHidden {
                        self.viewRenewSession.isHidden = false
                        TimerOperation.isRenewAvailable = false
                    } else {
                        getTimeSlotsToBook {
                            self.viewRenewSession.isHidden = false
                            TimerOperation.isRenewAvailable = false
                        }
                    }
                }
            }
        }
    }
    
    func timeString(time:TimeInterval) -> String {
        let hours = Int(time) / 3600
        let minutes = Int(time) / 60 % 60
        let seconds = Int(time) % 60
        return String(format:"%02i:%02i:%02i", hours, minutes, seconds)
    }
    
    func stopTimer() {
        if countdownTimer != nil {
            countdownTimer?.invalidate()
            countdownTimer = nil
        }
    }
    
    //  MARK:- Action methods
    @IBAction func btnBack_Action(_ sender: UIButton) {
        self.navigationController?.popViewController(animated: true)
    }
    
    @IBAction func btnEndShoot_Action(_ sender: UIButton) {
        let alert = UIAlertController(title: "END SHOOT",  message: "Do you want to end this session", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title:"Yes", style: .default, handler: { (action) -> Void in
            self.stopTimer()
            self.endSession{
                self.sessionCompleted()
            }
        }))
        alert.addAction(UIAlertAction(title:"No", style: .destructive, handler: { (action) -> Void in }))
        present(alert, animated: true, completion: nil)
    }
    
    @IBAction func btnProcess_Action(_ sender: UIButton) {
        switch sender.tag {
        case 100:
            let alert = UIAlertController(title: "PAYMENT",message: "Yor credit card will be charged \(self.bookingInfo.price!.currencyInputFormatting())",
                preferredStyle: .alert)
            let doneAction = UIAlertAction(title: "Confirm" , style: .default, handler: { (action) -> Void in
                self.startRequest {
                    TimerOperation.bookingStatus = OrderStatus.Pending.rawValue
                    self.setUpButtonUI()
                }
            })
            let cancel = UIAlertAction(title: "Cancel" , style: .destructive, handler: { (action) -> Void in })
            alert.addAction(doneAction)
            alert.addAction(cancel)
            self.present(alert, animated: true, completion: nil)
            
        case 200:
            let alert = UIAlertController(title: "CANCEL PHOTOSHOOT",
                                          message: "You will be charged a cancellation fee of \(self.bookingInfo.cancelCharge!.currencyInputFormatting())",
                preferredStyle: .alert)
            let doneAction = UIAlertAction(title: "Yes", style: .default, handler: { (action) -> Void in
                self.cancelRequest {
                    if let array = self.navigationController?.viewControllers.filter(({$0 is HomeVC})), array.count > 0 {
                        self.locationTracker?.stop()
                        LocationTracker.shared.stop()
                        TimerOperation.bookingStatus = OrderStatus.Completed.rawValue
                        self.navigationController?.popToViewController(array[0], animated: true)
                    }
                }
            })
            let cancel = UIAlertAction(title: "No", style: .destructive, handler: { (action) -> Void in })
            alert.addAction(doneAction)
            alert.addAction(cancel)
            self.present(alert, animated: true, completion: nil)
            
        default:
            break
        }
    }

    @IBAction func btnRenewSession_Action(_ sender: UIButton) {
        if sender.tag == 1000 {
            if arrSlots.count > 0 {
                renewStartTimeRequest()
            } else {
                getTimeSlotsToBook {
                    self.renewStartTimeRequest()
                }
            }
        }
        self.viewRenewSession.isHidden = true
    }
    
    //  MARK:- API methods
    func getTimeSlotsToBook(handler: @escaping() -> Void) {
        APIComponents.Customer.getTimeSlots { [weak self] (success, data, error) in
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            let json = data?.deserialize()!["data"] as! [HTTPParameters]
            strongSelf.arrSlots?.removeAll()
            strongSelf.arrSlots = json.map({ Slot(duration: ($0["slot_minutes"] as! TimeInterval).getMinutesIntoHoursDay(),time: $0["slot_minutes"] as! Int , charge: $0["price"] as! Double ) })
            
            if strongSelf.arrSlots.count > 0 {
                strongSelf.tblRenewSession.reloadData()
            }
            handler()
        }
    }
    
    func startRequest(handler : @escaping() -> Void) {
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
    
    func endSession(handler : @escaping() -> Void) {
        FanHUD.show()
        APIComponents.Customer.endSession(orderID: bookingInfo.orderId!) { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let _ = object["data"] as? HTTPParameters {
                handler()
            }
        }
    }
    
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
    
    func renewStartTime(handler: @escaping() -> Void) {
        FanHUD.show()
        let param: HTTPParameters = ["order_id" : bookingInfo.orderId!,
                                     "slot"     : arrSlots[selectedIndex.row].time,
                                     "price"    : arrSlots[selectedIndex.row].charge]
        
        APIComponents.Customer.renewTime(parameter: param) { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            
            let slot = strongSelf.arrSlots[strongSelf.selectedIndex.row]
            let localModel  = strongSelf.bookingInfo!
            var price = Double((localModel.price)!)!
            price += slot.charge
            localModel.price = (price as NSNumber).stringValue
            var duration = Int((localModel.slotTime)!)!
            duration += slot.time
            localModel.slotTime = (duration as NSNumber).stringValue
            PhotographerProfile.currentOrder  = strongSelf.bookingInfo
            
            handler()
        }
    }
}

extension StartShootVC: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return arrSlots != nil ? arrSlots.count : 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "RenewSessionCell", for: indexPath) as! RenewSessionCell
        let session = arrSlots[indexPath.row]
        cell.durationLabel.text = session.duration + " (\(String(session.charge).currencyInputFormatting()))"
        cell.checkmarkImageView.image =  UIImage.init(named: indexPath == selectedIndex ? "radio_on" : "radio_off")
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        selectedIndex = indexPath
        tableView.reloadData()
    }
}
