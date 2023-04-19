package com.babata.concurrent.util;

import java.util.function.Consumer;

/**
 * 分片工具类
 * @author zqj
 */
public class PartitionUtil {

    public static class Node {
        public Integer index;
        public Node next;

        public Node(Integer index) {
            this.index = index;
            this.next = null;
        }

    }

    /**
     * 自动分片
     * @param batchCount 批次总数
     * @param partitionSize 每片包含的批次数
     * @param doAction 分片后回调
     */
    public static void partition(int batchCount, int partitionSize, Consumer<Node> doAction) {
        Node focus = new Node(0);
        Node next = null;
        for (int i = 1; i < batchCount; i++) {
            if(next == null) {
                next = new Node(i);
                focus.next = next;
            } else {
                next = next.next = new Node(i);
            }
        }
        Node prev = focus;
        Node first = focus;
        int pos = 0;
        int turns = 0;
        while(focus != null) {
            Node expectNext = focus.next;
            if(pos++ % (partitionSize - turns) == 0) {
                //执行
                doAction.accept(focus);
                if(prev != focus) {
                    prev.next = expectNext;
                } else {
                    prev = first = expectNext;
                }
            } else {
                prev = focus;
            }
            if(expectNext == null) {
                prev = focus = first;
                pos = 0;
                turns++;
            } else {
                focus = expectNext;
            }
        }
    }
}
