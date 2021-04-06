/**
 * A network library for processing which supports UDP, TCP and Multicast.
 *
 * ##copyright##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author		##author##
 * @modified	##date##
 * @version		##version##
 */

package net.netP5android;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;

public interface Transmitter {

	boolean send(byte[] theContent);

	boolean send(byte[] theContent, Collection<InetSocketAddress> theAddress);

	boolean send(byte[] theContent, String theHost, int thePort);

	boolean send(byte[] theContent, SocketAddress... theAddress);

	boolean close();
}
