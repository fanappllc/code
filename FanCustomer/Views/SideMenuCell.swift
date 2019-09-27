//
//  SideMenuCell.swift
//  FanCustomer
//
//  Created by Codiant on 11/15/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class SideMenuCell: UITableViewCell {

    @IBOutlet weak var lblMenuTitle : UILabel!
    @IBOutlet weak var lblNotificationCount : UILabel!
    @IBOutlet weak var notificationView: UIView!
    @IBOutlet weak var imgMenuTitle : UIImageView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        //notificationView.layer.cornerRadius = 11
        //notificationView.clipsToBounds = true
    }
}
