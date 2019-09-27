//
//  BankAccountDetailVC.swift
//  FanPhotographer
//
//  Created by Codiant on 11/29/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class BankAccountDetailVC: UIViewController {
    @IBOutlet weak var txfFullName: CustomTextField!
    @IBOutlet weak var txfRoutingNumber: CustomTextField!
    @IBOutlet weak var txfAcNumber: CustomTextField!
    @IBOutlet weak var txfConfirmAcNumber: CustomTextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    //  MARK:- Private methods
    private func validated() -> Bool {
        
        if Validator.emptyString(txfFullName.text) {
            showAlertWith(message: "Please enter account holder's full name")
            return false
        }
        else if Validator.emptyString(txfRoutingNumber.text) {
            showAlertWith(message: "Please enter account holder's routing name")
            return false
        } else if Validator.emptyString(txfAcNumber.text) {
            showAlertWith(message: "Please enter account number of your bank account")
            return false
        } else if Validator.emptyString(txfConfirmAcNumber.text) {
            showAlertWith(message: "Please enter confirm account number")
            return false
        } else if txfConfirmAcNumber.text != txfAcNumber.text {
            showAlertWith(message: "account number and confirm account number does not match")
            return false
        }
        
        return true
    }
    //  MARK:- Action methods
    @IBAction func btnBack_Action(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
    @IBAction func btnContinue_Action(_ sender: UIButton) {
        guard validated() else {
            return
        }
        
        let parameters: HTTPParameters = ["account_holder_name": txfFullName.text!, "account_no": txfAcNumber.text!, "account_no_confirmation": txfConfirmAcNumber.text!, "routing_no": txfRoutingNumber.text!]
        FanHUD.show()
        APIComponents.Payment.addBankAccount(parameters: parameters, closure: { [unowned self] (success, data, error) in
            FanHUD.hide()
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    self.showAlertWith(message: message)
                }
                return
            }
            if let paymentVC = self.storyboard?.viewController(withClass: PaymentVC.self) {
                self.push(paymentVC)
            }
        })
    }
}
