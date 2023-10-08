package com.example;

import java.util.ArrayList;
import java.util.List;

public class Subset {
    public static void combination(int[] nums, int n, List<List<Integer>> ans) {
        List<Integer> cur = new ArrayList<>();
        int[] used = new int[nums.length];
        backtrack(cur, nums, used, n, ans, 0);
    }
    public static void backtrack(List<Integer> cur, int[] nums, int[] used, int n, List<List<Integer>> ans, int start) {
        if (cur.size() == n) {
            ans.add(new ArrayList<>(cur));
            return;
        }
        for (int i = start; i < nums.length; i++) {
            if (used[i] == 0) {
                used[i] = 1;
                cur.add(nums[i]);
                backtrack(cur, nums, used, n, ans, i + 1);
                used[i] = 0;
                cur.remove(cur.size() - 1);
            }
        }
    }
    public static List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> ans = new ArrayList<>();
        for (int i = 0; i <= nums.length; i++) {
            combination(nums, i, ans);
        }
        return ans;
    }
}
