//
//  FaqData.swift
//  FanCustomer
//
//  Created by Codiant on 11/24/17.
//  Copyright © 2017 Codiant. All rights reserved.
//

import UIKit

class FaqData: NSObject {
    public var question: String
    public var answers: [AnswerData]?
    
    init(question: String, answer: [AnswerData]?) {
        self.question = question
        self.answers = answer
    }
}

public class AnswerData {
    public var answer: String
    
    init(answer: String) {
        self.answer = answer
    }
}
