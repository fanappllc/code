//
//  RatingVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/21/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class RatingVC: UIViewController {
    @IBOutlet weak var imgViewProfile: UIImageView!
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var viewRating: HCSStarRatingView!
    @IBOutlet weak var viewRatinginput: HCSStarRatingView!
    @IBOutlet weak var lblHeading: TTTAttributedLabel!
    @IBOutlet weak var lblBottomHeading: TTTAttributedLabel!
    @IBOutlet weak var txvComment: UITextView!
    
    var bookingInfo : PhotographerProfile!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        displayData()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Private methods
    private func displayData() -> Void {
        if let rating = NumberFormatter().number(from: bookingInfo.rating ?? "0.0") {
            viewRating.value =  CGFloat(truncating: rating)
        }
        lblName.text = "\(bookingInfo.fname!) \(bookingInfo.lname!)"
        imgViewProfile.setImage(with: URL(string: bookingInfo.profileUrl)!)

        let left = "How would you rate your "
        let middle = "experience"
        let right = " with us"
        
        lblHeading.setText(left + middle + right) { (attributedString) -> NSMutableAttributedString? in
            attributedString?.setFont(UIFont.josefinSans(.regular, size: .large), color: UIColor.black, onString: left)
            attributedString?.setFont(UIFont.josefinSans(.regular, size: .mediumLarge), color: Color.orange, onString: middle)
            attributedString?.setFont(UIFont.josefinSans(.regular, size: .large), color: UIColor.black, onString: right)
            return attributedString
        }
        let first = "Love "
        let second = "to hear from you, Leave a comment "
        let third = "(optional)"
        
        lblBottomHeading.setText(first + second + third) { (attributedString) -> NSMutableAttributedString? in
            attributedString?.setFont(UIFont.josefinSans(.regular, size: .mediumLarge), color: Color.orange, onString: first)
            attributedString?.setFont(UIFont.josefinSans(.regular, size: .large), color: UIColor.black, onString: second)
            attributedString?.setFont(UIFont.josefinSans(.regular, size: .large), color: UIColor.lightGray, onString: third)
            return attributedString
        }
    }
  
    func isSubmit() -> Bool {
        if viewRatinginput.value == 0 {
            self.showAlertWith(message: "Please select rating")
            return false
        }
//        else if Validator.emptyString(self.txvComment.text) {
//            self.showAlertWith(message: "Please enter your reveiw")
//            return false
//        }
        return true
    }
    
    //  MARK:- Action methods
    @IBAction func btnSubmit_Action(_ sender: UIButton) {
        guard isSubmit() else {
            return
        }
        postRating {
            let alert = UIAlertController(title: "THANK YOU",
                                          message: "For using our services. \n Looking forward to be back soon!",
                                          preferredStyle: .alert)
            alert.addAction(UIAlertAction(title:"Done", style: .default, handler: { (action) -> Void in
                self.navigationController?.popToRootViewController(animated: true)
            }))
            self.present(alert, animated: true, completion: nil)
        }
    }
    
    //  MARK:- API methods
    func postRating(handler : @escaping() -> Void) {
        FanHUD.show()
        let param: HTTPParameters = ["to_user_id" : bookingInfo.photographerId! ,
                                     "order_id"  : bookingInfo.orderId!,
                                     "rating" : "\(viewRatinginput.value)",
            "review" : self.txvComment.text!]
        APIComponents.Customer.rating(parameter: param) { [weak self] (success, data, error) in
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
}
