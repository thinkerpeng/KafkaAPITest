package com.pwx.test;

import org.apache.zookeeper.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Desc: Apache Zookeeper API 使用
 * Creator: pengweixiang
 * Date: 2019-06-14
 */
public class ZkClientDemo
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkClientDemo.class);

    private static final int SESSION_TIME_OUT = 6000;
    private static final String CONNECT_STRING = "192.168.31.162:2181";
    private static final String WATCH_PATH = "/test";
    private ZooKeeper zkClient;
    private CountDownLatch connectedSemaphore = new CountDownLatch(1);

    @Before
    public void init() throws Exception
    {
        zkClient = new ZooKeeper(CONNECT_STRING, SESSION_TIME_OUT, new Watcher()
        {
            @Override
            public void process(WatchedEvent event)
            {
                if (connectedSemaphore.getCount() > 0 && event.getState() == Watcher.Event.KeeperState.SyncConnected)
                {
                    // 已连接ZK
                    connectedSemaphore.countDown();
                }
                if (event.getType() == Watcher.Event.EventType.NodeDataChanged && WATCH_PATH.equals(event.getPath()))
                {
                    System.out.println("Path: " + event.getPath());
                    try
                    {
                        byte[] data = zkClient.getData(WATCH_PATH, true, null);
                        System.out.println("New data: " + new String(data));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
        //等待zk连接
        connectedSemaphore.await();
    }

    /**
     * 创建znode节点
     * @throws Exception exception
     */
    @Test
    public void create() throws Exception
    {
        zkClient.create(WATCH_PATH, "null".getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    /**
     * 修改znode的值
     * @throws Exception exception
     */
    @Test
    public void setData() throws Exception
    {
        zkClient.setData(WATCH_PATH, "ShenZhen".getBytes("UTF-8"), -1);
    }

    /**
     * 获取znode的值
     * @throws Exception exception
     */
    @Test
    public void getData() throws Exception
    {
        byte[] data = zkClient.getData(WATCH_PATH, false, null);
        LOGGER.info("Read znode data: {}", new String(data));
    }
}
