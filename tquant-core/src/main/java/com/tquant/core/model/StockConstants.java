package com.tquant.core.model;

import com.google.common.collect.Lists;
import java.util.List;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/15
 */
public class StockConstants {

    public static String CLOSE = "close";                   //最新价,收盘价
    public static String HIGH = "high";                     //最高价
    public static String LOW = "low";                       //最低价
    public static String OPEN = "open";                     //开盘价
    public static String LAST_CLOSE = "lastClose";          //昨日收盘价
    public static String CHANGE_AMOUNT = "changeAmount";    //涨跌额   今天收盘价-昨天收盘价
    public static String CHANGE = "change";                 //涨跌幅
    public static String TURNOVER_RATE = "turnoverRate";    //换手率
    public static String VOLUME = "volume";                 //成交量，单位：手
    public static String AMOUNT = "amount";                 //成交额,单位:万
    public static String TOTAL_VALUE = "totalValue";        //总市值,单位:亿
    public static String MARKET_VALUE = "marketValue";      //流通市值,单位:亿
    public static String AMPLITUDE = "amplitude";           //振幅
    public static String FACTOR = "factor";                 //复权因子

    public static String PE = "PE";                         //动态市盈率

    public static String POSITION="position";
    //macd指标
    public static String DIF = "dif";
    public static String DEA = "dea";
    public static String MACD = "macd";
    public static String MACD_CROSS = "macd_cross";
    //布林线指标
    public static String UPPER = "upper";
    public static String MID = "mid";
    public static String LOWER = "lower";
    public static String BOLL_SHRINK = "boll_shrink";
    //均线指标
    public static String CLOSE_MA5 = "close_ma5";
    public static String CLOSE_MA10 = "close_ma10";
    public static String CLOSE_MA13 = "close_ma13";
    public static String CLOSE_MA20 = "close_ma20";
    public static String CLOSE_MA30 = "close_ma30";
    public static String CLOSE_MA34 = "close_ma34";
    public static String CLOSE_MA40 = "close_ma40";
    public static String CLOSE_MA55 = "close_ma55";
    public static String CLOSE_MA60 = "close_ma60";
    public static String CLOSE_MA120 = "close_ma120";

    public static String MAX = "max";
    public static String MIN = "min";

    public static String AVERAGE_BOND = "average_bond";

    public static String GOLDEN_SPIDER = "golden_spider";
    public static String GOLDEN_SPIDER_RANGE = "golden_spider_range";

    public static String DATE_OF_DECLARATION = "dateOfDeclaration";  //公告日期
    public static String ANNUAL = "annual"; //分红年度
    public static String DATE_OF_RECORD = "dateOfRecord"; //股权登记日
    public static String DATE_OF_EX_DIVIDEND = "dateOfExDividend";   //除权除息日

    public static String STOCK_SHARES = "stockShares"; //送股
    public static String STOCK_TRANSFERRED = "stockTransferred"; //转增
    public static String STOCK_DIVIDEND = "stockDividend"; //派息

    public static String TYPE = "type";

    public static List<String> DAILY = Lists.newArrayList(
        "close",            //最新价,收盘价
        "high",             //最高价
        "low",              //最低价
        "open",             //开盘价
        "lastClose",        //昨日收盘价
        "changeAmount",     //涨跌额
        "change",           //涨跌幅
        "turnoverRate",     //换手率
        "volume",           //成交量,单位：手
        "amount",           //成交额,单位:万
        "totalValue",       //总市值,单位:亿
        "marketValue",      //流通市值,单位:亿
        "amplitude"         //振幅
    );

    public static int DAILY_SIZE = 15;


    public static List<String> REALTIME = Lists.newArrayList(
        "marketType",           //0     市场类型,沪市:1,深市:2
        "code",                 //1     证券代码
        "name",                 //2     证券名称
        "buy1",                 //3     买一
        "buy2",                 //4     买二
        "buy3",                 //5     买三
        "buy4",                 //6     买四
        "buy5",                 //7     买五
        "sell1",                //8     卖一
        "sell2",                //9     卖二
        "sell3",                //10    卖三
        "sell4",                //11    卖四
        "sell5",                //12    卖五
        "buy1Volume",           //13    买一手数
        "buy2Volume",           //14    买二手数
        "buy3Volume",           //15    买三手数
        "buy4Volume",           //16    买四手数
        "buy5Volume",           //17    买五手数
        "sell1Volume",          //18    卖一手数
        "sell2Volume",          //19    卖二手数
        "sell3Volume",          //20    卖三手数
        "sell4Volume",          //21    卖四手数
        "sell5Volume",          //22    卖五手数
        "limitUp",              //23    涨停价
        "limitDown",            //24    跌停价
        "close",                //25    最新价,收盘价
        "avgCost",              //26    均价
        "changeAmount",         //27    涨跌额
        "open",                 //28    开盘价
        "change",               //29    涨跌幅
        "high",                 //30    最高价
        "volume",               //31    成交量,单位：手
        "low",                  //32    最低价
        "",                     //33    未知
        "lastClose",            //34    昨日收盘价
        "amount",               //35    成交额,单位:万
        "quantityRelative",     //36    量比
        "turnoverRate",         //37    换手率
        "PE",                   //38    市盈率
        "outerDisc",            //39    外盘,主动买,单位:手
        "innerDisc",            //40    内盘,主动卖,单位:手
        "committeeThan",        //41    委比,百分比
        "committeeSent",        //42    委差
        "PB",                   //43    市净率
        "",                     //44    未知
        "marketValue",          //45    流通市值,单位:亿
        "totalValue",           //46    总市值,单位:亿
        "",                     //47    未知
        "",                     //48    未知
        "date",                 //49    时间
        "",                     //50    未知
        "",                     //51    未知
        ""                      //52    未知
    );

