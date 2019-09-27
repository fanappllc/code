//
//  KingfisherHelper.swift
//
//  - Copyright (c) 2017 Codiant. All rights reserved.
//

import Foundation
import Kingfisher


struct KingfisherHelper {
    
    static func configure(maxCacheSize: UInt, maxCachePeriod: TimeInterval) {
        ImageCache.default.maxDiskCacheSize = maxCacheSize
        ImageCache.default.maxCachePeriodInSecond = maxCachePeriod
        
        KingfisherManager.shared.cache.pathExtension = "jpg"
        
    
    }
 
    
    
    
    
    
}

extension UIImageView {
    
    func setImageForAlbum(with url: URL) {
        self.kf.indicatorType = .image(imageData: UIImagePNGRepresentation(#imageLiteral(resourceName: "photo-default-image"))!)
        self.kf.setImage(with: url)
   }
    func setImage(with url: URL) {
        self.kf.indicatorType = .activity
        self.kf.setImage(with: url)
    }
    func setImage(with url: URL, size: CGSize) {
        
        let processor = ResizingImageProcessor(referenceSize: size, mode: .aspectFill)
        
        self.kf.indicatorType = .activity
        self.kf.setImage(with: url, options: [.processor(processor)])
        
    
    }
    func setImageForAlbumFixedSize(with url: URL) {
        
        let processor = ResizingImageProcessor(referenceSize: CGSize(width: 200, height: 200), mode: .aspectFill)
        
        self.kf.indicatorType = .image(imageData: UIImagePNGRepresentation(#imageLiteral(resourceName: "photo-default-image"))!)
        self.kf.setImage(with: url, options: [.processor(processor)])
        
        
    }

    
    
    func cancelDownload() {
    
        self.kf.cancelDownloadTask()
    }
}
