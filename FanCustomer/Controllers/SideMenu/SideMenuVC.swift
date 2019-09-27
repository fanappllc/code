//
//  SideMenuVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/15/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class SideMenuVC: UIViewController {
    @IBOutlet weak var tblSideMenu: UITableView!
    @IBOutlet weak var imgViewUser: UIImageView!
    @IBOutlet weak var lblUserName: UILabel!
    
    //  Prepare table content.
    let menuItems = ["Home", "My Photos", "Manage Card", "Booking History", "Notifications", "Faq", "Terms & Conditions", "Contact", "Complaint", "Logout"]
    var selectedIndex = IndexPath(row: 0, section: 0)
   
    override func viewDidLoad() {
        super.viewDidLoad()
        updateUserInformation()
        // Observe for update user information on side menu when user update profile
        NotificationCenter.default.addObserver(self, selector: #selector(self.updateUserInformation), name: NSNotification.Name(rawValue: "REFRESH_USERINFO"), object: nil)
        
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        self.view.gradientBackground(from: Color.ultraBlue, to: Color.celrianBlue , direction: .topToBottom)
        self.tblSideMenu.reloadRows(at: [IndexPath(row: 4, section: 0)], with: .none)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: Private Methods
    @objc private func updateUserInformation() {
        lblUserName.text = Profile.shared.firstName + " " + Profile.shared.lastName
        imgViewUser.setImage(with: URL(string: Profile.shared.photo)!)
    }
    
    fileprivate func imageName(for menu: String) -> String {
        
        switch menu {
        case "Home"         : return "home"
        case "My Photos"    : return "my_photos"
        case "Manage Card"  : return "my_card"
        case "Booking History"  : return "history"
        case "Notifications"    : return "notification"
        case "Faq"          : return "faq"
        case "Terms & Conditions": return "terms"
        case "Contact"      : return "contact"
        case "Complaint"      : return "complaint"
        case "Logout"       : return "logout"
        default:
            return ""
        }
    }
    
    fileprivate func viewControllerIdentifier(for menu: String) -> String {
        
        switch menu {
        case "Home"                 : return "HomeVC"
        case "My Photos"            : return "GalleryImgVC"
        case "Booking History"      : return "BookingHistoryVC"
        case "Notifications"        : return "NotificationsVC"
        case "Terms & Conditions"   : return "TermsVC"
        case "Contact"              : return "ContactUsVC"
        case "Faq"                  : return "FaqVC"
        case "Manage Card"          : return "CreditCardListVC"
        case "Complaint"             : return "ComplainVC"

        default:
            return "EditProfileVC"
        }
    }
    
    fileprivate func logoutConfirmation(handler: @escaping() -> Void) {
        
        let alertController = UIAlertController(title: "Log Out", message: "Are you sure, you want to Log Out?", preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: "Stay In", style: .cancel, handler: nil))
        alertController.addAction(UIAlertAction(title: "Log Out", style: .destructive, handler: { (action) in
            handler()
        }))
        
        self.present(alertController, animated: true, completion: nil)
    }
    
    @IBAction func btnProfile_Action(_ sender: Any) {
        if  (selectedIndex.row != -1) {
            let profileVC = storyboard?.instantiateViewController(withIdentifier: "EditProfileVC")
            sideMenuViewController!.contentViewController?.setViewControllers([profileVC!], animated: false)
            selectedIndex = IndexPath(row:-1 , section: 0)
        }
        sideMenuViewController?.hideMenuViewController()
        tblSideMenu.reloadData()
    }
    // Clear document directory
    func clearDocumentDirectory(){
        let path = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
        guard let items = try? FileManager.default.contentsOfDirectory(atPath: path) else { return }
        
        for item in items {
            // This can be made better by using pathComponent
            let completePath = path.appending("/").appending(item)
            try? FileManager.default.removeItem(atPath: completePath)
        }
    }
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}

// MARK:  UITableViewDataSource and Delegate
extension SideMenuVC: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        if Device.iPhone6 {
            return 52
        } else if Device.iPhone6p {
            return 58
        }
        return 44
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return menuItems.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
    
        var cell = tblSideMenu.dequeueReusableCell(withIdentifier: "SideMenuCell") as? SideMenuCell
    
        if cell == nil {
            cell = SideMenuCell(style: UITableViewCellStyle.default, reuseIdentifier: "SideMenuCell")
        }
        let menu = menuItems[(indexPath as NSIndexPath).row]
        
        cell?.lblMenuTitle.text = menu
        cell?.imgMenuTitle.image = UIImage(named: self.imageName(for: menu))?.withRenderingMode(.alwaysOriginal)
        cell?.backgroundColor = indexPath == selectedIndex ? UIColor.darkGray.withAlphaComponent(0.1) : UIColor.clear
        cell?.notificationView.isHidden = menu != "Notifications"
        cell?.lblNotificationCount.text = menu == "Notifications" ? LoggedInUser.getUnreadCount : ""
        return cell!
    }
    
    internal func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        let menu = menuItems[indexPath.row]
        guard menu != "Logout" else {
            logoutConfirmation {
                FanHUD.show()
                APIComponents.Account.logout({ [weak self] (success, data, error) in
                    FanHUD.hide()
                    guard let strongSelf = self else {
                      return
                    }
                    guard success, error == nil else {
                        if let message = error?.localizedDescription {
                            strongSelf.showAlertWith(message: message)
                        }
                        return
                    }
                    strongSelf.clearDocumentDirectory()
                    AppDelegate.setHomeViewConroller()
                })
            }
            return
        }
        
        let identifier = self.viewControllerIdentifier(for: menu)
        
        var controller: UIViewController?
        
        if self.storyboard!.controllerExists(withIdentifier: identifier) {
            controller = self.storyboard!.instantiateViewController(withIdentifier: identifier)
            
        }
        guard controller != nil else {
            return
        }

        if indexPath != selectedIndex {
            selectedIndex = indexPath
            tableView.reloadData()
            sideMenuViewController!.contentViewController?.setViewControllers([controller!], animated: false)
        }
        sideMenuViewController?.hideMenuViewController()
    }
}
