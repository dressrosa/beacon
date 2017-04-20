/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.netty;

import com.xiaoyu.core.rpc.message.RequestMsg;
import com.xiaoyu.core.serialize.DefaultSerialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Encoder extends MessageToByteEncoder<RequestMsg> {

	@Override
	protected void encode(ChannelHandlerContext ctx, RequestMsg msg, ByteBuf out) throws Exception {
		out.writeBytes(DefaultSerialize.serialize(msg));
	}

}
