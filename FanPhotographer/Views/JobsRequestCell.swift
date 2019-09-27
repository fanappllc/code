//
//  JobsRequestCell.swift
//  FanPhotographer
//
//  Created by Codiant on 11/30/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
protocol JobProcessDelegate {
    func cellButtonActionTapped(at index:IndexPath, isAccept:Bool)
}

class JobsRequestCell: UITableViewCell {
    var delegate:JobProcessDelegate!
    var indexPath:IndexPath!
    @IBOutlet weak var imgViewUser: UIImageView!
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var lblAddress: UILabel!
    @IBOutlet weak var lblDuration: UILabel!
    @IBOutlet weak var lblETA: UILabel!
    
    
    @IBAction func cellButton_Action(_ sender: UIButton) {
        self.delegate.cellButtonActionTapped(at: indexPath, isAccept: sender.tag == 100 ? true : false)
    }
}
