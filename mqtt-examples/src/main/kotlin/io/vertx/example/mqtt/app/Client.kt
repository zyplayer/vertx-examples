package io.vertx.example.mqtt.app

import io.netty.handler.codec.mqtt.MqttQoS
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.kotlin.mqtt.*
import io.vertx.mqtt.MqttClient
import io.vertx.mqtt.MqttClientOptions

class Client : io.vertx.core.AbstractVerticle()  {
  var MQTT_MESSAGE = "Hello Vert.x MQTT Client"
  var BROKER_HOST = "localhost"
  var MQTT_TOPIC = "/my_topic"
  var BROKER_PORT = 1883
  override fun start() {
    var options = MqttClientOptions(
      keepAliveTimeSeconds = 2)

    var client = MqttClient.create(Vertx.vertx(), options)


    // handler will be called when we have a message in topic we subscribing for
    client.publishHandler({ publish ->
      println("Just received message on [${publish.topicName()}] payload [${publish.payload().toString(java.nio.charset.Charset.defaultCharset())}] with QoS [${publish.qosLevel()}]")
    })

    // handle response on subscribe request
    client.subscribeCompletionHandler({ h ->
      println("Receive SUBACK from server with granted QoS : ${h.grantedQoSLevels()}")

      // let's publish a message to the subscribed topic
      client.publish(MQTT_TOPIC, Buffer.buffer(MQTT_MESSAGE), MqttQoS.AT_MOST_ONCE, false, false, { s ->
        println("Publish sent to a server")
      })

      // unsubscribe from receiving messages for earlier subscribed topic
      vertx.setTimer(5000, { l ->
        client.unsubscribe(MQTT_TOPIC)
      })
    })

    // handle response on unsubscribe request
    client.unsubscribeCompletionHandler({ h ->
      println("Receive UNSUBACK from server")
      vertx.setTimer(5000, { l ->
        client.disconnect({ d ->
          println("Disconnected form server")
        })
      })
    })

    // connect to a server
    client.connect(BROKER_PORT, BROKER_HOST, { ch ->
      if (ch.succeeded()) {
        println("Connected to a server")
        client.subscribe(MQTT_TOPIC, 0)
      } else {
        println("Failed to connect to a server")
        println(ch.cause())
      }
    })
  }
}
