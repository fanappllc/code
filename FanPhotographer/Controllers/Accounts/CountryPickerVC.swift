//
//  CountryPickerVC.swift
//  FanPhotographer
//
//  Created by Codiant on 11/27/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class CountryPickerVC: UITableViewController {
    
    var onDidSelectCountry: ((Country) -> Void)?
    
    
    //  MARK:- Lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.title = "Pick Country"
    }
    
    //  MARK:- Action methods
    @IBAction func didPressClose(_ sender: UIBarButtonItem) {
        self.dismiss(animated: true, completion: nil)
    }
}

extension CountryPickerVC {
    
    //  MARK:- Table view data source
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return CountryPicker.countryNamesByCode().count
    }
    
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Cell", for: indexPath) as! CountryPickerCell
        
        // Configure the cell...
        let country = CountryPicker.countryNamesByCode()[indexPath.row]
        
        cell.countryNameLabel.text = country.name
        cell.dialCodeLabel.text = country.phoneCode
        cell.flagImageView.image = country.flag
        
        return cell
        
    }
    
    //  MARK:- Table view delegate
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        if let callback = self.onDidSelectCountry {
            callback(CountryPicker.countryNamesByCode()[indexPath.row])
        }
        
        self.dismiss(animated: true, completion: nil)
    }
    
}
