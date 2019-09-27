//
//  GalleryImgVC.swift
//  FanCustomer
//
//  Created by Codiant on 2/27/18.
//  Copyright © 2018 Codiant. All rights reserved.
//

import UIKit
import Photos
import Kingfisher

class GalleryImgVC: UIViewController {
    @IBOutlet weak var tblPgFolder: UITableView!
    @IBOutlet weak var collectionView: UICollectionView!
    @IBOutlet weak var cnstrtToggleViewWidth: NSLayoutConstraint! // 200
    @IBOutlet weak var cnstrtInfoViewHeight: NSLayoutConstraint! // 200
    @IBOutlet weak var cnstrtViewBottomHeight: NSLayoutConstraint!
    @IBOutlet weak var btnToggle: UIButton!
    @IBOutlet weak var btnSaved: UIButton!
    @IBOutlet weak var btnUnsaved: UIButton!
    @IBOutlet weak var btnSideMenuToggle: UIButton!
    @IBOutlet weak var btnNavigationBarRight: UIButton!
    @IBOutlet weak var btnNavigationBarLeft: UIButton!
    @IBOutlet weak var viewToggle: UIView!
    @IBOutlet weak var viewInfo: UIView!
    var assetCollection : PHAssetCollection!
    var photosAsset: PHFetchResult<AnyObject>!
    var assetCollectionPlaceholder: PHObjectPlaceholder!
    let fileManager = FileManager.default
    
    let itemsPerRow : CGFloat = 3.0
    fileprivate let sectionInsets = UIEdgeInsets(top: 5.0, left: 5.0, bottom: 5.0, right: 5.0)
    var selectedIndex = 0
    var isSavedTabSelected = false
    var isMultiSelectionOn = false
    
    var photographers = [[Photographer]]()
    var arrPGAlbums = [String]()
    var multipleSelectedImages = [MultipleImagesData]()
    var arrAllSavedImages = [[[String: Any]]]()
    var currentSelectedAlbumName = ""
    
    enum CellIdentifiers {
        static let pgCell = "PhotographerCell"
        static let pgAlbumCell = "GalleryItemCollectionViewCell"
        static let pgHeaderCell = "GalleryItemCommentView"
    }
    enum SlideOutState {
        case rightPanelExpanded
        case rightPanelCollapsed
    }
    var currentState: SlideOutState = .rightPanelCollapsed
    
