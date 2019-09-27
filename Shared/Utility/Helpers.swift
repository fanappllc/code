//
//  Helpers.swift
//  TaxiAppUKCustomer
//
//  Created by Codiant on 8/18/17.
//  Copyright © 2017 Codiant Software Technologies Pvt ltd. All rights reserved.
//

import Foundation
import UIKit
import ObjectMapper

enum GradientDirection {
    case leftToRight
    case rightToLeft
    case topToBottom
    case bottomToTop
}
extension String {
    func nsRangeFromRange(range : Range<String.Index>) -> NSRange {
        return NSRange(range, in: self)
    }
    
    func currencyInputFormatting() -> String {
        var number: NSNumber!
        let formatter = NumberFormatter()
        formatter.numberStyle = .currencyAccounting
        formatter.currencySymbol = "$ "
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
        formatter.usesGroupingSeparator = false
        
        let amountWithPrefix = self
        
        // remove from String: "$", ".", ","
        //let regex = try! NSRegularExpression(pattern: "[^0-9]", options: .caseInsensitive)
        //amountWithPrefix = regex.stringByReplacingMatches(in: amountWithPrefix, options: NSRegularExpression.MatchingOptions(rawValue: 0), range: NSMakeRange(0, self.count), withTemplate: "")
        
        let double = (amountWithPrefix as NSString).doubleValue
        number = NSNumber(value: (double))
        
        // if first number is 0 or all numbers were deleted
        guard number != 0 as NSNumber else {
            return "$ 0"
        }
        return formatter.string(from: number)!
    }
}

extension Date {
    
    func UTCDateString() -> String {
        let formatter = DateFormatter()
        formatter.timeZone = TimeZone(abbreviation: "UTC")
        formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        return formatter.string(from: self)
    }
    
    func offsetFrom(date: Date) -> String {
        
        let dayHourMinuteSecond: Set<Calendar.Component> = [.day, .hour, .minute, .second]
        let difference = NSCalendar.current.dateComponents(dayHourMinuteSecond, from: date, to: self);
        
        let seconds = "\(difference.second ?? 0)s"
        let minutes = "\(difference.minute ?? 0)m" + " " + seconds
        let hours = "\(difference.hour ?? 0)h" + " " + minutes
        let days = "\(difference.day ?? 0)d" + " " + hours
        
        if let day = difference.day, day          > 0 { return days }
        if let hour = difference.hour, hour       > 0 { return hours }
        if let minute = difference.minute, minute > 0 { return minutes }
        if let second = difference.second, second > 0 { return seconds }
        return ""
    }
}
extension Data {
    
    func deserialize() -> [String: Any]? {
        
        do {
            let object = try JSONSerialization.jsonObject(with: self, options: []) as? [String: Any]
           // let jsonData = try JSONSerialization.data(withJSONObject: self, options: [.prettyPrinted])
//            guard let jsonString = String(data: self, encoding: String.Encoding.utf8) else {
//                print("Can't create string with data.")
//                return [:]
//            }
            print("\(String(describing: object) )")
            
            return object
        }
        catch let error {
            DDLogError(error.localizedDescription)
            return nil
        }
    }
}
extension UIStoryboard {
    
    func controllerExists(withIdentifier: String) -> Bool {
        if let availableIdentifiers = self.value(forKey: "identifierToNibNameMap") as? [String: Any] {
            return availableIdentifiers[withIdentifier] != nil
        }
        
        return false
    }
    
    func viewController<T>(withClass: T.Type) -> T? {
        
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        let identifier = NSStringFromClass(withClass as! AnyClass).components(separatedBy: ".")[1] // trimmed module name
        
        guard self.controllerExists(withIdentifier: identifier) else {
            return nil
        }
        
        return storyboard.instantiateViewController(withIdentifier: identifier) as? T
    }
}

extension UIViewController {
    
    func push(_ viewController: UIViewController, animated: Bool = true) -> Void {
        navigationController?.pushViewController(viewController, animated: animated)
    }
    
    func showAlertWith(message: String, cancelButtonCallback: (() -> Void)? = nil) {
        
        let alertController = UIAlertController(title: "FAN", message: message, preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: "OK", style: .cancel, handler: { (action) in
            cancelButtonCallback?()
        }))
        
