//
//  LoginVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/11/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
import libPhoneNumber_iOS

class LoginVC: UIViewController {
    @IBOutlet weak var countryCodeLabel: UILabel!
    @IBOutlet weak var txfPhoneNumber: CustomTextField!
    
    private var countryCode: String!

    override func viewDidLoad() {
        super.viewDidLoad()
        txfPhoneNumber.text = ""
        // Do any additional setup after loading the view.
        configure()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        self.txfPhoneNumber.becomeFirstResponder()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Private methods
    private func configure() {
        if let currentCountry = CountryPicker.countryFor(region: Locale.current.regionCode) {
            self.countryCodeLabel.text = currentCountry.phoneCode
            self.countryCode = currentCountry.code
        }
    }
    private func invalidPhoneNumberAlert() {
        self.showAlertWith(message: "Please enter a valid phone number")
    }
    //  MARK:- Action methods
    @IBAction func didPressCountrySelection(_ sender: UIButton) {
        
        if let countryPickerVC = self.storyboard?.viewController(withClass: CountryPickerVC.self) {
            countryPickerVC.onDidSelectCountry = ({ [unowned self] (country) in
                self.countryCodeLabel.text = country.phoneCode
                self.countryCode = country.code
            })
            
            let rootController = UINavigationController(rootViewController: countryPickerVC)
            self.present(rootController, animated: true, completion: nil)
        }
    }
    
    @IBAction func didPressRequestCode(_ sender: UIButton) {
        
        self.view.endEditing(true)
        guard let number = self.txfPhoneNumber.text else {
            invalidPhoneNumberAlert()
            return
        }
        let phoneUtility = NBPhoneNumberUtil()
        do {
            let phoneNumber = try phoneUtility.parse(number, defaultRegion: self.countryCode)
            
            guard phoneUtility.isValidNumber(phoneNumber),let significantNumber = phoneUtility.getNationalSignificantNumber(phoneNumber) else {
                invalidPhoneNumberAlert()
                return
            }
            
            /*
            if let enterSMSCodeVC = self.storyboard?.viewController(withClass: EnterSMSCodeVC.self) {
                sender.loading = false
                enterSMSCodeVC.phoneNumberDetails = (number: significantNumber, countryCode: self.countryCode, dialCode: "+\(phoneNumber.countryCode!)")
                self.push(enterSMSCodeVC)
            }
             */
            //
            FanHUD.show()
            APIComponents.Account.requestCode(phoneNumber: significantNumber, country: countryCode, dialCode: "+\(phoneNumber.countryCode!)", closure: { [unowned self] (success, data, error) in
                FanHUD.hide()
                guard success, error == nil else {
                    if let message = error?.localizedDescription {
                        self.showAlertWith(message: message)
                    }
                    return
                }
                DDLogDebug("\(String(describing: data))")
                if let enterSMSCodeVC = self.storyboard?.viewController(withClass: EnterSMSCodeVC.self) {
                enterSMSCodeVC.phoneNumberDetails = (number: significantNumber, countryCode: self.countryCode, dialCode: "+\(phoneNumber.countryCode!)")
                self.push(enterSMSCodeVC)
             }
           })
        }
//
        catch {
            invalidPhoneNumberAlert()
        }
    }
}