    override func viewDidLoad() {
        super.viewDidLoad()
        getPhotos {
            if self.photographers.count > 0 {
                self.initialSetupUI()
            }
        }
        
        let photos = PHPhotoLibrary.authorizationStatus()
        if photos == .notDetermined  || photos == .denied {
            PHPhotoLibrary.requestAuthorization({status in
                print("Hello")
            })
        }
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //  MARK:- Private methods
    func toggleRightPanel() {
        let shouldExpand = (currentState != .rightPanelExpanded)
        // Do expand
        self.tblPgFolder.layoutIfNeeded()
        self.cnstrtToggleViewWidth.constant = shouldExpand ? 206 : 32
        UIView.animate(withDuration: 0.6, delay: 0, usingSpringWithDamping: 0.8, initialSpringVelocity: 0, options: .curveEaseInOut, animations: {
            self.view.layoutIfNeeded()
            self.btnToggle.isSelected = shouldExpand
        }, completion: { (completed) in
        })
        
        currentState = shouldExpand ? .rightPanelExpanded : .rightPanelCollapsed
    }
    
    func initialSetupUI() {
        self.cnstrtViewBottomHeight.constant = 40
        self.cnstrtInfoViewHeight.constant = 45
        self.btnSaved.isHidden = false
        self.btnUnsaved.isHidden = false
        self.viewToggle.isHidden = false
        self.viewInfo.isHidden = false
        self.collectionView.reloadData()
        self.btnNavigationBarRight.isHidden = false
    }
    
    func setNavigationBarButtons() {
        if !isSavedTabSelected {
            if isMultiSelectionOn {
                btnSideMenuToggle.isHidden = true
                btnNavigationBarLeft.isHidden = false
                btnNavigationBarRight.setTitle("Save", for: .normal)
            } else {
                btnSideMenuToggle.isHidden = false
                btnNavigationBarRight.setTitle("Select", for: .normal)
                btnNavigationBarLeft.isHidden = true
                multipleSelectedImages.removeAll()
            }
            btnNavigationBarRight.isHidden = false
        } else {
            btnNavigationBarLeft.isHidden = true
            btnSideMenuToggle.isHidden = false
            btnNavigationBarRight.setTitle("Select", for: .normal)
            btnNavigationBarRight.isHidden = true
            multipleSelectedImages.removeAll()
        }
        collectionView.reloadData()
    }
    func deleteFileAtPath() {
        let fileNameToDelete = "myFileName.txt"
        var filePath = ""
        
        // Fine documents directory on device
        let dirs : [String] = NSSearchPathForDirectoriesInDomains(FileManager.SearchPathDirectory.documentDirectory, FileManager.SearchPathDomainMask.allDomainsMask, true)
        
        if dirs.count > 0 {
            let dir = dirs[0] //documents directory
            filePath = dir.appendingFormat("/" + fileNameToDelete)
            print("Local path = \(filePath)")
            
        } else {
            print("Could not find local directory to store file")
            return
        }
        
        
        do {
            let fileManager = FileManager.default
            
            // Check if file exists
            if fileManager.fileExists(atPath: filePath) {
                // Delete file
                try fileManager.removeItem(atPath: filePath)
            } else {
                print("File does not exist")
            }
            
        }
        catch let error as NSError {
            print("An error took place: \(error)")
        }
    }
    // Photo Operation
    //*   Get all saved image in document directory */
    func  getAlbumImageFromDrectory(albumName: String,handler: @escaping() -> Void){
        
        FanHUD.show()
        
        var photographerData = [[String: Any]]()
        // TODO: Clean memory for this images
        currentSelectedAlbumName = albumName
        
        let documentsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
        
        do {
            let fileURLs = try fileManager.contentsOfDirectory(at: documentsURL, includingPropertiesForKeys: nil)
            
            let sortedImages = fileURLs.filter{$0.absoluteString.contains(albumName)}
            
            for i in 0..<sortedImages.count{
                
                var urlArr = String(describing: sortedImages[i]).components(separatedBy: "/")
                
                let imagePath = (NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0] as NSString).appendingPathComponent(urlArr[urlArr.count - 1])
                
                if fileManager.fileExists(atPath: imagePath) {
                    let img = UIImage(contentsOfFile:imagePath)
                    var nameDate = String(describing: sortedImages[i]).components(separatedBy: "+")
                    let albumName = nameDate[0]
                    let albumDate = nameDate[1]
                    let userContact = nameDate[2]
                    let imageID = nameDate[3]
                    
                    guard userContact == LoggedInUser.shared.phoneNumber else{ return }
                    
                    let imgDate = Date(timeIntervalSince1970: Double(albumDate)!)
                    
                    var newDateComponents = DateComponents()
                    let components = Calendar.current.dateComponents([.day,.month,.year], from: imgDate)
                    newDateComponents.day = components.day
                    newDateComponents.month = components.month
                    newDateComponents.year = components.year
                    
                    
                    var imageData = [String: Any]()
                    imageData["albumName"] = albumName
                    imageData["albumDate"]  = Calendar.current.date(from: newDateComponents)?.timeIntervalSince1970
                    imageData["albumImage"]  = img!
                    imageData["imageID"]  = imageID
                    photographerData.append(imageData)
                }else{
                    print("Panic! No Image!")
                }
                
            }
            
            print(fileURLs)
            var albumArr = [String]()
            for i in 0..<fileURLs.count{
                
                var fileURLsArr = String(describing: fileURLs[i]).components(separatedBy: "/")
                let albumName = fileURLsArr[fileURLsArr.count - 1].components(separatedBy: "+")[0]
                albumArr.append(albumName)
            }
            
        } catch { }
        var timeStampArr =  Array(Set(photographerData.map({$0["albumDate"] as! Double}))).sorted(by: <)
        
        
        //arrAllSavedData append whole array
        arrAllSavedImages = [[[String: Any]]]()
        
        
        for i in 0..<timeStampArr.count{
            
            let filteredArr =  photographerData.filter { $0["albumDate"]  as! Double == timeStampArr[i] }
            arrAllSavedImages.append(filteredArr)
            
        }
        self.arrPGAlbums = self.arrPGAlbums.sorted(by: <)
        self.collectionView.reloadData()
        self.tblPgFolder.reloadData()
        handler()
    }
    
