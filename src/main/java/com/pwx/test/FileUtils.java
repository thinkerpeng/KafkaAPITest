package com.pwx.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Desc:
 * Creator: pengweixiang
 * Date: 2019-06-22
 */
public class FileUtils
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaApiDemo.class);

    public static Properties getProps(String filePath)
    {
        InputStream is = ClassLoader.getSystemResourceAsStream(filePath);
        Properties props = new Properties();

        if (is == null)
        {
            LOGGER.error("Can not find properties file: {}", filePath);
            return props;
        }

        try
        {
            props.load(is);
        }
        catch (IOException e)
        {
            LOGGER.error("Load properties file failed. error msg: {}", e.getMessage());
        }

        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                LOGGER.error("Close inputStream failed.");
            }
        }
        return props;
    }
}
