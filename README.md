## Tiger Quant

### 启动前配置
0. 可以把`tiger_quant`项目导入到本地IDE中(比如Idea)，有2种方式导入为maven项目：
   1. 右键单击根目录下的pom.xml(选择mark as maven project)。
   2. 在根目录对应的命令行执行编译命令：
    ```shell script
    mvn clean compile  -Dmaven.test.skip=true
    ```
    等命令执行完成后，即可正常运行项目。

1. 编译完成后，在本地安装mysql数据库，按照 `tquant-storage` 模块下的 `resources/datasource/mysql_table_create.sql` 中的sql来创建本地数据库和表

2. 把 `tquant-storage` 模块下的 `resources/datasource/mybatis-config.xml` 文件中的数据库连接信息改成本地数据库配置，用户名和密码
![image](https://user-images.githubusercontent.com/3766355/156764862-d559d515-4119-4ec3-8abe-9548bb7930b8.png)

3. 配置 `tquant-gateway` 模块下的 `resources/tiger_gateway_setting.json` Tiger OpenAPI 账号信息(参考下方配置说明)

4. 执行`tquant-loader` 模块下的命令，可以下载历史数据，包括合约，K线，逐笔等数据。
比如BarLoader来举例，项目导入Idea后，需要在Idea启动配置中设置参数：

然后启动 BarLoader 下载K线数据，合约和逐笔数据的下载方法也是类似方式。

以上步骤执行后，可以执行下面的启动命令来运行量化策略程序。

5. `tquant-algorithm` 是策略模块，里面包含了部分策略示例
6. 策略编写完成后，可以通过 `tquant-backtester` 回测模块完成功能回测（会用到K线或逐笔数据，可以通过`tquant-loader`模块提前导入），里面有可以直接执行的main方法。如：
`com.tquant.backtester.Backtesting.main`
，可以参考已实现的示例，需注意下单接口要在回测模块中进行覆盖，否则会导致回测服务启动失败。

7. 策略正式上线时，会通过 `tquant-bootstrap` 模块中的 `com.tquant.bootstrap.TigerQuantBootstrap` 来启动，也可以通过IDE方式来启动。

### 启动命令
`tquant-bootstrap` 模块中的 TigerQuantBootstrap 是项目的Main方法入口，负责项目的启动。

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
策略配置文件名：`algo_setting.json` , 在 `tquant-algorithm` 模块`resource`目录下。

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
全局配置文件名：`global_setting.json` ， 在`tquant-core`模块 `resources` 目录下。


* log.enable：是否开启日志开关。true 打开，false 关闭
* log.level：日志级别，默认info级别。取值包括 error,warn,info
* log.console：日志是否输出到控制台。true 输出到控制台，false 不输出到控制台
* log.file：日志是否输出到文件。true 输出到文件，false 不输出到文件
* log.path：日志输出到文件的路径。支持绝对路径和相对路径。默认当前项目下的log目录
* storage.enable：是否开启持久化存储。true 开启，false 不开启

### 柜台配置
目前只支持Tiger券商接口，配置文件名：`tiger_gateway_setting.json`，在 `tquant-gateway` 模块 `resources` 目录下。

* gateway：固定为TigerGateway
* apiLogEnable：是否开启SDK的日志记录
* apiLogPath：SDK日志文件输出路径，默认当前项目下的log目录

下面配置为开发者信息相关，需要先申请开发者账号，注册开发者账号地址：https://www.tigersecurities.com/openapi
* tigerId：开发者账号ID
* account：开发者交易账号，可以是老虎综合账号或模拟账号。
* privateKey：开发者自己生成的RSA私钥

* 配置实例
```
{
  "gateway": "TigerGateway",
  "apiLogEnable": true,
  "apiLogPath": "log/",
  "tigerId": "2015xxxx",
  "account": "20190419163707900",
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
