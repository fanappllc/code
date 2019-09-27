//
//  FanHUD.swift
//  FanCustomer
//
//  Created by Codiant on 12/19/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import Foundation
import UIKit
import QuartzCore
import CoreGraphics

let loaderSpinnerMarginSide : CGFloat = 20.0
let loaderSpinnerMarginTop : CGFloat = 20.0
let loaderTitleMargin : CGFloat = 5.0

open class FanHUD: UIView {
    
    fileprivate var coverView : UIView?
    fileprivate var titleLabel : UILabel?
    fileprivate var loadingView : SwiftLoadingView?
    fileprivate var animated : Bool = true
    fileprivate var canUpdated = false
    fileprivate var title: String?
    fileprivate var speed = 1
    
    fileprivate var config : Config = Config() {
        didSet {
            self.loadingView?.config = config
        }
    }
    
    @objc fileprivate func rotated(_ notification: Notification) {
        
        let loader = FanHUD.sharedInstance
        
        let height : CGFloat = UIScreen.main.bounds.size.height
        let width : CGFloat = UIScreen.main.bounds.size.width
        let center : CGPoint = CGPoint(x: width / 2.0, y: height / 2.0)
        
        loader.center = center
        loader.coverView?.frame = UIScreen.main.bounds
    }
    
    override open var frame : CGRect {
        didSet {
            self.update()
        }
    }
    
    class var sharedInstance: FanHUD {
        struct Singleton {
            static let instance = FanHUD(frame: CGRect(origin: CGPoint(x: 0,y: 0),size: CGSize(width: Config().size,height: Config().size)))
        }
        return Singleton.instance
    }
    
//    open class func show(animated: Bool) {
//        self.show(animated: animated)
//    }
//    
    open class func show(_ title: String = "") {
        
        let currentWindow : UIWindow = UIApplication.shared.keyWindow!
        
        let loader = FanHUD.sharedInstance
        loader.tag = 1500
        loader.canUpdated = true
        loader.animated = true
        loader.title = title
        loader.update()
        
        
        
        NotificationCenter.default.addObserver(loader, selector: #selector(loader.rotated(_:)),
                                               name: NSNotification.Name.UIDeviceOrientationDidChange,
                                               object: nil)
        
        let height : CGFloat = UIScreen.main.bounds.size.height
        let width : CGFloat = UIScreen.main.bounds.size.width
        let center : CGPoint = CGPoint(x: width / 2.0, y: height / 2.0)
        
        loader.center = center
        
        if (loader.superview == nil) {
            loader.coverView = UIView(frame: currentWindow.bounds)
            loader.coverView?.backgroundColor = loader.config.foregroundColor.withAlphaComponent(loader.config.foregroundAlpha)
            
            currentWindow.addSubview(loader.coverView!)
            currentWindow.addSubview(loader)
            loader.start()
        }
    }
    
    open class func hide() {
        
        let loader = FanHUD.sharedInstance
        NotificationCenter.default.removeObserver(loader)
        
        loader.stop()
    }
    
    public class func setConfig(config : Config) {
        let loader = FanHUD.sharedInstance
        loader.config = config
        loader.frame = CGRect(origin: CGPoint(x: 0, y: 0),size: CGSize(width: loader.config.size, height: loader.config.size))
    }
    
    /**
     Private methods
     */
    
    private func setup() {
        self.alpha = 0
        self.update()
    }
    
    func start() {
        self.loadingView?.start()
        
        if (self.animated) {
            UIView.animate(withDuration: 0.3, animations: { () -> Void in
                self.alpha = 1
            }, completion: { (finished) -> Void in
                
            });
        } else {
            self.alpha = 1
        }
    }
    
    private func stop() {
        
        if (self.animated) {
            UIView.animate(withDuration: 0.3, animations: { () -> Void in
                DispatchQueue.main.asyncAfter(deadline: .now(), execute: {
                    self.alpha = 0
                })
                
            }, completion: { (finished) -> Void in
                self.removeFromSuperview()
                self.coverView?.removeFromSuperview()
                self.loadingView?.stop()
            });
        } else {
            self.alpha = 0
            self.removeFromSuperview()
            self.coverView?.removeFromSuperview()
            self.loadingView?.stop()
        }
    }
    
    private func update() {
        self.backgroundColor = self.config.backgroundColor
        self.layer.cornerRadius = self.config.cornerRadius
        let loadingViewSize = self.frame.size.width - (loaderSpinnerMarginSide * 2)
        
        if (self.loadingView == nil) {
            self.loadingView = SwiftLoadingView(frame: self.frameForSpinner())
            self.addSubview(self.loadingView!)
        } else {
            self.loadingView?.frame = self.frameForSpinner()
        }
        
        if (self.titleLabel == nil) {
            self.titleLabel = UILabel(frame: CGRect(origin: CGPoint(x: loaderTitleMargin, y: loaderSpinnerMarginTop + loadingViewSize), size: CGSize(width: self.frame.width - loaderTitleMargin*2, height:  42.0)))
            self.addSubview(self.titleLabel!)
            self.titleLabel?.numberOfLines = 1
            self.titleLabel?.textAlignment = NSTextAlignment.center
            self.titleLabel?.adjustsFontSizeToFitWidth = true
        } else {
            self.titleLabel?.frame = CGRect(origin: CGPoint(x: loaderTitleMargin, y: loaderSpinnerMarginTop + loadingViewSize), size: CGSize(width: self.frame.width - loaderTitleMargin*2, height: 42.0))
        }
        
        self.titleLabel?.font = self.config.titleTextFont
        self.titleLabel?.textColor = self.config.titleTextColor
        self.titleLabel?.text = self.title
        
        self.titleLabel?.isHidden = self.title == nil
    }
    
