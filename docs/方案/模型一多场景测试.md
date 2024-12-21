# 前置数据

## 规则一
```json
{
  "channel": "game",
  "ruleCode": "ruleCode01",
  "combinedConditionOperator": 0,
  "warnInterval": 180000,
  "ruleConditionGroup": [
    {
      "eventCode": "eventCode01",
      "eventThreshold": 10,
      "windowSize": 300000,
      "isCrossHistory": true,
      "crossHistoryTimeline": "2024-08-26 16:41:40"
    },
    {
      "eventCode": "eventCode02",
      "eventThreshold": 20,
      "windowSize": 600000,
      "isCrossHistory": true,
      "crossHistoryTimeline": "2024-08-26 16:41:40"
    }
  ]
}
```

## 规则二
```json
{
  "channel": "mall",
  "ruleCode": "ruleCode02",
  "combinedConditionOperator": 1,
  "warnInterval": 300000,
  "ruleConditionGroup": [
    {
      "eventCode": "eventCode03",
      "eventThreshold": 20,
      "windowSize": 600000,
      "isCrossHistory": true,
      "crossHistoryTimeline": "2024-08-27 17:35:28"
    },
    {
      "eventCode": "eventCode04",
      "eventThreshold": 10,
      "windowSize": 300000,
      "isCrossHistory": true,
      "crossHistoryTimeline": "1970-01-01 00:00:00"
    }
  ]
}
```

## redis
```
hset slr_doris_history_value:ruleCode01:eventCode01 userId01 1
hset slr_doris_history_value:ruleCode01:eventCode01 userId02 2
hset slr_doris_history_value:ruleCode01:eventCode02 userId01 3
hset slr_doris_history_value:ruleCode01:eventCode02 userId02 4
hset slr_doris_history_value:ruleCode02:eventCode03 userId01 5
hset slr_doris_history_value:ruleCode02:eventCode03 userId02 6
```

# 第一分钟

## 数据

```json
[
  {
    "channel": "game",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode02",
    "eventValue": "3",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode01",
    "eventValue": "2",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode02",
    "eventValue": "4",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode03",
    "eventValue": "5",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode04",
    "eventValue": "6",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode03",
    "eventValue": "7",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode04",
    "eventValue": "8",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },

  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode04",
    "eventValue": "9",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "10",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "hjf",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "11",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  }
]
```

## 预期结果

```
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode02, eventCodeList=["eventCode03","eventCode04"], keyCode=userId01, bigMapState={"eventCode03":{"1724834280000":10},"eventCode04":{"1724834280000":6}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode01, eventCodeList=["eventCode01","eventCode02"], keyCode=userId01, bigMapState={"eventCode01":{"1724834280000":2},"eventCode02":{"1724834280000":6}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode02, eventCodeList=["eventCode03","eventCode04"], keyCode=userId02, bigMapState={"eventCode03":{"1724834280000":13},"eventCode04":{"1724834280000":8}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode01, eventCodeList=["eventCode01","eventCode02"], keyCode=userId02, bigMapState={"eventCode01":{"1724834280000":4},"eventCode02":{"1724834280000":8}}
```

# 第二分钟

## 业务数据

```json
[
  {
    "channel": "game",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode02",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode02",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode03",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode04",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode03",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode04",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },

  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode04",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "hjf",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  }
]
```

## 预期结果

```
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode02, eventCodeList=["eventCode03","eventCode04"], keyCode=userId01, bigMapState={"eventCode03":{"1724834340000":1,"1724834280000":10},"eventCode04":{"1724834340000":1,"1724834280000":6}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode01, eventCodeList=["eventCode01","eventCode02"], keyCode=userId01, bigMapState={"eventCode01":{"1724834340000":1,"1724834280000":2},"eventCode02":{"1724834340000":1,"1724834280000":6}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode02, eventCodeList=["eventCode03","eventCode04"], keyCode=userId02, bigMapState={"eventCode03":{"1724834340000":1,"1724834280000":13},"eventCode04":{"1724834340000":1,"1724834280000":8}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode01, eventCodeList=["eventCode01","eventCode02"], keyCode=userId02, bigMapState={"eventCode01":{"1724834340000":1,"1724834280000":4},"eventCode02":{"1724834340000":1,"1724834280000":8}}
```

# 第三分钟

## 业务数据

```json
[
  {
    "channel": "game",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "8",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode02",
    "eventValue": "12",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode03",
    "eventValue": "10",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },

  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode04",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "hjf",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  }
]
```

## 预期结果

```
mall渠道的userId01(用户01)用户触发了ruleName02(ruleCode02)规则，请尽快处理!
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode02, eventCodeList=["eventCode03","eventCode04"], keyCode=userId01, bigMapState={"eventCode03":{"1724834400000":10,"1724834340000":1,"1724834280000":10},"eventCode04":{"1724834340000":1,"1724834280000":6}}
game渠道的userId01(用户01)用户触发了ruleName01(ruleCode01)规则，请尽快处理!
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode01, eventCodeList=["eventCode01","eventCode02"], keyCode=userId01, bigMapState={"eventCode01":{"1724834400000":8,"1724834340000":1,"1724834280000":2},"eventCode02":{"1724834400000":14,"1724834340000":1,"1724834280000":6}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode02, eventCodeList=["eventCode03","eventCode04"], keyCode=userId02, bigMapState={"eventCode03":{"1724834340000":1,"1724834280000":13},"eventCode04":{"1724834340000":1,"1724834280000":8}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode01, eventCodeList=["eventCode01","eventCode02"], keyCode=userId02, bigMapState={"eventCode01":{"1724834340000":1,"1724834280000":4},"eventCode02":{"1724834340000":1,"1724834280000":8}}
```

