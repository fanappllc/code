//
//  AlbumVC.swift
//  FanCustomer
//
//  Created by Codiant on 12/13/17.
//  Copyright © 2017 Codiant. All rights reserved.
//


import UIKit
import Photos
import Kingfisher
class AlbumVC: UIViewController {
    
    @IBOutlet weak var tblPgFolder: UITableView!
    @IBOutlet weak var collectionView: UICollectionView!
    @IBOutlet weak var cnstrtToggleViewWidth: NSLayoutConstraint! // 200
    @IBOutlet weak var cnstrtViewBottomHeight: NSLayoutConstraint!
    @IBOutlet weak var btnToggle: UIButton!
    @IBOutlet weak var btnSaved: UIButton!
    @IBOutlet weak var btnUnsaved: UIButton!
    @IBOutlet weak var btnSideMenuToggle: UIButton!
    @IBOutlet weak var btnNavigationBarRight: UIButton!
    @IBOutlet weak var btnNavigationBarLeft: UIButton!
    @IBOutlet weak var viewToggle: UIView!

    
    var multipleImages = [MultipleImagesData]()
    var photographers = [[Photographer]]()
    
    let itemsPerRow : CGFloat = 3.0
    var selectedIndex = 0
    var isSaved = false
    var arrSavedAlbums = [String]()
    var arrAllSavedData = [[[String: Any]]]()
    var selectedPhotographer : Int = 0
    var albumFound : Bool = false
    var assetCollection : PHAssetCollection!
    var photosAsset: PHFetchResult<AnyObject>!
    var assetCollectionPlaceholder: PHObjectPlaceholder!
    fileprivate let sectionInsets = UIEdgeInsets(top: 5.0, left: 5.0, bottom: 5.0, right: 5.0)
    let fileManager = FileManager.default
    var currentSelectedAlbumName = ""
    var isMultiSelectionOn = false
    
    
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
        
        if !isSaved {
            getPhotos {
                if self.photographers.count > 0 {
                    self.btnSaved.isHidden = false
                    self.btnUnsaved.isHidden = false
                    self.viewToggle.isHidden = false
                    self.cnstrtViewBottomHeight.constant = 40
                    self.btnNavigationBarRight.isHidden = false
                    self.collectionView.reloadData()
                } else {
                    self.btnNavigationBarRight.isHidden = true
                }
            }
        } 
        
        let photos = PHPhotoLibrary.authorizationStatus()
        if photos == .notDetermined  || photos == .denied{
            PHPhotoLibrary.requestAuthorization({status in
                
            })
        }
        setNavigationBarButtons()
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
    
