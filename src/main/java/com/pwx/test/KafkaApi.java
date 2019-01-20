package com.pwx.test;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZkUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.security.JaasUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Desc: Kafka API Test
 * Creator: pengweixiang
 * Date: 2019-01-13
 */
public class KafkaApi
{
	private static final String ZK_CONNECTOR = "192.168.31.201:2181";

	private static final String TOPIC_NAME_CREATE = "topic_create";

	private static final int PRODUCE_PERIOD = 50;

	private static final int CONSUME_PERIOD = 100;

	private static AtomicInteger count = new AtomicInteger(0);

	public static void main(String[] args)
	{
		KafkaApi kafkaApi = new KafkaApi();
		kafkaApi.produceMessage();
		//kafkaApi.consumeMessage();
		//kafkaApi.createTopic();
	}

	private void produceMessage()
	{
		Properties props = new Properties();
		props.put("bootstrap.servers", "192.168.31.229:9092");
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		Producer<String, String> producer = new KafkaProducer<String, String>(props);

		Runnable produceTask = () ->
		{
			ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME_CREATE,
					"key", "message-" + Integer.toString(count.get()));
			//异步生产消息
			producer.send(record, (recordMetadata, e) ->
			{
				if (!Objects.isNull(e))
				{
					e.printStackTrace();
				}
				else
				{
					System.out.println("send message success. " + count.incrementAndGet());
				}
			});
		};
		ScheduledExecutorService produceService = Executors.newSingleThreadScheduledExecutor();
		produceService.scheduleAtFixedRate(produceTask, 0, PRODUCE_PERIOD, TimeUnit.MILLISECONDS);
	}

	private void consumeMessage()
	{
		Properties props = new Properties();
		props.put("bootstrap.servers", "192.168.31.229:9092");
		props.put("group.id", TOPIC_NAME_CREATE);
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		//订阅主题
		consumer.subscribe(Collections.singletonList(TOPIC_NAME_CREATE));

		Runnable consumeTask = () ->
		{
			ConsumerRecords<String, String> records = consumer.poll(CONSUME_PERIOD);
			records.forEach(record ->
			{
				System.out.println("receive message " + record.value());
			});
		};
		ScheduledExecutorService consumeService = Executors.newSingleThreadScheduledExecutor();
		consumeService.scheduleAtFixedRate(consumeTask, 0, 1000, TimeUnit.MILLISECONDS);
	}

	private void createTopic()
	{
		ZkUtils zkUtils = ZkUtils.apply(ZK_CONNECTOR, 30000, 30000, JaasUtils.isZkSecurityEnabled());
		AdminUtils.createTopic(zkUtils, TOPIC_NAME_CREATE, 1, 1, new Properties(), RackAwareMode.Enforced$.MODULE$);
		zkUtils.close();
		//TopicCommand.createTopic();
	}
}