# 第四分钟

## 业务数据

```json
[
  {
    "channel": "game",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode02",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode01",
    "eventValue": "6",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode02",
    "eventValue": "12",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode03",
    "eventValue": "6",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode04",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },

  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode04",
    "eventValue": "11",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "12",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "hjf",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "190",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  }
]
```

## 预期结果

```
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode02, eventCodeList=["eventCode03","eventCode04"], keyCode=userId01, bigMapState={"eventCode03":{"1724834400000":10,"1724834340000":1,"1724834280000":10},"eventCode04":{"1724834340000":1,"1724834280000":6}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode01, eventCodeList=["eventCode01","eventCode02"], keyCode=userId01, bigMapState={"eventCode01":{"1724834340000":1,"1724834280000":2,"1724834460000":1,"1724834400000":8},"eventCode02":{"1724834340000":1,"1724834280000":6,"1724834460000":1,"1724834400000":14}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode02, eventCodeList=["eventCode03","eventCode04"], keyCode=userId02, bigMapState={"eventCode03":{"1724834460000":6,"1724834340000":1,"1724834280000":13},"eventCode04":{"1724834460000":1,"1724834340000":1,"1724834280000":8}}
game渠道的userId02(用户02)用户触发了ruleName01(ruleCode01)规则，请尽快处理!
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode01, eventCodeList=["eventCode01","eventCode02"], keyCode=userId02, bigMapState={"eventCode01":{"1724834460000":6,"1724834340000":1,"1724834280000":4},"eventCode02":{"1724834460000":12,"1724834340000":1,"1724834280000":8}}
```

# 第五分钟

## 业务数据

```json
[
  {
    "channel": "game",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "100",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode02",
    "eventValue": "100",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode03",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode04",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },

  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode04",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "hjf",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  }
]
```

## 预期结果

```
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode02, eventCodeList=["eventCode03","eventCode04"], keyCode=userId01, bigMapState={"eventCode03":{"1724834400000":10,"1724834340000":1,"1724834280000":10},"eventCode04":{"1724834340000":1,"1724834280000":6}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode01, eventCodeList=["eventCode01","eventCode02"], keyCode=userId01, bigMapState={"eventCode01":{"1724834340000":1,"1724834280000":2,"1724834460000":1,"1724834400000":8,"1724834520000":100},"eventCode02":{"1724834340000":1,"1724834280000":6,"1724834460000":1,"1724834400000":14,"1724834520000":100}}
mall渠道的userId02(用户02)用户触发了ruleName02(ruleCode02)规则，请尽快处理!
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode02, eventCodeList=["eventCode03","eventCode04"], keyCode=userId02, bigMapState={"eventCode03":{"1724834340000":1,"1724834280000":13,"1724834460000":6,"1724834520000":1},"eventCode04":{"1724834340000":1,"1724834280000":8,"1724834460000":1,"1724834520000":1}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode01, eventCodeList=["eventCode01","eventCode02"], keyCode=userId02, bigMapState={"eventCode01":{"1724834460000":6,"1724834340000":1,"1724834280000":4},"eventCode02":{"1724834460000":12,"1724834340000":1,"1724834280000":8}}
```

# 第六分钟

## 业务数据

```json
[
  {
    "channel": "game",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode02",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode02",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode03",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode04",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode03",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode04",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },

  {
    "channel": "game",
    "keyCode": "userId02",
    "keyValue": "用户02",
    "eventCode": "eventCode04",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "mall",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  },
  {
    "channel": "hjf",
    "keyCode": "userId01",
    "keyValue": "用户01",
    "eventCode": "eventCode01",
    "eventValue": "1",
    "attribute": {
      "campaignId": "campaignId01",
      "campaignName": "campaignName01"
    }
  }
]
```

## 预期结果

```
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode02, eventCodeList=["eventCode03","eventCode04"], keyCode=userId01, bigMapState={"eventCode03":{"1724834340000":1,"1724834280000":10,"1724834400000":10,"1724834580000":1},"eventCode04":{"1724834340000":1,"1724834580000":1}}
game渠道的userId01(用户01)用户触发了ruleName01(ruleCode01)规则，请尽快处理!
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode01, eventCodeList=["eventCode01","eventCode02"], keyCode=userId01, bigMapState={"eventCode01":{"1724834340000":1,"1724834460000":1,"1724834400000":8,"1724834580000":1,"1724834520000":100},"eventCode02":{"1724834340000":1,"1724834280000":6,"1724834460000":1,"1724834400000":14,"1724834580000":1,"1724834520000":100}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode02, eventCodeList=["eventCode03","eventCode04"], keyCode=userId02, bigMapState={"eventCode03":{"1724834340000":1,"1724834280000":13,"1724834460000":6,"1724834580000":1,"1724834520000":1},"eventCode04":{"1724834340000":1,"1724834460000":1,"1724834580000":1,"1724834520000":1}}
ProcessorOne对象onTimer方法结束; ruleCode=ruleCode01, eventCodeList=["eventCode01","eventCode02"], keyCode=userId02, bigMapState={"eventCode01":{"1724834460000":6,"1724834340000":1,"1724834580000":1},"eventCode02":{"1724834340000":1,"1724834280000":8,"1724834460000":12,"1724834580000":1}}
```