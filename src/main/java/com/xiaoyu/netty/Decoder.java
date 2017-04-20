/**
 * 唯有读书,不慵不扰 
 **/
package com.xiaoyu.netty;

import java.util.List;

import com.xiaoyu.core.rpc.message.ResponseMsg;
import com.xiaoyu.core.serialize.DefaultSerialize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class Decoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		out.add(DefaultSerialize.deserialize(in.array(), ResponseMsg.class));
	}

}
