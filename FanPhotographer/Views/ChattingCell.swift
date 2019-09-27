//
//  ChattingCell.swift
//  FanCustomer
//
//  Created by Urvashi Bhagat on 1/30/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import UIKit

class ChattingCell: UITableViewCell {

    @IBOutlet weak var lblDescription: UILabel!
    @IBOutlet weak var lblTime: UILabel!
    @IBOutlet weak var viewContainer: UIView!
    
    @IBOutlet weak var constViewContainerGreaterLeading: NSLayoutConstraint!
    @IBOutlet weak var constViewContainerLeading: NSLayoutConstraint!
    @IBOutlet weak var constViewContainerGreaterTrailing: NSLayoutConstraint!
    @IBOutlet weak var constViewContainerTrailing: NSLayoutConstraint!
    
    var isOpponent: Bool!

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }

    override func layoutIfNeeded() {
        super.layoutIfNeeded()
        viewContainer.backgroundColor = isOpponent ? Color.green : Color.purple
        viewContainer.roundCorners(isOpponent ? [.topLeft, .topRight, .bottomRight] : [.topLeft, .topRight, .bottomLeft], radius: 10.0)
    }
    
    internal func setContraintFromLeft() {
        constViewContainerGreaterLeading.priority = UILayoutPriority(rawValue: 250)
        constViewContainerLeading.priority = UILayoutPriority(rawValue: 999)
        constViewContainerGreaterTrailing.priority = UILayoutPriority(rawValue: 999)
        constViewContainerTrailing.priority = UILayoutPriority(rawValue: 250)
    }
    
    internal func setContraintFromRight() {
        constViewContainerGreaterLeading.priority = UILayoutPriority(rawValue: 999)
        constViewContainerLeading.priority = UILayoutPriority(rawValue: 250)
        constViewContainerGreaterTrailing.priority = UILayoutPriority(rawValue: 250)
        constViewContainerTrailing.priority = UILayoutPriority(rawValue: 999)
    }
}
