//
//  SSNVerificationVC.swift
//  FanPhotographer
//
//  Created by Codiant on 11/28/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class SSNVerificationVC: UIViewController {

    @IBOutlet weak var txfSSN: CustomTextField!
    override func viewDidLoad() {
        super.viewDidLoad()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Action methods
    @IBAction func btnBack_Action(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
    @IBAction func btnContinue_Action(_ sender: UIButton) {
        guard let number = self.txfSSN.text, number.count > 0 else {
            self.showAlertWith(message: "Please enter a valid SSN number")
            return
        }
        FanHUD.show()
        APIComponents.Account.updateSSN(ssnNumber: number) { [unowned self] (success, data, error) in
            FanHUD.hide()
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    self.showAlertWith(message: message)
                }
                return
            }
            if let idVerificationVC = self.storyboard?.viewController(withClass: IDVerificationVC.self) {
                self.push(idVerificationVC)
            }
        }
    }
}
