//
//  ContentView.swift
//  chatdemoios
//
//  Created by Hieu Vu on 23/12/2023.
//

import SwiftUI
import common

struct ContentView: View {
    
    var body: some View {
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text("Hello, world!")
        }
        .padding()
    }
}

struct MyScreenToSwiftUI: UIViewControllerRepresentable {
    
    func makeUIViewController(context: Context) -> UIViewController {
        return AppKt.AppIos(viewModel: RootComponent().viewModel)
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

#Preview {
    ContentView()
}
