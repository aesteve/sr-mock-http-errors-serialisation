package com.github.aesteve

import io.confluent.kafka.serializers.{AbstractKafkaSchemaSerDeConfig, KafkaAvroSerializer, KafkaAvroSerializerConfig}
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.{AdminClient, CreateTopicsOptions, NewTopic}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import java.time.Duration
import java.util.Properties
import scala.jdk.CollectionConverters.*

val TopicCount = 300
val PartitionsPerTopic = 1
val NbRecordsQueued = 1_000_000

@main def runAvroProducerWithNoCache(): Unit =
  setupEnv()

  // Avro setup
  val userSchema: String = "{\"type\":\"record\"," + "\"name\":\"something\"," + "\"fields\":[{\"name\":\"somestring\",\"type\":\"string\"}]}"
  val parser = Schema.Parser()
  val schema = parser.parse(userSchema)

  // Kafka Producer setup
  val producerProps = new Properties() {
    put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[KafkaAvroSerializer].getName) // TODO: Avro here
    put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081")
  }
  val producer = new KafkaProducer[String, GenericData.Record](producerProps)


  (1 to NbRecordsQueued).foreach(i => {
    val topic = s"topic-${i % TopicCount}"
    val avroRecord = new GenericData.Record(schema)
    avroRecord.put("somestring", s"value-$i")
    val record = new ProducerRecord(topic, "", avroRecord)
    producer.send(record)
  })
  producer.flush()



def setupEnv(): Seq[NewTopic] =
  val props = new Properties() {
    put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  }
  val adminClient = AdminClient.create(props)
  val existingTopics = adminClient.listTopics.names.get
  if (!existingTopics.isEmpty)
    adminClient.deleteTopics(existingTopics).all.get
    Thread.sleep(Duration.ofSeconds(1))

  val topics = (1 to TopicCount).map(i => {
    new NewTopic(s"topic-$i", PartitionsPerTopic, 1.toShort)
  })
  adminClient.createTopics(topics.asJava).all.get // wait for success
  topics
