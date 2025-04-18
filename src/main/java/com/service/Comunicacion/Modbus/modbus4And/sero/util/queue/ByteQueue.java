/*
 * ============================================================================
 * GNU General Public License
 * ============================================================================
 *
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * When signing a commercial license with Infinite Automation Software,
 * the following extension to GPL is made. A special exception to the GPL is
 * included to allow you to distribute a combined work that includes BAcnet4J
 * without being obliged to provide the source code for any proprietary components.
 *
 * See www.infiniteautomation.com for commercial license options.
 * 
 * @author Matthew Lohbihler
 */
package com.service.Comunicacion.Modbus.modbus4And.sero.util.queue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ByteQueue implements Cloneable {
    private byte[] queue;
    private int head = -1;
    private int tail = 0;
    private int size = 0;

    private int markHead;
    private int markTail;
    private int markSize;

    public ByteQueue() {
        this(1024);
    }

    public ByteQueue(int initialLength) {
        queue = new byte[initialLength];
    }

    public ByteQueue(byte[] b) {
        this(b.length);
        push(b, 0, b.length);
    }

    public ByteQueue(byte[] b, int pos, int length) {
        this(length);
        push(b, pos, length);
    }

    public ByteQueue(String hex) {
        this(hex.length() / 2);
        push(hex);
    }

    public void push(String hex) {
        if (hex.length() % 2 != 0)
            throw new IllegalArgumentException("Hex string must have an even number of characters");
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0; i < b.length; i++)
            b[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        push(b, 0, b.length);
    }

    public void push(byte b) {
        if (room() == 0)
            expand();

        queue[tail] = b;

        if (head == -1)
            head = 0;
        tail = (tail + 1) % queue.length;
        size++;
    }

    public void push(int i) {
        push((byte) i);
    }

    public void push(long l) {
        push((byte) l);
    }

    /**
     * Push unsigned 2 bytes.
     */
    public void pushU2B(int i) {
        push((byte) (i >> 8));
        push((byte) i);
    }

    /**
     * Push unsigned 3 bytes.
     */
    public void pushU3B(int i) {
        push((byte) (i >> 16));
        push((byte) (i >> 8));
        push((byte) i);
    }

    /**
     * Push signed 4 bytes.
     */
    public void pushS4B(int i) {
        pushInt(i);
    }

    /**
     * Push unsigned 4 bytes.
     */
    public void pushU4B(long l) {
        push((byte) (l >> 24));
        push((byte) (l >> 16));
        push((byte) (l >> 8));
        push((byte) l);
    }

    public void pushChar(char c) {
        push((byte) (c >> 8));
        push((byte) c);
    }

    public void pushDouble(double d) {
        pushLong(Double.doubleToLongBits(d));
    }

    public void pushFloat(float f) {
        pushInt(Float.floatToIntBits(f));
    }

    public void pushInt(int i) {
        push((byte) (i >> 24));
        push((byte) (i >> 16));
        push((byte) (i >> 8));
        push((byte) i);
    }

    public void pushLong(long l) {
        push((byte) (l >> 56));
        push((byte) (l >> 48));
        push((byte) (l >> 40));
        push((byte) (l >> 32));
        push((byte) (l >> 24));
        push((byte) (l >> 16));
        push((byte) (l >> 8));
        push((byte) l);
    }

    public void pushShort(short s) {
        push((byte) (s >> 8));
        push((byte) s);
    }

    public void read(InputStream in, int length) throws IOException {
        if (length == 0)
            return;

        while (room() < length)
            expand();

        int tailLength = queue.length - tail;
        if (tailLength > length)
            readImpl(in, tail, length);
        else
            readImpl(in, tail, tailLength);

        if (length > tailLength)
            readImpl(in, 0, length - tailLength);

        if (head == -1)
            head = 0;
        tail = (tail + length) % queue.length;
        size += length;
    }

    private void readImpl(InputStream in, int offset, int length) throws IOException {
        int readcount;
        while (length > 0) {
            readcount = in.read(queue, offset, length);
            offset += readcount;
            length -= readcount;
        }
    }

    public void push(byte[] b) {
        push(b, 0, b.length);
    }

    public void push(byte[] b, int pos, int length) {
        if (length == 0)
            return;

        while (room() < length)
            expand();

        int tailLength = queue.length - tail;
        if (tailLength > length)
            System.arraycopy(b, pos, queue, tail, length);
        else
            System.arraycopy(b, pos, queue, tail, tailLength);

        if (length > tailLength)
            System.arraycopy(b, tailLength + pos, queue, 0, length - tailLength);

        if (head == -1)
            head = 0;
        tail = (tail + length) % queue.length;
        size += length;
    }

    public void push(ByteQueue source) {
        if (source.size == 0)
            return;

        if (source == this)
            source = (ByteQueue) clone();

        int firstCopyLen = source.queue.length - source.head;
        if (source.size < firstCopyLen)
            firstCopyLen = source.size;
        push(source.queue, source.head, firstCopyLen);

        if (firstCopyLen < source.size)
            push(source.queue, 0, source.tail);
    }

    public void push(ByteQueue source, int len) {
        // TODO There is certainly a more elegant way to do this...
        while (len-- > 0)
            push(source.pop());
    }

    public void push(ByteBuffer source) {
        int length = source.remaining();
        if (length == 0)
            return;

        while (room() < length)
            expand();

        int tailLength = queue.length - tail;
        if (tailLength > length)
            source.get(queue, tail, length);
        else
            source.get(queue, tail, tailLength);

        if (length > tailLength)
            source.get(queue, 0, length - tailLength);

        if (head == -1)
            head = 0;
        tail = (tail + length) % queue.length;
        size += length;
    }

    public void mark() {
        markHead = head;
        markTail = tail;
        markSize = size;
    }

    public void reset() {
        head = markHead;
        tail = markTail;
        size = markSize;
    }

    public byte pop() {
        byte retval = queue[head];

        if (size == 1) {
            head = -1;
            tail = 0;
        }
        else
            head = (head + 1) % queue.length;

        size--;

        return retval;
    }

    public int popU1B() {
        return pop() & 0xff;
    }

    public int popU2B() {
        return ((pop() & 0xff) << 8) | (pop() & 0xff);
    }

    public int popU3B() {
        return ((pop() & 0xff) << 16) | ((pop() & 0xff) << 8) | (pop() & 0xff);
    }

    public short popS2B() {
        return (short) (((pop() & 0xff) << 8) | (pop() & 0xff));
    }

    public int popS4B() {
        return ((pop() & 0xff) << 24) | ((pop() & 0xff) << 16) | ((pop() & 0xff) << 8) | (pop() & 0xff);
    }

    public long popU4B() {
        return ((long) (pop() & 0xff) << 24) | ((long) (pop() & 0xff) << 16) | ((long) (pop() & 0xff) << 8)
                | (pop() & 0xff);
    }

    public int pop(byte[] buf) {
        return pop(buf, 0, buf.length);
    }

    public int pop(byte[] buf, int pos, int length) {
        length = peek(buf, pos, length);

        size -= length;

        if (size == 0) {
            head = -1;
            tail = 0;
        }
        else
            head = (head + length) % queue.length;

        return length;
    }

    public int pop(int length) {
        if (length == 0)
            return 0;
        if (size == 0)
            throw new ArrayIndexOutOfBoundsException(-1);

        if (length > size)
            length = size;

        size -= length;

        if (size == 0) {
            head = -1;
            tail = 0;
        }
        else
            head = (head + length) % queue.length;

        return length;
    }

    public String popString(int length, Charset charset) {
        byte[] b = new byte[length];
        pop(b);
        return new String(b, charset);
    }

    public byte[] popAll() {
        byte[] data = new byte[size];
        pop(data, 0, data.length);
        return data;
    }

    public void write(OutputStream out) throws IOException {
        write(out, size);
    }

    public void write(OutputStream out, int length) throws IOException {
        if (length == 0)
            return;
        if (size == 0)
            throw new ArrayIndexOutOfBoundsException(-1);

        if (length > size)
            length = size;

        int firstCopyLen = queue.length - head;
        if (length < firstCopyLen)
            firstCopyLen = length;

        out.write(queue, head, firstCopyLen);
        if (firstCopyLen < length)
            out.write(queue, 0, length - firstCopyLen);

        size -= length;

        if (size == 0) {
            head = -1;
            tail = 0;
        }
        else
            head = (head + length) % queue.length;
    }

    public byte tailPop() {
        if (size == 0)
            throw new ArrayIndexOutOfBoundsException(-1);

        tail = (tail + queue.length - 1) % queue.length;
        byte retval = queue[tail];

        if (size == 1) {
            head = -1;
            tail = 0;
        }

        size--;

        return retval;
    }

    public byte peek(int index) {
        if (index >= size)
            throw new IllegalArgumentException("index " + index + " is >= queue size " + size);

        index = (index + head) % queue.length;
        return queue[index];
    }

    public byte[] peek(int index, int length) {
        byte[] result = new byte[length];
        // TODO: use System.arraycopy instead.
        for (int i = 0; i < length; i++)
            result[i] = peek(index + i);
        return result;
    }

    public byte[] peekAll() {
        byte[] data = new byte[size];
        peek(data, 0, data.length);
        return data;
    }

    public int peek(byte[] buf) {
        return peek(buf, 0, buf.length);
    }

    public int peek(byte[] buf, int pos, int length) {
        if (length == 0)
            return 0;
        if (size == 0)
            throw new ArrayIndexOutOfBoundsException(-1);

        if (length > size)
            length = size;

        int firstCopyLen = queue.length - head;
        if (length < firstCopyLen)
            firstCopyLen = length;

        System.arraycopy(queue, head, buf, pos, firstCopyLen);
        if (firstCopyLen < length)
            System.arraycopy(queue, 0, buf, pos + firstCopyLen, length - firstCopyLen);

        return length;
    }

    public int indexOf(byte b) {
        return indexOf(b, 0);
    }

    public int indexOf(byte b, int start) {
        if (start >= size)
            return -1;

        int index = (head + start) % queue.length;
        for (int i = start; i < size; i++) {
            if (queue[index] == b)
                return i;
            index = (index + 1) % queue.length;
        }
        return -1;
    }

    public int indexOf(byte[] b) {
        return indexOf(b, 0);
    }

    public int indexOf(byte[] b, int start) {
        if (b == null || b.length == 0)
            throw new IllegalArgumentException("cannot search for empty values");

        while ((start = indexOf(b[0], start)) != -1 && start < size - b.length + 1) {
            boolean found = true;
            for (int i = 1; i < b.length; i++) {
                if (peek(start + i) != b[i]) {
                    found = false;
                    break;
                }
            }

            if (found) {
                return start;
            }

            start++;
        }

        return -1;
    }

    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
        head = -1;
        tail = 0;
    }

    private int room() {
        return queue.length - size;
    }

    private void expand() {
        byte[] newb = new byte[queue.length * 2];

        if (head == -1) {
            queue = newb;
            return;
        }

        if (tail > head) {
            System.arraycopy(queue, head, newb, head, tail - head);
            queue = newb;
            return;
        }

        System.arraycopy(queue, head, newb, head + queue.length, queue.length - head);
        System.arraycopy(queue, 0, newb, 0, tail);
        head += queue.length;
        queue = newb;
    }

    @Override
    public Object clone() {
        try {
            ByteQueue clone = (ByteQueue) super.clone();
            // Array is mutable, so make a copy of it too.
            clone.queue = queue.clone();
            return clone;
        }
        catch (CloneNotSupportedException e) { /* Will never happen because we're Cloneable */
        }
        return null;
    }

    @Override
    public String toString() {
        if (size == 0)
            return "[]";

        StringBuffer sb = new StringBuffer();
        sb.append('[');
        sb.append(Integer.toHexString(peek(0) & 0xff));
        for (int i = 1; i < size; i++)
            sb.append(',').append(Integer.toHexString(peek(i) & 0xff));
        sb.append("]");

        return sb.toString();
    }

    public String dumpQueue() {
        StringBuffer sb = new StringBuffer();

        if (queue.length == 0)
            sb.append("[]");
        else {
            sb.append('[');
            sb.append(queue[0]);
            for (int i = 1; i < queue.length; i++) {
                sb.append(", ");
                sb.append(queue[i]);
            }
            sb.append("]");
        }

        sb.append(", h=").append(head).append(", t=").append(tail).append(", s=").append(size);
        return sb.toString();
    }
}
