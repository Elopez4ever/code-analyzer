package com.analyzer.modules.project.progress;

@FunctionalInterface
public interface ItemProgressCallback {
    void onItem(String itemName, int index, int total);
}
