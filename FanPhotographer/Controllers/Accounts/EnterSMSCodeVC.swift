//
//  EnterSMSCodeVC.swift
//  FanPhotographer
//
//  Created by Codiant on 11/27/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
import IQKeyboardManagerSwift

class EnterSMSCodeVC: UIViewController, UITextFieldDelegate {
    
    @IBOutlet weak var titleWithNumberLabel: UILabel!
    @IBOutlet weak var code1: UITextField!
    @IBOutlet weak var code2: UITextField!
    @IBOutlet weak var code3: UITextField!
    @IBOutlet weak var code4: UITextField!
    @IBOutlet weak var btnGetCodeAgain: UIButton!
    @IBOutlet weak var progressBarView: UIProgressView!
    
    private var resendTimer: Timer!
    private var retryCount = 0
    var phoneNumberDetails: (number: String, countryCode: String, dialCode: String)!
    
    
    //  MARK:- Lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
        IQKeyboardManager.sharedManager().disabledToolbarClasses = [EnterSMSCodeVC.self]
        self.configure()
        self.startResendTimer()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        code1.becomeFirstResponder()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        if self.resendTimer != nil {
            self.resendTimer.invalidate()
            self.resendTimer = nil
        }
    }
    
    //  MARK:- Private methods
    private func configure() {
        code1.addTarget(self, action: #selector(EnterSMSCodeVC.textFieldDidChange(_:)), for: UIControlEvents.editingChanged)
        code2.addTarget(self, action: #selector(EnterSMSCodeVC.textFieldDidChange(_:)), for: UIControlEvents.editingChanged)
        code3.addTarget(self, action: #selector(EnterSMSCodeVC.textFieldDidChange(_:)), for: UIControlEvents.editingChanged)
        code4.addTarget(self, action: #selector(EnterSMSCodeVC.textFieldDidChange(_:)), for: UIControlEvents.editingChanged)
        self.titleWithNumberLabel.text = self.titleWithNumberLabel.text! + " " + phoneNumberDetails.dialCode + " " + phoneNumberDetails.number
    }
    
    private func startResendTimer() {
        self.progressBarView.isHidden = false
        self.btnGetCodeAgain.isHidden = true
        self.progressBarView.progress = 0
        self.resendTimer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(updateProgress), userInfo: nil, repeats: true)
    }
    
    @objc func updateProgress() {
        let progress = self.progressBarView.progress + (self.retryCount > 0 ? 1/60.0 : 1/30.0)
        
        UIView.animate(withDuration: 1.5, animations: {
            self.progressBarView.setProgress(progress, animated:true)
        }) { (isFinished) in
            
            if self.progressBarView.progress == 1.0 {
                self.btnGetCodeAgain.isHidden = false
                self.progressBarView.isHidden = true
                
                if self.resendTimer != nil {
                    self.resendTimer.invalidate()
                    self.resendTimer = nil
                }
            }
        }
    }
    
    private func resendCode() {
        self.view.endEditing(true)
        self.btnGetCodeAgain.isHidden = true
        FanHUD.show()
        APIComponents.Account.requestCode(phoneNumber: phoneNumberDetails.number, country: phoneNumberDetails.countryCode , dialCode: "+\(phoneNumberDetails.dialCode)", closure: { [unowned self] (success, data, error) in
            FanHUD.hide()
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    self.showAlertWith(message: message)
                }
                return
            }
            self.startResendTimer()
        })
    }
    
    private func verify(code: String) {
        
        self.view.endEditing(true)
        self.btnGetCodeAgain.isHidden = true
        self.progressBarView.isHidden = true
        
        if self.resendTimer != nil {
            self.resendTimer.invalidate()
            self.resendTimer = nil
        }
    
        FanHUD.show()
        APIComponents.Account.verify(phoneNumber: phoneNumberDetails.number, dialCode: phoneNumberDetails.dialCode, otp: code, device: "ios", deviceToken: LoggedInUser.deviceToken) { [unowned self] (success, data, error) in
            FanHUD.hide()
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    self.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let data = object["data"] as? HTTPParameters {
                LoggedInUser.shared.map(JSONObject: data, context: nil)
                // Call API for get registration status
                self.getProfileDetail()
            }
        }
    }
    
    func getProfileDetail()  {
        FanHUD.show()
        APIComponents.Account.getProfile { [unowned self] (success, data, error) in
            FanHUD.hide()
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    self.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let data = object["data"] as? HTTPParameters {
                if let value = data["is_available"] as? String  {
                    LoggedInUser.PhotographerAvailable  = value == "1"
                }
                
                if LoggedInUser.shared.isRegistered {
                    UserDefaults.standard.set("WaitingApprovalVC", forKey: "VCIdentifier")
                    // Map in Profile
                    Profile.shared.map(JSONObject: data, context: nil)
                    AppDelegate.setRootViewConroller {
                        
                    }
                } else if let value = data["is_registration_fee_paid"] as? String, value == "1" {
                    if let approvalVC = self.storyboard?.viewController(withClass: WaitingApprovalVC.self) {
                        Profile.shared.map(JSONObject: data, context: nil)
                        UserDefaults.standard.set("WaitingApprovalVC", forKey: "VCIdentifier")
                        self.push(approvalVC)
                    }
                } else {
                    if let termsVC = self.storyboard?.viewController(withClass: TermsVC.self) {
                        self.push(termsVC)
                    }
                }
            }
        }
    }

    //  MARK:- Action methods
    @IBAction func btnGetCodeAgain_Action(_ sender: UIButton) {
        self.retryCount += 1
        self.retryCount <= 2 ? self.resendCode() : self.showAlertWith(message: "Maximum retry attempts reached, Please try again later")
    }
    
    @IBAction func btnBack_Action(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
}

extension EnterSMSCodeVC {
    func textFieldDidBeginEditing(_ textField: UITextField) {
        textField.text = ""
    }
    @objc func textFieldDidChange(_ textField: UITextField){
        
        if let string = textField.text, string.count >= 1 {
            switch textField{
            case code1:
                code2.becomeFirstResponder()
            case code2:
                code3.becomeFirstResponder()
            case code3:
                code4.becomeFirstResponder()
            case code4:
                code4.resignFirstResponder()
                checkComplete()
            default:
                break
            }
        }
    }
    
    func checkComplete()  {
        if let code1 = code1.text, code1.count == 1 , let code2 = code2.text, code2.count == 1, let code3 = code3.text, code3.count == 1, let code4 = code4.text, code4.count == 1 {
            
            self.verify(code: code1+code2+code3+code4)
        }
    }
    
    @objc func keyboardInputShouldDelete(_ textField: UITextField) -> Bool {
        let shouldDelete: Bool = true
        if textField.text?.count == 0 && (textField.text == "") {
            let tagValue: Int = textField.tag - 1
            let txtField: UITextField? = (view.viewWithTag(tagValue) as? UITextField)
            txtField?.becomeFirstResponder()
        }
        return shouldDelete
    }
}
