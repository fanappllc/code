//
//  CountryPicker.swift
//  TaxiAppUKCustomer
//
//  Created by Codiant on 8/25/17.
//  Copyright © 2017 Codiant Software Technologies Pvt ltd. All rights reserved.
//

import Foundation


class CountryPicker {
    
    static func countryNamesByCode() -> [Country]  {
        
        var countries = [Country]()
        
        let frameworkBundle = Bundle(for: type(of: CountryPicker()))
        
        guard let jsonPath = frameworkBundle.path(forResource: "CountryPicker.bundle/Data/countryCodes", ofType: "json"), let jsonData = try? Data(contentsOf: URL(fileURLWithPath: jsonPath)) else {
            return countries
        }
        
        do {
            if let jsonObjects = try JSONSerialization.jsonObject(with: jsonData, options: JSONSerialization.ReadingOptions.allowFragments) as? NSArray {
                
                for jsonObject in jsonObjects {
                    
                    guard let countryObj = jsonObject as? [String: String] else {
                        return countries
                    }
                    
                    guard let code = countryObj["code"], let phoneCode = countryObj["dial_code"], let name = countryObj["name"] else {
                        return countries
                    }
                    
                    let flagName = "CountryPicker.bundle/Images/\(code.uppercased())"
                    
                    let country = Country(code: code, name: name, phoneCode: phoneCode, flagName: flagName)
                    
                    countries.append(country)
                }
                
            }
        }
        catch {
            return countries
        }
        
        return countries
    }
    
    static func currentCountry() -> Country? {
        if let countryCode = Locale.current.regionCode {
            return self.countryNamesByCode().filter( {$0.code == countryCode} ).first
        }
        
        return nil
    }
    
    static func countryFor(region: String?) -> Country? {
        return self.countryNamesByCode().filter( {$0.code == region} ).first
    }
    
}

struct Country {
    
    var code: String?
    var name: String?
    var phoneCode: String?
    var flagName: String
    
    
    init(code: String?, name: String?, phoneCode: String?, flagName: String) {
        self.code = code
        self.name = name
        self.phoneCode = phoneCode
        self.flagName = flagName
    }
    
    var flag: UIImage? {
        return UIImage(named: flagName, in: Bundle(for: CountryPicker.self), compatibleWith: nil)
    }
}
