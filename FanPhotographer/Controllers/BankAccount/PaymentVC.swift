//
//  PaymentVC.swift
//  FanPhotographer
//
//  Created by Codiant on 11/29/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class PaymentVC: UIViewController {
    @IBOutlet weak var txfCardNumber: CustomTextField!
    @IBOutlet weak var txfExpiryDate: CustomTextField!
    @IBOutlet weak var txfCVV       : CustomTextField!
    @IBOutlet weak var imgCardType  : UIImageView!
    @IBOutlet weak var lblAmount  : UILabel!
    @IBOutlet weak var btnPayment : UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        configure()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Private methods
    func configure() {
        creditCardInput().groupSeparater = " "
        creditCardInput().showsCardLogo = true
        creditCardInput().initWithCardNumber(txfCardNumber, expDateField: txfExpiryDate, cvvField: txfCVV, cardLogo: imgCardType)
        getRegisrationCharge()
    }
    
    private func validated() -> Bool {
        if !creditCardInput().isValid() {
            showAlertWith(message: "Please enter valid credit card number")
            return false
        }
        else if creditCardInput().dateComponents.month == 0 || creditCardInput().dateComponents.year == 0 {
            showAlertWith(message: "Please enter valid expiry month and year")
            return false
        }
        else if Validator.emptyString(txfCVV.text) {
            showAlertWith(message: "Please enter CVC of your credit card")
            return false
        }
        return true
    }

    func getRegisrationCharge() {
        FanHUD.show()
        APIComponents.Photographer.getRegisrationFee { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let data = object["data"] as? HTTPParameters {
                if let value = data["value"] as? String {
                    strongSelf.lblAmount.text = "$\(value)"
                    strongSelf.btnPayment.isEnabled = true
                }
            }
        }
    }
    //  MARK:- Action methods
    @IBAction func btnBack_Action(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
    @IBAction func btnMakePayment_Action(_ sender: NSLoadingButton) {
        guard validated() else {
            return
        }
        let cardParams = STPCardParams()
        cardParams.number = creditCardInput().cardNumber
        cardParams.expMonth = UInt(creditCardInput().dateComponents.month!)
        cardParams.expYear = UInt(creditCardInput().dateComponents.year!)
        cardParams.cvc = txfCVV.text!
        FanHUD.show()
        self.refreshPublisherKey { (success) in
            guard success else {
                FanHUD.hide()
                return
            }
            STPAPIClient.shared().createToken(withCard: cardParams) { (token: STPToken?, error: Error?) in
                guard let token = token, error == nil else {
                    FanHUD.hide()
                    self.showAlertWith(message: error!.localizedDescription)
                    return
                }
                

                let charge =   self.lblAmount.text?.dropFirst()
                let parameters: HTTPParameters = ["stripe_token": token.tokenId, "amount": charge!]
                APIComponents.Payment.registrationPayment(parameters: parameters, closure: { [unowned self] (success, data, error) in
                    FanHUD.hide()
                    guard success, error == nil else {
                        if let message = error?.localizedDescription {
                            self.showAlertWith(message: message)
                        }
                        return
                    }
                    if LoggedInUser.shared.isRegistered {
                        self.showAlertWith(message: "Payment successfully!")
                    } else {
                        if let approvalVC = self.storyboard?.viewController(withClass: WaitingApprovalVC.self) {
                            UserDefaults.standard.set("WaitingApprovalVC", forKey: "VCIdentifier")
                            self.push(approvalVC)
                        }
                    }
                })
            }
        }
    }
    func refreshPublisherKey(_ closure: @escaping(Bool)->Void) {
        guard STPPaymentConfiguration.shared().publishableKey.count == 0 else {
            closure(true)
            return
        }
        /*
         STPPaymentConfiguration.shared().publishableKey = "pk_test_PXJXzT4pZI6A1cMC4MtNB0ph"
         closure(true)
         */
        APIComponents.Payment.getStripePublisherKey { (success, data, error) in
            guard success, error == nil else {
                DDLogDebug("Error occured while initializing payment gateway")
                closure(false)
                return
            }
            
            if let object = data!.deserialize(),
                let data = object["data"] as? HTTPParameters,
                let pk_Key = data["stripe_key"] as? String {
                STPPaymentConfiguration.shared().publishableKey = pk_Key
                closure(true)
            }
            else {
                closure(false)
            }
        }
    }
}
