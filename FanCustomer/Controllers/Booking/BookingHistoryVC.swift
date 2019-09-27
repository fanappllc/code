//
//  BookingHistoryVC.swift
//  FanCustomer
//
//  Created by Codiant on 11/22/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class BookingHistoryVC: UIViewController {
    
   @IBOutlet weak var tblHistory: PlaceholderTableView!
    var bookings = [BookingHistoryData]()
    
    private var currentPage     = 0
    private var totalPageCount  = 0
    private var isFetching      = false
    private var isInitialFetchComplete = false
 
    override func viewDidLoad() {
        super.viewDidLoad()
        tblHistory.tableFooterView = UIView()
        getBookingHistoryList(pageCount: 1)
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        totalPageCount = 0
        currentPage = 0
        isFetching = false
        isInitialFetchComplete = false
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // MARK: UIScrollView delegate
    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        let endScrolling = scrollView.contentOffset.y + scrollView.frame.size.height
        if endScrolling >= scrollView.contentSize.height - 100 {
            if isInitialFetchComplete, !isFetching {
                isFetching = true
                getBookingHistoryList(pageCount: currentPage+1)
            }
        }
    }
    
    //MARK:- Private Methods
    func performBatchUpdate(_ data:[HTTPParameters]) {
        if (data.count) > 0 {
            let lastIndex = bookings.count
            tblHistory.beginUpdates()
            var indexPath = [NSIndexPath]()
            for i in 0...data.count-1 {
                indexPath.append(IndexPath(row: lastIndex+i, section: 0) as NSIndexPath)
            }
            bookings.append(contentsOf: data.map({ BookingHistoryData.map(JSONObject: $0, context: nil) }))
            tblHistory.insertRows(at: indexPath as [IndexPath], with: .top)
            tblHistory.endUpdates()
        }
    }
    
    func getColor(_ book : BookingHistoryData) -> UIColor {
        switch book.orderStatus {
        case .Cancelled :
            return Color.orange
        case .Accepted , .Completed, .Pending, .Proceed:
            return Color.ultraBlue
        default:
            break
        }
        return Color.green
    }

    //MARK:- Webservice Call

    func getBookingHistoryList(pageCount:Int) {
        
        if isInitialFetchComplete, pageCount > totalPageCount {
            return
        }
        if !isInitialFetchComplete {
            FanHUD.show()
        }
    
        APIComponents.Customer.getBookingHistory(page: "\(pageCount)") { [weak self] (success, data, error) in
            guard let strongSelf = self else { return }
            if !strongSelf.isInitialFetchComplete {
                FanHUD.hide()
                strongSelf.isInitialFetchComplete = true
            }
            strongSelf.isFetching = false
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize(), let items = object["items"] as? HTTPParameters, let dataArray = items["data"] as? [HTTPParameters] {
                if let totalPage = items["last_page"] {
                    strongSelf.totalPageCount = totalPage as! Int
                    strongSelf.currentPage += 1
                }
                else {
                    strongSelf.totalPageCount = 0
                    strongSelf.currentPage = 0
                }
                strongSelf.performBatchUpdate(dataArray)
            }
        }
    }
}

//MARK:- UITableView Methods

extension BookingHistoryVC: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return bookings.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "BookingHistoryCell", for: indexPath) as! BookingHistoryCell
        let booking = bookings[indexPath.row]
        cell.lblBookingDate.text = booking.bookingDate.toString(format: .custom("MMM dd, yyyy"))
        cell.lblBookingTime.text = booking.bookingDate.toString(format: .custom("hh:mm a"))
        cell.lblBookingID.text   = "ID: " + booking.bookingId
        cell.lblName.text        = "\(booking.f_name ?? "") \(booking.l_name ?? "")"
        cell.lblAmount.text      = "Total Amount: $" + booking.amount
        cell.lblBookingStatus.text = booking.orderStatus.rawValue
        cell.lblBookingStatus.textColor =  getColor(booking)
        cell.imgViewPG.setImage(with: URL(string: booking.profileUrl)!)
        return cell
    }
    
}
