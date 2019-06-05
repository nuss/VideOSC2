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
r = NetAddr("192.168.1.2", 32000);

// test feedback OSC on different color channels
// if different messages are sent under one and the same command name
// they should be listed in the regarding pixel
// selecting the same color channel again via menu resets the message display

// red
r.sendMsg('/vosc/red1', "red test 12")
r.sendMsg('/vosc/red1', "red test A")
r.sendMsg('/vosc/red5', "red test 2R")
r.sendMsg('/vosc/red5', "red test DDD")
r.sendMsg('/vosc/red15', "red test X")

// green
r.sendMsg('/vosc/green4', "green test 12")
r.sendMsg('/vosc/green4', "green test A")
r.sendMsg('/vosc/green3', "green test 2R")
r.sendMsg('/vosc/green15', "green test DDD")

// blue
r.sendMsg('/vosc/blue5', "blue test 12")
r.sendMsg('/vosc/blue5', "blue test A")
r.sendMsg('/vosc/blue5', "blue test 2R")
r.sendMsg('/vosc/blue8', "blue test DDD")
r.sendMsg('/vosc/blue13', "blue test X")

// send messages to random pixels in the green channel
// assuming a layout size of at least 24 pixels, e.g. 6 pixels wide, 4 pixels high
(
var slot = 24.rand+1;
r.sendMsg("/vosc/blue"++slot, "blue"+slot);
)

// send periodic messages to the green channel
// whenever switched to some other channel and then back to green
// the message should reappear in the 13th pixel immediately
(
fork ({
	loop {
		0.1.wait;
		r.sendMsg('/vosc/green13', "green13");
		"sent".postln;
	}
}, AppClock)
)