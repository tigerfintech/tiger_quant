## Tiger Quant

该量化框架是基于`vnpy`的一个java 版本实现，里面集成了一些量化基础功能，同时接入了老虎证券API接口。

### 快速上手
1. **首先要把`tiger_quant`项目导入到本地IDE中(比如Idea)，导入成maven项目**。
2. **然后完成策略的编写，在`tquant-algorithm` 模块下实现自己的策略类（也可以直接运行示例策略）**。一个简单的策略大致如下：
```java
    public class BestLimitAlgo extends AlgoTemplate {

      public BestLimitAlgo() {
      }

      public BestLimitAlgo(Map<String, Object> settings) {
        super(settings);
      }

      @Override
      public void init() {
        this.direction = (String) settings.get("direction");
        this.volume = (Integer) settings.get("volume");
        this.symbol = (String) settings.get("symbol");
      }

      @Override
      public void onStart() {
        //
        barGenerator = new BarGenerator(bar -> onBar(bar));
        //订阅 AAPL 行情
        List<String> symbols = new ArrayList<>();
        symbols.add("AAPL");
        subscribe(symbol);
      }
    
      @Override
      public void onTick(Tick tick) {
    
      }
    
      private void buyBestLimit() {
        int orderVolume = volume - traded;
        orderPrice = lastTick.getBidPrice();
        if (orderPrice < 10) {
          buy(symbol, orderPrice, orderVolume, OrderType.LMT);
        }
      }
    
      private void sellBestLimit() {
        int orderVolume = volume - traded;
        orderPrice = lastTick.getAskPrice();
        if (orderPrice > 12) {
          sell(symbol, orderPrice, orderVolume, OrderType.LMT);
        }
      }
    
      @Override
      public void onOrder(Order order) {
        
      }
    
      @Override
      public void onTrade(Trade trade) {
      }
    
      @Override
      public void onBar(Bar bar) {
        log("onBar {}", bar);
      }
}
```
实现的策略类需要继承 `AlgoTemplate`类，这样即可调用封装好的一些方法，同时自动注入策略配置项。常用的封装方法包括：buy，sell等下单功能，onBar（K线），onOrder（订单），onTick（实时行情）等实时事件，还有一些券商封装的api接口以及日志功能等。
   
3. **实现策略后，要完成对应的策略配置，以及券商API配置**。可以拷贝根目录下的2个配置模板，一个是`algo_setting.json`，对应的是策略参数。另一个是`gateway_setting.json`，对应老虎API的账号信息，完成对应配置即可（下面有详细的配置说明）。

4. 实现完策略以及配置工作后，即可开始进行项目的编译打包，以及运行了。在项目的根目录下执行如下mvn命令即可完成打包工作：
```shell script
mvn -U clean install  -Dmaven.test.skip=true
```
等命令执行完成后，会在 `tquant-bootstrap`的`target`目录下生成可执行jar包：`tquant-bootstrap-1.0.0-jar-with-dependencies.jar`，
把该jar包以及`algo_setting.json`，`gateway_setting.json`拷贝到指定目录后，再通过执行如下命令即可运行策略：
```
    java -jar tquant-bootstrap-1.0.0-jar-with-dependencies.jar -a /yourpath/algo_setting.json -g /yourpath/tiger_gateway_setting.json
```
调试阶段也可以通过IDE来运行，通过配置`TigerQuantBootstrap`的启动参数即可。如在Idea编辑器里的配置如下：


* 停止策略执行
1）在命令行执行查出项目运行的进程 pid。
```
    ps -ef|grep TigerQuantBootstrap
```
2）执行kill命令停止策略运行
```
    kill {pid}
```
kill命令执行时会同时执行项目的stop方法回调。


### 配置说明

#### 策略配置
策略配置文件：`algo_setting.json`

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

### 接入券商配置
目前只支持Tiger券商接口，配置文件名：`gateway_setting.json`

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