    func saveImageToPhotos(imageURL: String,albumName:String,date:Date,imgUI: UIImage, handler: @escaping() -> Void) {
        guard assetCollection != nil else{ return }
        
        // let url = URL(string: image)
        // let imageData =    KingfisherHelper.getImage(url: url!)
        // let imageData = NSData(contentsOf: url!) //20 seconds
        
        let imageData  = UIImageJPEGRepresentation(imgUI, 1.0)
        //var localIdentifier: String?
        PHPhotoLibrary.shared().performChanges({
            let assetChangeRequest = PHAssetChangeRequest.creationRequestForAsset(from: (UIImage(data: imageData!)!))
            let assetPlaceHolder = assetChangeRequest.placeholderForCreatedAsset
            
            let albumChangeRequest = PHAssetCollectionChangeRequest.init(for: self.assetCollection)
            
            let fastEnumeration = NSMutableArray(array: [assetPlaceHolder] as! [PHObjectPlaceholder])
            
            albumChangeRequest?.addAssets(fastEnumeration as NSArray)
            _ = (assetPlaceHolder?.localIdentifier)!
            
        }, completionHandler: nil)
        
        let imageID = imageURL.split(separator: "/")[imageURL.split(separator: "/").count - 1 ].split(separator: ".")[0]
        
        let imagePath = (NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0] as NSString).appendingPathComponent(albumName + "+\(Int(date.timeIntervalSince1970))+\(LoggedInUser.shared.phoneNumber!)+\(imageID)")
        //  let data = UIImagePNGRepresentation((UIImage(data: imageData! as Data)!))
        