        self.present(alertController, animated: true, completion: nil)
    }
    
    func isModal() -> Bool {
        if let navigationController = self.navigationController{
            if navigationController.viewControllers.first != self{
                return false
            }
        }
        if self.presentingViewController != nil {
            return true
        }
        if self.navigationController?.presentingViewController?.presentedViewController == self.navigationController  {
            return true
        }
        if self.tabBarController?.presentingViewController is UITabBarController {
            return true
        }
        return false
    }
    class func topViewController(_ base: UIViewController? = UIApplication.shared.keyWindow?.rootViewController) -> UIViewController? {
        
        if let nav = base as? UINavigationController {
            return topViewController(nav.visibleViewController)
        }
        if let tab = base as? UITabBarController {
            if let selected = tab.selectedViewController {
                return topViewController(selected)
            }
        }
        if let reveal = base as? RootVC {
            return topViewController(reveal.contentViewController)
        }
        if let presented = base?.presentedViewController {
            return topViewController(presented)
        }
        
        return base
    }
}

extension UIView {
    func gradientBackground(from color1: UIColor, to color2: UIColor, direction: GradientDirection) {
        let gradient = CAGradientLayer()
        gradient.frame = self.bounds
        gradient.colors = [color1.cgColor, color2.cgColor]
        
        switch direction {
        case .leftToRight:
            gradient.startPoint = CGPoint(x: 0.0, y: 0.5)
            gradient.endPoint = CGPoint(x: 1.0, y: 0.5)
        case .rightToLeft:
            gradient.startPoint = CGPoint(x: 1.0, y: 0.5)
            gradient.endPoint = CGPoint(x: 0.0, y: 0.5)
        case .bottomToTop:
            gradient.startPoint = CGPoint(x: 0.5, y: 1.0)
            gradient.endPoint = CGPoint(x: 0.5, y: 0.0)
        default:
            break
        }
        
        self.layer.insertSublayer(gradient, at: 0)
    }
    
    @discardableResult
    func roundCorners(_ corners: UIRectCorner, radius: CGFloat) -> CAShapeLayer {
        let path = UIBezierPath(roundedRect: self.bounds, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        let mask = CAShapeLayer()
        mask.path = path.cgPath
        self.layer.mask = mask
        return mask
    }
}
struct Validator {
    
    static func emptyString(_ string: String?) -> Bool {
        if string == nil || string!.isKind(of: NSNull.self) || string == "null" || string == "<null>" || string == "(null)" {
            return true
        }
        return string!.trimmingCharacters(in: CharacterSet.whitespaces).isEmpty
    }
    
    static func validEmail(_ email: String?) -> Bool {
        if self.emptyString(email) {
            return false
        }
        let emailRegEx = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
        let emailValidator = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailValidator.evaluate(with: email)
    }
    
    static func validBadgeNumber(_ number: String?) -> Bool {
        if self.emptyString(number) {
            return false
        }
        return (number!.count > 4 && number!.count < 10)
    }
    
}
extension UIDevice {
    var modelName: String {
        var systemInfo = utsname()
        uname(&systemInfo)
        let machineMirror = Mirror(reflecting: systemInfo.machine)
        let identifier = machineMirror.children.reduce("") { identifier, element in
            guard let value = element.value as? Int8, value != 0 else { return identifier }
            return identifier + String(UnicodeScalar(UInt8(value)))
        }
        if let value = ProcessInfo.processInfo.environment["SIMULATOR_MODEL_IDENTIFIER"] {
            return value
        } else {
            return DeviceName(identifier)
        }
    }
    
