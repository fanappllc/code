//
//  CreditCardListVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/24/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class CreditCardListVC: UIViewController {
    @IBOutlet weak var tblCards: UITableView!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        CardManager.shared.fetchCards { [weak self] in
            guard let strongSelf = self else {
                return
            }
            strongSelf.tblCards.reloadData()
        }
 }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Action methods
    @IBAction func btnAddCrad_Action(_ sender: UIButton) {
        if let addCardVC = self.storyboard?.viewController(withClass: AddCardVC.self) {
            self.push(addCardVC)
        }
    }
}

extension CreditCardListVC: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return CardManager.shared.cards.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "CreditCardCell", for: indexPath) as! CreditCardCell
        let card = CardManager.shared.cards[indexPath.row]
        cell.display(card: card)
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
    }
    
    func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return true
    }
    
    // this method handles row deletion
    func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        
        if editingStyle == .delete {
            if CardManager.shared.cards.count == 1 {
                 self.showAlertWith(message: "Atleast one card required")
                 return
            }
            CardManager.shared.deleteCard(cardId: CardManager.shared.cards[indexPath.row].card_id!, closure: { (success) in
                if success {
                    self.tblCards.reloadData()
                }
            })
        } else if editingStyle == .insert {
            // Not used in our example, but if you were adding a new row, this is where you would do it.
        }
    }
}

