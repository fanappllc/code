//
//  NSLoadingButton.swift
//
//  - Copyright (c) 2017 Codiant. All rights reserved.
//

import UIKit

public enum ActivityIndicatorAlignment: Int {
    case Left
    case Right
    case Center
}

public class NSLoadingButton: UIButton {
    
    let activityIndicatorView = UIActivityIndicatorView(activityIndicatorStyle: .gray)
    
    public var indicatorAlignment:ActivityIndicatorAlignment = .Right {
        didSet {
            setupPositionIndicator()
        }
    }
    
    public var loading:Bool = false {
        didSet {
            realoadView()
        }
    }
    
    public var indicatorColor:UIColor = .gray {
        didSet {
            activityIndicatorView.color = indicatorColor
        }
    }
    
    var topContraints: NSLayoutConstraint?
    var bottomContraints: NSLayoutConstraint?
    var widthContraints: NSLayoutConstraint?
    var rightContraints: NSLayoutConstraint?
    var leftContraints: NSLayoutConstraint?
    var centerXContraints: NSLayoutConstraint?
    var centerYContraints: NSLayoutConstraint?
    
    required  public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setupView()
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupView()
    }
    
    func setupView() {
        activityIndicatorView.hidesWhenStopped = true;
        self.addSubview(activityIndicatorView)
        setupPositionIndicator()
    }
    
    func realoadView() {
        
        if loading {
            self.isEnabled = false
            activityIndicatorView.isHidden = false;
            activityIndicatorView.startAnimating()
        }
        else {
            self.isEnabled = true
            activityIndicatorView.stopAnimating()
        }
    }
    
    func setupPositionIndicator() {
        
        activityIndicatorView.translatesAutoresizingMaskIntoConstraints = false
        
        if topContraints == nil {
            topContraints = NSLayoutConstraint(item: activityIndicatorView, attribute:
                .top, relatedBy: .equal, toItem: self,
                      attribute: NSLayoutAttribute.top, multiplier: 1.0,
                      constant: 0)
        }
        
        if bottomContraints == nil{
            bottomContraints = NSLayoutConstraint(item: activityIndicatorView, attribute:
                .bottom, relatedBy: .equal, toItem: self,
                         attribute: NSLayoutAttribute.bottom, multiplier: 1.0,
                         constant: 0)
        }
        
        if widthContraints == nil {
            widthContraints = NSLayoutConstraint(item: activityIndicatorView, attribute:
                .width, relatedBy: .equal, toItem: nil,
                        attribute: .width, multiplier: 1.0,
                        constant: 30)
        }
        
        if rightContraints == nil {
            rightContraints = NSLayoutConstraint(item: activityIndicatorView, attribute:
                .trailingMargin, relatedBy: .equal, toItem: self,
                                 attribute: .trailingMargin, multiplier: 1.0,
                                 constant: -10)
        }
        
        if leftContraints == nil {
            leftContraints = NSLayoutConstraint(item: activityIndicatorView, attribute:
                .leading, relatedBy: .equal, toItem: self,
                          attribute: .leading, multiplier: 1.0,
                          constant: 10)
        }
        
        if centerXContraints == nil {
            centerXContraints = NSLayoutConstraint(item: activityIndicatorView, attribute:
                .centerX, relatedBy: .equal, toItem: self,
                          attribute: .centerX, multiplier: 1.0,
                          constant: 0)
        }
        
        if centerYContraints == nil {
            centerYContraints = NSLayoutConstraint(item: activityIndicatorView, attribute:
                .centerY, relatedBy: .equal, toItem: self,
                          attribute: .centerY, multiplier: 1.0,
                          constant: 0)
        }
        
        switch indicatorAlignment {
        case .Right:
            NSLayoutConstraint.deactivate([leftContraints!])
            NSLayoutConstraint.activate([topContraints!,rightContraints!,widthContraints!,bottomContraints!])
            
        case .Left:
            NSLayoutConstraint.deactivate([rightContraints!])
            NSLayoutConstraint.activate([topContraints!,leftContraints!,widthContraints!,bottomContraints!])
            
        case .Center:
            NSLayoutConstraint.deactivate([rightContraints!, leftContraints!, topContraints!])
            NSLayoutConstraint.activate([centerXContraints!,centerYContraints!,widthContraints!,bottomContraints!])
        }
    }
    
    deinit {
        activityIndicatorView.removeFromSuperview()
    }
    
}
