//
//  ContactUsVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/23/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
import MessageUI

class ContactUsVC: UIViewController {

    @IBOutlet weak var cnsrntBottomShareView: NSLayoutConstraint! // Default 5
    @IBOutlet weak var lblAdminEmail: UILabel!
    @IBOutlet weak var stackView: UIStackView!
    
    var composeVC = MFMailComposeViewController()
    var contact = [String : Any]()

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        if Device.iPhone6 {
            cnsrntBottomShareView.constant = 25
        } else if Device.iPhone6p {
            cnsrntBottomShareView.constant = 35
        } else if Device.iPhoneX {
            cnsrntBottomShareView.constant = 50
        }
        getContact()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    //  MARK:- API methods
    
    func getContact() {
        FanHUD.show()
        APIComponents.Setting.getContact{ [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let data = object["data"] as? HTTPParameters {
                strongSelf.contact = data
                if let value = strongSelf.contact["admin_email"] as? String {
                    strongSelf.lblAdminEmail.text = value
                    strongSelf.stackView.isHidden = false
                }
            }
        }
    }
    //  MARK:- Action methods
    
    @IBAction func btnHeader_Action(_ sender: UIButton) {
        composeVC.mailComposeDelegate = self
        composeVC.setToRecipients([contact["admin_email"] as! String])

        if (MFMailComposeViewController.canSendMail()) {
            // Present the view controller modally.
                self.present(self.composeVC, animated: true, completion: nil)
        }
    }
    
    @IBAction func btnShare_Action(_ sender: UIButton) {
        switch sender.tag {
        case 0:
            let fbUrl = URL(string: contact["facebook"] as! String)!
            if UIApplication.shared.canOpenURL(fbUrl) {
                UIApplication.shared.open(fbUrl, options: [:], completionHandler: nil)
            }
            break
        case 1:
            let twitterUrl = URL(string: contact["twitter"] as! String)!
            if UIApplication.shared.canOpenURL(twitterUrl) {
                UIApplication.shared.open(twitterUrl, options: [:], completionHandler: nil)
            }
            break
        case 2:
            let googleUrl = URL(string: contact["google_plus"] as! String)!
            if UIApplication.shared.canOpenURL(googleUrl) {
                UIApplication.shared.open(googleUrl, options: [:], completionHandler: nil)
            }
            break
        case 3:
            let linkedInUrl = URL(string: contact["linkedin"] as! String)!
            if UIApplication.shared.canOpenURL(linkedInUrl) {
                UIApplication.shared.open(linkedInUrl, options: [:], completionHandler: nil)
            }
            break
        default:
            break
        }
    }
}
extension ContactUsVC : MFMailComposeViewControllerDelegate {
    
    func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
        self.dismiss(animated: true, completion: nil)
    }
}
