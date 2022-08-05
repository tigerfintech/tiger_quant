## Tiger Quant

### 启动前配置
1. 本地安装mysql数据库，依据 resources/datasource/mysql_table_create.sql 中的sql来创建数据库和表
2. 修改 resources/datasource/mybatis-config.xml 文件中的数据库连接信息，改为自己的数据库，用户名和密码
![image](https://user-images.githubusercontent.com/3766355/156764862-d559d515-4119-4ec3-8abe-9548bb7930b8.png)
3. 配置 Tiger OpenAPI 账号信息(参考下方配置说明)，然后执行 ContractLoader 下载合约数据
以上步骤执行后，可以执行下面的启动命令来运行量化策略程序。


### 启动命令
TigerQuantBootstrap 是项目的Main方法入口，负责项目的启动。

### 停止命令
* 查出项目运行的进程 pid。
ps -ef|grep TigerQuantBootstrap
* kill pid。
kill命令执行时会同时执行项目的stop方法回调。

### 策略编写
用户实现的策略需要继承自AlgoTemplate，同时要提供默认构造方法。
在algorithm/algos文件目录下实现了几个demo策略，可以参考一下。

### 配置说明

#### 策略配置
策略配置文件名：algo_setting.json

每个算法文件对应一个配置项，配置项的Key与策略Java文件名称要保持一致。
配置项中必填参数如下：
* enable：是否启用该策略。true 启用，false 不启用
* class：策略算法对应的文件全路径名
* 其他参数为自选参数，在策略启动时会自动注册到策略中。

* 配置实例
```
{
  "BestLimitAlgo": {
    "enable": false,
    "class":"com.tquant.algorithm.algos.BestLimitAlgo",
    "direction": "BUY",
    "volume": 100,
    "symbol": "00700"
  },
  "DmaAlgo": {
    "enable": false,
    "class":"com.tquant.algorithm.algos.DmaAlgo",
    "direction": "BUY",
    "price": 13.2,
    "volume": 100
  },
  "SpreadAlgo": {
    "enable": true,
    "class":"com.tquant.algorithm.algos.MacdAlgo",
    "symbol": "SPY",
    "bars": 100
  }
}
```

#### 全局配置
全局配置文件名：global_setting.json

#### 日志配置
* log.enable：是否开启日志开关。true 打开，false 关闭
* log.level：日志级别，默认info级别。取值包括 error,warn,info
* log.console：日志是否输出到控制台。true 输出到控制台，false 不输出到控制台
* log.file：日志是否输出到文件。true 输出到文件，false 不输出到文件
* log.path：日志输出到文件的路径。支持绝对路径和相对路径。默认当前项目下的log目录

#### 存储配置
* storage.enable：是否开启持久化存储。true 开启，false 不开启

### 柜台配置
目前只支持Tiger券商接口，配置文件名：tiger_gateway_setting.json

* gateway：固定为TigerGateway
* apiLogEnable：是否开启SDK的日志记录
* apiLogPath：SDK日志文件输出路径，默认当前项目下的log目录

下面配置为开发者信息相关，需要先申请开发者账号，注册开发者账号地址：https://www.tigersecurities.com/openapi
* tigerId：开发者ID
* account：开发者交易账号
* serverUrl：服务器地址
* socketUrl：服务器推送地址
* privateKey：开发者注册私钥

* 配置实例
```
{
  "gateway": "TigerGateway",
  "apiLogEnable": true,
  "apiLogPath": "log/",
  "tigerId": "2015xxxx",
  "account": "20190419163707900",
  "serverUrl": "https://openapi.itiger.com/gateway",
  "socketUrl": "wss://openapi.itiger.com:8887/stomp",
  "privateKey": "MIICeQIBADANBgkqhkiG9w0BAQEFAASCAmMwggJfAgEAAoGBAL7..."
 }
```

### 外部依赖

#### ta4j

指标计算工具，包括常见的上百种指标计算。
* 项目地址：https://github.com/ta4j/ta4j
* 项目wiki：https://github.com/ta4j/ta4j-wiki

### 环境准备

* 支持windows、linux、mac等常见系统。
* JDK 1.8 以及以上。


### 问题反馈

使用上遇到任何问题，或有任何建议，欢迎在github上反馈，也欢迎加入官方QQ群：441334668。
