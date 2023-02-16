package com.tquant.gateway.tiger;

import com.alibaba.fastjson.JSONObject;
import com.tquant.core.core.Gateway;
import com.tquant.core.model.data.Asset;
import com.tquant.core.model.data.Order;
import com.tquant.core.model.data.Position;
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
    public void connectionKickoff(int errorCode, String errorMsg) {
        gateway.log("{} connectionKickoff", gateway.getGatewayName());
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
    public void orderStatusChange(JSONObject jsonObject) {
        Order order = jsonObject.toJavaObject(Order.class);
        gateway.log("orderChange {}",jsonObject.toJSONString());
        gateway.onOrder(order);
    }

    @Override
    public void positionChange(JSONObject jsonObject) {
        Position position = jsonObject.toJavaObject(Position.class);
        //gateway.log("positionChange {}",jsonObject.toJSONString());
        //gateway.onPosition(position);
    }

    @Override
    public void assetChange(JSONObject jsonObject) {
        Asset asset = jsonObject.toJavaObject(Asset.class);
        //gateway.log("assetChange {}",jsonObject.toJSONString());
        //gateway.onAsset(asset);
    }

    @Override
    public void quoteChange(JSONObject jsonObject) {
        Tick tick = new Tick();
        tick.jsonToTick(jsonObject);
        gateway.onTick(tick);
    }

    @Override
    public void tradeTickChange(JSONObject jsonObject) {

    }

    @Override
    public void optionChange(JSONObject jsonObject) {
        Tick tick = new Tick();
        tick.jsonToTick(jsonObject);
        gateway.onTick(tick);
    }

    @Override
    public void futureChange(JSONObject jsonObject) {
        Tick tick = new Tick();
        tick.jsonToTick(jsonObject);
        gateway.onTick(tick);
    }

    @Override
    public void depthQuoteChange(JSONObject jsonObject) {
        gateway.log("{} depthQuote change {}", gateway.getGatewayName(), jsonObject.toJSONString());
    }

    @Override
    public void subscribeEnd(String id, String subject, JSONObject jsonObject) {
        gateway.log("{} subscribeEnd {} {} {}", gateway.getGatewayName(), id, subject, jsonObject.toJSONString());
    }

    @Override
    public void cancelSubscribeEnd(String id, String subject, JSONObject jsonObject) {
        gateway.log("{} cancelSubscribeEnd {} {} {}", gateway.getGatewayName(), id, subject, jsonObject.toJSONString());
    }
}
