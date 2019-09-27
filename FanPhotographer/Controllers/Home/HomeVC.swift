//
//  HomeVC.swift
//  FanPhotographer
//
//  Created by Codiant on 11/29/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
import ObjectMapper

class HomeVC: UIViewController {

    @IBOutlet weak var tblJobs: PlaceholderTableView!
    @IBOutlet weak var viewInfo: UIView!
    @IBOutlet weak var switchAvailable: UISwitch!
    var orderRequest = [OrderRequest]()

    override func viewDidLoad() {
        super.viewDidLoad()
        self.switchAvailable.setOn(LoggedInUser.PhotographerAvailable, animated: false)
        self.viewInfo.isHidden = switchAvailable.isOn
        configure()
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        if LoggedInUser.PhotographerAvailable {
            getOrderRequest {
                self.tblJobs.reloadData()
            }
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
    @objc private func filterRequest() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            self.getOrderRequest {
                self.tblJobs.reloadData()
            }
        }
    }
    
    //  MARK:- Private methods
    private func configure() {
        NSLocationManager.shared.startUpdatingLocationWith(type: .continuous)
        // Observe when customer request a new photography session and refresh the list
        NotificationCenter.default.addObserver(self, selector: #selector(self.filterRequest), name: NSNotification.Name(rawValue: "REFRESH_ORDER_REQUEST"), object: nil)
    }
    
    //  MARK:- Action methods
    @IBAction func switchAvailability_Action(_ sender: UISwitch) {
        print(sender.isOn)
        FanHUD.show()
        APIComponents.Photographer.photographerAvailablity(isAvailable: sender.isOn) { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                sender.setOn(!sender.isOn, animated: false)
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            strongSelf.viewInfo.isHidden = sender.isOn
            print(LoggedInUser.PhotographerAvailable)
            LoggedInUser.PhotographerAvailable = sender.isOn
            print(LoggedInUser.PhotographerAvailable)
        }
    }
    
    //  MARK:- API methods
    func getOrderRequest(handler: @escaping() -> Void) {
        FanHUD.show()
        APIComponents.Photographer.getOrderRequest() { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            let json = data?.deserialize()!["data"] as! [HTTPParameters]
            strongSelf.orderRequest.removeAll()
            strongSelf.orderRequest.append(contentsOf: json.map({ OrderRequest.map(JSONObject: $0, context: nil) }))
            handler()
        }
    }
}

extension HomeVC: UITableViewDelegate, UITableViewDataSource, JobProcessDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return orderRequest.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "JobsRequestCell", for: indexPath) as! JobsRequestCell
        let booking = orderRequest[indexPath.row]
        cell.lblName.text       = booking.f_name + " " + booking.l_name
        cell.lblAddress.text    = booking.address
        cell.lblDuration.text   = TimeInterval(booking.duration)?.getMinutesIntoHoursDay()
        cell.lblETA.text        = booking.arrivingTime
        cell.imgViewUser.setImage(with: URL(string: booking.profileUrl)!)
        cell.delegate = self
        cell.indexPath = indexPath
        return cell
    }
    
    func cellButtonActionTapped(at index: IndexPath, isAccept: Bool) {
        DDLogDebug("button tapped at index:\(index) \(isAccept ? "Accept": "Decline")")
        guard orderRequest.count > 0 else {
            return
        }
        if !isAccept {
            // remove the item from the data model
            if orderRequest.count > 0 {
                FanHUD.show()
                orderRequest.remove(at: index.row)
                //tblJobs.beginUpdates()
                // delete the table view row
                //tblJobs.deleteRows(at: [index], with: .fade)
                //tblJobs.endUpdates()
                tblJobs.reloadData()
                FanHUD.hide()
            }
        } else {
        
            let booking = orderRequest[index.row]
            FanHUD.show()
            APIComponents.Photographer.orderRequestOperation(orderID : booking.id) { [weak self] (success, data, error) in
                 FanHUD.hide()
                guard let strongSelf = self else { return }
                guard success, error == nil else {
                    if let message = error?.localizedDescription {
                        strongSelf.showAlertWith(message: message)
                        strongSelf.getOrderRequest {
                            strongSelf.tblJobs.reloadData()
                        }
                    }
                    return
                }
                if let trackingVC = strongSelf.storyboard?.viewController(withClass: TrackingVC.self) {
                    let selectedOrder = Order.map(JSONObject: booking.toJSON(), context: nil)
                    trackingVC.order = selectedOrder
                    strongSelf.push(trackingVC)
                }
            }
         }
    }
}
