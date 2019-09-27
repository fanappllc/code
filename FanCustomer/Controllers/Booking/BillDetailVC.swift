//
//  BillDetailVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/21/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class BillDetailVC: UIViewController {
    @IBOutlet weak var imgViewProfile: UIImageView!
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var lblDateTime: UILabel!
    @IBOutlet weak var lblDuration: UILabel!
    @IBOutlet weak var lblCardNumber: UILabel!
    @IBOutlet weak var lblTotalAmount: UILabel!
    @IBOutlet weak var viewRating: HCSStarRatingView!
    
    var bookingInfo : PhotographerProfile!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        displayData()
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Private methods
    
    private func displayData() -> Void {
        TimerOperation.shared.resetTime()
        TimerOperation.bookingStatus = OrderStatus.Completed.rawValue
        
        if let rating = NumberFormatter().number(from: bookingInfo.rating ?? "0.0") {
            viewRating.value =  CGFloat(truncating: rating)
        }
        lblName.text = "\(bookingInfo.fname!) \(bookingInfo.lname!)"
        imgViewProfile.setImage(with: URL(string: bookingInfo.profileUrl)!)
        lblDateTime.text = Date().toString(format: .custom("MMM d, yyyy hh:mm a"))
        lblDuration.text = TimeInterval(bookingInfo.slotTime)?.getMinutesIntoHoursDay()
        //lblTotalAmount.text = "$\(self.bookingInfo.price!)"
        lblTotalAmount.text = self.bookingInfo.price.currencyInputFormatting()
        lblCardNumber.text = "**** **** **** " + bookingInfo.lastdigit
    }
    
    //  MARK:- Action methods
    @IBAction func btnDone_Action(_ sender: UIButton) {
        if let ratingVC = self.storyboard?.viewController(withClass: RatingVC.self) {
            ratingVC.bookingInfo = self.bookingInfo
            self.push(ratingVC)
        }
    }
}
