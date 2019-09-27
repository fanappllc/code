//
//  PickerView.swift
//  Carwash
//
//  Created by Urvashi Bhagat on 9/19/17.
//  Copyright © 2017 Codiant Software Technologies Pvt ltd. All rights reserved.
//

import UIKit

class PickerView: UIView, UIPickerViewDelegate, UIPickerViewDataSource {
    
    enum PickerType : Int{
        case textPicker
    }
    
    var items = [Any]()
    var pickerType : PickerType = .textPicker
    var pickerHandler : ((PickerType, Any) -> (Void))!
    
    @IBOutlet weak var view_Container_Bottom : NSLayoutConstraint!
    @IBOutlet weak var picker_Text : UIPickerView!
    
    //MARK: Class method to show picker
    class func showPicker(_ type : PickerType, pickerItems : [String]?, handler : @escaping (PickerType, Any) -> (Void)){
        
        let pickerView = UINib(nibName: "PickerView", bundle: nil).instantiate(withOwner: nil, options: nil)[0] as! PickerView
        pickerView.frame = UIScreen.main.bounds
        UIApplication.shared.keyWindow?.addSubview(pickerView)
        
        pickerView.pickerType = type
        pickerView.pickerHandler = handler
        
        switch type {
        case .textPicker:
            pickerView.addPickerItems(pickerItems)
        }
        pickerView.show()
    }
    
    //new
    class func showPickerWithComponents(pickerItems: [[String]], handler: @escaping (PickerType, Any) -> (Void)) {
        
        let pickerView = UINib(nibName: "PickerView", bundle: nil).instantiate(withOwner: nil, options: nil)[0] as! PickerView
        pickerView.frame = UIScreen.main.bounds
        UIApplication.shared.keyWindow?.addSubview(pickerView)
        
        pickerView.pickerType = .textPicker
        pickerView.pickerHandler = handler
        pickerView.updatePickerItemsForComponents(pickerItems)
        pickerView.show()
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
    }
    
    func show(){
        view_Container_Bottom.constant = 0
        self.backgroundColor = .clear
        UIView.animate(withDuration: 0.3) {
            self.layoutIfNeeded()
            self.backgroundColor = UIColor.black.withAlphaComponent(0.5)
        }
    }
    
    func hide(){
        //From Rect
        view_Container_Bottom.constant = -210
        UIView.animate(withDuration: 0.3, animations: {
            self.layoutIfNeeded()
            self.backgroundColor = .clear
            
        }){ isFinished in
            self.removeFromSuperview()
        }
    }
    
    //MARK: Picker Delegate methods
    func addPickerItems(_ item : [String]?){
        self.items = []
        self.items = item!
        picker_Text.reloadAllComponents()
    }
    
    func updatePickerItemsForComponents(_ item : [[String]]) {
        self.items = []
        self.items = item
        picker_Text.reloadAllComponents()
    }
    
    
    //MARK: Text Picker Delegate methods
    func numberOfComponents(in pickerView: UIPickerView) -> Int{
        //new
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int{
        //new
        switch pickerType {
        case .textPicker:
            return items.count
        }
    }
    
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return items[row] as? String
    }
    
    
    //MARK: Event Action Methods
    @IBAction func btnDone_Action(_ sender: UIButton){
        
        if sender.tag == 1 {
            switch pickerType {
            case .textPicker :
                pickerHandler(pickerType, items[picker_Text.selectedRow(inComponent: 0)])
            }
        }
        
        //Remove Picker From SuperView
        hide()
    }
}