    //*   Get all saved image in document directory */
    func  getAlbumImageFromDrectory(albumName: String,handler: @escaping() -> Void){
        
        FanHUD.show()
        
        var photographerData = [[String: Any]]()
        
        currentSelectedAlbumName = albumName
        
        let documentsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
        
        do {
            let fileURLs = try fileManager.contentsOfDirectory(at: documentsURL, includingPropertiesForKeys: nil)
            
            let sortedImages = fileURLs.filter{$0.absoluteString.contains(albumName)}
       
            albumFound =  sortedImages.count > 0 ? true : false
            for i in 0..<sortedImages.count{
                
                
                var urlArr = String(describing: sortedImages[i]).components(separatedBy: "/")
                
                let imagePath = (NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0] as NSString).appendingPathComponent(urlArr[urlArr.count - 1])
                
                
                
                if fileManager.fileExists(atPath: imagePath){
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
        arrAllSavedData = [[[String: Any]]]()
        
        
        for i in 0..<timeStampArr.count{
            
            let filteredArr =  photographerData.filter { $0["albumDate"]  as! Double == timeStampArr[i] }
            arrAllSavedData.append(filteredArr)
            
        }
        self.arrSavedAlbums = self.arrSavedAlbums.sorted(by: <)
        self.collectionView.reloadData()
        self.tblPgFolder.reloadData()
        handler()
        
    }

    
    func saveImageToPhotos(imageURL: String,albumName:String,date:Date,imgUI: UIImage) {
        guard assetCollection != nil else{ return }
        
        //let url = URL(string: image)
        // let imageData =    KingfisherHelper.getImage(url: url!)
        // let imageData = NSData(contentsOf: url!) //20 seconds

        let imageData  = UIImageJPEGRepresentation(imgUI, 1.0)
        
        var localIdentifier: String?
        PHPhotoLibrary.shared().performChanges({
            let assetChangeRequest = PHAssetChangeRequest.creationRequestForAsset(from: (UIImage(data: imageData!)!))
            let assetPlaceHolder = assetChangeRequest.placeholderForCreatedAsset
            
            let albumChangeRequest = PHAssetCollectionChangeRequest.init(for: self.assetCollection)
            
            let fastEnumeration = NSMutableArray(array: [assetPlaceHolder] as! [PHObjectPlaceholder])
            
            albumChangeRequest?.addAssets(fastEnumeration as NSArray)
            localIdentifier = (assetPlaceHolder?.localIdentifier)!
            
        }, completionHandler: nil)
        
        let imageID = imageURL.split(separator: "/")[imageURL.split(separator: "/").count - 1 ].split(separator: ".")[0]
        
        let imagePath = (NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0] as NSString).appendingPathComponent(albumName + "+\(Int(date.timeIntervalSince1970))+\(LoggedInUser.shared.phoneNumber!)+\(imageID)")
        //  let data = UIImagePNGRepresentation((UIImage(data: imageData! as Data)!))
        
        //store it in the document directory
        fileManager.createFile(atPath: imagePath as String, contents: imageData, attributes: nil)
        
    }
    
    func saveImageToDirectory(imageURL : String, name: String, date: Date,imgUI:UIImage) {
        let fetchOption = PHFetchOptions()
        fetchOption.predicate = NSPredicate(format: "title = %@", "FAN")
        let collection = PHAssetCollection.fetchAssetCollections(with: .album, subtype: .any, options: fetchOption)
        if collection.firstObject != nil {
            self.albumFound = true
            self.assetCollection = collection.firstObject
            ImageDownloader.default.downloadImage(with: URL.init(string: imageURL)!, options: [], progressBlock: nil) {
                (image, error, url, data) in
                guard image != nil, error == nil else {
                    if let message = error?.localizedDescription {
                        self.showAlertWith(message: message)
                    }
                    return
                }
                print("Downloaded Image: \(String(describing: image))")
                self.saveImageToPhotos(imageURL: imageURL,albumName: name,date:date, imgUI: image!)
            }
        }
        else {
            PHPhotoLibrary.shared().performChanges({
                let createAlbumRequest : PHAssetCollectionChangeRequest = PHAssetCollectionChangeRequest.creationRequestForAssetCollection(withTitle: "FAN")
                self.assetCollectionPlaceholder = createAlbumRequest.placeholderForCreatedAssetCollection
            }, completionHandler: { success, error in
                if success {
                    let collectionFetchResult = PHAssetCollection.fetchAssetCollections(withLocalIdentifiers: [self.assetCollectionPlaceholder.localIdentifier], options: nil)
                    print(collectionFetchResult)
                    self.assetCollection = collectionFetchResult.firstObject
                    ImageDownloader.default.downloadImage(with: URL.init(string: imageURL)!, options: [], progressBlock: nil) {
                        (image, error, url, data) in
                        guard image != nil , error == nil else {
                            if let message = error?.localizedDescription {
                                self.showAlertWith(message: message)
                            }
                            return
                        }
                        print("Downloaded Image: \(String(describing: image))")
                        self.saveImageToPhotos(imageURL: imageURL,albumName: name,date:date, imgUI: image!)
                    }
                }
            })
        }
    }
    
    
    func setNavigationBarButtons(){
        
        if isSaved {
              btnNavigationBarRight.isHidden = true
        }else{
              btnNavigationBarRight.isHidden = false
        }
        
        if isMultiSelectionOn {
            
            btnSideMenuToggle.isHidden = true
            btnNavigationBarLeft.isHidden = false
            btnNavigationBarRight.setTitle("Save", for: .normal)
            
        }else{
            
            btnSideMenuToggle.isHidden = false
            btnNavigationBarRight.setTitle("Select", for: .normal)
            btnNavigationBarLeft.isHidden = true
        }
        
        
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
                    
                    if strongSelf.arrSavedAlbums.count > 0 {
                        
                        strongSelf.arrSavedAlbums.removeAll()
                        
                    }
                    for i in 0..<strongSelf.photographers.count{
                        
                        strongSelf.arrSavedAlbums.append(strongSelf.photographers[i][0].photographerName)
                        
                    }
                    strongSelf.arrSavedAlbums = strongSelf.arrSavedAlbums.sorted(by: <)
                    //strongSelf.tblPgFolder.reloadData()
                }
                strongSelf.photographers =  strongSelf.photographers.sorted(by: { $0[0].photographerName < $1[0].photographerName})
                handler()
            }
        }
    }
    
    //  MARK:- Action methods
    @IBAction func btnToggle_Action(_ sender: UIButton) {
        toggleRightPanel()
        
        isMultiSelectionOn = false
        setNavigationBarButtons()
        multipleImages.removeAll()
        collectionView.reloadData()
    }
    
    @IBAction func btnSaveUnsave_Action(_ sender: UIButton) {
        
        if currentState == .rightPanelExpanded{
            toggleRightPanel()
            
        }
        isMultiSelectionOn = false
        multipleImages.removeAll()
        
        self.btnSaved.backgroundColor = sender.tag == 100 ? Color.celrianBlue : .clear
        self.btnUnsaved.backgroundColor = sender.tag == 200 ? Color.celrianBlue : .clear
        self.btnSaved.setTitleColor(sender.tag == 100 ? .white : Color.celrianBlue, for: .normal)
        self.btnUnsaved.setTitleColor(sender.tag == 200 ? .white : Color.celrianBlue, for: .normal)
        
        // self.viewToggle.isHidden = sender.tag == 100 ? true : false
        isSaved = sender.tag == 100 ? true : false
        if isSaved {
            
           

            // getImagesFromDirectory()
            guard photographers.count > 0 else {  return }
            
            arrAllSavedData.removeAll()
            getAlbumImageFromDrectory(albumName: photographers[selectedIndex][0].photographerName){
                
                self.collectionView.collectionViewLayout.invalidateLayout() // or reloadData()
                DispatchQueue.main.async {
                    FanHUD.hide()
                    
                }
            }
            setNavigationBarButtons()

            return
        }
        self.setNavigationBarButtons()
        getPhotos {
            
            self.collectionView.reloadData()
            
            

        }
    }
    @IBAction func btnNavigationBarRight_Action(_ sender: Any) {
        
        
        if currentState == .rightPanelExpanded{
            toggleRightPanel()
            
        }
        
        guard !isSaved else { return }
        
        if btnNavigationBarRight.title(for: .normal) == "Select" {
            
            isMultiSelectionOn = true
            setNavigationBarButtons()
            multipleImages.removeAll()
            collectionView.reloadData()
            
            
        } else {
            
            
            guard multipleImages.count > 0 else{
                showAlertWith(message: "Please Select Image")
                return
            }
            
            isMultiSelectionOn = false
            setNavigationBarButtons()
            
            //save Here
            FanHUD.show()
           
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1, execute: {
                for data in self.multipleImages{
                    self.saveImageToDirectory(imageURL: data.imageUrl, name: data.photographerName, date: data.createdAt, imgUI: UIImage())
                }
            })
            FanHUD.hide()
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1, execute: {
                self.showAlertWith(message: "Image Saved Successfully.")
                
            })
            collectionView.reloadData()
            
        }
    }
    @IBAction func btnNavigationBarLeft_Action(_ sender: Any) {
        
        isMultiSelectionOn = false
        setNavigationBarButtons()
        multipleImages.removeAll()
        collectionView.reloadData()
    }
    
}
extension AlbumVC: UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
    
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return isSaved ? arrAllSavedData[section].count :  photographers.count > 0 ? photographers[selectedIndex][section].photos.count : 0
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: CellIdentifiers.pgAlbumCell, for: indexPath) as! GalleryItemCollectionViewCell
        
        if isSaved {
            cell.imgViewAlbum.image = arrAllSavedData[indexPath.section][indexPath.row]["albumImage"] as? UIImage
            cell.ivSelected.isHidden = true
        }
        else{
            
            
            isMultiSelectionOn ? (cell.ivSelected.isHidden = false) : (cell.ivSelected.isHidden = true)
            let album = photographers[selectedIndex][indexPath.section].photos[indexPath.row]
            
            if  multipleImages.count > 0 {
                
                (multipleImages.filter({$0.imageUrl == album.imageURLString}).count > 0) ?  (cell.ivSelected.image = #imageLiteral(resourceName: "radio_on")) : (cell.ivSelected.image = #imageLiteral(resourceName: "radio_off"))
                
            }else{
                
                cell.ivSelected.image = #imageLiteral(resourceName: "radio_off")
                
            }
            
            cell.imgViewAlbum.setImageForAlbumFixedSize(with: URL.init(string: album.imageURLString)!)
            
        }
        
        return cell
    }
    
    func numberOfSections(in collectionView: UICollectionView) -> Int {
        print(selectedIndex)
        return isSaved ? arrAllSavedData.count : photographers.count > 0 ? photographers[selectedIndex].count : 0
    }
    
    func collectionView(_ collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, at indexPath: IndexPath) -> UICollectionReusableView {
        
        
        let commentView = collectionView.dequeueReusableSupplementaryView(ofKind: kind, withReuseIdentifier: CellIdentifiers.pgHeaderCell, for: indexPath) as! GalleryItemCommentView
        commentView.lblPgName.text = isSaved ? "\(currentSelectedAlbumName) Photos" : photographers[selectedIndex][indexPath.section].photographerName + " Photos"
        var date = ""
        if isSaved{
            
            date = Date(timeIntervalSince1970:arrAllSavedData[indexPath.section][indexPath.row]["albumDate"] as! Double).toString(format: .custom("EEE dd, yyyy"))
        }
        commentView.lblShootDate.text = isSaved ? date : photographers[selectedIndex][indexPath.section].createdAt.toString(format: .custom("EEE dd, yyyy"))
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
        
        
        if currentState == .rightPanelExpanded{
            toggleRightPanel()
        }
        
        if isSaved{
            
            let cell = collectionView.cellForItem(at: indexPath) as! GalleryItemCollectionViewCell
            let imageInfo      = GSImageInfo(image: arrAllSavedData[indexPath.section][indexPath.row]["albumImage"] as! UIImage, imageMode: .aspectFit, imageHD: nil)
            let transitionInfo = GSTransitionInfo(fromView: cell.imgViewAlbum)
            let imageViewer    = GSImageViewerController(imageInfo: imageInfo, transitionInfo: transitionInfo)
            imageViewer.isSaveButtonEnabled = false
            self.present(imageViewer, animated: true, completion: nil)
            
        }else{
            
            let cell = collectionView.cellForItem(at: indexPath) as! GalleryItemCollectionViewCell
            guard cell.imgViewAlbum.image != nil else { return }
            let imageInfo = GSImageInfo(image: cell.imgViewAlbum.image!, imageMode: .aspectFit, imageHD: nil)
            
            if isMultiSelectionOn{
                
                let album = photographers[selectedIndex][indexPath.section].photos[indexPath.row]
                if  multipleImages.filter({$0.imageUrl == album.imageURLString}).count > 0 {
                    
                    multipleImages.remove(at:multipleImages.index(of:multipleImages.filter({$0.imageUrl == album.imageURLString})[0])!)
//                    if multipleImages.count == 0 {
//                        isMultiSelectionOn = false
//                        setNavigationBarButtons()
//                    }
                } else {
                    
                    let data = MultipleImagesData()
                    data.imageUrl = album.imageURLString
                    data.createdAt =  photographers[selectedIndex][indexPath.section].createdAt
                    data.photographerName = photographers[selectedIndex][indexPath.section].photographerName
                    multipleImages.append(data)
                    
                }
                collectionView.reloadData()
                
                
            }else{
                let transitionInfo = GSTransitionInfo(fromView: cell.imgViewAlbum)
                let imageViewer    = GSImageViewerController(imageInfo: imageInfo, transitionInfo: transitionInfo)
                let img = imageViewer.imageInfo.image
                
                imageViewer.isSaveButtonEnabled = true
                imageViewer.callback = {
                    
                    self.saveImageToDirectory(imageURL: self.photographers[self.selectedIndex][indexPath.section].photos[indexPath.row].imageURLString, name: self.photographers[self.selectedIndex][indexPath.section].photographerName, date: self.photographers[self.selectedIndex][indexPath.section].createdAt, imgUI: img)
                    let photos = PHPhotoLibrary.authorizationStatus()
                    if photos == .notDetermined  || photos == .denied{
                        PHPhotoLibrary.requestAuthorization({status in
                            print(status)
                            if status == .denied {
                                
                                self.showAlertWith(message: "Image Not Saved Allow access for Photo Library from privacy.")
                                
                            }
                        })
                    }else{
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2, execute: {
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

// MARK: Table View Data Source
extension AlbumVC: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    
        return isSaved ? arrSavedAlbums.count :  photographers.count
        
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: CellIdentifiers.pgCell, for: indexPath) as! PhotographerCell
        if isSaved {
            cell.pgName.text = arrSavedAlbums[indexPath.row] + " Photos"
        }else{
            //cell.configureForPhotographer(photographers[indexPath.section][indexPath.row])
            cell.pgName.text = arrSavedAlbums[indexPath.row] + " Photos"
        }
        
        return cell
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
         return 1
    }
}

// Mark: Table View Delegate
extension AlbumVC: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        toggleRightPanel()

        if isSaved  {
            
            getAlbumImageFromDrectory(albumName: arrSavedAlbums[indexPath.row]){
                FanHUD.hide()
            }
        }
        selectedIndex = indexPath.row
        collectionView.reloadData()
    }
}
class MultipleImagesData: NSObject {
    var imageUrl: String!
    var createdAt: Date!
    var photographerName: String!
    
    override init() {}
    
}
