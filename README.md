# Tiger Quant

该量化框架是基于`vnpy`的一个java 版本实现，里面集成了一些量化基础功能，同时接入了老虎证券API接口。

## 环境准备

* 支持Windows、Linux、Mac等常见操作系统。
* JDK 1.8 及以上。

## 快速上手
### 1. 导出项目到本地
首先要把`tiger_quant`项目导入到本地IDE中(比如Idea)，导入成maven项目。

### 2. 编写策略
在`tquant-algorithm` 模块下实现自己的策略类（也可以直接运行示例策略）。一个简单的策略大致如下：
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
   
### 3. 完成对应的策略配置及券商接入配置
可以拷贝根目录下的2个配置模板，一个是`algo_setting.json`，对应的是策略参数。另一个是`gateway_setting.json`，对应老虎API的账号信息，完成对应配置即可（下面有详细的配置说明）。

### 4. 编译运行策略

在项目的根目录下执行如下mvn命令即可完成打包工作：
```shell script
mvn -U clean install  -Dmaven.test.skip=true
```
等命令执行完成后，会在 `tquant-bootstrap`的`target`目录下生成可执行jar包：`tquant-bootstrap-1.0.0-jar-with-dependencies.jar`，
把该jar包以及`algo_setting.json`，`gateway_setting.json`拷贝到指定目录后，再通过执行如下命令即可运行策略：
```
    java -jar tquant-bootstrap-1.0.0-jar-with-dependencies.jar -a /yourpath/algo_setting.json -g /yourpath/tiger_gateway_setting.json
```
调试阶段也可以通过IDE来运行，通过配置`TigerQuantBootstrap`的启动参数即可。如在Idea编辑器里的配置如下：
![tquant-bootstrap](https://user-images.githubusercontent.com/3766355/219582428-9f2a6d81-4118-46f5-82c5-1fe77e0ea306.png)

### 5. 停止执行策略
有些策略是在程序里自动退出的，也有一些策略是一直运行的，如想停止对应的策略，可以在命令行下执行`ps`命令查出项目运行的进程 pid，再执行kill命令停止策略运行。
kill命令执行时会同时执行项目的stop方法回调。
```
    ps -ef|grep TigerQuantBootstrap
    kill {pid}
```


## 配置说明

### 策略配置
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

### 全局配置
全局配置文件名：`global_setting.json` ， 在`tquant-core`模块 `resources` 目录下。


* log.enable：是否开启日志开关。true 打开，false 关闭
* log.level：日志级别，默认info级别。取值包括 error,warn,info
* log.console：日志是否输出到控制台。true 输出到控制台，false 不输出到控制台
* log.file：日志是否输出到文件。true 输出到文件，false 不输出到文件
* log.path：日志输出到文件的路径。支持绝对路径和相对路径。默认当前项目下的log目录
* storage.enable：是否开启持久化存储。true 开启，false 不开启
* subscribe.enable: 是否开启API长连接订阅，默认为 false，开启后会通过长连接回调方法获取实时行情，交易订单变更，持仓和资产变更等。未开启的话可以通过API接口获取对应数据。
* contract.load.enable: 是否在启动时开启合约加载，默认为 false，开启后会通过本地数据库加载全量合约，需要配合`tquant-loader`中的合约加载功能一块儿使用。

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

## Thetagang策略说明

Thetagang是我们封装的一个期权策略，策略的意图是赚取theta（时间价值）流失的钱，该策略最初是在reddit论坛里发起，是一个比较成熟的期权策略。
具体介绍可以参考：https://www.reddit.com/r/options/comments/a36k4j/the_wheel_aka_triple_income_strategy_explained/

同时在github有一个基于IB的[thetagang策略](https://github.com/brndnmtthws/thetagang)，我们的java版本策略也是基于此来改造的。

策略使用参数介绍如下：
> 注意：以下策略配置文件不能直接使用，因为使用了注释说明，不是标准json格式，如需使用，可以直接使用项目根目录下的模板文件：`algo_setting.json`

```json
 "ThetaGangAlgo": {
    "enable": true, //是否启用策略
    "class":"com.tquant.algorithm.algos.ThetaGangAlgo", //对应策略实现的代码路径
    "account": {
      "account_id": "20190419163107900", //使用的账号信息，可以配置为模拟账号或综合账号
      "cancel_orders": true, //策略执行前，是否要取消已经挂出去的订单
      "margin_usage": 0.5, //该策略要使用的资金占总资产的比例，如 0.5 表示为 50%
    },
    //期权链是延迟加载的，在你确定期权希腊值（delta）或期权价格之前，你需要先扫描期权链。
    //这里的设置是告诉thetagang策略要加载多少个合约。不要让这些值太大，因为它们会导致扫描期权链过多，可能会失败。
    //如果你遇到thetagang找不到合适的contract的问题，可以试试略微增加这些值。
    "option_chains": {
      "expirations": 4, //从期权链上加载的到期日数量
      "strikes": 15 //从期权链上加载的行权价数量
    },
    "roll_when": {
      "pnl": 0.9, //盈亏(pnl)到达 90% 时，需要滚动持仓
      //或者，当离到期日<=15天，并且盈亏至少到min_pnl (min_pnl默认为0)时。
      //注意：对于期权最终是深度 ITM 的情况，特别是在卖出套期保值（covered call）的时候，盈亏有可能是负数、
      //示例：如果你想在这种情况下进行滚动，请将min_pnl设置为一个负值，如-1（代表-100%）。
      "dte": 15, 
      "min_pnl": 0.2,
      //可选的： 当盈亏达到这个阈值时，创建一个平仓单。
      //这会覆盖其他参数，也就是说，它忽略了dte和其他参数。如果不指定，它没有任何作用。
      //这可以处理这样的情况，即你有长期的期权，已经慢慢变得毫无价值，你只是想把它们从你的投资组合中删除。
      "close_at_pnl": 0.99,
      "calls": {
        "itm": true,
        "credit_only": false //只有在有合适的contract可用时，才会进行滚动，从而获得一个 credit。
      },
      "puts": {
        "itm": false,
        "credit_only": false //只有在有合适的contract可用时，才会进行滚动，从而获得一个 credit。
      }
    },
    "write_when": {
      "calls": {
        "green": true, //可选的，只有在对应标的上涨时才会write。
        //有了套期保值(covered call)，我们就可以通过这个因素来限定写的套期保值的数量。
        //在1.0的时候，我们对100%的头寸写覆盖性看涨。
        //在0.5时，我们只写 50%的头寸。这个值必须在1和0之间（含）。
        "cap_factor": 1
      },
      "puts": {
        "red": true  //可选的，只有在对应标的下跌时才会write。
      }
    },
    "target": {
      "dte": 45,  // Target 45 or more days to expiry
      "delta": 0.3,  //Target delta of 0.3 or less. Defaults to 0.3 if not specified.
      // When writing new contracts (either covered calls or naked puts), or rolling
      // before `roll_when.dte` is reached, never write more than this amount of
      // contracts at once. This can be useful to avoid bunching by spreading contract
      // placement out over time (and possibly expirations) in order to protect
      // yourself from large swings. This value does not affect rolling existing
      // contracts to the next expiration. This value is expressed as a percentage of
      // buying power based on the market price of the underlying ticker, as a range
      // from [0.0-1.0].
      //
      // Once the `roll_when.dte` date is reached, all the remaining positions are
      // rolled regardless of the current position quantity.
      //
      // Defaults to 5% of buying power. Set this to 1.0 to effectively disable the
      // limit.
      "maximum_new_contracts_percent": 0.05, 
      // Minimum amount of open interest for a contract to qualify
      "minimum_open_interest": 10
    },

    // Optional: specify delta separately for puts/calls. Takes precedent over
    // target.delta.
    //
    //  [target.puts]
    //  delta = 0.5
    //  [target.calls]
    //  delta = 0.3
    "symbols": {
        # NOTE: Please change these symbols and weights according to your preferences.
        # These are provided only as an example for the purpose of configuration. These
        # values were chosen as sane values should someone decide to run this code
        # without changes, however it is in no way a recommendation or endorsement.
        #
        # You can specify the weight either as a percentage of your buying power (which
        # is calculated as your NLV * account.margin_usage), or in terms of parts. Parts
        # are summed from all symbols, then the weight is calculated by dividing the
        # parts by the total parts.
        #
        # You should try to choose ETFs or stocks that:
        #
        #  1) Have sufficient trading volume for the underlying
        #  2) Have standard options contracts (100 shares per contract)
        #  3) Have options with sufficient open interest and trading volume
        #
        # The target delta may also be specified per-symbol, and takes precedence over
        # `target.delta` or `target.puts/calls.delta`. You can specify a value for the
        # symbol, or override individually for puts/calls.
      "SPY": {
        "weight": 0.4
      },
      "AAPL": {
        "weight": 0.3
      },
      "MSFT": {
        "weight": 0.3
      }
    }
  }
```

## 外部依赖

#### ta4j

指标计算工具，包括常见的上百种指标计算。
* 项目地址：https://github.com/ta4j/ta4j
* 项目wiki：https://github.com/ta4j/ta4j-wiki


## 问题反馈

使用上遇到任何问题，或有任何建议，欢迎在github上反馈，也欢迎加入官方QQ群：441334668。