        //store it in the document directory
        fileManager.createFile(atPath: imagePath as String, contents: imageData, attributes: nil)
        handler()
    }
    
    func saveImageToDirectory(imageURL : String, name: String, date: Date, handler: @escaping() -> Void) {
        let fetchOption = PHFetchOptions()
        fetchOption.predicate = NSPredicate(format: "title = %@", "FAN")
        let collection = PHAssetCollection.fetchAssetCollections(with: .album, subtype: .any, options: fetchOption)
        if collection.firstObject != nil {
            self.assetCollection = collection.firstObject
            ImageDownloader.default.downloadImage(with: URL.init(string: imageURL)!, options: [], progressBlock: nil) {
                (image, error, url, data) in
                guard image != nil, error == nil else {
                    DispatchQueue.main.asyncAfter(deadline: .now(), execute: {
                        FanHUD.hide()
                        if let message = error?.localizedDescription {
                            self.showAlertWith(message: message)
                        }
                    })
                    return
                }
                print("Downloaded Image: \(String(describing: image))")
                self.saveImageToPhotos(imageURL: imageURL, albumName: name, date: date, imgUI: image!, handler: {
                   handler()
                })
            }
        }
        else {
            // Album not available...Create New
            PHPhotoLibrary.shared().performChanges({
                let createAlbumRequest : PHAssetCollectionChangeRequest = PHAssetCollectionChangeRequest.creationRequestForAssetCollection(withTitle: "FAN")
                self.assetCollectionPlaceholder = createAlbumRequest.placeholderForCreatedAssetCollection
            }, completionHandler: { success, error in
                guard success, error == nil else {
                    DispatchQueue.main.asyncAfter(deadline: .now(), execute: {
                        FanHUD.hide()
                        if let message = error?.localizedDescription {
                            self.showAlertWith(message: message)
                        }
                    })
                    return
                }
                
                let collectionFetchResult = PHAssetCollection.fetchAssetCollections(withLocalIdentifiers: [self.assetCollectionPlaceholder.localIdentifier], options: nil)
                print(collectionFetchResult)
                self.assetCollection = collectionFetchResult.firstObject
                ImageDownloader.default.downloadImage(with: URL.init(string: imageURL)!, options: [], progressBlock: nil) {
                    (image, error, url, data) in
                    guard image != nil , error == nil else {
                        DispatchQueue.main.asyncAfter(deadline: .now(), execute: {
                            FanHUD.hide()
                            if let message = error?.localizedDescription {
                                self.showAlertWith(message: message)
                            }
                        })
                        return
                    }
                    print("Downloaded Image: \(String(describing: image))")
                    self.saveImageToPhotos(imageURL: imageURL, albumName: name, date: date, imgUI: image!, handler: {
                            handler()
                    })
                }
            })
        }
    }
    
    
    //  MARK:- Action methods
    @IBAction func btnToggle_Action(_ sender: UIButton) {
        toggleRightPanel()
    }
    
    @IBAction func btnSaveUnsave_Action(_ sender: UIButton) {
        if currentState == .rightPanelExpanded{
            toggleRightPanel()
        }
        
        if isSavedTabSelected,  sender.tag == 100 {
            return
        } else if !isSavedTabSelected,  sender.tag == 200 {
            return
        }
        
        self.btnSaved.backgroundColor = sender.tag == 100 ? Color.celrianBlue : .clear
        self.btnUnsaved.backgroundColor = sender.tag == 200 ? Color.celrianBlue : .clear
        self.btnSaved.setTitleColor(sender.tag == 100 ? .white : Color.celrianBlue, for: .normal)
        self.btnUnsaved.setTitleColor(sender.tag == 200 ? .white : Color.celrianBlue, for: .normal)
        isSavedTabSelected = sender.tag == 100 ? true : false
        
        setNavigationBarButtons()
        
        if isSavedTabSelected {
            self.cnstrtInfoViewHeight.constant = 0
            if isMultiSelectionOn {
                isMultiSelectionOn = false
                
            }
            guard photographers.count > 0 else {  return }
            arrAllSavedImages.removeAll()
            // TODO: Get saved gallery imges on selected index
            getAlbumImageFromDrectory(albumName: photographers[selectedIndex][0].photographerName){
                self.collectionView.collectionViewLayout.invalidateLayout() // or reloadData()
                DispatchQueue.main.async {
                    FanHUD.hide()
                }
            }
            
            guard self.cnstrtInfoViewHeight.constant == 45 else {
                return
            }
            self.cnstrtInfoViewHeight.constant = 0
        } else {
            self.collectionView.reloadData()
            guard self.cnstrtInfoViewHeight.constant == 0 else {
                return
            }
            self.cnstrtInfoViewHeight.constant = 45
        }
        
    }
    
    @IBAction func btnNavigationBarRight_Action(_ sender: Any) {
        if currentState == .rightPanelExpanded{
            toggleRightPanel()
        }
        guard !isSavedTabSelected else { return }
        
        if btnNavigationBarRight.title(for: .normal) == "Select" {
            // Select Case
            isMultiSelectionOn = true
            setNavigationBarButtons()
        } else {
            // Save Case
            guard multipleSelectedImages.count > 0 else {
                showAlertWith(message: "Please Select Image")
                return
            }
            FanHUD.show()
            saveMultipleImages(indexCount: 1)
        }
    }
    
    func saveMultipleImages( indexCount : Int) {
        var indexCount = indexCount
        if indexCount <=  self.multipleSelectedImages.count {
            let selectedImage = self.multipleSelectedImages[indexCount - 1]
            self.saveImageToDirectory(imageURL: selectedImage.imageUrl, name: selectedImage.photographerName, date: selectedImage.createdAt, handler: {
                 indexCount = indexCount + 1
                self.saveMultipleImages(indexCount: indexCount)
            })
        } else {
            FanHUD.hide()
            isMultiSelectionOn = false
            setNavigationBarButtons()
            self.showAlertWith(message: "Image Saved Successfully.")
        }
    }
    
    @IBAction func btnNavigationBarLeft_Action(_ sender: Any) {
        isMultiSelectionOn = false
        setNavigationBarButtons()
    }
    
    //  MARK:- API methods
    func getPhotos(handler: @escaping() -> Void) {
        FanHUD.show()
        APIComponents.Customer.getPhotos { [weak self] (success, data, error) in
            FanHUD.hide()
            guard let strongSelf = self else { return }
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    strongSelf.showAlertWith(message: message)
                }
                return
            }
            if let object = data!.deserialize() {
                strongSelf.photographers.removeAll()
                
                for response in (object["data"] as? [[HTTPParameters]])! {
                    strongSelf.photographers.append(response.map({ Photographer.map(JSONObject: $0, context: nil) }))
                    
                    if strongSelf.arrPGAlbums.count > 0 {
                        strongSelf.arrPGAlbums.removeAll()
                    }
                    for i in 0..<strongSelf.photographers.count {
                        strongSelf.arrPGAlbums.append(strongSelf.photographers[i][0].photographerName)
                    }
                    strongSelf.arrPGAlbums = strongSelf.arrPGAlbums.sorted(by: <)
                }
                strongSelf.photographers =  strongSelf.photographers.sorted(by: { $0[0].photographerName < $1[0].photographerName})
                handler()
            }
        }
    }
}
// MARK: Table View Data Source
extension GalleryImgVC: UITableViewDataSource, UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return isSavedTabSelected ? arrPGAlbums.count : photographers.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: CellIdentifiers.pgCell, for: indexPath) as! PhotographerCell
        if isSavedTabSelected {
            cell.pgName.text = arrPGAlbums[indexPath.row] + " Photos"
        } else {
            cell.pgName.text = arrPGAlbums[indexPath.row] + " Photos"
        }
        return cell
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        self.toggleRightPanel()
        if isMultiSelectionOn {
            isMultiSelectionOn = false
            setNavigationBarButtons()
        }
        if isSavedTabSelected {
            arrAllSavedImages.removeAll()
            // TODO: Get saved gallery imges on selected index
            getAlbumImageFromDrectory(albumName: arrPGAlbums[indexPath.row]){
                FanHUD.hide()
            }
        }
        selectedIndex = indexPath.row
        collectionView.reloadData()
    }
}

