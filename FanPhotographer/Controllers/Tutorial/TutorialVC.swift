//
//  TutorialVC.swift
//  FanPhotographer
//
//  Created by Codiant on 11/27/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit
private let kSkipButtonBottomConstraintIdentifier = "kSkipButtonBottomConstraint"
class TutorialVC: UIViewController {
    
    @IBOutlet weak var scrollView: UIScrollView!
    @IBOutlet weak var pageControl: UIPageControl!
    @IBOutlet weak var descriptionLabel: UILabel!
    
    private var headingLabel: UILabel!
    fileprivate let images = ["Tutorial_1", "Tutorial_2", "Tutorial_3"]
    fileprivate let descriptions = ["Snap photos of customers and get paid doing it!", "Your fan photographer will be available for you, to snap up to 75 photographs in 30 minutes, and an additional 50 photos each additional 30 minutes.", "Your photos will be available almost instantly for you to view, download, and share right from your phone."]
    fileprivate let heading = ["The World’s First Selfie Assistant App", "Pose for a shot… or not your choice", "Download and share your photos"]
    
    //  MARK:- Lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
        //self.navigationController?.applyStyling()
        self.configure()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        self.view.viewWithTag(10)?.gradientBackground(from: Color.orange, to: Color.lightOrange , direction: .leftToRight)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.navigationBar.isHidden = true
    }
    
    //  MARK:- Private methods
    private func configure() {
        
        self.headingLabel = UILabel(frame: .zero)
        self.headingLabel.translatesAutoresizingMaskIntoConstraints = false
        self.view.addSubview(self.headingLabel)
        _ = self.headingLabel.constructConstraints(attributes: [(.centerX, 0, self.view, .centerX), (.bottom, ((Device.iPhone6 || Device.iPhone6p) ? -5 : 0), self.descriptionLabel, .top)], setActive: true)
        self.headingLabel.textColor = Color.white
        self.headingLabel.font = UIFont.josefinSans(.regular, size: .custom((Device.iPhone6 || Device.iPhone6p) ? 22 : 20))
        self.headingLabel.text = self.heading.first
        
        let topValue: CGFloat = (Screen.height/2 + ((Device.iPhone6 || Device.iPhone6p || Device.iPhoneX) ? 160 : 75))
        self.descriptionLabel.font = UIFont.quicksand(.regular, size: .custom((Device.iPhone6 || Device.iPhone6p) ? 16 : 14))
        _ = self.descriptionLabel.constructConstraints(attributes: [(.top, topValue, self.view, .top)], setActive: true)
        if Device.iPhone6 || Device.iPhone6p {
            self.view.constraints.filter({ $0.identifier == kSkipButtonBottomConstraintIdentifier }).first?.constant = 35
            //btn.constraints.filter({ $0.firstAttribute == .height }).first?.constant = 40
        }
        
        self.descriptionLabel.text = self.descriptions.first
        self.setUpScrollImages()
    }
    
    private func setUpScrollImages() {
        
        self.pageControl.numberOfPages = self.images.count
        var originX : CGFloat = 0.0
        
        for image in self.images  {
            let imgView = UIImageView(frame: CGRect(x: originX , y: 0, width: Screen.width, height: Screen.height))
            imgView.contentMode = .scaleAspectFill
            imgView.clipsToBounds = true
            imgView.image = UIImage(named: image)
            self.scrollView.addSubview(imgView)
            originX += Screen.width
        }
        
        self.scrollView.contentSize = CGSize(width: Screen.width * CGFloat(self.images.count), height: Screen.height)
        self.view.bringSubview(toFront: self.pageControl)
        self.view.bringSubview(toFront: self.descriptionLabel)
    }
    
    //  MARK:- Action methods
    @IBAction func btnSkip_Action(_ sender: UIButton) {
        if let loginVC = self.storyboard?.viewController(withClass: LoginVC.self) {
            self.push(loginVC)
        }
    }
}

extension TutorialVC: UIScrollViewDelegate {
    
    //  MARK:- Scrollview delegate
    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        
        let xOffset: CGFloat = scrollView.contentOffset.x
        
        if (xOffset < 1.0) {
            self.pageControl.currentPage = 0
        } else if (xOffset < self.view.bounds.width + 1) {
            self.pageControl.currentPage = 1
        } else {
            self.pageControl.currentPage = 2
        }
        
        self.descriptionLabel.text = self.descriptions[self.pageControl.currentPage]
        self.headingLabel.text = self.heading[self.pageControl.currentPage]
    }
}
