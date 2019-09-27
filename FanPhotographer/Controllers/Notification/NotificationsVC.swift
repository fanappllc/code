//
//  NotificationsVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/23/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class NotificationsVC: UIViewController {
    
    @IBOutlet weak var tblNotiifcation: UITableView!
    @IBOutlet weak var viewEmptyInfo: UIView!
    @IBOutlet weak var btnClearAll: UIButton!
    
    var notification = [Notifications]()
    private var currentPage     = 0
    private var totalPageCount  = 0
    private var isFetching      = false
    private var isInitialFetchComplete = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        tblNotiifcation.tableFooterView = UIView()
        getNotificationList(pageCount: 1)
    }
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        totalPageCount = 0
        currentPage = 0
        isFetching = false
        isInitialFetchComplete = false
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: UIScrollView delegate
    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        let endScrolling = scrollView.contentOffset.y + scrollView.frame.size.height
        if endScrolling >= scrollView.contentSize.height - 100 {
            if isInitialFetchComplete, !isFetching {
                isFetching = true
                getNotificationList(pageCount: currentPage+1)
            }
        }
    }
    
    //MARK:- Private Methods
    func performBatchUpdate(_ data:[HTTPParameters]) {
        if (data.count) > 0 {
            let lastIndex = notification.count
            tblNotiifcation.beginUpdates()
            var indexPath = [NSIndexPath]()
            for i in 0...data.count-1 {
                indexPath.append(IndexPath(row: lastIndex+i, section: 0) as NSIndexPath)
            }
            notification.append(contentsOf: data.map({ Notifications.map(JSONObject: $0, context: nil) }))
            tblNotiifcation.insertRows(at: indexPath as [IndexPath], with: .top)
            tblNotiifcation.endUpdates()
        }
    }
    
    //  MARK:- API methods
    func getNotificationList(pageCount:Int) {
        if isInitialFetchComplete, pageCount > totalPageCount {
            return
        }
        if !isInitialFetchComplete {
            FanHUD.show()
        }
        
        APIComponents.Photographer.getNotifictionList(page: "\(pageCount)") {[weak self] (success, data, error) in
            guard let strongSelf = self else { return }
            if !strongSelf.isInitialFetchComplete {
                FanHUD.hide()
                strongSelf.isInitialFetchComplete = true
                LoggedInUser.getUnreadCount = "0"
                UIApplication.shared.applicationIconBadgeNumber = 0
            }
            strongSelf.isFetching = false
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let items = object["items"] as? HTTPParameters, let dataArray = items["data"] as? [HTTPParameters] {
                if let totalPage = items["last_page"] {
                    strongSelf.totalPageCount = totalPage as! Int
                    strongSelf.currentPage += 1
                }
                else {
                    strongSelf.totalPageCount = 0
                    strongSelf.currentPage = 0
                }
                if strongSelf.currentPage == 1 {
                    if dataArray.count == 0 {
                        strongSelf.viewEmptyInfo.isHidden = false
                        strongSelf.btnClearAll.isHidden = true
                    } else {
                        strongSelf.viewEmptyInfo.isHidden = true
                        strongSelf.btnClearAll.isHidden = false
                    }
                }
                strongSelf.performBatchUpdate(dataArray)
            }
        }
    }
    
    func deleteAllNotifications(handler: @escaping() -> Void) {
        FanHUD.show()
        APIComponents.Photographer.deleteAllNotifiction { [weak self] (success, error) in
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
    
    func deleteNotification(notificationID : String, handler: @escaping() -> Void) {
        FanHUD.show()
        APIComponents.Photographer.deleteNotifiction(notificationID: notificationID) { [weak self] (success, error) in
            FanHUD.hide()
            guard let strongSelf  = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            handler()
        }
    }
    
    //  MARK:- Action methods
    @IBAction func btnClearAll_Action(_ sender: UIButton) {
        
        if self.notification.count > 0 {
            let alertController = UIAlertController(title: "Notification", message: "Are you sure, you want to clear all notifications?", preferredStyle: .alert)
            alertController.addAction(UIAlertAction(title: "No", style: .cancel, handler: nil))
            alertController.addAction(UIAlertAction(title: "Yes", style: .destructive, handler: { (action) in
                self.deleteAllNotifications {
                    self.notification.removeAll()
                    self.tblNotiifcation.reloadData()
                    self.viewEmptyInfo.isHidden = false
                    sender.isHidden = true
                }
            }))
            present(alertController, animated: true, completion: nil)
        }
    }
}

extension NotificationsVC: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return notification.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "NotificationCell", for: indexPath) as! NotificationCell
        let info = notification[indexPath.row]
        cell.lblMessage.text = info.message
        cell.lblTime.text = info.messageDate.toString(format: .custom("hh:mm a"))
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
    }
    
    func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return true
    }
    
    // this method handles row deletion
    func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        
        if editingStyle == .delete {
            let info = notification[indexPath.row]
            deleteNotification(notificationID: info.id, handler: {
                // remove the item from the data model
                self.notification.remove(at: indexPath.row)
                // delete the table view row
                tableView.deleteRows(at: [indexPath], with: .fade)
                if self.notification.count == 0 {
                    self.viewEmptyInfo.isHidden = false
                    self.btnClearAll.isHidden = true
                }
            })
        } else if editingStyle == .insert {
            // Not used in our example, but if you were adding a new row, this is where you would do it.
        }
    }
}