extension GalleryImgVC: UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return isSavedTabSelected ? arrAllSavedImages[section].count : photographers.count > 0 ? photographers[selectedIndex][section].photos.count : 0
    }

    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: CellIdentifiers.pgAlbumCell, for: indexPath) as! GalleryItemCollectionViewCell
        if isSavedTabSelected {
            cell.imgViewAlbum.image = arrAllSavedImages[indexPath.section][indexPath.row]["albumImage"] as? UIImage
            cell.ivSelected.isHidden = true
        }  else{
            isMultiSelectionOn ? (cell.ivSelected.isHidden = false) : (cell.ivSelected.isHidden = true)
            let album = photographers[selectedIndex][indexPath.section].photos[indexPath.row]
            if  multipleSelectedImages.count > 0 {
                (multipleSelectedImages.filter({$0.imageUrl == album.imageURL}).count > 0) ?  (cell.ivSelected.image = #imageLiteral(resourceName: "radio_on")) : (cell.ivSelected.image = #imageLiteral(resourceName: "radio_off"))
            } else {
                cell.ivSelected.image = #imageLiteral(resourceName: "radio_off")
            }
            cell.imgViewAlbum.setImageForAlbumFixedSize(with: URL.init(string: album.imageURLString)!)
        }
        return cell
    }

    func numberOfSections(in collectionView: UICollectionView) -> Int {
        return isSavedTabSelected ? arrAllSavedImages.count : photographers.count > 0 ? photographers[selectedIndex].count : 0
    }
    
    func collectionView(_ collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, at indexPath: IndexPath) -> UICollectionReusableView {
        
        let commentView = collectionView.dequeueReusableSupplementaryView(ofKind: kind, withReuseIdentifier: CellIdentifiers.pgHeaderCell, for: indexPath) as! GalleryItemCommentView
        
        commentView.lblPgName.text = isSavedTabSelected ? "\(currentSelectedAlbumName) Photos" : photographers[selectedIndex][indexPath.section].photographerName + " Photos"
        var date = ""
        if isSavedTabSelected {
            
            date = Date(timeIntervalSince1970:arrAllSavedImages[indexPath.section][indexPath.row]["albumDate"] as! Double).toString(format: .custom("EEE dd, yyyy"))
        }
        commentView.lblShootDate.text = isSavedTabSelected ? date : photographers[selectedIndex][indexPath.section].createdAt.toString(format: .custom("EEE dd, yyyy"))
        
        return commentView
    }
    func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize{
        
        let paddingSpace = sectionInsets.left * (itemsPerRow + 1)
        let availableWidth = view.frame.width - paddingSpace
        let widthPerItem = availableWidth / itemsPerRow
        return CGSize(width: widthPerItem, height: widthPerItem)
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        insetForSectionAt section: Int) -> UIEdgeInsets {
        return sectionInsets
    }
    
    func collectionView(_ collectionView: UICollectionView,
                        layout collectionViewLayout: UICollectionViewLayout,
                        minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return sectionInsets.bottom
    }
    
    func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        if  currentState == .rightPanelExpanded {
            self.toggleRightPanel()
        }
        let cell = collectionView.cellForItem(at: indexPath) as! GalleryItemCollectionViewCell
        guard cell.imgViewAlbum.image != nil else {
            return
        }
        
        
        if isSavedTabSelected {
            // View Image
            let imageInfo = GSImageInfo(image: cell.imgViewAlbum.image!, imageMode: .aspectFit, imageHD: nil)
            let transitionInfo = GSTransitionInfo(fromView: cell.imgViewAlbum)
            let imageViewer    = GSImageViewerController(imageInfo: imageInfo, transitionInfo: transitionInfo)
            imageViewer.isSaveButtonEnabled = false
            self.present(imageViewer, animated: true, completion: nil)
        } else {
            
            let imageInfo = GSImageInfo(image: cell.imgViewAlbum.image!, imageMode: .aspectFit, imageHD: nil)
            
            if isMultiSelectionOn {
                let album = photographers[selectedIndex][indexPath.section].photos[indexPath.row]
                // Remove if already selacted for save
                if  multipleSelectedImages.filter({$0.imageUrl == album.imageURL}).count > 0 {
                    multipleSelectedImages.remove(at:multipleSelectedImages.index(of:multipleSelectedImages.filter({$0.imageUrl == album.imageURL})[0])!)
                } else {
                    // Add if already selacted for save
                    let data = MultipleImagesData()
                    data.imageUrl = album.imageURL
                    data.createdAt =  photographers[selectedIndex][indexPath.section].createdAt
                    data.photographerName = photographers[selectedIndex][indexPath.section].photographerName
                    multipleSelectedImages.append(data)
                }
                collectionView.reloadData()
            } else {
                // Normal view case of unsaved images
                let transitionInfo = GSTransitionInfo(fromView: cell.imgViewAlbum)
                let imageViewer    = GSImageViewerController(imageInfo: imageInfo, transitionInfo: transitionInfo)
                
                imageViewer.isSaveButtonEnabled = true
                imageViewer.callback = {
                    let photos = PHPhotoLibrary.authorizationStatus()
                    if photos == .notDetermined  || photos == .denied {
                        PHPhotoLibrary.requestAuthorization({status in
                            print(status)
                            if status == .denied {
                                self.showAlertWith(message: "Image Not Saved Allow access for Photo Library from privacy.")
                                return
                            }
                        })
                    } else if photos == .authorized {
                        FanHUD.show()
                        self.saveImageToDirectory(imageURL: self.photographers[self.selectedIndex][indexPath.section].photos[indexPath.row].imageURL, name: self.photographers[self.selectedIndex][indexPath.section].photographerName, date: self.photographers[self.selectedIndex][indexPath.section].createdAt, handler: {
                            FanHUD.hide()
                            self.showAlertWith(message: "Image Saved Successfully.")
                        })
                    }
                }
                self.present(imageViewer, animated: true, completion: nil)
            }
        }
    }
}

