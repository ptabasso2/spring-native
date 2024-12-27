package com.datadoghq.pej.spring_native;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
public class PerfController {

    @GetMapping("/cpu")
    public String simulateCpuIssue() {
        // Generate a large dataset to simulate load
        int[] dataset = new int[1000];
        for (int i = 0; i < dataset.length; i++) {
            dataset[i] = i;
        }
        // Simulate an inefficient algorithm: finding triplets with sum = 1000
        int targetSum = 1000;
        int count = findTripletsWithSum(dataset, targetSum);
        return "Task completed! Triplets found: " + count;
    }

    private int findTripletsWithSum(int[] arr, int target) {
        int count = 0;
        int n = arr.length;
        // Triple nested loop - O(n³) complexity
        for (int i = 0; i < n - 2; i++) {
            for (int j = i + 1; j < n - 1; j++) {
                for (int k = j + 1; k < n; k++) {
                    if (arr[i] + arr[j] + arr[k] == target) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private final List<Object> memoryHog = new ArrayList<>();

    @GetMapping("/memory")
    public String simulateMemoryIssue() {
        try {
            // Continuously add large objects to the list
            for (int i = 0; i < 100_000; i++) {
                byte[] chunk = new byte[200_000]; // 1 MB allocation
                memoryHog.add(chunk);
            }
        } catch (OutOfMemoryError e) {
            return "Memory issue simulated: " + e.getMessage();
        }
        return "Memory issue simulation completed. Memory usage might be high!";
    }
}

