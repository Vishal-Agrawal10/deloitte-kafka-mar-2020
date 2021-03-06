// SimpleConsumer.java
package kafka.workshop;


import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

public class SimpleConsumer {
    public static String BOOTSTRAP_SERVERS = "localhost:9092,localhost:9093,localhost:9094";
    public static String TOPIC = "greetings";

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();

        props.put(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        // Consumer group id, should be unique, partition allocated to consumers inside teh group
        props.put(GROUP_ID_CONFIG, "greeting-consumer-groups2"); // offset, etc, TODO

        // -- true, automatically commit the offset, automatically
        // -- false, developers manually commit the offset
        props.put(ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, "100"); // applicable if auto commit true

        props.put(SESSION_TIMEOUT_MS_CONFIG, "30000");

        // key deserialize the bytes data into String format, JSON,AVRO formats
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        // value deserialize the bytes data into String format, JSON,AVRO formats

        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        // discussed later
        //  props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        // props.put(ConsumerConfig.CLIENT_ID_CONFIG, "your_client_id");
        // props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // <Key as string, Value as string>
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        TopicPartition topicPartition = new TopicPartition(TOPIC, 0);
        OffsetAndMetadata offsetMeta = consumer.committed(topicPartition);

        System.out.println("Last commit offset " + offsetMeta.offset());


        // subscribe from one or more topics
        consumer.subscribe(Collections.singletonList(TOPIC));

        System.out.println("Consumer starting..");


        List<PartitionInfo> partitions = consumer.partitionsFor(TOPIC);
        for (PartitionInfo partitionInfo: partitions) {
            // partitionInfo.
            System.out.println("Partition " + partitionInfo);
            System.out.println("Leader Node " + partitionInfo.leader());
        }


        while (true) {
            // Consumer poll for the data with wait time
            // Pulling from brokers
            // poll for msgs for 1 second, any messges within second, group together
            // if no msg, exit in 1 second, records length is 0
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            if (records.count() == 0)
                continue; // wait for more msg


            // Iterate record and print the record
            for (ConsumerRecord<String, String> record: records) {

                System.out.printf("partition=%d, offset=%d\n", record.partition(),
                        record.offset());

                System.out.printf("key=%s,value=%s\n", record.key(), record.value());


                // manual commit if ENABLE_AUTO_COMMIT_CONFIG is "false"
                // technically consumer send a message to broker about commited offset against consumer group
                consumer.commitSync();
                Thread.sleep(3000);
            }

            // Thread.sleep(2000);
        }

    }

}