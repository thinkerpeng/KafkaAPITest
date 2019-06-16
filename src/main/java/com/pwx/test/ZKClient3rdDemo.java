package com.pwx.test;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Desc: I0Itec zk 第三方客户端
 * Creator: pengweixiang
 * Date: 2019-06-16
 */
public class ZKClient3rdDemo
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ZKClient3rdDemo.class);

    private static final int SESSION_TIME_OUT = 6000;
    private static final String CONNECT_STRING = "192.168.31.162:2181";
    private static final String WATCH_PATH = "/rest";
    private static final String WATCH_PATH_DIR1 = "/rest/dir1";
    private static final String WATCH_PATH_DIR2 = "/rest/dir2";

    private ZkClient zkClient;

    @Before
    public void init()
    {
        zkClient = new ZkClient(CONNECT_STRING, SESSION_TIME_OUT);
    }

    @Test
    public void createZnode()
    {
        if (zkClient.exists(WATCH_PATH_DIR1))
        {
            LOGGER.warn("Znode exist: {}", WATCH_PATH_DIR1);
            return;
        }
        //创建递归节点，不担心不存在父节点
        zkClient.createPersistent(WATCH_PATH_DIR1, true);


        if (zkClient.exists(WATCH_PATH_DIR2))
        {
            LOGGER.warn("Znode exist: {}", WATCH_PATH_DIR2);
            return;
        }
        //创建递归节点，不担心不存在父节点
        zkClient.createPersistent(WATCH_PATH_DIR2, true);
    }

    @Test
    public void readZnode()
    {
        String value = zkClient.readData(WATCH_PATH_DIR1);
        LOGGER.info("Read znode value: {}", value);
    }

    @Test
    public void writeZnode()
    {
        zkClient.writeData(WATCH_PATH_DIR1, "hello world!");
    }

    @Test
    public void queryChildren()
    {
        List<String> children = zkClient.getChildren("/rest");
        children.forEach(path -> LOGGER.info("Children path: {}", path));
    }

    @Test
    public void deleteZnode()
    {
        zkClient.delete(WATCH_PATH_DIR1);
        zkClient.delete(WATCH_PATH_DIR2);
    }

    /**
     * 监听znode节点
     * @throws Exception exception
     */
    @Test
    public void watchZnode() throws Exception
    {
        zkClient.subscribeChildChanges(WATCH_PATH, new IZkChildListener()
        {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception
            {
                LOGGER.info("Parent path: {}, children path: {}", parentPath, currentChilds);
            }
        });

        zkClient.subscribeDataChanges(WATCH_PATH_DIR1, new IZkDataListener()
        {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception
            {
                LOGGER.info("Data path: {}, new value: {}", dataPath, data);
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception
            {
                LOGGER.info("Delete data path: {}", dataPath);
            }
        });

        zkClient.subscribeDataChanges(WATCH_PATH_DIR2, new IZkDataListener()
        {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception
            {
                LOGGER.info("Data path: {}, new value: {}", dataPath, data);
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception
            {
                LOGGER.info("Delete data path: {}", dataPath);
            }
        });

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        zkClient.createPersistent(WATCH_PATH_DIR1, true);
        zkClient.createPersistent(WATCH_PATH_DIR2, true);

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        zkClient.writeData(WATCH_PATH_DIR1, "dir1");
        zkClient.writeData(WATCH_PATH_DIR2, "dir2");

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        zkClient.delete(WATCH_PATH_DIR1);
        zkClient.delete(WATCH_PATH_DIR2);

        Thread.sleep(TimeUnit.SECONDS.toMillis(1000));
    }

    @After
    public void closeZkClient()
    {
        zkClient.close();
    }
}
