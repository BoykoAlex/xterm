function resizeTerminal(terminal, fitAddon, ws, session) {
    if (terminal.element.clientWidth > 0 && terminal.element.clientHeight > 0) {
        fitAddon.fit();
        ws.send(JSON.stringify({
            type: 'size',
            id: session,
            size: {
            	cols: terminal.cols,
            	rows: terminal.rows
            }
        }));
    }
}

function ping(ws) {
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({type: "ping"}));
        setTimeout(ping, 50000);
    }
}

let started = false;

function startTerminal(elementId, session, wsUrl, theme) {

	if (started) {
		return
	} else {
		started = true;
	}

    const terminal = new Terminal({
        cursorBlink: true,
        theme: theme
    });

    const fitAddon = new FitAddon.FitAddon();

    const terminalParent = document.getElementById(elementId);

    terminal.loadAddon(fitAddon);

    terminal.open(terminalParent);

    const ws = new WebSocket(wsUrl);

    ws.onopen = function() {
        // Web Socket is connected, send data using send()
        // ws.send("Message to send");
        ws.send(JSON.stringify({
            type: 'init',
            id: session
        }));

        // Setup periodic ping messages
        ping(ws);

        // Send initial size of the terminal for the pty process to adjust
        // Unless the size of the parent element is 0
        resizeTerminal(terminal, fitAddon, ws, session);
    };

    ws.onmessage = function (evt) {
        const message = JSON.parse(evt.data);
        if (message.id === session) {
            terminal.write(message.data);
        } else {
            console.warning("Client session " + session + " received message for session " + message.id);
        }
    };

    ws.onclose = function(evt) {
        terminal.write("Closed");
    };

	let previousData = '';
	const is_eclipse_buggy_browser = typeof window.navigator.userAgent === 'string'
		&& (window.navigator.userAgent.indexOf("Safari/522.0") >= 0 || window.navigator.userAgent.indexOf("Windows NT 6.2") >= 0); 
    terminal.onData(function(data) {
		if (is_eclipse_buggy_browser) {
			if (data.length === 1 && data === previousData) {
				// Workaround double input on eclipse browser on mac
				// skip - don't send the message. Let the next message however
				previousData = '';
				return;
			} else {
				previousData = data;
			}
		}
		
        ws.send(JSON.stringify({
            type: 'data',
            id: session,
            data: data
        }));
	        
    });

    new ResizeSensor(terminalParent, _.throttle(function() {
        if (ws.readyState === WebSocket.OPEN) {
            resizeTerminal(terminal, fitAddon, ws, session);
        }
    }, 500));

}