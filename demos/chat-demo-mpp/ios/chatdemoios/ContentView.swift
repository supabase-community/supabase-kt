//
//  ContentView.swift
//  chatdemoios
//
//  Created by Hieu Vu on 23/12/2023.
//

import SwiftUI
import common


struct ContentView: UIViewControllerRepresentable {
    let viewModel = RootComponent().getViewModel()
    func makeUIViewController(context: Context) -> UIViewController {
        return AppKt.AppIos(viewModel: viewModel)
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