    public static List<String> MONEYFLOW = Lists.newArrayList(
        "今日主力净流入",      //0    今日主力净流入
        "主力净比",         //1 主力净比
        "今日超大单净流入",     //2 今日超大单净流入
        "超大单净比",        //3 超大单净比
        "今日大单净流入",      //4 今日大单净流入
        "大单净比",         //5 大单净比
        "今日中单净流入",      //6 今日中单净流入
        "中单净比",         //7 中单净比
        "今日小单净流入",      //8 今日小单净流入
        "小单净比",         //9 小单净比
        "未知1",           //10    未知
        "未知2",           //11    未知
        "超大单:流入",       //12    超大单:流入
        "超大单:流出",       //13    超大单:流出
        "大单:流入",       //14    大单:流入
        "大单:流出",       //15    大单:流出
        "中单:流入",       //16    中单:流入
        "中单:流出",       //17    中单:流出
        "小单:流入",       //17    小单:流入
        "小单:流出",       //18    小单:流出
        "未知3",           //19    未知
        "未知4"           //20    未知

    );

    public static List<String> MONEYFLOW_HIS = Lists.newArrayList(
        "date",         //0     日期
        "close",        //1     收盘价
        "change",       //2     涨跌幅
        "主力净流入-净额",     //3     主力净流入-净额
        "主力净流入-净占比",    //4     主力净流入-净占比
        "超大单净流入-净额",    //5     超大单净流入-净额
        "超大单净流入-净占比",   //6     超大单净流入-净占比
        "大单净流入-净额",     //7     大单净流入-净额
        "大单净流入-净占比",    //8     大单净流入-净占比
        "中单净流入-净额",     //9     中单净流入-净额
        "中单净流入-净占比",    //10    中单净流入-净占比
        "小单净流入-净额",     //11    小单净流入-净额
        "小单净流入-净占比"    //12     小单净流入-净占比
    );

    public static List<String> DAPAN_MONEYFLOW_HIS = Lists.newArrayList(
        "date",         //0     日期
        "sh-close",     //1     上证-收盘价
        "sh-change",     //2     上证-涨跌幅
        "sz-close",     //3     深证-收盘价
        "sz-change",     //4     深证-涨跌幅
        "主力净流入-净额",     //5     主力净流入-净额
        "主力净流入-净占比",    //6     主力净流入-净占比
        "超大单净流入-净额",    //7     超大单净流入-净额
        "超大单净流入-净占比",   //8     超大单净流入-净占比
        "大单净流入-净额",     //9     大单净流入-净额
        "大单净流入-净占比",    //10     大单净流入-净占比
        "中单净流入-净额",     //11     中单净流入-净额
        "中单净流入-净占比",    //12    中单净流入-净占比
        "小单净流入-净额",     //13    小单净流入-净额
        "小单净流入-净占比"    //14     小单净流入-净占比
    );

    public static List<String> INDUSTRY_MONEYFLOW = Lists.newArrayList(
        "num",         //0     序号
        "symbol",         //1     日期
        "name",        //2     名称
        "change",       //3     涨跌幅
        "主力净流入-净额",     //4     主力净流入-净额
        "主力净流入-净占比",    //5     主力净流入-净占比
        "超大单净流入-净额",    //6     超大单净流入-净额
        "超大单净流入-净占比",   //7     超大单净流入-净占比
        "大单净流入-净额",     //8     大单净流入-净额
        "大单净流入-净占比",    //9     大单净流入-净占比
        "中单净流入-净额",     //10     中单净流入-净额
        "中单净流入-净占比",    //11    中单净流入-净占比
        "小单净流入-净额",     //12    小单净流入-净额
        "小单净流入-净占比"    //13     小单净流入-净占比
    );
}
