package com.tquant.gateway.tiger;

import com.tigerbrokers.stock.openapi.client.socket.data.TradeTick;
import com.tigerbrokers.stock.openapi.client.socket.data.pb.AssetData;
import com.tigerbrokers.stock.openapi.client.socket.data.pb.OrderStatusData;
import com.tigerbrokers.stock.openapi.client.socket.data.pb.OrderTransactionData;
import com.tigerbrokers.stock.openapi.client.socket.data.pb.PositionData;
import com.tigerbrokers.stock.openapi.client.socket.data.pb.QuoteBBOData;
import com.tigerbrokers.stock.openapi.client.socket.data.pb.QuoteBasicData;
import com.tigerbrokers.stock.openapi.client.socket.data.pb.QuoteDepthData;
import com.tquant.core.core.Gateway;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Tick;
import com.tigerbrokers.stock.openapi.client.socket.ApiComposeCallback;
import com.tigerbrokers.stock.openapi.client.struct.SubscribedSymbol;

/**
 * Description:
 *
 * @author kevin
 * @date 2019/08/20
 */
public class TigerSubscribeApi implements ApiComposeCallback {

    private Gateway gateway;

    public TigerSubscribeApi(Gateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public void error(String errorMsg) {
        gateway.log("{} subscribeError {}", gateway.getGatewayName(), errorMsg);
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {
        gateway.log("{} subscribeError {}", gateway.getGatewayName(), errorMsg);
    }

    @Override
    public void connectionClosed() {
        gateway.log("{} connectionClosed ", gateway.getGatewayName());
    }


    @Override
    public void connectionAck() {
        gateway.log("{} connectionAck ", gateway.getGatewayName());
    }

    @Override
    public void connectionAck(int serverSendInterval, int serverReceiveInterval) {
        gateway.log("{} connectionAck sendInterval:{},receiveInterval:{} ", gateway.getGatewayName(), serverSendInterval,
                serverReceiveInterval);
    }

    @Override
    public void hearBeat(String heartBeatContent) {
        gateway.log("{} connectionHeartBeat {}", gateway.getGatewayName(), heartBeatContent);
    }

    @Override
    public void serverHeartBeatTimeOut(String channelIdAsLongText) {
        gateway.log("{} ConnectionHeartBeatTimeout {}", gateway.getGatewayName(), channelIdAsLongText);
    }

    @Override
    public void getSubscribedSymbolEnd(SubscribedSymbol subscribedSymbol) {
        gateway.log("{} subscribeSymbols {}", gateway.getGatewayName(), subscribedSymbol);
    }

    @Override
    public void connectionKickout(int errorCode, String errorMsg) {
        gateway.log("{} connectionKickoff {} {}", gateway.getGatewayName(), errorCode, errorMsg);
    }

    @Override
    public void orderStatusChange(OrderStatusData data) {
        gateway.log("orderChange {}",data);
        Order order = new Order();
        order.setAccount(data.getAccount());
        order.setAverageFilledPrice(data.getAvgFillPrice());
        order.setDirection(data.getAction());
        order.setId(data.getId());
        order.setOrderType(data.getOrderType());
        order.setSymbol(data.getSymbol());
        order.setVolume(data.getTotalQuantity());
        order.setFilledVolume(data.getFilledQuantity());
        order.setStatus(data.getStatus());
        gateway.onOrder(order);
    }

    @Override
    public void orderTransactionChange(OrderTransactionData data) {

    }

    @Override
    public void positionChange(PositionData data) {

    }

    @Override
    public void assetChange(AssetData data) {

    }

    @Override
    public void tradeTickChange(TradeTick data) {

    }

    @Override
    public void quoteChange(QuoteBasicData data) {
        gateway.log("quoteChange {}",data);
        Tick tick = new Tick();
        tick.setSymbol(data.getSymbol());
        tick.setIdentifier(data.getIdentifier());
        tick.setVolume(data.getVolume());
        tick.setLatestPrice(data.getLatestPrice());
        tick.setAmount(data.getAmount());
        tick.setOpen(data.getOpen());
        tick.setHigh(data.getHigh());
        tick.setLow(data.getLow());
        //close
        //tick.setClose(data.get);
        tick.setPreClose(data.getPreClose());

        gateway.onTick(tick);
    }

    @Override
    public void quoteAskBidChange(QuoteBBOData data) {

    }

    @Override
    public void optionChange(QuoteBasicData data) {

    }

    @Override
    public void optionAskBidChange(QuoteBBOData data) {

    }

    @Override
    public void futureChange(QuoteBasicData data) {

    }

    @Override
    public void futureAskBidChange(QuoteBBOData data) {

    }

    @Override
    public void depthQuoteChange(QuoteDepthData data) {
        gateway.log("{} depthQuote change {}", gateway.getGatewayName(), data);
    }

    @Override
    public void subscribeEnd(int id, String subject, String result) {
        gateway.log("{} subscribeEnd {} {} {}", gateway.getGatewayName(), id, subject, result);
    }

    @Override
    public void cancelSubscribeEnd(int id, String subject, String result) {
        gateway.log("{} cancelSubscribeEnd {} {} {}", gateway.getGatewayName(), id, subject, result);
    }

}
