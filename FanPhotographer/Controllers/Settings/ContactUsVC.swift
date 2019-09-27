//
//  ContactUsVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/23/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class ContactUsVC: UIViewController {

    @IBOutlet weak var cnsrntBottomShareView: NSLayoutConstraint! // Default 25
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        if Device.iPhone6 {
            cnsrntBottomShareView.constant = 45
        } else if Device.iPhone6p {
            cnsrntBottomShareView.constant = 55
        } else if Device.iPhoneX {
            cnsrntBottomShareView.constant = 70
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    //  MARK:- Action methods
    
    @IBAction func btnHeader_Action(_ sender: UIButton) {
        
    }
    
    @IBAction func btnShare_Action(_ sender: UIButton) {
    }
}