    func DeviceName(_ model: String) -> String {
        
        
        let deviceNamesByCode : [String: String] = ["iPod1,1":"iPod Touch 1",
                                                    "iPod2,1":"iPod Touch 2",
                                                    "iPod3,1":"iPod Touch 3",
                                                    "iPod4,1":"iPod Touch 4",
                                                    "iPod5,1":"iPod Touch 5",
                                                    "iPod7,1":"iPod Touch 6",
                                                    "iPhone1,1":"iPhone",
                                                    "iPhone1,2":"iPhone ",
                                                    "iPhone2,1":"iPhone ",
                                                    "iPhone3,1":"iPhone 4",
                                                    "iPhone3,2":"iPhone 4",
                                                    "iPhone3,3":"iPhone 4",
                                                    "iPhone4,1":"iPhone 4s",
                                                    "iPhone5,1":"iPhone 5",
                                                    "iPhone5,2":"iPhone 5",
                                                    "iPhone5,3":"iPhone 5c",
                                                    "iPhone5,4":"iPhone 5c",
                                                    "iPhone6,1":"iPhone 5s",
                                                    "iPhone6,2":"iPhone 5s",
                                                    "iPhone7,2":"iPhone 6",
                                                    "iPhone7,1":"iPhone 6 Plus",
                                                    "iPhone8,1":"iPhone 6s",
                                                    "iPhone8,2":"iPhone 6s Plus",
                                                    "iPhone8,4":"iPhone SE",
                                                    "iPhone9,1" : "iPhone 7 (CDMA)",
                                                    "iPhone9,3" : "iPhone 7 (GSM)",
                                                    "iPhone9,2" : "iPhone 7 Plus (CDMA)",
                                                    "iPhone9,4" : "iPhone 7 Plus (GSM)",
                                                    "iPhone10,1" : "iPhone 8 (CDMA)",
                                                    "iPhone10,4" : "iPhone 8 (GSM)",
                                                    "iPhone10,2" : "iPhone 8 Plus (CDMA)",
                                                    "iPhone10,5" : "iPhone 8 Plus (GSM)",
                                                    "iPhone10,3" : "iPhone X (CDMA)",
                                                    "iPhone10,6" : "iPhone X (GSM)",
                                                    "iPad2,1":"iPad 2",
                                                    "iPad2,2":"iPad 2",
                                                    "iPad2,3":"iPad 2",
                                                    "iPad2,4":"iPad 2",
                                                    "iPad3,1":"iPad 3",
                                                    "iPad3,2":"iPad 3",
                                                    "iPad3,3":"iPad 3",
                                                    "iPad3,4":"iPad 4",
                                                    "iPad3,5":"iPad 4",
                                                    "iPad3,6":"iPad 4",
                                                    "iPad4,1":"iPad Air",
                                                    "iPad4,2":"iPad Air",
                                                    "iPad4,3":"iPad Air",
                                                    "iPad5,3":"iPad Air 2",
                                                    "iPad5,4":"iPad Air 2",
                                                    "iPad2,5":"iPad Mini",
                                                    "iPad2,6":"iPad Mini",
                                                    "iPad2,7":"iPad Mini",
                                                    "iPad4,4":"iPad Mini 2",
                                                    "iPad4,5":"iPad Mini 2",
                                                    "iPad4,6":"iPad Mini 2",
                                                    "iPad4,7":"iPad Mini 3",
                                                    "iPad4,8":"iPad Mini 3",
                                                    "iPad4,9":"iPad Mini 3",
                                                    "iPad5,1":"iPad Mini 4",
                                                    "iPad5,2":"iPad Mini 4",
                                                    "iPad6,3":"iPad Pro",
                                                    "iPad6,4":"iPad Pro",
                                                    "iPad6,7":"iPad Pro",
                                                    "iPad6,8":"iPad Pro",
                                                    "AppleTV5,3":"Apple TV",
                                                    "i386":"Simulator",
                                                    "x86_64":"Simulator"
            
        ]
        
        if deviceNamesByCode[model] != nil {
            return deviceNamesByCode[model]!
        }
        return "iPhone"
    }
}

extension UIImage {
    
    func compressImage() -> Data? {
        let bcf = ByteCountFormatter()
        bcf.allowedUnits = [.useMB] // optional: restricts the units to MB only
        bcf.countStyle = .file
        let imageData1: Data = UIImageJPEGRepresentation(self, 1)!
        let size1 = bcf.string(fromByteCount: Int64(imageData1.count))
        print("Fresh size is: \(size1)")
        
        var actualHeight : CGFloat = self.size.height
        var actualWidth : CGFloat = self.size.width
        let maxHeight : CGFloat = 1024.0
        let maxWidth : CGFloat = 1024.0
        var imgRatio : CGFloat = actualWidth/actualHeight
        let maxRatio : CGFloat = maxWidth/maxHeight
        var compressionQuality : CGFloat = 1
        
        if (actualHeight > maxHeight || actualWidth > maxWidth){
            if(imgRatio < maxRatio){
                //adjust width according to maxHeight
                imgRatio = maxHeight / actualHeight
                actualWidth = imgRatio * actualWidth
                actualHeight = maxHeight
            }
            else if(imgRatio > maxRatio){
                //adjust height according to maxWidth
                imgRatio = maxWidth / actualWidth
                actualHeight = imgRatio * actualHeight
                actualWidth = maxWidth
            }
            else{
                actualHeight = maxHeight
                actualWidth = maxWidth
                compressionQuality = 1
            }
        }
        
        let rect = CGRect(x: 0.0, y: 0.0, width: actualWidth, height: actualHeight)
        UIGraphicsBeginImageContext(rect.size)
        self.draw(in: rect)
        guard let img = UIGraphicsGetImageFromCurrentImageContext() else {
            return nil
        }
        UIGraphicsEndImageContext()
        guard let imageData = UIImageJPEGRepresentation(img, compressionQuality)else{
            return nil
        }

        
        let size = bcf.string(fromByteCount: Int64(imageData.count))
        print("Data size is: \(size)")
        return imageData
    }
    
