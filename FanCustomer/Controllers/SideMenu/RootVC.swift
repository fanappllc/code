//
//  RootVC.swift
//  Trackter
//
//  Created by Codiant on 5/15/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class RootVC: AKSideMenu, AKSideMenuDelegate {
    
    // MARK: Lifecycle
    override internal func awakeFromNib() {
        
        self.menuPreferredStatusBarStyle = UIStatusBarStyle.lightContent
        self.contentViewShadowColor      = UIColor.black
        self.contentViewShadowOffset     = CGSize(width: 0, height: 0)
        self.contentViewShadowOpacity    = 0.6
        self.contentViewShadowRadius     = 12
        self.contentViewShadowEnabled    = true
        
        self.contentViewController  = self.storyboard!.instantiateViewController(withIdentifier: "contentViewController") as! UINavigationController
        self.leftMenuViewController = self.storyboard!.instantiateViewController(withIdentifier: "SideMenuVC")
        
        self.delegate = self
    }
    
    override internal func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
    }
    
    // MARK: Memory warning
    override internal func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    // MARK: - <AKSideMenuDelegate>
    
    func sideMenu(_ sideMenu: AKSideMenu, willShowMenuViewController menuViewController: UIViewController) {
        // DDLogDebug("willShowMenuViewController")
    }
    
    func sideMenu(_ sideMenu: AKSideMenu, didShowMenuViewController menuViewController: UIViewController) {
        // DDLogDebug("didShowMenuViewController")
    }
    
    func sideMenu(_ sideMenu: AKSideMenu, willHideMenuViewController menuViewController: UIViewController) {
        // DDLogDebug("willHideMenuViewController")
    }
    
    func sideMenu(_ sideMenu: AKSideMenu, didHideMenuViewController menuViewController: UIViewController) {
        //DDLogDebug("didHideMenuViewController")
    }
}
