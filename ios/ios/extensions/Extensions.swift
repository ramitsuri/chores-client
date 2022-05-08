//
//  Extensions.swift
//  ios
//
//  Created by Ramit Suri on 5/5/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation

extension Optional where Wrapped: Collection {
    var isEmptyOrNil: Bool {
        return self?.isEmpty ?? true
    }
}
