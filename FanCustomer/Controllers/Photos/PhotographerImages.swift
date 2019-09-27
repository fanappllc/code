//
//  PhotographerImages.swift
//  FanCustomer
//
//  Created by Codiant on 12/13/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class PhotographerImages {
    static func getAlbumImages() -> [[AlbumImage]] {
        var images = [[AlbumImage]]()
        
        let frameworkBundle = Bundle(for: type(of: PhotographerImages()))
        
        guard let jsonPath = frameworkBundle.path(forResource: "GalleryImages.bundle/imageName/imageName", ofType: "json"), let jsonData = try? Data(contentsOf: URL(fileURLWithPath: jsonPath)) else {
            return images
        }
        do {
            if let jsonObjects = try JSONSerialization.jsonObject(with: jsonData, options: JSONSerialization.ReadingOptions.allowFragments) as? NSArray {
                
                for jsonObject in jsonObjects {
                    
                    guard let obj = jsonObject as? NSArray else {
                         return images
                    }
                    var albumImages = [AlbumImage]()
                    for albumObj in obj {
                        guard let galleryObj = albumObj as? [String: String] else {
                            return images
                        }
                        guard let name = galleryObj["name"] else {
                            return images
                        }
                        let flagName = "GalleryImages.bundle/images/\(name)"
                        let image = AlbumImage(imageName : flagName)
                        albumImages.append(image)
                    }
                    images.append(albumImages)
                }
            }
        }
        catch {
            return images
        }
        
        return images
    }
}


struct AlbumImage {
    var imgName : String!
    
    init(imageName: String) {
        self.imgName = imageName
    }
    
    var image: UIImage? {
        return UIImage(named: imgName+".jpeg", in: Bundle(for: PhotographerImages.self), compatibleWith: nil)
    }
}
