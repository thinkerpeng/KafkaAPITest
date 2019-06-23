package com.pwx.test;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.KafkaFuture;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Desc: Kafka API Test
 * Creator: pengweixiang
 * Date: 2019-01-13
 */
public class KafkaApiDemo
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaApiDemo.class);

	private static final String TOPIC_NAME_CREATE = "topic002";

	private static final int PRODUCE_PERIOD = 500;

	private static final int CONSUME_PERIOD = 1000;

	private static AtomicInteger count = new AtomicInteger(0);

	@Test
	public void produceMessage() throws InterruptedException
    {
        Properties props = FileUtils.getProps("config/produce.properties");
        Producer<String, String> producer = new KafkaProducer<>(props);
        CountDownLatch produceFinish = new CountDownLatch(1);
		ScheduledExecutorService produceService = Executors.newSingleThreadScheduledExecutor();
		produceService.scheduleAtFixedRate(() ->
        {
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME_CREATE,
                    "key", "message-" + Integer.toString(count.get()));
            //异步生产消息
            producer.send(record, (recordMetadata, e) ->
            {
                if (!Objects.isNull(e))
                {
                    LOGGER.error("produce message failed. error msg: {}", e.getMessage());
                }
                else
                {
                    LOGGER.info("produce message success.{}, partition:{}, offset: {}",
                            count.incrementAndGet(), recordMetadata.partition(), recordMetadata.offset());
                }
            });
        }, 0, PRODUCE_PERIOD, TimeUnit.MILLISECONDS);
		//等待主线程结束
        produceFinish.await();
        producer.close();
	}

	@Test
	public void consumeMessage() throws InterruptedException
	{
        Properties props = FileUtils.getProps("config/consume.properties");
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        CountDownLatch consumeFinish = new CountDownLatch(1);
		//订阅主题
		consumer.subscribe(Collections.singletonList(TOPIC_NAME_CREATE));
		ScheduledExecutorService consumeService = Executors.newSingleThreadScheduledExecutor();
		consumeService.scheduleAtFixedRate(() ->
        {
            ConsumerRecords<String, String> records = consumer.poll(CONSUME_PERIOD);
            records.forEach(record ->
            {
                LOGGER.info("receive key:{}, value:{}, offset:{}",
                        record.key(), record.value(), record.offset());
            });
        }, 0, 1000, TimeUnit.MILLISECONDS);
        //等待主线程结束
        consumeFinish.await();
	}

	@Test
	public void createTopic() throws InterruptedException
	{
	    Properties props = FileUtils.getProps("config/adminclient.properties");
        AdminClient client = AdminClient.create(props);
        NewTopic newTopic1 = new NewTopic("topic005", 1, (short) 1);
        NewTopic newTopic2 = new NewTopic("topic006", 1, (short) 1);
        CreateTopicsResult result =client.createTopics(Arrays.asList(newTopic1, newTopic2));
        result.values().forEach((topicCallback, future) ->
        {
            try
            {
                future.get(5, TimeUnit.SECONDS);
                if (future.isDone())
                {
                    LOGGER.info("Create topic success. topic name: {}", topicCallback);
                }
            }
            catch (Exception e)
            {
                LOGGER.error("Create topic failed. topic name: {}, error msg: {}", topicCallback, e.getMessage());
            }
        });
	}

	@Test
	public void deleteTopic()
    {
        Properties props = FileUtils.getProps("config/adminclient.properties");
        AdminClient client = AdminClient.create(props);
        DeleteTopicsResult result = client.deleteTopics(Arrays.asList("topic005", "topic006"));
        result.values().forEach((topicCallback, future) ->
        {
            try
            {
                future.get(5, TimeUnit.SECONDS);
                if (future.isDone())
                {
                    LOGGER.info("Delete topic success. topic name: {}", topicCallback);
                }
            }
            catch (Exception e)
            {
                LOGGER.error("Delete topic failed. topic name: {}, error msg: {}", topicCallback, e.getMessage());
            }
        });
    }

    @Test
    public void listTopic()
    {
        Properties props = FileUtils.getProps("config/adminclient.properties");
        AdminClient client = AdminClient.create(props);
        ListTopicsResult result = client.listTopics();
        KafkaFuture<Collection<TopicListing>> future = result.listings();
        try
        {
            Collection<TopicListing> topicListings = future.get(5, TimeUnit.SECONDS);
            if (future.isDone())
            {
                topicListings.stream().forEach(topicListing ->
                {
                    LOGGER.info("Topic: {}", topicListing.toString());
                });
            }
        }
        catch (Exception e)
        {
            LOGGER.error("List topic failed. error msg: {}", e.getMessage());
        }
    }
}
