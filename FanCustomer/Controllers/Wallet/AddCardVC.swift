//
//  AddCardVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/15/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class AddCardVC: UIViewController {
    @IBOutlet weak var txfCardNumber: CustomTextField!
    @IBOutlet weak var txfExpiryDate: CustomTextField!
    @IBOutlet weak var txfCVV       : CustomTextField!
    @IBOutlet weak var imgCardType  : UIImageView!
    @IBOutlet weak var btnBack      : UIButton!
    
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
        
        if LoggedInUser.shared.isRegistered {
            btnBack.isHidden = false
        }
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
    
    //  MARK:- Action methods
    
    @IBAction func btnAddCard_Action(_ sender: NSLoadingButton) {
        guard validated() else {
            return
        }
        let cardParams = STPCardParams()
        cardParams.number = creditCardInput().cardNumber
        cardParams.expMonth = UInt(creditCardInput().dateComponents.month!)
        cardParams.expYear = UInt(creditCardInput().dateComponents.year!)
        cardParams.cvc = txfCVV.text!
        
        CardManager.shared.refreshPublisherKey { (success) in
            guard success else {
                return
            }
            
            CardManager.shared.tokenizeCard(params: cardParams) { [unowned self] (token, error) in
                guard let token = token, error == nil else {
                    if let errorMessage = error?.localizedDescription {
                        self.showAlertWith(message: errorMessage)
                    }
                    return
                }
                FanHUD.show()
                // Send token to server
                CardManager.shared.addCard(token: token.tokenId, closure: { [unowned self] (success, data, error) in
                    FanHUD.hide()
                    guard success, error == nil else {
                        if let errorMessage = error?.localizedDescription {
                            self.showAlertWith(message: errorMessage)
                        }
                        return
                    }
                    if let object = data!.deserialize(),
                        let data = object["data"] as? HTTPParameters {
                        if LoggedInUser.shared.isRegistered {
                            self.showAlertWith(message: "Card added successfully!")
                        } else {
                            LoggedInUser.shared.map(JSONObject: data, context: nil)
                            self.showAlertWith(message: "Your default card is successfully added", cancelButtonCallback: {
                                AppDelegate.setRootViewConroller {
                                    
                                }
                            })
                        }
                    }
                })
            }
        }
    }
    
    @IBAction func btnBack_Action(_ sender: UIButton) {
        self.navigationController?.popViewController(animated: true)
    }
}
