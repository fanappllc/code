//
//  FaqCell.swift
//  FanCustomer
//
//  Created by Codiant on 11/23/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
protocol FaqCellDelegate {
    func moreTapped(cell: FaqCell)
}

class FaqCell: UITableViewCell {

    @IBOutlet weak var labelQuestion: UILabel!
    @IBOutlet weak var imgViewExpand: UIImageView!

}
