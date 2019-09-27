//
//  ChatVC.swift
//  FanCustomer
//
//  Created by Urvashi Bhagat on 1/30/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import UIKit
import UserNotifications
class ChatVC: UIViewController, UITextViewDelegate {

    @IBOutlet var tblChatList : UITableView!
    @IBOutlet weak var imgViewProfile: UIImageView!
    @IBOutlet weak var lblName: UILabel!
    @IBOutlet weak var constViewBottom: NSLayoutConstraint!
    @IBOutlet weak var constViewBottomHeight: NSLayoutConstraint!
    @IBOutlet weak var txtMessage: KMPlaceholderTextView!
    
    var chat = [Chat]()
    var bookingInfo : PhotographerProfile!
    var keyboardHeight : CGFloat = 0.0

    override func viewDidLoad() {
        super.viewDidLoad()
        configure()
        NotificationCenter.default.addObserver(self, selector: #selector(self.getMessage), name: NSNotification.Name(rawValue: "NEW_MESSAGE"), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(self.applicationDidBecomeActive), name: NSNotification.Name.UIApplicationDidBecomeActive, object: nil)
    }

    override func viewDidDisappear(_ animated: Bool) {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Notification observer
    @objc func applicationDidBecomeActive() {
        if TimerOperation.bookingStatus == OrderStatus.Proceed.rawValue {
            checkNotifications()
        }
    }
    func checkNotifications()  {
        UNUserNotificationCenter.current().getDeliveredNotifications { notifications in
            DispatchQueue.main.sync { /* or .async {} */
                // update UI
                for notification in notifications {
                    let userInfo = notification.request.content.userInfo
                    if userInfo["type"] as? String == "cancel_session_by_photographer", TimerOperation.bookingStatus == OrderStatus.Proceed.rawValue {
                        self.navigationController?.popViewController(animated: true)
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                            NotificationCenter.default.post(name: NSNotification.Name(rawValue: "ORDER_REQUEST_CANCEL"), object: nil, userInfo: userInfo)
                        }
                    } else if userInfo["type"] as? String == "new_message", TimerOperation.bookingStatus == OrderStatus.Proceed.rawValue {
                        let message = Chat()
                        if let text = userInfo["message"] as? String {
                            message.message = text
                        }
                        if let date = userInfo["created_at"] as? String {
                            let dateFormatter = DateFormatter()
                            dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
                            message.createdDate = dateFormatter.date(from: date)
                        }
                        if let id = userInfo["from_id"] as? Int {
                            message.fromId = "\(id)"
                        }
                        self.chat.append(message)
                        self.tblChatList.beginUpdates()
                        self.tblChatList.insertRows(at: [IndexPath(row: self.chat.count-1, section: 0)], with: .fade)
                        self.tblChatList.endUpdates()
                        self.tableViewScrollToBottom(animated: false)
                    }
                }
            }
            let center = UNUserNotificationCenter.current()
            center.removeAllDeliveredNotifications()
        }
    }
    
    // MARK: - Private
    func configure() {
        tblChatList.estimatedRowHeight = 68
        tblChatList.rowHeight = UITableViewAutomaticDimension

        lblName.text = "\(bookingInfo.fname!) \(bookingInfo.lname!)"
        imgViewProfile.setImage(with: URL(string: bookingInfo.profileUrl)!)
       // self.tableViewScrollToBottom(animated: false)
        getMessageList {
            self.tblChatList.reloadData()
        }
        
        NotificationCenter.default.addObserver(self, selector: #selector(self.keyboardWillChangeFrame(_:)), name: NSNotification.Name.UIKeyboardWillChangeFrame, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(self.keyboardWillHide(_:)), name: NSNotification.Name.UIKeyboardWillHide, object: nil)

    }
    
     @objc func getMessage(_ notification: NSNotification) {
        if let userInfo = notification.userInfo {
            if let notificationType = userInfo["type"] as? String {
                switch notificationType {
                case "new_message" :
                    let message = Chat()
                    if let text = userInfo["message"] as? String {
                        message.message = text
                    }
                    if let date = userInfo["created_at"] as? String {
                        let dateFormatter = DateFormatter()
                        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
                        message.createdDate = dateFormatter.date(from: date)
                    }
                    if let id = userInfo["from_id"] as? Int {
                        message.fromId = "\(id)"
                    }
                    chat.append(message)
                    self.tblChatList.beginUpdates()
                    self.tblChatList.insertRows(at: [IndexPath(row: self.chat.count-1, section: 0)], with: .fade)
                    self.tblChatList.endUpdates()
                    self.tableViewScrollToBottom(animated: false)
                    break
                default:
                    break
                }
            }

          }
    }
    
    private func tableViewScrollToBottom(animated: Bool) {
        
        DispatchQueue.main.async {
            let numberOfSections = self.tblChatList.numberOfSections
            let numberOfRows = self.tblChatList.numberOfRows(inSection: numberOfSections-1)
            if numberOfRows > 0 {
                let indexPath = IndexPath(row: numberOfRows-1, section: (numberOfSections-1))
                self.tblChatList.scrollToRow(at: indexPath, at: .bottom, animated: animated)
            }
        }
    }
  
    private func numberOfLines(textView: UITextView) -> Int {
        let layoutManager = textView.layoutManager
        let numberOfGlyphs = layoutManager.numberOfGlyphs
        var lineRange: NSRange = NSMakeRange(0, 1)
        var index = 0
        var numberOfLines = 0
        
        while index < numberOfGlyphs {
            layoutManager.lineFragmentRect(forGlyphAt: index, effectiveRange: &lineRange)
            index = NSMaxRange(lineRange)
            numberOfLines += 1
        }
        return numberOfLines
    }
    
    // MARK: Keyboard Notifications
    @objc internal func keyboardWillChangeFrame(_ notification: Notification) {
        
        let keyboardSize = ((notification as NSNotification).userInfo![UIKeyboardFrameEndUserInfoKey] as! NSValue).cgRectValue
        constViewBottom.constant = keyboardSize.height
        UIView.animate(withDuration: 0.3) {
            self.view.layoutIfNeeded()
        }
    }
    
    @objc internal func keyboardWillHide(_ notification: Notification) {
        
        self.constViewBottom.constant = 0
        UIView.animate(withDuration: 0.3, animations: {
            self.view.layoutIfNeeded()
        })
    }
    
    // MARK: - IBAction
    @IBAction func btnBack_Action(_ sender: UIButton) {
        navigationController?.popViewController(animated: true)
    }
    @IBAction func btnSendMessage_Action(_ sender: UIButton) {
        view.endEditing(true)
        if self.txtMessage.text != "" {
            let text = self.txtMessage.text.trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)

            guard text.count > 0 else {
                return
            }
        sendMessage {
            let numberOfSections = self.tblChatList.numberOfSections
            let numberOfRows = self.tblChatList.numberOfRows(inSection: numberOfSections-1)
            let indexPath = IndexPath(row: numberOfRows, section: (numberOfSections-1))
            self.tblChatList.insertRows(at: [indexPath], with: .right)
            self.tableViewScrollToBottom(animated: false)
            self.txtMessage.text = ""
          }
        }
    }
    
    //  MARK:- API methods
    func sendMessage(handler: @escaping() -> Void) {
        FanHUD.show()
        let param: HTTPParameters = ["to_id" : bookingInfo.photographerId ,
                                     "order_id"  : bookingInfo.orderId!,
                                     "message" : self.txtMessage.text!]
        
        APIComponents.Customer.sendMessage(parameter: param) { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let data = object["data"] as? HTTPParameters {
                let message = Chat()
                if let text = data["message"] as? String {
                    message.message = text
                }
                if let date = data["created_at"] as? String {
                    let dateFormatter = DateFormatter()
                    dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
                    
                    message.createdDate = dateFormatter.date(from: date)
                }
                if let id = data["from_id"] as? Int {
                    message.fromId = "\(id)"
                }
                strongSelf.chat.append(message)
                print(strongSelf.chat)
                handler()
            }
        }
    }
    
    func getMessageList(handler: @escaping() -> Void) {
        FanHUD.show()
        APIComponents.Customer.getMessageList(orderID: bookingInfo.orderId!) { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let data = object["data"] as? [HTTPParameters] {
                strongSelf.chat.removeAll()
                strongSelf.chat.append(contentsOf: data.map({ Chat.map(JSONObject: $0, context: nil) }))
                handler()
            }
        }
    }
    
    //  MARK:- UITextView methods
    func textViewDidChange(_ textView: UITextView) {
        if txtMessage.text?.count == 1 {
            if txtMessage.text?.first == " " {
                txtMessage.text = ""
                return
            }
        }
        constViewBottomHeight.constant = numberOfLines(textView: txtMessage) < 2 ? 48.0 : 70.0
        UIView.animate(withDuration: 0.3) {
            self.view.layoutIfNeeded()
        }
    }
    
    func textView(_ textView: UITextView, shouldChangeTextIn range: NSRange, replacementText text: String) -> Bool {
        if (text == "\n") {
            txtMessage.text = ""
            textView.resignFirstResponder()
            constViewBottomHeight.constant = numberOfLines(textView: txtMessage) < 2 ? 48.0 : 65.0
            
            return false
        }
        return true
    }
}

extension ChatVC : UITableViewDelegate , UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return chat.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let dict = chat[indexPath.row]
        let cell = tblChatList.dequeueReusableCell(withIdentifier: "ChattingCell") as! ChattingCell
        cell.lblDescription.text = dict.message
        cell.lblTime.text = dict.createdDate.toString(format: .custom("hh:mm a"))
        chat[indexPath.row].opponent = !(chat[indexPath.row].fromId == Profile.shared.id)
        cell.isOpponent = chat[indexPath.row].opponent
        chat[indexPath.row].opponent ? cell.setContraintFromLeft() : cell.setContraintFromRight()
        cell.layoutIfNeeded()
        return cell
    }
    
    internal func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return UITableViewAutomaticDimension
    }
}
