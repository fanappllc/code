//
//  PhotographerCell.swift
//  FanCustomer
//
//  Created by Codiant on 12/12/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class PhotographerCell: UITableViewCell {

    @IBOutlet weak var pgName: UILabel!
    func configureForPhotographer(_ photographer: Photographer) {
        pgName.text = photographer.photographerName + " Photos"
    }
}
