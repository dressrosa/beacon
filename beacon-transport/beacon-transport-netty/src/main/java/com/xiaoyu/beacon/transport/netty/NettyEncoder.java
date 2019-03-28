/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.beacon.transport.netty;

import com.xiaoyu.beacon.common.constant.From;
import com.xiaoyu.beacon.common.extension.SpiManager;
import com.xiaoyu.beacon.common.message.RpcMessage;
import com.xiaoyu.beacon.common.message.RpcRequest;
import com.xiaoyu.beacon.serialize.Serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码
 * 
 * @author hongyu
 * @date 2018-02
 * @description
 */
public class NettyEncoder extends MessageToByteEncoder<RpcMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        if (msg instanceof RpcRequest) {
            msg.setFrom((byte) From.CLIENT.ordinal());
        } else {
            msg.setFrom((byte) From.SERVER.ordinal());
        }
        byte[] b = SpiManager.defaultSpiExtender(Serializer.class)
                .serialize(msg);
        // 写入请求端
        out.writeByte(msg.getFrom());
        // 写入长度
        out.writeInt(b.length);
        // 写入字节
        out.writeBytes(b);

    }

}
