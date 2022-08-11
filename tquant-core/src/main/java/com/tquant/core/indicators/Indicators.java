package com.tquant.core.indicators;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import com.tquant.core.model.StockConstants;
import com.tquant.core.model.data.StockData;
import com.tquant.core.util.QuantUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description:
 *
 * @author kevin
 * @date 2022/08/05
 */
public class Indicators {
  public Core core;

  public Indicators() {
    core = new Core();
  }

  public List<? extends StockData> sma(List<? extends StockData> stockDatas){
    stockDatas = sma(stockDatas,5);
    stockDatas = sma(stockDatas,10);
    stockDatas = sma(stockDatas,20);
    stockDatas = sma(stockDatas,30);
    stockDatas = sma(stockDatas,40);
    stockDatas = sma(stockDatas,60);
    return stockDatas;
  }

  public List<? extends StockData> sma(List<? extends StockData> stockDatas,int ma){
    return sma(stockDatas,"close",ma);
  }

  public List<? extends StockData> sma(List<? extends StockData> stockDatas,String column,int ma){
    double[] doubles = QuantUtils.toDoubleArray(stockDatas,column);
    double[] sma = sma(doubles, ma);
    QuantUtils.addDoubleArrayToList(sma,stockDatas,"close_ma" + ma);
    return stockDatas;
  }

