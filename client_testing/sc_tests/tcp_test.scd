(
OSCdef(\oscp5, { |msg, time, addr, recvPort|
	[msg, time, addr, recvPort].postln;
}, '/response')
)

(
n = NetAddr("127.0.0.1", 11000).tryConnectTCP(
	{ "halleluja!".postln },
	{ "fuck off!".postln }
)
)

n.sendMsg('/test', "fuckers!")

t = NetAddr("10.42.0.237", 32100).tryConnectTCP(
	{ "halleluja!".postln },
	{ "fuck off!".postln }
);