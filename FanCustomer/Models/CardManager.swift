//
//  CardManager.swift
//  FanCustomer
//
//  Created by Codiant on 12/19/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import Foundation

class CardManager {
    
    var cards: [Card]!
    var cardsFirstFetch = false
    
    static let shared: CardManager = {
        let instance = CardManager()
        instance.cards = [Card]()
        return instance
    }()
    
    
    func refreshPublisherKey(_ closure: @escaping(Bool)->Void) {
        guard STPPaymentConfiguration.shared().publishableKey.count == 0 else {
            closure(true)
            return
        }
        /*
        STPPaymentConfiguration.shared().publishableKey = "pk_test_PXJXzT4pZI6A1cMC4MtNB0ph"
        closure(true)
        */
        APIComponents.Payment.getStripePublisherKey { (success, data, error) in
            guard success, error == nil else {
                DDLogDebug("Error occured while initializing payment gateway")
                closure(false)
                return
            }
            
            if let object = data!.deserialize(),
                let data = object["data"] as? HTTPParameters,
                let pk_Key = data["stripe_key"] as? String {
                STPPaymentConfiguration.shared().publishableKey = pk_Key
                closure(true)
            }
            else {
                closure(false)
            }
        }
    }
    
    func tokenizeCard(params: STPCardParams, closure: @escaping(STPToken?, Error?)->Void) {
        STPAPIClient.shared().createToken(withCard: params) { (token, error) in
            closure(token, error)
        }
    }
    
    func addCard(token: String, closure: @escaping(Bool, Data?, Error?)->Void) {
        
        APIComponents.Payment.addCard(tokenId: token) { (success, data, error) in
            closure(success, data, error)
     }
    }
    
    func deleteCard(cardId: String, closure: @escaping(Bool)->Void) {
        APIComponents.Payment.deleteCard(cardId: cardId) { [unowned self] (success, data, error) in
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    DDLogDebug("Error occured while deleting card: " + message)
                }
                
                closure(false)
                return
            }
            
            if let index = self.cards.index(where: { (card) -> Bool in
                return card.card_id == cardId
            }) {
                self.cards.remove(at: index)
                self.cards.sort(by: {$0.isDefault && !$1.isDefault})
                closure(true)
            }
        }
    }
    
    func defaultCard(cardId: String, closure: @escaping(Bool)->Void) {
        APIComponents.Payment.defaultCard(cardId: cardId) { [unowned self] (success, data, error) in
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    DDLogDebug("Error occured while setting default card: " + message)
                }
                
                closure(false)
                return
            }
            
            for item in self.cards {
                item.isDefault = item.id == cardId
            }
            
            self.cards.sort(by: {$0.isDefault && !$1.isDefault})
            closure(true)
        }
    }
    
    func fetchCards(_ closure: @escaping()->Void) {
        
        // Return If already fetch is done
        guard !cardsFirstFetch else {
            closure()
            return
        }
        
        cards.removeAll()   // Remove all cards before the fetch
        APIComponents.Payment.fetchCards { [unowned self] (success, data, error) in
            guard success, error == nil else {
                if let message = error?.localizedDescription {
                    DDLogDebug("Error occured while fetching cards: " + message)
                }
                
                closure()
                return
            }
            
            if let object = data!.deserialize(),
                let data = object["data"] as? [HTTPParameters] {
                
                for item in data {
                    self.cards.append(Card.map(JSONObject: item, context: nil))
                }
                
                self.cards.sort(by: {$0.isDefault && !$1.isDefault})
                self.cardsFirstFetch = true
                closure()
            }
            else {
                closure()
            }
        }
    }
}

