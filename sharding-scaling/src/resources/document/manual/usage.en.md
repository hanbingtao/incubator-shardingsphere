+++
pre = "<b>4.5.2. </b>"
toc = true
title = "Manual"
weight = 2
+++

## Manual

### Environment
JAVA，JDK 1.8+.

The migration scene we support：

| Source     | Destination    | Whether support or not |
| ---------- | -------------- | ---------------------- |
| MySQL      | sharding-proxy | support                |
| PostgreSQL | sharding-proxy | support                |

### API

ShardingScaling provides a simple HTTP API

#### Start scaling job

Interface description：POST /shardingscaling/job/start

Body：

| Parameter                                         | Describe                                        |
|---------------------------------------------------|-------------------------------------------------|
| ruleConfiguration.sourceDatasource                | source sharding sphere data source configuration |
| ruleConfiguration.sourceRule                      | source sharding sphere table rule configuration  |
| ruleConfiguration.destinationDataSources.name     | destination sharding proxy name                 |
| ruleConfiguration.destinationDataSources.url      | destination sharding proxy jdbc url             |
| ruleConfiguration.destinationDataSources.username | destination sharding proxy username             |
| ruleConfiguration.destinationDataSources.password | destination sharding proxy password             |
| jobConfiguration.concurrency                      | sync task proposed concurrency                  |

Example：

```
curl -X POST \
  http://localhost:8888/shardingscaling/job/start \
  -H 'content-type: application/json' \
  -d '{
   "ruleConfiguration": {
      "sourceDatasource": "ds_0: !!org.apache.shardingsphere.orchestration.yaml.config.YamlDataSourceConfiguration\n  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n  properties:\n    jdbcUrl: jdbc:mysql://127.0.0.1:3306/test?serverTimezone=UTC&useSSL=false\n    username: root\n    password: '\''123456'\''\n    connectionTimeout: 30000\n    idleTimeout: 60000\n    maxLifetime: 1800000\n    maxPoolSize: 50\n    minPoolSize: 1\n    maintenanceIntervalMilliseconds: 30000\n    readOnly: false\n",
      "sourceRule": "defaultDatabaseStrategy:\n  inline:\n    algorithmExpression: ds_${user_id % 2}\n    shardingColumn: user_id\ntables:\n  t1:\n    actualDataNodes: ds_0.t1\n    keyGenerator:\n      column: order_id\n      type: SNOWFLAKE\n    logicTable: t1\n    tableStrategy:\n      inline:\n        algorithmExpression: t1\n        shardingColumn: order_id\n  t2:\n    actualDataNodes: ds_0.t2\n    keyGenerator:\n      column: order_item_id\n      type: SNOWFLAKE\n    logicTable: t2\n    tableStrategy:\n      inline:\n        algorithmExpression: t2\n        shardingColumn: order_id\n",
      "destinationDataSources": {
         "name": "dt_0",
         "password": "123456",
         "url": "jdbc:mysql://127.0.0.1:3306/test2?serverTimezone=UTC&useSSL=false",
         "username": "root"
      }
   },
   "jobConfiguration": {
      "concurrency": 3
   }
}'
```

Response：

```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": null
}
```

#### Get scaling progress

Interface description：GET /shardingscaling/job/progress/{jobId}

Example：
```
curl -X GET \
  http://localhost:8888/shardingscaling/job/progress/1
```

Response：
```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": {
        "id": 1,
        "jobName": "Local Sharding Scaling Job",
        "status": "RUNNING/STOPPED"
        "syncTaskProgress": [{
            "id": "127.0.0.1-3306-test",
            "status": "PREPARING/MIGRATE_HISTORY_DATA/SYNCHRONIZE_REALTIME_DATA/STOPPING/STOPPED",
            "historySyncTaskProgress": [{
                "id": "history-test-t1#0",
                "estimatedRows": 41147,
                "syncedRows": 41147
            }, {
                "id": "history-test-t1#1",
                "estimatedRows": 42917,
                "syncedRows": 42917
            }, {
                "id": "history-test-t1#2",
                "estimatedRows": 43543,
                "syncedRows": 43543
            }, {
                "id": "history-test-t2#0",
                "estimatedRows": 39679,
                "syncedRows": 39679
            }, {
                "id": "history-test-t2#1",
                "estimatedRows": 41483,
                "syncedRows": 41483
            }, {
                "id": "history-test-t2#2",
                "estimatedRows": 42107,
                "syncedRows": 42107
            }],
            "realTimeSyncTaskProgress": {
                "id": "realtime-test",
                "delayMillisecond": 1576563771372,
                "logPosition": {
                    "filename": "ON.000007",
                    "position": 177532875,
                    "serverId": 0
                }
            }
        }]
   }
}
```

#### List scaling jobs
Interface description：GET /shardingscaling/job/list

Example：
```
curl -X GET \
  http://localhost:8888/shardingscaling/job/list
```

Response：

```
{
  "success": true,
  "errorCode": 0,
  "model": [
    {
      "jobId": 1,
      "jobName": "Local Sharding Scaling Job",
      "status": "RUNNING"
    }
  ]
}
```

#### Stop scaling job
Interface description：POST /shardingscaling/job/stop

Body：

| Parameter | Describe |
| --------- | -------- |
| jobId     | job id   |

Example：
```
curl -X POST \
  http://localhost:8888/shardingscaling/job/stop \
  -H 'content-type: application/json' \
  -d '{
   "jobId":1
}'
```
Response：
```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": null
}
```

### Configuration
The existing configuration items are as follows, We can modify them in `conf/server.yaml`：
| Name           | Description                                                  | Default value |
| -------------- | ------------------------------------------------------------ | ------------- |
| port           | Listening port of HTTP server                                | 8888          |
| blockQueueSize | Queue size of data transmission channel                      | 10000         |
| pushTimeout    | Data push timeout(ms)                                        | 1000          |
| workerThread   | Worker thread pool size, the number of migration task threads allowed to run concurrently | 30            |

