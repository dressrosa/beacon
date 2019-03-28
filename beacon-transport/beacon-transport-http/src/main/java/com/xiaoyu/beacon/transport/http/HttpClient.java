/**
 * 唯有读书,不慵不扰
 */
package com.xiaoyu.beacon.transport.http;

import java.util.concurrent.Future;

import com.xiaoyu.beacon.common.constant.From;
import com.xiaoyu.beacon.transport.api.Client;

import feign.Feign;
import feign.Feign.Builder;

/**
 * @author hongyu
 * @date 2018-08
 * @description 采用feign的http client
 */
public class HttpClient implements Client {

    /**
     * feign builder
     */
    private static Builder feign;

    /**
     * 访问地址
     */
    private String url;

    public HttpClient(String host, Integer port) {
        url = "http://";
        if (port == null) {
            url = url.concat(host);
        } else {
            url = url.concat(host + ":" + port);
        }
        feign = Feign.builder()
                .encoder(new HttpEncoder())
                .decoder(new HttpDecoder());
    }

    @Override
    public void stop() {
    }

    @Override
    public void start() {
    }

    @Override
    public Future<Object> sendFuture(Object message) throws Exception {
        return HttpChannel.getChannel(feign, url, From.CLIENT).sendFuture(message);
    }

    @Override
    public Object send(Object message) throws Exception {
        return HttpChannel.getChannel(feign, url, From.CLIENT).send(message);
    }

}
