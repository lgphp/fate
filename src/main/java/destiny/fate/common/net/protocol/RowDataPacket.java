package destiny.fate.common.net.protocol;

import destiny.fate.common.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangtianlong01 on 2017/10/11.
 */
public class RowDataPacket extends MySQLPacket {

    private static final byte NULL_MARK = (byte) 251;

    public final int fieldCount;
    public final List<byte[]> fieldValues;

    public RowDataPacket(int fieldCount) {
        this.fieldCount = fieldCount;
        this.fieldValues = new ArrayList<byte[]>(fieldCount);
    }

    public void add(byte[] value) {
        fieldValues.add(value);
    }

    public void read(byte[] data) {
        MySQLMessage mm = new MySQLMessage(data);
        packetLength = mm.readUB3();
        packetId = mm.read();
        for (int i = 0; i < fieldCount; i++) {
            fieldValues.add(mm.readBytesWithLength());
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx) {
        ByteBuf buf = ctx.alloc().buffer();
        BufferUtil.writeUB3(buf, calcPacketSize());
        buf.writeByte(packetId);
        for (int i = 0; i < fieldCount; i++) {
            byte[] fieldValue = fieldValues.get(i);
            if (fieldValue == null || fieldValue.length == 0) {
                buf.writeByte(RowDataPacket.NULL_MARK);
            } else {
                BufferUtil.writeLength(buf, fieldValue.length);
                buf.writeBytes(fieldValue);
            }
        }
    }

    @Override
    public ByteBuf writeBuf(ByteBuf buffer, ChannelHandlerContext ctx) {
        BufferUtil.writeUB3(buffer, calcPacketSize());
        buffer.writeByte(packetId);
        for (int i = 0; i < fieldCount; i++) {
            byte[] fv = fieldValues.get(i);
            if (fv == null || fv.length == 0) {
                buffer.writeByte(RowDataPacket.NULL_MARK);
            } else {
                BufferUtil.writeLength(buffer, fv.length);
                buffer.writeBytes(fv);
            }
        }
        return buffer;
    }

    @Override
    public int calcPacketSize() {
        int size = 0;
        for (int i = 0; i < fieldCount; i++) {
            byte[] v = fieldValues.get(i);
            size += (v == null || v.length == 0) ? 1 : BufferUtil.getLength(v);
        }
        return size;
    }

    @Override
    protected String getPacketInfo() {
        return "MySQL RowData Packet";
    }
}
