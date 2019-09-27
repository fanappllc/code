//
//  CreditCardCell.swift
//  FanCustomer
//
//  Created by Codiant on 11/24/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class CreditCardCell: UITableViewCell {
    
    @IBOutlet weak var lblCardNumber: UILabel!
    @IBOutlet weak var lblCardType: UILabel!
    @IBOutlet weak var imgViewCard: UIImageView!
    
    func display(card: Card) {
        lblCardType.text = card.brand
        lblCardNumber.text = "...." + card.last4
        imgViewCard.image = UIImage(named: "UTCardInput.bundle/CardLogo/" + card.brand + "@2x")
    }
}
