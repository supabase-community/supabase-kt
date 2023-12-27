//
//  chatdemoiosApp.swift
//  chatdemoios
//
//  Created by Hieu Vu on 23/12/2023.
//

import SwiftUI
import common


@main
struct chatdemoiosApp: App {
    
    init() {
        KoinKt.doInitKoin(additionalConfiguration: {_ in})
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
