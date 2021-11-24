# Network Programming - Java Chat & Server

## EchoChat

The UI allow to choose the user of unicast or multicast:
- To choose unicast, don't select the checkbox. It's necessary to start the EchoServer first.
- To choose multicast, select the checkbox. 

## Architecture

The client have two threads: 
  - The first for the UI, witch allow sending messages to the network
  - The second for receive messages from the network and show them in the UI 

### Unicast

In the unicast or centralized mode all messages pass through the EchoServer, who distribute the messages to the EchoClients.
The EchoServer save the history of the chat, so new EchoClients can view the message sent before they start the app.

### Multicast

In the multicast or distributed mode all messages are sent to the group, going direct to the EchoClients connected to the group.
There is no EchoServer as intermediary.

## Server HTTP

### Config
- PORT 3000

### Methods (Status)

- GET (200, 404, 500):
  - HTMLs
  - Images
  - JSON
- POST (200, 500, 409):
  - JSON
- PUT (200, 500)
  - JSON
- DELETE (200, 404, 500)
  - JSON

### Adder

When the user access to the localhost:3000 can see the main page "adder.html", where it can sum two number, the operation is calculated in the WebServer and returned to the Client.
The client can see the favicon.ico, audio.mp3, and video.mp3.