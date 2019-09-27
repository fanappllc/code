//
//  ComplainVC.swift
//  FanCustomer
//
//  Created by Urvashi Bhagat on 1/18/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import UIKit
import ObjectMapper

class ComplainVC: UIViewController {

    @IBOutlet weak var txfOrderId: CustomTextField!
    @IBOutlet weak var txtViewMsg: KMPlaceholderTextView!
    @IBOutlet weak var btnComplain: UIButton!
    var complains = [ComplainData]()
    var complainId : String!

    override func viewDidLoad() {
        super.viewDidLoad()
        getComplainReasons()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Private method
    fileprivate func isValid() -> Bool {
        if Validator.emptyString(self.txfOrderId.text) {
            self.showAlertWith(message: "Please enter order id")
            return false
        }else if self.complainId == nil {
            self.showAlertWith(message: "Please select complain reason")
            return false
        }else if Validator.emptyString(self.txtViewMsg.text) {
            self.showAlertWith(message: "Please enter message")
            return false
        }
        return true
    }
    
    func emptyFields() {
        self.txfOrderId.text = ""
        self.btnComplain.setTitle("Select your complain", for: .normal)
        self.btnComplain.setTitleColor(Color.gray, for: .normal)
        self.txtViewMsg.placeholder = "Message"
        self.txtViewMsg.text = ""
        self.complainId = nil
    }
    
    func showComplainPicker() {
        
        var complainList = [String]()
        for data in self.complains {
            complainList.append(data.title)
        }
        PickerView.showPicker(.textPicker, pickerItems: complainList) { (type, result) -> (Void) in
            self.btnComplain.setTitle("\(result)", for: .normal)
            self.btnComplain.setTitleColor(UIColor.black, for: .normal)
            for values in  self.complains {
                if values.title == result as? String {
                    self.complainId = values.id
                }
            }
        }
    }
    
    //  MARK:- API method
    func getComplainReasons() {
        FanHUD.show()
        APIComponents.Setting.getComplainReasons{ [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let data = object["data"] as? [HTTPParameters] {
                strongSelf.complains.removeAll()
                strongSelf.complains.append(contentsOf: data.map({ ComplainData.map(JSONObject: $0, context: nil) }))
            }
        }
    }
        
    func postComplain(handler: @escaping() -> Void) {
    FanHUD.show()
    let param: HTTPParameters = ["order_id" : self.txfOrderId.text! ,
                                 "complain_header_id"  : self.complainId!,
                                "description" : self.txtViewMsg.text!]
    
    APIComponents.Setting.addComplain(parameter: param) { [weak self] (success, data, error) in
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
        
    //  MARK:- Action methods
    @IBAction func btnPicker_Action(_ sender: UIButton) {
        view.endEditing(true)
        
        if self.complains.count > 0 {
            showComplainPicker()
            return
        }
    }
    
    @IBAction func btnSubmit_Action(_ sender: UIButton) {
        guard isValid() else { return }
        postComplain {
            let alert = UIAlertController(title: "THANK YOU",
                                          message: "For your feedback.",
                                          preferredStyle: .alert)
            alert.addAction(UIAlertAction(title:"OK", style: .default, handler: { (action) -> Void in
                self.emptyFields()
            }))
            self.present(alert, animated: true, completion: nil)
        }
    }
}
