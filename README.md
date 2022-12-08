Pre-requisites

* Build jar
```
sbt mockSrThrottled/assembly
```
* Build Docker image
```
docker build -f SRThrottled.Dockerfile --tag sr-throttled:latest .
```
* Run docker compose
```
docker compose up
```

* [AKHQ](http://localhost:8080)
* [The mock schema registry (returns HTTP 429)](http://localhost:8081) and its [metrics](http://localhost:8081/metrics)
* [Grafana](http://localhost:3000)