// what's coming in?
OSCFunc.trace(true)
OSCFunc.trace(false)

/*****************/
/* receiving OSC */
/*****************/

// collect OSC messages coming in under '/vosc/red1' into an array,
// n is the number of messages to be collected
(
n = 80; ~vals = nil!n; i = 0;
OSCdef(\collect, { |msg| if (i < n) { ~vals[i] = msg[1]; i = i+1 } }, '/vosc/red1');
)
// upper expression collects 80 incoming messages. wait a little, then execute next line...
OSCdef(\collect).clear

// look at the array
[~vals, ~times].postln;

// plot array

// if incoming values are not normalized
~vals.plot(bounds: Window.screenBounds.width @ Window.screenBounds.height, numChannels: 1, discrete: true, minval: 0, maxval: 255);
// normalized OSC values
~vals.plot(bounds: Window.screenBounds.width @ Window.screenBounds.height, numChannels: 1, discrete: true, minval: 0, maxval: 1);

/****************/
/* OSC feedback */
/****************/

// create a NetAddr equaling your phone's IP and reception port (device IP, feedback port)
// set IP address to your phone's IP address
// port 32000 is VideOSC's default feedback port
r = NetAddr("192.168.1.2", 32000);

// test feedback OSC on different color channels
// if different messages are sent under one and the same command name
// they should be listed in the regarding pixel
// selecting the same color channel again via menu resets the message display

// red, whatever string is sent should flicker shortly in the regarding pixel
r.sendMsg('/vosc/red1/name', "red test 12")
r.sendMsg('/vosc/red1/name', "red test A")
r.sendMsg('/vosc/red5/name', "red test 2R")
r.sendMsg('/vosc/red5/name', "red test DDD")
r.sendMsg('/vosc/red15/name', "red test X")

// green
r.sendMsg('/vosc/green4/name', "green test 12")
r.sendMsg('/vosc/green4/name', "green test A")
r.sendMsg('/vosc/green3/name', "green test 2R")
r.sendMsg('/vosc/green15/name', "green test DDD")

// blue
r.sendMsg('/vosc/blue5/name', "blue test 12")
r.sendMsg('/vosc/blue5/name', "blue test A")
r.sendMsg('/vosc/blue5/name', "blue test 2R")
r.sendMsg('/vosc/blue8/name', "blue test DDD")
r.sendMsg('/vosc/blue13/name', "blue test X")

// send messages to random pixels in the green channel
// assuming a layout size of at least 24 pixels, e.g. 6 pixels wide, 4 pixels high
(
var slot = 24.rand+1;
r.sendMsg("/vosc/blue"++slot++"/name", "blue"+slot);
)

// send periodic messages to the green channel
// whenever switched to some other channel and then back to green
// the message should reappear in the 13th pixel immediately
// the sent name will flicker. However this should be improved in future versions of VideOSC
(
fork ({
	loop {
		0.1.wait;
		r.sendMsg('/vosc/green13/name', "green13");
		"sent".postln;
	}
}, AppClock)
)

// a simple OSC feedback test
(
n ?? { n = NetAddr("192.168.0.201", 32000) };
OSCdef(\videosc, { |msg, time, addr, recvPort|
	n.sendMsg((msg[0] ++ '/name').asSymbol, "sent in response to OSC coming from" + msg[0]);
}, '/vosc/blue9')
)