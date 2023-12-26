//
//  ChatViewModel.swift
//  chatdemoios
//
//  Created by Hieu Vu on 26/12/2023.
//

import Foundation
import common


public class SampleObservableObject: ObservableObject {
    var viewModel: ChatViewModel
    
    init(wrapper: ChatViewModel) {
        viewModel = wrapper
    }
}

public extension  ChatViewModel {
    func asObservableObject() -> SampleObservableObject {
        return  SampleObservableObject(wrapper: self)
    }
}
