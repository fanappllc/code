//
//  EditProfileVC.swift
//  FanPhotographer
//
//  Created by Codiant on 11/27/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class EditProfileVC: UIViewController {
    
    @IBOutlet weak var imgViewProfile: UIImageView!
    @IBOutlet weak var scrollView    : UIScrollView!
    @IBOutlet weak var txfFirstName  : CustomTextField!
    @IBOutlet weak var txfLastName   : CustomTextField!
    @IBOutlet weak var txfEmail      : CustomTextField!
    @IBOutlet weak var txfAddress    : CustomTextField!
    @IBOutlet weak var txfZipCode    : CustomTextField!
    @IBOutlet weak var txfMobileNo   : CustomTextField!
    @IBOutlet weak var txfMobileModel   : CustomTextField!
    @IBOutlet weak var btnEditImage : UIButton!
    @IBOutlet weak var btnEditImageSmall : UIButton!
    @IBOutlet weak var btnMenu      : UIButton!
    @IBOutlet weak var btnSubmit    : UIButton!
    @IBOutlet weak var cnstrntTxtfieldLead: NSLayoutConstraint! // Default = 20
    fileprivate var imagePickerController : UIImagePickerController?
    fileprivate var alertController       : UIAlertController?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        configure()
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
    
    //  MARK:- Private methods
    private func configure() {
        
        imgViewProfile.layer.masksToBounds = false
        imgViewProfile.layer.cornerRadius  = imgViewProfile.frame.size.height/2
        imgViewProfile.clipsToBounds = true
        
        self.txfMobileNo.text = LoggedInUser.shared.dialCode + " " + LoggedInUser.shared.phoneNumber
        txfMobileModel.text =  UIDevice.current.modelName
        if LoggedInUser.shared.isRegistered {
            imgViewProfile.setImage(with: URL(string: Profile.shared.photo)!)
            txfFirstName.text   = Profile.shared.firstName
            txfLastName.text    = Profile.shared.lastName
            txfEmail.text       = Profile.shared.email
            txfAddress.text     = Profile.shared.address
            txfZipCode.text     = Profile.shared.zipcode
            btnMenu.setImage(#imageLiteral(resourceName: "menu"), for: .normal)
            setProfileActive(false)
        } else {
            btnMenu.setImage(#imageLiteral(resourceName: "back"), for: .normal)
        }
    }
    
    private func setProfileActive(_ isEditable : Bool) {
        for subView in self.scrollView.subviews as [UIView] {
            if let lblHeader = subView as? UILabel, lblHeader.tag == 10  {
                lblHeader.textColor = isEditable ? UIColor.black : Color.orange
            }
            if subView.tag == 20  {
                subView.isHidden = isEditable
            }
        }
        
        txfFirstName.layer.borderColor  = isEditable ? UIColor.groupTableViewBackground.cgColor : UIColor.white.cgColor
        txfLastName.layer.borderColor   = isEditable ? UIColor.groupTableViewBackground.cgColor : UIColor.white.cgColor
        txfEmail.layer.borderColor      = isEditable ? UIColor.groupTableViewBackground.cgColor : UIColor.white.cgColor
        txfAddress.layer.borderColor    = isEditable ? UIColor.groupTableViewBackground.cgColor : UIColor.white.cgColor
        txfZipCode.layer.borderColor    = isEditable ? UIColor.groupTableViewBackground.cgColor : UIColor.white.cgColor
        txfMobileNo.layer.borderColor   = isEditable ? UIColor.groupTableViewBackground.cgColor : UIColor.white.cgColor
        txfMobileModel.layer.borderColor   = isEditable ? UIColor.groupTableViewBackground.cgColor : UIColor.white.cgColor
        
        txfFirstName.isUserInteractionEnabled   = isEditable
        txfLastName.isUserInteractionEnabled    = isEditable
        txfEmail.isUserInteractionEnabled       = isEditable
        txfAddress.isUserInteractionEnabled     = isEditable
        txfZipCode.isUserInteractionEnabled     = isEditable
        
        btnEditImage.isUserInteractionEnabled = isEditable
        btnEditImageSmall.isHidden = !isEditable
        self.btnSubmit.setTitle(isEditable ? "SUBMIT" : "EDIT", for: .normal)
        cnstrntTxtfieldLead.constant = isEditable ? 20 : 5
    }
    
    private func validated() -> Bool {
        if Validator.emptyString(self.txfFirstName.text) {
            self.showAlertWith(message: "Please enter your first name")
            return false
        } else if Validator.emptyString(self.txfLastName.text) {
            self.showAlertWith(message: "Please enter your last name")
            return false
        } else if !Validator.validEmail(self.txfEmail.text) {
            self.showAlertWith(message: "Please enter valid email address")
            return false
        } else if Validator.emptyString(self.txfAddress.text) {
            self.showAlertWith(message: "Please enter your address")
            return false
        } else if Validator.emptyString(self.txfZipCode.text) {
            self.showAlertWith(message: "Please enter your zip code")
            return false
        }
        return true
    }
    
    //  MARK:- Action methods
    
    @IBAction func selectProfileImage(_ sender: UIButton) {
        
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
    @IBAction func btnMenu_Action(_ sender: UIButton) {
        if LoggedInUser.shared.isRegistered {
            presentLeftMenuViewController(self)
        } else {
            self.navigationController?.popViewController(animated: true)
        }
    }
    @IBAction func btnSubmit_Action(_ sender: UIButton) {
        if sender.titleLabel?.text == "EDIT" {
            setProfileActive(true)
        } else {
            guard validated() else {
                return
            }
            let parameters = ["first_name": txfFirstName.text!,"last_name": txfLastName.text!, "email": txfEmail.text!, "is_terms_condition_accepted": "1", "address": self.txfAddress.text!, "zip_code": txfZipCode.text!, "mobile_model": self.txfMobileModel.text!]
            var images = [String: UIImage]()
            if let profileImage = imgViewProfile.image {
                images["profile_image"] = profileImage
            }
            FanHUD.show()
            APIComponents.Account.updateProfile(parameters: parameters, images: images) { [unowned self] (success, data, error) in
                FanHUD.hide()
                guard success, error == nil else {
                    if let message = error?.localizedDescription {
                        self.showAlertWith(message: message)
                    }
                    return
                }
                
                if let object = data!.deserialize(), let data = object["data"] as? HTTPParameters {
                    Profile.shared.map(JSONObject: data, context: nil)
                    if !LoggedInUser.shared.isRegistered {
                        if let ssnVC = self.storyboard?.viewController(withClass: SSNVerificationVC.self) {
                            self.push(ssnVC)
                        }
                    } else {
                        NotificationCenter.default.post(name: NSNotification.Name(rawValue: "REFRESH_USERINFO"), object: nil)
                        self.showAlertWith(message: "Profile successfully updated")
                        self.setProfileActive(false)
                    }
                }
            }
        }
    }
}

extension EditProfileVC: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    //  MARK:- Image picker controller delegate
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
        if let image = info[UIImagePickerControllerEditedImage] as? UIImage {
            imgViewProfile.image = image
        }
        
        self.dismiss(animated: true, completion: nil)
    }
}