     func compress(image: UIImage, maxFileSize: Int, compression: CGFloat = 1.0, maxCompression: CGFloat = 0.5) -> Data? {
        
        if let data = UIImageJPEGRepresentation(image, compression) {
            
            let bcf = ByteCountFormatter()
            bcf.allowedUnits = [.useMB] // optional: restricts the units to MB only
            bcf.countStyle = .file
            let size = bcf.string(fromByteCount: Int64(data.count))
            print("Data size is: \(size)")
            
            if data.count > (maxFileSize * 1024 * 1024) && (compression > maxCompression) {
                let newCompression = compression - 0.1
                let compressedData = self.compress(image: image, maxFileSize: maxFileSize, compression: newCompression, maxCompression: maxCompression)
                let newSize = bcf.string(fromByteCount: Int64(compressedData!.count))
                print("New size is: \(newSize)")
                return compressedData
            }
            return data
        }
        
        return nil
    }
    
    func applyBinaryCompression(minFileSizeBytes: Int64, maxFileSizeBytes: Int64, numLoop: Int) -> Data? {
        var maxCompressionRate: CGFloat = 1.0
        var minCompressionRate: CGFloat = 0.0
        var compressedData: Data?
        
        if let imageData: Data = UIImageJPEGRepresentation(self, maxCompressionRate) {
            let bcf = ByteCountFormatter()
            bcf.allowedUnits = [.useMB] // optional: restricts the units to MB only
            bcf.countStyle = .file
            let size = bcf.string(fromByteCount: Int64(imageData.count))
            print("Data size is: \(size)")
            guard imageData.count > maxFileSizeBytes else {
                let orignalSize = bcf.string(fromByteCount: Int64(imageData.count))
                print("Orignal data size is: \(orignalSize)")
                return imageData
            }
            
            for _ in 1...numLoop {
                let compressionRate = (maxCompressionRate + minCompressionRate) / 2
                guard let data = UIImageJPEGRepresentation(self, compressionRate) else {
                    return nil
                }
                let compressedSize1 = bcf.string(fromByteCount: Int64(data.count))
                print("Loop Compressed data size is: \(compressedSize1)")
                if data.count > maxFileSizeBytes {
                    maxCompressionRate = compressionRate
                } else {
                    minCompressionRate = compressionRate
                    compressedData = data
                    print("Loop created once not expressed: \(data.count)")
                    let compressedSize2 = bcf.string(fromByteCount: Int64(compressedData!.count))
                    print("Loop else Compressed data size is: \(compressedSize2)")
                    if data.count > minFileSizeBytes {
                        let compressedSize = bcf.string(fromByteCount: Int64(data.count))
                        print("Compressed data size is: \(compressedSize)")
                        return compressedData
                    }
                }
            }
        }
        let bcf1 = ByteCountFormatter()
        bcf1.allowedUnits = [.useMB] // optional: restricts the units to MB only
        bcf1.countStyle = .file
        let finalCompressedSize = bcf1.string(fromByteCount: Int64(compressedData!.count))
        print("Final Compressed data size is: \(finalCompressedSize)")
        return compressedData
    }
    
