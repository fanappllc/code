//
//  PaymentComponent.swift
//  FanCustomer
//
//  Created by Codiant on 12/19/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

struct PaymentComponent {
    static func getStripePublisherKey(_ closure: @escaping (Bool, Data?, Error?)->Void) {
        NSSession.shared.requestWith(path: "customer-stripe-key", method: .get, parameters: nil, retryCount: 2) { (success, data, error) in
            closure(success, data, error)
        }
    }

    #if FANCUSTOMER

    static func addCard(tokenId: String, closure: @escaping (Bool, Data?, Error?)->Void) {
    NSSession.shared.requestWith(path: "add-card", method: .post, parameters: ["stripe_token": tokenId], retryCount: 2) { (success, data, error) in
    closure(success, data, error)
    }
    }
    
    static func defaultCard(cardId: String, closure: @escaping (Bool, Data?, Error?)->Void) {
    NSSession.shared.requestWith(path: "card/" + cardId + "/default", method: .post, parameters: nil, retryCount: 2) { (success, data, error) in
    closure(success, data, error)
    }
    }
    
    static func deleteCard(cardId: String, closure: @escaping (Bool, Data?, Error?)->Void) {
    NSSession.shared.requestWith(path: "delete-card/" + cardId, method: .delete, parameters: nil, retryCount: 2) { (success, data, error) in
    closure(success, data, error)
    }
    }
    
    static func fetchCards(_ closure: @escaping (Bool, Data?, Error?)->Void) {
    NSSession.shared.requestWith(path: "get-my-cards", method: .get, parameters: nil, retryCount: 2) { (success, data, error) in
    closure(success, data, error)
    }
    }
    
    #else
    
    static func transactionList(parameters: HTTPParameters, _ closure: @escaping (Bool, Data?, Error?)->Void) {
        
        let path = "photographer/" + Profile.shared.id + "/transaction"
        
        NSSession.shared.requestWith(path: path, method: .get, parameters: parameters, retryCount: 2) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func addBankAccount(parameters: HTTPParameters, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        let path = "add-bank-account"
        
        NSSession.shared.requestWith(path: path, method: .post, parameters: parameters, retryCount: 2) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    static func registrationPayment(parameters: HTTPParameters, closure: @escaping (Bool, Data?, Error?)->Void) {
        
        let path = "photographer-registration-payment"
        
        NSSession.shared.requestWith(path: path, method: .post, parameters: parameters, retryCount: 2) { (success, data, error) in
            closure(success, data, error)
        }
    }
    
    #endif
    
}
