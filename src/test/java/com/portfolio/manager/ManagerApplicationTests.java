package com.portfolio.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
class ManagerApplicationTests {

    public int[] maxSlidingWindow(int[] nums, int k) {
        List<Integer> ans = new ArrayList<>();
        Deque<Integer> window = new LinkedList<>();
        window.offer(nums[0]);
        for (int i = 1; i < k; i++) {
            if (nums[i] > window.peekFirst()) {
                window.poll();
            }
            window.offer(nums[i]);
        }
        ans.add(window.peekFirst());
        for (int i = k; i < nums.length; i++) {
            if(window.size() == k){
                window.poll();
            }
            if (window.peekFirst() != null && nums[i] > window.peekFirst()) {
                while (window.peekFirst() != null && nums[i] > window.peekFirst()) {
                    window.poll();
                }
            }
            window.offer(nums[i]);
            ans.add(window.peekFirst());
        }
        return ans.stream().mapToInt(i -> i).toArray();
    }

    @Test
    void contextLoads() {
        String str = "rat";
        char[] chars = str.toCharArray();
        Arrays.sort(chars);
        System.out.println(chars);
    }

}