        func fixedOrientation() -> UIImage {
            if imageOrientation == .up { return self }
            
            var transform:CGAffineTransform = .identity
            switch imageOrientation {
            case .down, .downMirrored:
                transform = transform.translatedBy(x: size.width, y: size.height).rotated(by: .pi)
            case .left, .leftMirrored:
                transform = transform.translatedBy(x: size.width, y: 0).rotated(by: .pi/2)
            case .right, .rightMirrored:
                transform = transform.translatedBy(x: 0, y: size.height).rotated(by: -.pi/2)
            default: break
            }
            
            switch imageOrientation {
            case .upMirrored, .downMirrored:
                transform = transform.translatedBy(x: size.width, y: 0).scaledBy(x: -1, y: 1)
            case .leftMirrored, .rightMirrored:
                transform = transform.translatedBy(x: size.height, y: 0).scaledBy(x: -1, y: 1)
            default: break
            }
            
            let ctx = CGContext(data: nil, width: Int(size.width), height: Int(size.height),
                                bitsPerComponent: cgImage!.bitsPerComponent, bytesPerRow: 0,
                                space: cgImage!.colorSpace!, bitmapInfo: cgImage!.bitmapInfo.rawValue)!
            ctx.concatenate(transform)
            
            switch imageOrientation {
            case .left, .leftMirrored, .right, .rightMirrored:
                ctx.draw(cgImage!, in: CGRect(x: 0, y: 0, width: size.height,height: size.width))
            default:
                ctx.draw(cgImage!, in: CGRect(x: 0, y: 0, width: size.width,height: size.height))
            }
            return UIImage(cgImage: ctx.makeImage()!)
        }
}

// Vaue Transformers
let DoubleToStringTransform = TransformOf<String, Double>(fromJSON: { (value: Double?) -> String? in
    guard let val = value else { return nil }
    return "\(val)"
}, toJSON: { (value: String?) -> Double? in
    if let value = value { return Double(value) }
    return nil
})

let IntToStringTransform = TransformOf<String, Int>(fromJSON: { (value: Int?) -> String? in
    guard let val = value else { return nil }
    return "\(val)"
}, toJSON: { (value: String?) -> Int? in
    if let value = value { return Int(value) }
    return nil
})

let StringToDoubleTransform = TransformOf<Double, String>(fromJSON: { (value: String?) -> Double? in
    if let value = value { return Double(value) }
    return 0.0
}, toJSON: { (value: Double?) -> String? in
    if let value = value { return String(value) }
    return "0.0"
})

let BoolToStringTransform = TransformOf<String, Bool>(fromJSON: { (value: Bool?) -> String? in
    guard let nonOptionalValue = value else {return "false"}
    return nonOptionalValue.description
}, toJSON: { (value: String?) -> Bool? in
    guard let nonOptionalValue = value else {return false}
    return nonOptionalValue == "true" ? true : false
})
// UINavigationController and UINavigationBar operation
/*
 extension CAGradientLayer {
 
 convenience init(frame: CGRect, colors: [UIColor]) {
 self.init()
 self.frame = frame
 self.colors = []
 for color in colors {
 self.colors?.append(color.cgColor)
 }
 startPoint = CGPoint(x: 0.0, y: 0.5)
 endPoint = CGPoint(x: 1.0, y: 0.5)
 }
 
 func creatGradientImage() -> UIImage? {
 
 var image: UIImage? = nil
 UIGraphicsBeginImageContext(bounds.size)
 if let context = UIGraphicsGetCurrentContext() {
 render(in: context)
 image = UIGraphicsGetImageFromCurrentImageContext()
 }
 UIGraphicsEndImageContext()
 return image
 }
 
 }
 extension UINavigationBar {
 func setGradientBackground(colors: [UIColor]) {
 
 var updatedFrame = bounds
 updatedFrame.size.height += 20
 let gradientLayer = CAGradientLayer(frame: updatedFrame, colors: colors)
 
 setBackgroundImage(gradientLayer.creatGradientImage(), for: UIBarMetrics.default)
 }
 }
 
 extension UINavigationController {
 
 func applyStyling() {
 self.navigationBar.barTintColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 1)
 self.navigationBar.tintColor = UIColor.blue
 self.navigationBar.titleTextAttributes = [NSAttributedStringKey.foregroundColor: UIColor.white, NSAttributedStringKey.font:UIFont.quicksand(.medium, size: .mediumLarge)]
 UIBarButtonItem.appearance().setTitleTextAttributes([NSAttributedStringKey.foregroundColor: UIColor.blue, NSAttributedStringKey.font:UIFont.quicksand(.medium, size: .medium)], for: .normal)
 }
 }
 */
