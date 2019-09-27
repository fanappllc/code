//
//  WaitingApprovalVC.swift
//  FanPhotographer
//
//  Created by Codiant on 12/26/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class WaitingApprovalVC: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
    }

    override func viewWillAppear(_ animated: Bool) {
         super.viewWillAppear(animated)
         getAccountStatus()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    func getAccountStatus()  {
        FanHUD.show()
        APIComponents.Account.getAccountStatus { [unowned self] (success, data, error) in
            FanHUD.hide()
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    self.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let data = object["data"] as? HTTPParameters {
                if let value = data["is_user_registered"] as? String, value == "0" {
                } else {
                    LoggedInUser.PhotographerAvailable = true
                    LoggedInUser.shared.map(JSONObject: data, context: nil)
                    AppDelegate.setRootViewConroller {
                    }
                }
            }
        }
    }
}
