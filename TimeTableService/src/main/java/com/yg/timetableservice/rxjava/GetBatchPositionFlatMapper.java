package com.yg.timetableservice.rxjava;


import com.alibaba.fastjson.JSONArray;
import com.yg.timetableservice.handler.DataHandler;
import com.yg.timetableservice.struct.PositionTurn;
import com.yg.timetableservice.util.OCSUtil;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetBatchPositionFlatMapper implements Func1<Map<String, JSONArray>, Observable<Map<String, JSONArray>>> {
    private class Zipper implements Func2<Map<String,?>, Map<String, JSONArray>, Map<String, JSONArray>> {
        @Override
        public Map<String, JSONArray> call(Map<String, ?> positionMap, Map<String, JSONArray> allTurns) {
            for (JSONArray turns : allTurns.values()) {
                DataHandler.zipTurnAndPos(positionMap, turns, timestamp);
            }
            return allTurns;
        }
    }
    private OCSUtil ocsUtil;
    private long timestamp;
    public GetBatchPositionFlatMapper(long timestamp, OCSUtil ocsUtil) {
        this.timestamp = timestamp;
        this.ocsUtil = ocsUtil;
    }
    @Override
    public Observable<Map<String, JSONArray>> call(Map<String, JSONArray> allturns) {
        List<String> ocsKeys = new ArrayList<>();
        for (JSONArray turns : allturns.values()) {
            for (Object o : turns) {
                PositionTurn turn = (PositionTurn)o;
                //006#639#1#13:19:33
                ocsKeys.add(turn.getTurnId());
            }
        }
        Observable<Map<String, ?>> ocsObservable = ocsUtil.asyncGetBulkData(ocsKeys).observeOn(Schedulers.computation());
        Observable<Map<String, JSONArray>> JSONArrayObservable = Observable.just(allturns);
        return Observable.zip(ocsObservable, JSONArrayObservable, new Zipper());
    }
}
