//
//  FaqVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/23/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class FaqVC: UIViewController, UIWebViewDelegate {
    @IBOutlet weak var webView: UIWebView!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        getFAQ()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Private methods
    func getFAQ() {
        FanHUD.show()
        APIComponents.Setting.getFAQ { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let data = object["data"] as? HTTPParameters {
                if let value = data["url"] as? String {
                    let url = NSURL (string: value)
                    strongSelf.webView.loadRequest(URLRequest(url: url! as URL))
                }
            }
        }
    }
    
    // MARK: WebView delegate
    func webViewDidStartLoad(_ webView: UIWebView) {
        activityIndicator.startAnimating()
    }
    
    func webViewDidFinishLoad(_ webView: UIWebView) {
        activityIndicator.stopAnimating()
    }
    
    func webView(_ webView: UIWebView, didFailLoadWithError error: Error) {
        self.showAlertWith(message: "Something went wrong!")
    }
}

/*
class FaqVC: UIViewController {
    var faqData: [FaqData?]?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        faqData = getFaqData()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Private methods
    
    private func getFaqData() -> [FaqData?] {
        
        let answer = [AnswerData(answer : "You can easily update WhatsApp from your phone's applications store. \n Note: If you received a message, but your version of WhatsApp doesn't support it, please update WhatsApp.")]
        let question = FaqData(question : "Updating WhatsApp", answer : answer)
        
        let answer1 = [AnswerData(answer : "WhatsApp requires an active phone number to create an account. If you are having problems, please check the following: \n You have the latest version of WhatsApp installed from the App Store.")]
        let question1 = FaqData(question : "Verifying your number", answer : answer1)
        
        let answer2 = [AnswerData(answer : "Two-step verification is an optional feature that adds more security to your account. When you have two-step verification enabled, any attempt to verify your phone number on WhatsApp must be accompanied by the six-digit PIN that you created using this feature.")]
        let question2 = FaqData(question : "Two-Step Verification", answer : answer2)
        
        let answer3 = [AnswerData(answer : "Your WhatsApp account can only be verified with one number on one device. If you have a dual SIM phone, please note that you still must choose one number to verify with WhatsApp. There is no option to have a WhatsApp account with two phone numbers.")]
        let question3 = FaqData(question : "Using one WhatsApp account on multiple phones, or with multiple phone numbers", answer : answer3)
        
        let answer4 = [AnswerData(answer : "You can delete your current photo, take a new photo with your camera or choose a photo from your albums.")]
        let question4 = FaqData(question : "Managing your profile", answer : answer4)
        
        let answer5 = [AnswerData(answer : "WhatsApp is available in over 40 languages (up to 60 on Android). As a general rule, WhatsApp follows the language of your phone. If you change the language of your phone to Spanish, WhatsApp will automatically be in Spanish.")]
        let question5 = FaqData(question : "Changing WhatsApp's language", answer : answer5)
        
        let answer6 = [AnswerData(answer : "The Live Location feature allows you to share your real-time location for a specific amount of time. You can choose to share your live location with the participants of a group chat or with just one contact via an individual chat. You need to turn on Location Services and allow WhatsApp location access in your phone's privacy settings.")]
        let question6 = FaqData(question : "Using Live Location", answer : answer6)
        
        let answer7 = [AnswerData(answer : "WhatsApp uses your phone's Internet connection (4G/3G/2G/EDGE or Wi-Fi, as available) to send and receive messages to your friends and family. You do not have to pay for every message. As long as you have not exceeded your data limit or you are connected to a free Wi-Fi network, your carrier should not charge you extra for messaging over WhatsApp.")]
        let question7 = FaqData(question : "Is it free to send messages over WhatsApp", answer : answer7)
        
        let answer8 = [AnswerData(answer : "WhatsApp quickly and easily recognizes which of your contacts are using WhatsApp by accessing your phone's address book. To delete a contact, simply delete their phone number from your phone's address book: \n Open WhatsApp and go to the Chats tab.")]
        let question8 = FaqData(question : "Deleting contacts", answer : answer8)
        
        let answer9 = [AnswerData(answer : "WhatsApp Calling lets you call your friends and family using WhatsApp for free, even if they’re in another country. Currently, WhatsApp Calling is available on iPhone (on iOS 7 and later), Windows Phone, Android and BlackBerry 10.")]
        let question9 = FaqData(question : "Making Voice Calls", answer : answer9)
        
        let answer10 = [AnswerData(answer : "Any WhatsApp user to see your read receipts, last seen, about and profile photo.")]
        let question10 = FaqData(question : "Configuring your privacy settings", answer : answer10)
        
        return [question, question1, question2, question3, question4, question5, question6, question7, question8, question9, question10]
    }
}

extension FaqVC: UITableViewDataSource, UITableViewDelegate {
    /*  Number of Rows  */
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if let data = faqData {
            return data.count
        } else {
            return 0
        }
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }
    
    /*  Create Cells    */
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        // Row is DefaultCell
        if let rowData = faqData?[indexPath.row] {
            let defaultCell = tableView.dequeueReusableCell(withIdentifier: "FaqCell", for: indexPath) as! FaqCell
            defaultCell.labelQuestion.text = rowData.question
            defaultCell.imgViewExpand.image = UIImage(named: "down_arrow")
            defaultCell.selectionStyle = .none
            return defaultCell
        }
            // Row is ExpansionCell
        else {
            if let rowData = faqData?[getParentCellIndex(expansionIndex: indexPath.row)] {
                
                //  Create an ExpansionCell
                let expansionCell = tableView.dequeueReusableCell(withIdentifier: "ExpansionCell", for: indexPath) as! ExpansionCell
                
                //  Get the index of the parent Cell (containing the data)
                let parentCellIndex = getParentCellIndex(expansionIndex: indexPath.row)
                
                //  Get the index of the flight data (e.g. if there are multiple ExpansionCells)
                let flightIndex = indexPath.row - parentCellIndex - 1
                
                //  Set the cell's data
                expansionCell.labelAnswer.text = rowData.answers?[flightIndex].answer
                
                expansionCell.selectionStyle = .none
                return expansionCell
            }
        }
        return UITableViewCell()
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if (faqData?[indexPath.row]) != nil {
            
            // If user clicked last cell, do not try to access cell+1 (out of range)
            if(indexPath.row + 1 >= (faqData?.count)!) {
                expandCell(tableView: tableView, index: indexPath.row)
            }
            else {
                // If next cell is not nil, then cell is not expanded
                if(faqData?[indexPath.row+1] != nil) {
                    expandCell(tableView: tableView, index: indexPath.row)
                    // Close Cell (remove ExpansionCells)
                } else {
                    contractCell(tableView: tableView, index: indexPath.row)
                }
            }
        }
    }
    
    /*  Expand cell at given index  */
    private func expandCell(tableView: UITableView, index: Int) {
        // Expand Cell (add ExpansionCells
        if let flights = faqData?[index]?.answers {
            for i in 1...flights.count {
                faqData?.insert(nil, at: index + i)
                tableView.insertRows(at: [NSIndexPath(row: index + i, section: 0) as IndexPath] , with: .top)
                let questionCell = tableView.cellForRow(at: NSIndexPath.init(row: index, section: 0) as IndexPath) as? FaqCell
                if questionCell != nil {
                    questionCell?.imgViewExpand.image = UIImage(named: "up_arrow")
                }
            }
        }
    }
    
    /*  Contract cell at given index    */
    private func contractCell(tableView: UITableView, index: Int) {
        if let answer = faqData?[index]?.answers {
            for _ in 1...answer.count {
                faqData?.remove(at: index+1)
                tableView.deleteRows(at: [NSIndexPath(row: index+1, section: 0) as IndexPath], with: .top)
                let questionCell = tableView.cellForRow(at: NSIndexPath.init(row: index, section: 0) as IndexPath) as? FaqCell
                if questionCell != nil {
                    questionCell?.imgViewExpand.image = UIImage(named: "down_arrow")
                }
            }
        }
    }
    
    /*  Get parent cell index for selected ExpansionCell  */
    private func getParentCellIndex(expansionIndex: Int) -> Int {
        
        var selectedCell: FaqData?
        var selectedCellIndex = expansionIndex
        
        while(selectedCell == nil && selectedCellIndex >= 0) {
            selectedCellIndex -= 1
            selectedCell = faqData?[selectedCellIndex]
        }
        
        return selectedCellIndex
    }
}
*/
