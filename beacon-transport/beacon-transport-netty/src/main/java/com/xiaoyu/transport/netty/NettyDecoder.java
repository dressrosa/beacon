/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.transport.netty;

import java.util.List;

import com.xiaoyu.core.common.constant.BeaconConstants;
import com.xiaoyu.core.common.constant.From;
import com.xiaoyu.core.common.extension.SpiManager;
import com.xiaoyu.core.common.message.RpcMessage;
import com.xiaoyu.core.common.message.RpcRequest;
import com.xiaoyu.core.common.message.RpcResponse;
import com.xiaoyu.core.serialize.Serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 解码
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class NettyDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < BeaconConstants.INT_LEN) {
            return;
        }
        byte[] bytes = new byte[in.readableBytes() - BeaconConstants.INT_LEN - BeaconConstants.FROM_LEN];
        // 读请求端
        byte from = in.readByte();
        // 读长度
        in.readInt();
        in.readBytes(bytes);
        // if(in.hasArray()) {//判断 不然direct buffer不支持
        // //https://stackoverflow.com/questions/25222392/netty-correct-usage-of-a-decoder
        // in.array()
        // }
        RpcMessage msg = null;
        if (from == From.CLIENT.ordinal()) {
            msg = SpiManager.defaultSpiExtender(Serializer.class)
                    .deserialize(bytes, RpcRequest.class);
        } else {
            try {
                msg = SpiManager.defaultSpiExtender(Serializer.class)
                        .deserialize(bytes, RpcResponse.class);
            } catch (RuntimeException e) {
                // provider抛出的异常,在consumer里面找不到该异常类,导致无法反序列化
                if (e.getCause() instanceof ClassNotFoundException) {
                    msg = SpiManager.defaultSpiExtender(Serializer.class)
                            .deserialize(bytes, RpcResponse.class, "exception");
                }
            }

        }
        out.add(msg);
    }

}
