//
//  IDVerificationVC.swift
//  FanPhotographer
//
//  Created by Codiant on 11/28/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class IDVerificationVC: UIViewController {
    fileprivate var imagePickerController : UIImagePickerController?
    fileprivate var alertController       : UIAlertController?
    @IBOutlet weak var imgViewLicense: UIImageView!
    @IBOutlet weak var btnBrowseImage: UIButton!
    @IBOutlet weak var btnChangeImage: UIButton!
    @IBOutlet weak var lblInfo: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if self.imagePickerController == nil {
            self.imagePickerController = UIImagePickerController()
            self.imagePickerController!.delegate = self
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Action methods
    @IBAction func btnBack_Action(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
    @IBAction func btnContinue_Action(_ sender: UIButton) {
        guard !self.btnChangeImage.isHidden else {
            self.showAlertWith(message: "Please pick driving license image")
            return
        }
        var images = [String: UIImage]()
        if let profileImage = imgViewLicense.image {
            images["driving_licence_image"] = profileImage
        }
        FanHUD.show()
        APIComponents.Account.updateLicenseImage(images: images) { [unowned self] (success, data, error) in
            FanHUD.hide()
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    self.showAlertWith(message: message)
                }
                return
            }
            if let bankAccountVC = self.storyboard?.viewController(withClass: BankAccountDetailVC.self) {
                self.push(bankAccountVC)
            }
        }
    }
    
    @IBAction func btnSelectLicenseImage_Action(_ sender: UIButton) {
        guard self.alertController == nil else {
            self.present(self.alertController!, animated: true, completion: nil)
            return
        }
        
        let closure = { (action: UIAlertAction!) -> Void in
            
            let camera = action.title == "Camera"
            
            self.imagePickerController!.sourceType = camera ? .camera : .photoLibrary
            self.imagePickerController!.allowsEditing = true
            if camera { self.imagePickerController!.cameraCaptureMode = .photo }
            self.present(self.imagePickerController!, animated: true, completion: nil)
        }
        
        self.alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        
        self.alertController!.addAction(UIAlertAction(title: "Camera", style: .default, handler: closure))
        self.alertController!.addAction(UIAlertAction(title: "Photos", style: .default, handler: closure))
        self.alertController!.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        
        self.present(self.alertController!, animated: true, completion: nil)
    }
}

extension IDVerificationVC: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    //  MARK:- Image picker controller delegate
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
        if let image = info[UIImagePickerControllerEditedImage] as? UIImage {
            self.imgViewLicense.image = image
            if self.btnChangeImage.isHidden {
                self.btnChangeImage.isHidden = false
                self.btnBrowseImage.isHidden = true
                self.lblInfo.isHidden        = true
            }
        }
        self.dismiss(animated: true, completion: nil)
    }
}
