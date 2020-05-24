# url-shortener-istio

## Description
The URL shortener converts long links into smaller hash based one. The project aims to play with Kubernetes and the service mesh Istio. The project is built to run in a Minikube cluster and isn't meant to go into production. Tradeoffs are made in this sense.

## Requirements
### Functional
* Given a URL, the service should generate a shorter and unique alias of it.
* When accessing the short link, the service should redirect to the original page.
### Non functional
* Aliases shouldn't be guessable.
* High available because we don't want any redirections to fail.
* The redirection should happen in real time.

## How to run
The following must be installed:
* Minikube
* Kubernetes
* Istio
* Docker
* Java 11

Go to the `url-shortener` directory and run the command `./start.sh`. The script will start the Kubernetes cluster using Minikube. The default configuration is `cpu=4, memory=4096, disk=20000`. Then, the script builds the microservices and push a docker image. Kubernetes deploys the microservices, the Mongodb stateful set, and an ingress gateway. The last command outputs the ingress gateway's host and port used to query the service. This will take a while.

After the script terminates, make sure all pods are ready and running.
```
$ watch kubectl get pods

NAME                             READY   STATUS    RESTARTS   AGE
key-generator-5fd5dffb9f-cwcwx   2/2     Running   0          5m9s
mongodb-0                        2/2     Running   0          5m9s
url-79599fc469-kx7n7             2/2     Running   0          5m9s
```

To stop the cluster, run the command `./stop.sh`. Be aware that the script will stop and destroy the Kubernetes cluster. All generated links will be lost. To stop the cluster without data loss, run the command `minikube stop`.

Curl example:
* First, generate keys:
```
curl -X PUT http://192.168.64.83:30292/key/init -H 'cache-control: no-cache'

Response:
Status code: 204
```
This endpoint generates and stores 128 unique Base62 6 digits long keys in the database. 
128 keys are enough for testing, but not for production. In a real world we could generate all the possible keys and store them into the database (6^62 ~= 57.10^9 ~= 402 GB, too much for my computer).
* Create a short link:
```
curl -X GET 'http://192.168.64.83:30292/url/create?original_url=https://github.com/' -H 'cache-control: no-cache'

Response:
Status code: 200
Body:
{
    "redirect_url": "http://{host}:{port}/url/redirect/2ZnxMv"
}
```
* Redirect to the original link:
```
curl -X GET http://192.168.64.83:30292/url/redirect/2ZnxMv -H 'cache-control: no-cache'

Response:
Status code: 302 redirect
```
* Delete a key:
```
curl -X DELETE 'http://192.168.64.83:30292/url/del?hash=2ZnxMv' -H 'cache-control: no-cache'

Response:
Status code: 200
Body:
{
    "hash": "2ZnxMv",
    "del": true
}
```
The deletion flow isn't safe; if the URL is deleted but the key isn't, then the key is lost forever. We could use an async queue to fix this problem.

## Laod test
To make sure the non functional requirements are met, I ran a laod test on the `create` and `redirect` endpoint. The load test is performed with [Vegeta](https://github.com/tsenart/vegeta).

**Scenario 1**
* The configuration of Minikube is `cpu=4, memory=4096, disk=20000`.
* Attack `/url/create?original_url=https://stackoverflow.com/` with 100 QPS for 60 seconds (create the short link for stackoverflow).
* Results:

| Infos | Results |
|-------|---------|
|Requests [total, rate, throughput]|6000, 100.02, 99.99|
|Duration [total, attack, wait]|1m0s, 59.99s, 13.58ms|
|Latencies     [min, mean, 50, 90, 95, 99, max]|8.633ms, 14.742ms, 14.4ms, 18.645ms, 22.085ms, 33.677ms, 100.924ms|
|Bytes In [total, mean]|354000, 59.00|
|Bytes Out [total, mean]|0, 0.00|
|Success [ratio]|100.00%|
|Status Codes [code:count]|200:6000|

**Scenario 2**
* The configuration of Minikube is `cpu=4, memory=4096, disk=20000`.
* Attack `/url/redirect/T2Yq5F` with 100 QPS for 60 seconds (redirect to stackoverflow).
* Results:

| Infos | Results |
|-------|---------|
|Requests [total, rate, throughput]|6000, 100.02, 42.83|
|Duration [total, attack, wait]|1m0s, 59.99s, 12.45ms|
|Latencies [min, mean, 50, 90, 95, 99, max]|7.739ms, 53.23ms, 13.292ms, 96.266ms, 99.123ms, 404.128ms, 2.181s|
|Bytes In [total, mean]|301417250, 50236.21|
|Bytes Out [total, mean]|0, 0.00|
|Success [ratio]| 42.83%|
|Status Codes [code:count]|200:2570  429:3430|
|Error Set| 429 Too Many Requests|

The success rate is ~42% because my IP address performed too many requests and has been temporarily rate limited by stackoverflow. This isn't obvious in the first place, but the success rate is 100% because all redirections made it. As we can see, the overall response time is good even if last percentile is a little bit high.

Metrics are available through the Grafana gateway and Istio Service Dashboard.
