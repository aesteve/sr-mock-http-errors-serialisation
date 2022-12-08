package com.github.aesteve

import io.micrometer.core.instrument.Metrics
import io.prometheus.client.Counter
import io.vertx.core.{Vertx, VertxOptions}
import io.vertx.ext.web.Router
import io.vertx.micrometer.backends.BackendRegistries
import io.vertx.micrometer.{MicrometerMetricsOptions, PrometheusScrapingHandler, VertxPrometheusOptions}

import java.util.concurrent.atomic.AtomicLong

val Port = 8081

@main
def runSchemaRegistryThrottled(): Unit =
  val metricsOptions = MicrometerMetricsOptions()
    .setPrometheusOptions(VertxPrometheusOptions().setEnabled(true))
    .setEnabled(true)
  val vertxOptions = VertxOptions().setMetricsOptions(metricsOptions)
  val vertx = Vertx.vertx(vertxOptions)

  val server = vertx.createHttpServer
  val router = Router.router(vertx)
  router.get("/metrics").handler(PrometheusScrapingHandler.create)

  val counter = BackendRegistries.getDefaultNow.counter("posts_received")


  router.post().handler(request => {
    counter.increment()
    request.response
      .setStatusCode(429)
      .end()
  })
  server.requestHandler(router)
  server.listen(Port)
