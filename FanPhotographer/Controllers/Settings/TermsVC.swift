//
//  TermsVC.swift
//  FanPhotographer
//
//  Created by Codiant on 11/27/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class TermsVC: UIViewController, UIWebViewDelegate, UIScrollViewDelegate {
    @IBOutlet weak var webView   : UIWebView!
    @IBOutlet weak var viewAgree : UIView!
    @IBOutlet weak var btnAgree  : UIButton!
    @IBOutlet weak var btnMenu   : UIButton!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    @IBOutlet weak var cnstrntBottomViewHeight: NSLayoutConstraint! // Default = 85
    var phoneNumberDetails: (number: String, countryCode: String, dialCode: String)!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //configure()
    }
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        getTerms()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Private methods
    private func configure() {
        // Load Local HTML 
        do {
            guard let filePath = Bundle.main.path(forResource: "terms-and-condition", ofType: "html")
                else {
                    // File Error
                    DDLogDebug ("File reading error")
                    return
            }
            
            let contents =  try String(contentsOfFile: filePath, encoding: .utf8)
            let baseUrl = URL(fileURLWithPath: filePath)
            webView.loadHTMLString(contents as String, baseURL: baseUrl)
            // if let url = Bundle.main.url(forResource: "terms-and-condition", withExtension: "html") {
            //     webview.loadRequest(URLRequest(url: url))
            // }
        }
        catch {
            DDLogDebug ("File HTML error")
        }
    }
    
    func getTerms() {
        FanHUD.show()
        APIComponents.Setting.getTerms { [weak self] (success, data, error) in
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
                    strongSelf.btnMenu.isHidden = false
                    if LoggedInUser.shared.isRegistered {
                        strongSelf.btnMenu.setImage(#imageLiteral(resourceName: "menu"), for: .normal)
                        strongSelf.view.layoutIfNeeded()
                    } else {
                        strongSelf.btnMenu.setImage(#imageLiteral(resourceName: "back"), for: .normal)
                        strongSelf.cnstrntBottomViewHeight.constant = 85
                    }
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
    
    //  MARK:- Action methods
    @IBAction func btnMenu_Action(_ sender: UIButton) {
        if LoggedInUser.shared.isRegistered {
            presentLeftMenuViewController(self)
        } else {
            let viewControllers: [UIViewController] = self.navigationController!.viewControllers
            for aViewController in viewControllers {
                if aViewController is LoginVC {
                    self.navigationController!.popToViewController(aViewController, animated: true)
                }
            }
        }
    }
    @IBAction func btnAgree_Action(_ sender: UIButton) {
        btnAgree.isSelected = !btnAgree.isSelected
    }
    @IBAction func btnContinue_Action(_ sender: UIButton) {
        if !btnAgree.isSelected {
            self.showAlertWith(message: "Please accept terms & conditions")
            return
        }
        if let editProfileVC = self.storyboard?.viewController(withClass: EditProfileVC.self) {
            self.push(editProfileVC)
        }
    }
}
