package com.davejy.modelsimplification;

/**
 * 基于二叉堆和平行数组的索引最小优先队列，参考Algorithms, Fourth Edition
 *
 * Created by Jiao Yue on 2017/4/27.
 */

class IndexMinPQ<Key extends Comparable<Key>> {

    private int N;         // PQ中元素的数量
    private int[] pq;      // 索引二叉堆，由1开始
    private int[] qp;      // 逆序：qp[pq[i]] = pq[qp[i]] = i, 对应着元素索引在二叉堆中的位置
    private Key[] keys;    // 有优先级之分的元素

    /**
     * 创建一个最大容量为maxN 的优先队列，索引的取值范围为0至maxN-1
     * @param maxN 最大容量
     */
    public IndexMinPQ(int maxN) {
        keys = (Key[]) new Comparable[maxN + 1];
        pq = new int[maxN + 1];
        qp = new int[maxN + 1];
        for (int i = 0; i <= maxN; i++) {
            qp[i] = -1;
        }
    }

    /**
     * 优先队列中的元素数量
     * @return
     */
    public int size() {
        return N;
    }

    /**
     * 优先队列是否为空
     * @return
     */
    public boolean isEmpty() {
        return N == 0;
    }

    /**
     * 是否存在索引为k的元素
     * @param k
     * @return
     */
    public boolean contains(int k) {
        return qp[k] != -1;
    }

    /**
     * 插入一个元素，将它和索引k相关联
     * @param k
     * @param key
     */
    public void insert(int k, Key key) {
        if (contains(k)) {
            throw new RuntimeException();
        }
        N++;
        qp[k] = N;
        pq[N] = k;
        keys[k] = key;
        swim(N);
    }

    /**
     * 返回最小的元素
     * @return
     */
    public Key min() {
        return keys[pq[1]];
    }

    /**
     * 删除最小元素并返回它的索引
     * @return 元素索引
     */
    public int delMin() {
        int indexOfMin = pq[1];
        exch(1, N--);
        sink(1);
        keys[pq[N + 1]] = null;
        qp[pq[N + 1]] = -1;
        return indexOfMin;
    }

    /**
     * 返回最小元素的索引
     * @return
     */
    public int minIndex() {
        return pq[1];
    }

    /**
     * 将索引k的元素设为key
     * @param k
     * @param key
     */
    public void change(int k, Key key) {
        if (!contains(k)) {
            throw new RuntimeException();
        }
        keys[k] = key;
        swim(qp[k]);
        sink(qp[k]);
    }

    /**
     * 删去索引k及其相关联的元素
     * @param k
     */
    public void delete(int k) {
        if (!contains(k)) {
            throw new RuntimeException();
        }
        int index = qp[k];
        exch(index, N--);
        swim(index);
        sink(index);
        keys[k] = null;
        qp[k] = -1;
    }


    /**
     * 上浮堆中的第k个元素
     * @param k
     */
    private void swim(int k) {
        while (k > 1 && less(k / 2, k)) {
            exch(k / 2, k);
            k = k / 2;
        }
    }

    /**
     * 下沉堆中的第k个元素
     * @param k
     */
    private void sink(int k) {
        while (2 * k < N) {
            int j = 2 * k;
            if (j < N && less(j, j + 1)) {
                j++;
            }
            if (!less(k, j)) {
                break;
            }
            exch(k, j);
            k = j;
        }
    }

    /**
     * keys[pq[j]]是否小于keys[pq[i]]
     * @param i
     * @param j
     * @return
     */
    private boolean less(int i, int j) {
        return keys[pq[i]].compareTo(keys[pq[j]]) > 0;
    }

    /**
     * 交换pq[i]和pq[j]
     * @param i
     * @param j
     */
    private void exch(int i, int j) {
        int t = pq[i];
        pq[i] = pq[j];
        pq[j] = t;

        qp[pq[i]] = i;
        qp[pq[j]] = j;
    }


    /**
     * 单元测试代码
     * @param args
     */
    public static final void main(String[] args) {
        IndexMinPQ<String> pq = new IndexMinPQ<>(10);
        pq.insert(0, "b");
        pq.insert(2, "a");
        pq.insert(3, "c");
        pq.insert(4, "f");
        pq.insert(5, "e");
        pq.insert(6, "d");
        pq.insert(7, "c");

        System.out.println(pq.size());
        System.out.println(pq.contains(10));
        System.out.println(pq.minIndex() + " " + pq.min());
        System.out.println("----------------------------------\n");

        pq.delMin();

        System.out.println(pq.size());
        System.out.println(pq.contains(10));
        System.out.println(pq.minIndex() + " " + pq.min());
        System.out.println("----------------------------------\n");

        pq.delMin();

        System.out.println(pq.size());
        System.out.println(pq.contains(10));
        System.out.println(pq.minIndex() + " " + pq.min());
        System.out.println("----------------------------------\n");

        pq.change(7, "a");
        System.out.println(pq.size());
        System.out.println(pq.contains(10));
        System.out.println(pq.minIndex() + " " + pq.min());
        System.out.println("----------------------------------\n");

    }
}
