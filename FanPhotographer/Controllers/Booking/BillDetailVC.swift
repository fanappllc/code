//
//  BillDetailVC.swift
//  FanPhotographer
//
//  Created by Codiant on 11/30/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class BillDetailVC: UIViewController {
    
    @IBOutlet weak var imgViewProfile: UIImageView!
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var lblAddress: UILabel!
    @IBOutlet weak var lblDateTime: UILabel!
    @IBOutlet weak var lblDuration: UILabel!
    @IBOutlet weak var lblTotalAmount: UILabel!
    var order = Order()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        TimerOperation.shared.resetTime()
        TimerOperation.bookingStatus = OrderStatus.Completed.rawValue
        
        imgViewProfile.setImage(with: URL(string: order.profileUrl)!)
        lblDateTime.text = Date().toString(format: .custom("MMM d, yyyy hh:mm a"))
        lblAddress.text  = order.address
        lblName.text = order.f_name + " " + order.l_name
        lblDuration.text = TimeInterval(order.duration)?.getMinutesIntoHoursDay()
        lblTotalAmount.text = order.amount.currencyInputFormatting()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Action methods
    @IBAction func btnDone_Action(_ sender: UIButton) {
       self.navigationController?.popToRootViewController(animated: true)
    }
}
