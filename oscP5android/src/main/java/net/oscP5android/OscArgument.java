/**
 * An OSC (Open Sound Control) library for processing.
 * <p>
 * ##copyright##
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 *
 * @author ##author##
 * @modified ##date##
 * @version ##version##
 */

package net.oscP5android;

import androidx.annotation.NonNull;

public final class OscArgument {

    private Object value;

    OscArgument() {
    }

    public int intValue() {
        return (Integer) value;
    }

    public char charValue() {
        return (Character) value;
    }

    public float floatValue() {
        return (Float) value;
    }

    public double doubleValue() {
        return (Double) value;
    }

    public long longValue() {
        return (Long) value;
    }

    public boolean booleanValue() {
        return (Boolean) value;
    }

    public String stringValue() {
        return ((String) value);
    }

    @NonNull
    public String toString() {
        return ((String) value);
    }

    public byte[] bytesValue() {
        return ((byte[]) value);
    }

    public byte[] blobValue() {
        return ((byte[]) value);
    }

    public int[] midiValue() {
        int[] myInt = new int[4];
        byte[] myByte = (byte[]) value;
        for (int i = 0; i < 4; i++) {
            myInt[i] = myByte[i];
        }
        return (myInt);
    }

    void setValue(Object theValue) {
        value = theValue;
    }
}