  public double[] sma(double[] prices, int ma) {

    double[] tempOutPut = new double[prices.length];
    double[] output = new double[prices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.sma(0, prices.length - 1, prices, ma, begin, length, tempOutPut);

    for (int i = 0; i < ma - 1; i++) {
      output[i] = 0;
    }
    for (int i = ma - 1; 0 < i && i < (prices.length); i++) {
      output[i] = tempOutPut[i - ma + 1];
    }

    return output;
  }

  public List<StockData> ema(List<StockData> stockDatas){
    stockDatas = ema(stockDatas,5);
    stockDatas = ema(stockDatas,10);
    stockDatas = ema(stockDatas,20);
    stockDatas = ema(stockDatas,30);
    stockDatas = ema(stockDatas,60);
    return stockDatas;
  }

  public List<StockData> ema(List<StockData> stockDatas,int ma){
    return ema(stockDatas,"close",ma);
  }


  public List<StockData> ema(List<StockData> stockDatas,String column,int ma){
    double[] doubles = QuantUtils.toDoubleArray(stockDatas,column);
    double[] ema = ema(doubles, ma);
    QuantUtils.addDoubleArrayToList(ema,stockDatas,"ema_" + ma);
    return stockDatas;
  }

  public double[] ema(double[] prices, int ma) {

    double[] tempOutPut = new double[prices.length];
    double[] output = new double[prices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.ema(0, prices.length - 1, prices, ma, begin, length, tempOutPut);

    for (int i = 0; i < ma - 1; i++) {
      output[i] = 0;
    }
    for (int i = ma - 1; 0 < i && i < (prices.length); i++) {
      output[i] = tempOutPut[i - ma + 1];
    }

    return output;
  }

  /**
   * 计算dma
   * @param prices prices
   * @return dma
   */
  public double[][] dma(double[] prices){
    double[] ma10 = sma(prices, 10);
    double[] ma50 = sma(prices, 50);
    double[] dif = new double[ma10.length];
    for(int i=0;i<dif.length;i++){
      dif[i] = ma10[i] - ma50[i];
    }
    double[] ama = sma(dif,10);
    double[][] result = {dif,ama};
    return result;
  }

  public List<StockData> macd(List<StockData> stockDatas){
    return macd(stockDatas,12, 26, 9);
  }

  public List<StockData> macd(List<StockData> stockDatas, int optInFastPeriod, int optInSlowPeriod, int optInSignalPeriod){
    double[] closes = QuantUtils.toDoubleArray(stockDatas,"close");
    double[][] macd = macd(closes, optInFastPeriod, optInSlowPeriod, optInSignalPeriod);
    for(int i=0;i<stockDatas.size();i++){
      stockDatas.get(i).attr("dif",new BigDecimal(macd[0][i]).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
      stockDatas.get(i).attr("dea",new BigDecimal(macd[1][i]).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
      stockDatas.get(i).attr("macd",new BigDecimal(macd[2][i]).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
    }
    return stockDatas;
  }

  /**
   *
   * @param prices prices
   * @param optInFastPeriod optInFastPeriod
   * @param optInSlowPeriod optInSlowPeriod
   * @param optInSignalPeriod optInSignalPeriod
   * @return macd
   *      macd[0][]:dif
   *      macd[1][]:dea
   *      macd[2][]:macd
   */
  public double[][] macd(double[] prices, int optInFastPeriod, int optInSlowPeriod, int optInSignalPeriod) {
    double[] tempoutput1 = new double[prices.length];
    double[] tempoutput2 = new double[prices.length];
    double[] tempoutput3 = new double[prices.length];
    double[][] output = {new double[prices.length], new double[prices.length], new double[prices.length]};

    double[] result1 = new double[prices.length];
    double[] result2 = new double[prices.length];
    double[] result3 = new double[prices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;
    MAType optInFastMAType = MAType.Ema;
    MAType optInSlowMAType = MAType.Ema;
    MAType optInSignalMAType = MAType.Ema;

    retCode = core.macdExt(0, prices.length - 1, prices, optInFastPeriod, optInFastMAType, optInSlowPeriod,
        optInSlowMAType, optInSignalPeriod, optInSignalMAType, begin, length, tempoutput1, tempoutput2,
        tempoutput3);
    for (int i = 0; i < begin.value; i++) {
      result1[i] = 0;
      result2[i] = 0;
      result3[i] = 0;
    }
    for (int i = begin.value; 0 < i && i < (prices.length); i++) {
      result1[i] = tempoutput1[i - begin.value];
      result2[i] = tempoutput2[i - begin.value];
      result3[i] = tempoutput3[i - begin.value];
    }

    for (int i = 0; i < prices.length; i++) {
      output[0][i] = result1[i];
      output[1][i] = result2[i];
      output[2][i] = (output[0][i] - output[1][i]) * 2;
    }
    return output;
  }

  public double[][] macd(double[] prices) {
    return macd(prices, 12, 26, 9);
  }

  public List<StockData> boll(List<StockData> stockDatas){
    double[] closes = QuantUtils.toDoubleArray(stockDatas,"close");
    double[][] boll = boll(closes);
    for(int i=0;i<stockDatas.size();i++){
      stockDatas.get(i).attr("boll_upper",new BigDecimal(boll[0][i]).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
      stockDatas.get(i).attr("boll_mid",new BigDecimal(boll[1][i]).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
      stockDatas.get(i).attr("boll_lower",new BigDecimal(boll[2][i]).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
    }
    return stockDatas;
  }

  /**
   *
   * @param prices prices
   * @param optInTimePeriod optInTimePeriod
   * @param optInNbDevUp optInNbDevUp
   * @param optInNbDevDn optInNbDevDn
   * @return boll
   *      boll[0][]: upper
   *      boll[1][]: mid
   *      boll[2][]: lower
   */
  public double[][] boll(double[] prices, int optInTimePeriod, double optInNbDevUp, double optInNbDevDn) {
    MAType optInMAType = MAType.Sma;

    double[] tempoutput1 = new double[prices.length];
    double[] tempoutput2 = new double[prices.length];
    double[] tempoutput3 = new double[prices.length];
    double[][] output = {new double[prices.length], new double[prices.length], new double[prices.length]};

    double[] result1 = new double[prices.length];
    double[] result2 = new double[prices.length];
    double[] result3 = new double[prices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.bbands(0, prices.length - 1, prices, optInTimePeriod, optInNbDevUp, optInNbDevDn, optInMAType,
        begin, length, tempoutput1, tempoutput2, tempoutput3);

    for (int i = 0; i < optInTimePeriod - 1; i++) {
      result1[i] = 0;
      result2[i] = 0;
      result3[i] = 0;
    }
    for (int i = optInTimePeriod - 1; 0 < i && i < (prices.length); i++) {
      result1[i] = tempoutput1[i - optInTimePeriod + 1];
      result2[i] = tempoutput2[i - optInTimePeriod + 1];
      result3[i] = tempoutput3[i - optInTimePeriod + 1];
    }

    for (int i = 0; i < prices.length; i++) {
      output[0][i] = result1[i];
      output[1][i] = result2[i];
      output[2][i] = result3[i];
    }
    return output;
  }

  public double[][] boll(double[] prices) {
    return boll(prices, 20, 2.0, 2.0);
  }

  /**
   * kdj指标,默认参数9,3,3
   * @param high high
   * @param low low
   * @param close close
   * @return kdj
   */
  public double[][] kdj(double[] high,double[] low,double[]close){
    int length = high.length;
    double outSlowK[] = new double[high.length];
    double outSlowD[] = new double[high.length];
    double outSlowJ[] = new double[high.length];
    double[] RSV = new double[high.length];

    for(int i=0;i<length;i++){
      if(i>=8){
        int start = i-8;
        double high9 = Double.MIN_VALUE;
        double low9 = Double.MAX_VALUE;
        while(start<=i){
          if(high[start]>high9){
            high9 = high[start];
          }
          if(low[start]<low9){
            low9 = low[start];
          }
          start++;
        }
        RSV[i] = (close[i] - low9) / (high9-low9) * 100;
      }else{
        RSV[i] = 0d;
      }
    }

    for(int i =0;i<length;i++){
      if(i>1){
        outSlowK[i] = 2/3d * outSlowK[i-1] + 1/3d * RSV[i];
        outSlowD[i] = 2/3d * outSlowD[i-1] + 1/3d * outSlowK[i];
        outSlowJ[i] = 3* outSlowK[i] - 2* outSlowD[i];

        if(outSlowJ[i]>100){
          outSlowJ[i]=100;
        }else if(outSlowJ[i]<0){
          outSlowJ[i]=0;
        }
      }else{
        outSlowK[i] = 50;
        outSlowD[i] = 50;
        outSlowJ[i] = 50;
      }
    }

    double[][] result = {outSlowK,outSlowD,outSlowJ};
    return result;
  }

  public List<StockData> kdj(List<StockData> stockDatas){
    double[] closes = new double[stockDatas.size()];
    double[] high = new double[stockDatas.size()];
    double[] low = new double[stockDatas.size()];
    for (int i = 0; i < stockDatas.size(); i++) {
      closes[i] = stockDatas.get(i).attr(StockConstants.CLOSE);
      high[i] = stockDatas.get(i).attr(StockConstants.HIGH);
      low[i] = stockDatas.get(i).attr(StockConstants.LOW);
    }
    double[][] kdj = kdj(high, low, closes);
    for(int i=0;i<stockDatas.size();i++){
      stockDatas.get(i).attr("kdj_k",new BigDecimal(kdj[0][i]).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
      stockDatas.get(i).attr("kdj_d",new BigDecimal(kdj[1][i]).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
      stockDatas.get(i).attr("kdj_j",new BigDecimal(kdj[2][i]).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue());
    }
    return stockDatas;
  }

  // 6,12,24
  public double[] rsi(double[] prices, int period) {

    double[] output = new double[prices.length];
    double[] tempOutPut = new double[prices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.rsi(0, prices.length - 1, prices, period, begin, length, tempOutPut);

    for (int i = 0; i < period; i++) {
      output[i] = 0;
    }
    for (int i = period; 0 < i && i < (prices.length); i++) {
      output[i] = tempOutPut[i - period];
    }
    return output;
  }

  public double[] sar(double[] highPrices, double[] lowPrices, double optInAcceleration, double optInMaximum) {

    double[] output = new double[lowPrices.length];
    double[] tempoutput = new double[lowPrices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.sar(0, lowPrices.length - 1, highPrices, lowPrices, optInAcceleration, optInMaximum, begin,
        length, tempoutput);

    for (int i = 1; i < lowPrices.length; i++) {
      output[i] = tempoutput[i - 1];
    }
    return output;

  }

  public double[] adx(double[] lowPrices, double[] highPrices, double[] closePrices, int optInTimePeriod) {

    double[] output = new double[lowPrices.length];
    double[] tempOutPut = new double[lowPrices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.adx(0, lowPrices.length - 1, highPrices, lowPrices, closePrices, optInTimePeriod, begin, length,
        tempOutPut);
    // Adx(int startIdx, int endIdx, double[] inHigh, double[] inLow,
    // double[] inClose, int optInTimePeriod, out int outBegIdx, out int
    // outNBElement, double[] outReal);

    for (int i = 0; i < lowPrices.length - length.value; i++) {
      output[i] = 0;
    }
    for (int i = lowPrices.length - length.value; 0 < i && i < (lowPrices.length); i++) {
      output[i] = tempOutPut[i - (lowPrices.length - length.value)];
    }
    return output;
  }

  public double[] adxr(double[] lowPrices, double[] highPrices, double[] closePrices, int optInTimePeriod) {

    double[] output = new double[lowPrices.length];
    double[] tempOutPut = new double[lowPrices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.adxr(0, lowPrices.length - 1, highPrices, lowPrices, closePrices, optInTimePeriod, begin,
        length, tempOutPut);
    // Adx(int startIdx, int endIdx, double[] inHigh, double[] inLow,
    // double[] inClose, int optInTimePeriod, out int outBegIdx, out int
    // outNBElement, double[] outReal);

    for (int i = 0; i < lowPrices.length - length.value; i++) {
      output[i] = 0;
    }
    for (int i = lowPrices.length - length.value; 0 < i && i < (lowPrices.length); i++) {
      output[i] = tempOutPut[i - (lowPrices.length - length.value)];
    }

    return output;
  }

  public double[] cci(double[] highPrices, double[] lowPrices, double[] closePrices, int inTimePeriod) {

    double[] output = new double[lowPrices.length];
    double[] tempOutPut = new double[lowPrices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.cci(0, lowPrices.length - 1, highPrices, lowPrices, closePrices, inTimePeriod, begin, length,
        tempOutPut);
    // Cci(int startIdx, int endIdx, double[] inHigh, double[] inLow,
    // double[] inClose, int optInTimePeriod, out int outBegIdx, out int
    // outNBElement, double[] outReal);

    for (int i = 0; i < inTimePeriod - 1; i++) {
      output[i] = 0;
    }
    for (int i = inTimePeriod - 1; 0 < i && i < (lowPrices.length); i++) {
      output[i] = tempOutPut[i - inTimePeriod + 1];
    }

    return output;
  }

  public double[] mfi(double[] highPrices, double[] lowPrices, double[] closePrices, double[] inVolume,
      int optInTimePeriod) {

    double[] output = new double[lowPrices.length];
    double[] tempOutPut = new double[lowPrices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.mfi(0, lowPrices.length - 1, highPrices, lowPrices, closePrices, inVolume, optInTimePeriod,
        begin, length, tempOutPut);

    // Mfi(int startIdx, int endIdx, double[] inHigh, double[] inLow,
    // double[] inClose, double[] inVolume, int optInTimePeriod, out int
    // outBegIdx, out int outNBElement, double[] outReal);
    for (int i = 0; i < optInTimePeriod; i++) {
      output[i] = 0;
    }
    for (int i = optInTimePeriod; 0 < i && i < (lowPrices.length); i++) {
      output[i] = tempOutPut[i - optInTimePeriod];
    }

    return output;
  }

  public double[] obv(double[] prices, double[] volume) {
    double[] output = new double[prices.length];
    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.obv(0, prices.length - 1, prices, volume, begin, length, output);
    // public static RetCode Obv(int startIdx, int endIdx, double[] inReal,
    // double[] inVolume, out int outBegIdx, out int outNBElement, double[]
    // outReal);
    return output;

  }

  public double[] roc(double[] prices, int optInTimePeriod) {
    /*
     * ((price/prevPrice)-1)*100
     */
    double[] tempOutPut = new double[prices.length];
    double[] output = new double[prices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.roc(0, prices.length - 1, prices, optInTimePeriod, begin, length, tempOutPut);
    // Roc(int startIdx, int endIdx, float[] inReal, int optInTimePeriod,
    // out int outBegIdx, out int outNBElement, double[] outReal);
    for (int i = 0; i < optInTimePeriod - 1; i++) {
      output[i] = 0;
    }
    for (int i = optInTimePeriod - 1; 0 < i && i < (prices.length); i++) {
      output[i] = tempOutPut[i - optInTimePeriod + 1];
    }
    return output;
  }

  public double[] rocP(double[] prices, int optInTimePeriod) {
    /*
     * (price-prevPrice)/prevPrice
     */
    double[] tempOutPut = new double[prices.length];
    double[] output = new double[prices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.rocP(0, prices.length - 1, prices, optInTimePeriod, begin, length, tempOutPut);
    // Roc(int startIdx, int endIdx, float[] inReal, int optInTimePeriod,
    // out int outBegIdx, out int outNBElement, double[] outReal);
    for (int i = 0; i < optInTimePeriod - 1; i++) {
      output[i] = 0;
    }
    for (int i = optInTimePeriod - 1; 0 < i && i < (prices.length); i++) {
      output[i] = tempOutPut[i - optInTimePeriod + 1];
    }
    return output;
  }

  public double[] trix(double[] prices, int period) {

    double[] output = new double[prices.length];
    double[] tempOutPut = new double[prices.length];
    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.trix(0, prices.length - 1, prices, period, begin, length, tempOutPut);

    for (int i = 0; i < begin.value; i++) {
      output[i] = 0;
    }
    for (int i = begin.value; 0 < i && i < (prices.length); i++) {
      output[i] = tempOutPut[i - begin.value];
    }
    return output;
  }

  public double[] willR(double[] highPrices, double[] lowPrices, double[] closePrices, int inTimePeriod) {

    double[] output = new double[lowPrices.length];
    double[] tempOutPut = new double[lowPrices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.willR(0, lowPrices.length - 1, highPrices, lowPrices, closePrices, inTimePeriod, begin, length,
        tempOutPut);
    // WillR(int startIdx, int endIdx, double[] inHigh, double[] inLow,
    // double[] inClose, int optInTimePeriod, out int outBegIdx, out int
    // outNBElement, double[] outReal);

    for (int i = 0; i < inTimePeriod - 1; i++) {
      output[i] = 0;
    }
    for (int i = inTimePeriod - 1; 0 < i && i < (lowPrices.length); i++) {
      output[i] = tempOutPut[i - inTimePeriod + 1];
    }

    return output;
  }

  // AD=Chaikin A/D Line
  public double[] ad(double[] highPrices, double[] lowPrices, double[] closePrices, double[] inVolume,
      int optInTimePeriod) {

    double[] output = new double[lowPrices.length];
    // double[] tempOutPut = new double[lowPrices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.ad(0, lowPrices.length - 1, highPrices, lowPrices, closePrices, inVolume, begin, length, output);

    // Ad(int startIdx, int endIdx, double[] inHigh, double[] inLow,
    // double[] inClose, double[] inVolume, out int outBegIdx, out int
    // outNBElement, double[] outReal);

    return output;
  }

  public double[][] aroon(double[] inHigh, double[] inLow, int optInTimePeriod) {
    double[][] output = {new double[inHigh.length], new double[inHigh.length]};
    double[] tempOutPut1 = new double[inHigh.length];
    double[] tempOutPut2 = new double[inHigh.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    // MAType optInMAType = new MAType();
    // MAType optInSlowD_MAType = new MAType();

    retCode = core.aroon(0, inHigh.length - 1, inHigh, inLow, optInTimePeriod, begin, length, tempOutPut1,
        tempOutPut2);
    // Aroon(int startIdx, int endIdx, double[] inHigh, double[] inLow, int
    // optInTimePeriod, out int outBegIdx, out int outNBElement, double[]
    // outAroonDown, double[] outAroonUp);

    for (int i = 0; i < inHigh.length - length.value; i++) {
      output[0][i] = 0;
      output[1][i] = 0;
    }
    for (int i = inHigh.length - length.value; 0 < i && i < (inHigh.length); i++) {
      output[0][i] = tempOutPut1[i - (inHigh.length - length.value)];
      output[1][i] = tempOutPut1[i - (inHigh.length - length.value)];
    }

    return output;
  }

  public double[] aroonOsc(double[] inHigh, double[] inLow, int optInTimePeriod) {
    double[] output = new double[inHigh.length];
    double[] tempOutPut = new double[inHigh.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.aroonOsc(0, inHigh.length - 1, inHigh, inLow, optInTimePeriod, begin, length, tempOutPut);
    // AroonOsc(int startIdx, int endIdx, double[] inHigh, double[] inLow,
    // int optInTimePeriod, out int outBegIdx, out int outNBElement,
    // double[] outReal);

    for (int i = 0; i < inHigh.length - length.value; i++) {
      output[i] = 0;
    }
    for (int i = inHigh.length - length.value; 0 < i && i < (inHigh.length); i++) {
      output[i] = tempOutPut[i - (inHigh.length - length.value)];
    }

    return output;

  }

  public double[] bop(double[] openPrices, double[] highPrices, double[] lowPrices, double[] closePrices) {
    double[] output = new double[highPrices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.bop(0, lowPrices.length - 1, openPrices, highPrices, lowPrices, closePrices, begin, length,
        output);
    // Bop(int startIdx, int endIdx, double[] inOpen, double[] inHigh,
    // double[] inLow, double[] inClose, out int outBegIdx, out int
    // outNBElement, double[] outReal);

    return output;
  }

  public double[] cmo(double[] closePrices, int period) {

    double[] output = new double[closePrices.length];
    double[] tempOutPut = new double[closePrices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.cmo(0, closePrices.length - 1, closePrices, period, begin, length, tempOutPut);
    // Cmo(int startIdx, int endIdx, double[] inReal, int optInTimePeriod,
    // out int outBegIdx, out int outNBElement, double[] outReal);

    for (int i = 0; i < closePrices.length - length.value; i++) {
      output[i] = 0;
    }
    for (int i = closePrices.length - length.value; 0 < i && i < (closePrices.length); i++) {
      output[i] = tempOutPut[i - (closePrices.length - length.value)];
    }
    return output;
  }

  public double[] kama(double[] prices, int optInTimePeriod) {

    double[] tempOutPut = new double[prices.length];
    double[] output = new double[prices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.kama(0, prices.length - 1, prices, optInTimePeriod, begin, length, tempOutPut);
    // Kama(int startIdx, int endIdx, double[] inReal, int optInTimePeriod,
    // out int outBegIdx, out int outNBElement, double[] outReal);
    for (int i = 0; i < optInTimePeriod; i++) {
      output[i] = 0;
    }
    for (int i = optInTimePeriod; 0 < i && i < (prices.length); i++) {
      output[i] = tempOutPut[i - optInTimePeriod];
    }
    return output;
  }

  public double[] trima(double[] prices, int optInTimePeriod) {

    double[] tempOutPut = new double[prices.length];
    double[] output = new double[prices.length];

    MInteger begin = new MInteger();
    MInteger length = new MInteger();
    RetCode retCode = RetCode.InternalError;
    begin.value = -1;
    length.value = -1;

    retCode = core.trima(0, prices.length - 1, prices, optInTimePeriod, begin, length, tempOutPut);
    // Kama(int startIdx, int endIdx, double[] inReal, int optInTimePeriod,
    // out int outBegIdx, out int outNBElement, double[] outReal);
    for (int i = 0; i < optInTimePeriod - 1; i++) {
      output[i] = 0;
    }
    for (int i = optInTimePeriod - 1; 0 < i && i < (prices.length); i++) {
      output[i] = tempOutPut[i - optInTimePeriod + 1];
    }
    return output;
  }
}
