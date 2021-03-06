package com.yg.timetableservice.util;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.internal.BulkGetCompletionListener;
import net.spy.memcached.internal.GetCompletionListener;
import net.spy.memcached.internal.GetFuture;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.Subscriber;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

@Component("ocsUtil")
public class OCSUtil {
    final private String host =  "e7bfd8efc27145da.m.cnhzaliqshpub001.ocs.aliyuncs.com";
    final int port = 11211;
    final private String username = "e7bfd8efc27145da";
    final private String password = "di10cEa93mtscz";

    /*final private String host =  "9c0e27d0f09544c9.m.cnhzaliqshpub001.ocs.aliyuncs.com";
    final int port = 11211;
    final private String username = "9c0e27d0f09544c9";
    final private String password = "Yuanguang2014";*/
    private MemcachedClient mcClient = null;

    public OCSUtil() {
        LogUtil.asyncDebug("ocs host: ", host);
        config();
    }

    public boolean config() {
        // try to initialize ocs
        try {
            PlainCallbackHandler callbackHandler = new PlainCallbackHandler(username, password);
            AuthDescriptor ad = new AuthDescriptor(new String[]{"PLAIN"}, callbackHandler);
            ConnectionFactoryBuilder cfBuilder = new ConnectionFactoryBuilder();
            ConnectionFactory cf = cfBuilder.setProtocol(ConnectionFactoryBuilder.Protocol.BINARY).setAuthDescriptor(ad).build();
            List<InetSocketAddress> addresses = AddrUtil.getAddresses(host + ":" + port);
            mcClient = new MemcachedClient(cf, addresses);
            return true;
        } catch (IOException e) {

        }
        return false;
    }

    public void setData(String key, int expireIn, Object value) {
        mcClient.set(key, expireIn, value);
    }

    public Object getData(String key) {
        Object result;
        result = mcClient.get(key);
        if (result == null)
            return null;
        return result;
    }

    public void asyncGetData(String key, GetCompletionListener listener) {
        GetFuture<Object> future = mcClient.asyncGet(key);
        future.addListener(listener);
    }
    public void asyncGetBulkData(List<String> keys, BulkGetCompletionListener listener) {
        BulkFuture<Map<String, Object>> future = mcClient.asyncGetBulk(keys);
        future.addListener(listener);
    }

    /**
     *
     * @param key OCSkey
     * @param defaultValue if ocs return null,put defaultValue
     * @return
     */
    public Observable<Object> asyncGetData(String key, Object defaultValue) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            public void call(Subscriber<? super Object> subscriber) {
                long beginTimeStamp = System.currentTimeMillis();
                asyncGetData(key, getFuture -> {
                    LogUtil.asyncDebug("ocs get cost time:" + (System.currentTimeMillis() - beginTimeStamp));
                    if (getFuture.get() != null) {
                        subscriber.onNext(getFuture.get());
                    }
                    else
                        subscriber.onNext(defaultValue);
                });
            }
        });
    }
    public  Observable<Map<String, ?>> asyncGetBulkData(List<String> keys) {
        return Observable.create(new Observable.OnSubscribe<Map<String, ?>>() {
            public void call(Subscriber<? super Map<String, ?>> subscriber) {
                long ocsGetTimeStamp = System.currentTimeMillis();
                asyncGetBulkData(keys, getFuture -> {
                    LogUtil.asyncDebug("ocs get time:" + (System.currentTimeMillis() - ocsGetTimeStamp));
                    subscriber.onNext(getFuture.get());
                });
            }
        });
    }

    public Map<String, Object> getBulkData(List<String> keys) {
        return mcClient.getBulk(keys);
    }

    public void close() {
        mcClient.shutdown();
    }
}
