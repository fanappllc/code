//
//  MRGradintView.swift
//  FanCustomer
//
//  Created by Codiant on 11/16/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

public class MRGradintView: UIView {
    let gradient = CAGradientLayer()
    enum GradientDirection:Int {
        case leftToRight
        case rightToLeft
        case topToBottom
        case bottomToTop
    }
    // Programmatically: use the enum
    var direction:GradientDirection = .leftToRight
    var internalColorAlpha: CGFloat = 1.0

    @IBInspectable var colorAlpha: CGFloat {
        set(newValue) {
            internalColorAlpha = newValue
            setGradientLayer()
        }
        get {
            return internalColorAlpha
        }
    }
    
    // IB: use the adapter
    @IBInspectable var shapeAdapter:Int {
        get {
            return self.direction.rawValue
        }
        set( shapeIndex) {
            self.direction = GradientDirection(rawValue: shapeIndex) ?? .leftToRight
            
        }
    }
    
    override open class var layerClass: AnyClass {
        return CAGradientLayer.classForCoder()
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    private func setGradientLayer() {
        //let gradient = self.layer as! CAGradientLayer
        gradient.frame = self.bounds
        gradient.colors = [Color.ultraBlue.withAlphaComponent(internalColorAlpha).cgColor, Color.celrianBlue.withAlphaComponent(internalColorAlpha).cgColor]
        
        switch direction {
        case .leftToRight:
            gradient.startPoint = CGPoint(x: 0.0, y: 0.5)
            gradient.endPoint = CGPoint(x: 1.0, y: 0.5)
        case .rightToLeft:
            gradient.startPoint = CGPoint(x: 1.0, y: 0.5)
            gradient.endPoint = CGPoint(x: 0.0, y: 0.5)
        case .topToBottom:
            gradient.startPoint = CGPoint(x: 0.5, y: 0.0)
            gradient.endPoint = CGPoint(x: 0.5, y: 1.0)
        case .bottomToTop:
            gradient.startPoint = CGPoint(x: 0.5, y: 1.0)
            gradient.endPoint = CGPoint(x: 0.5, y: 0.0)
        }
        self.layer.insertSublayer(gradient, at: 0)
        
    }
    override public func layoutSubviews() {
        super.layoutSubviews()
        if Device.iPhoneX {
            self.constraints.filter({ $0.firstAttribute == .height }).first?.constant = 84
            self.layoutIfNeeded()
        }
        gradient.frame = self.bounds
    }
}
public class MRGradintButton: UIButton {
   
    let gradient = CAGradientLayer()
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setGradientLayer()
    }
    
    override public func layoutSubviews() {
        super.layoutSubviews()
        if Device.iPhoneX || Device.iPhone6 || Device.iPhone6p {
            self.constraints.filter({ $0.firstAttribute == .height }).first?.constant = 45
            self.layoutIfNeeded()
        }
        gradient.frame = self.bounds
    }
    
    private func setGradientLayer() {
        gradient.frame = self.bounds
        gradient.colors = [Color.ultraBlue.cgColor, Color.celrianBlue.cgColor]
        gradient.startPoint = CGPoint(x: 0.0, y: 0.5)
        gradient.endPoint = CGPoint(x: 1.0, y: 0.5)
        self.layer.insertSublayer(gradient, at: 0)
    }
}