    func frameForSpinner() -> CGRect {
        let loadingViewSize = self.frame.size.width - (loaderSpinnerMarginSide * 2)
        
        if (self.title == nil) {
            let yOffset = (self.frame.size.height - loadingViewSize) / 2
            return CGRect(origin: CGPoint(x: loaderSpinnerMarginSide, y: yOffset), size: CGSize(width: loadingViewSize, height: loadingViewSize))
        }
        return CGRect(origin: CGPoint(x: loaderSpinnerMarginSide, y: loaderSpinnerMarginTop), size: CGSize(width: loadingViewSize, height: loadingViewSize))
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        self.setup()
    }
    
    required public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    /**
     *  Loader View
     */
    class SwiftLoadingView : UIView {
        
        fileprivate var speed : Int?
        fileprivate var lineWidth : Float?
        fileprivate var lineTintColor : UIColor?
        fileprivate var backgroundLayer : CAShapeLayer?
        fileprivate var isSpinning : Bool?
        
        var config : Config = Config() {
            didSet {
                self.update()
            }
        }
        
        override init(frame: CGRect) {
            super.init(frame: frame)
            self.setup()
        }
        
        required init?(coder aDecoder: NSCoder) {
            super.init(coder: aDecoder)
        }
        
        /**
         Setup loading view
         */
        
        func setup() {
            self.backgroundColor = UIColor.clear
            self.lineWidth = fmaxf(Float(self.frame.size.width) * 0.025, 1)
            
            self.backgroundLayer = CAShapeLayer()
            self.backgroundLayer?.strokeColor = self.config.spinnerColor.cgColor
            self.backgroundLayer?.fillColor = self.backgroundColor?.cgColor
            self.backgroundLayer?.lineCap = kCALineCapRound
            self.backgroundLayer?.lineWidth = CGFloat(self.lineWidth!)
            self.layer.addSublayer(self.backgroundLayer!)
        }
        
        func update() {
            self.lineWidth = self.config.spinnerLineWidth
            self.speed = self.config.speed
            
            self.backgroundLayer?.lineWidth = CGFloat(self.lineWidth!)
            self.backgroundLayer?.strokeColor = self.config.spinnerColor.cgColor
        }
        
        /**
         Draw Circle
         */
        
        override func draw(_ rect: CGRect) {
            self.backgroundLayer?.frame = self.bounds
        }
        
        func drawBackgroundCircle(_ partial : Bool) {
            let startAngle : CGFloat = CGFloat(Double.pi) / CGFloat(2.0)
            var endAngle : CGFloat = (2.0 * CGFloat(Double.pi)) + startAngle
            
            let center : CGPoint = CGPoint(x: self.bounds.size.width / 2,y: self.bounds.size.height / 2)
            let radius : CGFloat = (CGFloat(self.bounds.size.width) - CGFloat(self.lineWidth!)) / CGFloat(2.0)
            
            let processBackgroundPath : UIBezierPath = UIBezierPath()
            processBackgroundPath.lineWidth = CGFloat(self.lineWidth!)
            
            if (partial) {
                endAngle = (1.8 * CGFloat(Double.pi)) + startAngle
            }
            
            processBackgroundPath.addArc(withCenter: center, radius: radius, startAngle: startAngle, endAngle: endAngle, clockwise: true)
            self.backgroundLayer?.path = processBackgroundPath.cgPath;
        }
        
        /**
         Start and stop spinning
         */
        
        func start() {
            
            if ((self.backgroundLayer?.animationKeys()) != nil), (self.backgroundLayer?.animationKeys()?.contains("rotationAnimation"))! {
                stop()
            }
            
            self.isSpinning? = true
            self.drawBackgroundCircle(true)
            
            let rotationAnimation : CABasicAnimation = CABasicAnimation(keyPath: "transform.rotation.z")
            rotationAnimation.toValue = NSNumber(value: Double.pi * 2.0)
            rotationAnimation.duration = 1;
            rotationAnimation.isCumulative = true;
            rotationAnimation.repeatCount = HUGE;
            self.backgroundLayer?.add(rotationAnimation, forKey: "rotationAnimation")
        }
        
        func stop() {
            self.drawBackgroundCircle(false)
            
            self.backgroundLayer?.removeAllAnimations()
            self.isSpinning? = false
        }
    }
    
    
    /**
     * Loader config
     */
    public struct Config {
        
        /**
         *  Size of loader
         */
        public var size : CGFloat = 80.0
        
        /**
         *  Color of spinner view
         */
        public var spinnerColor = UIColor.white
        
        /**
         *  S
         */
        public var spinnerLineWidth :Float = 2.0
        
        /**
         *  Color of title text
         */
        public var titleTextColor = UIColor.white
        
        /**
         *  Speed of the spinner
         */
        public var speed :Int = 2
        
        /**
         *  Font for title text in loader
         */
        public var titleTextFont : UIFont = UIFont.boldSystemFont(ofSize: 16.0)
        
        /**
         *  Background color for loader
         */
        public var backgroundColor = UIColor(red:90/255, green:200/255, blue:249/255, alpha:1)
        
        /**
         *  Foreground color
         */
        public var foregroundColor = UIColor.clear
        
        /**
         *  Foreground alpha CGFloat, between 0.0 and 1.0
         */
        public var foregroundAlpha:CGFloat = 0.2
        
        /**
         *  Corner radius for loader
         */
        public var cornerRadius : CGFloat = 10.0
        
        public init() {}
        
    }
}
