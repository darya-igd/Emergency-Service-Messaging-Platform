# Emergency Service Platform

This project implements an "Emergency Service" platform subscription service where users can subscribe to specific emergency channels based on their interests or needs, such as fire, medical, police, and natural disasters. Subscribers can report emergencies and receive updates relevant to the channel's topic, enabling effective community collaboration during crises.

## Project Structure

The project consists of two main components:

### 1. Server (Java)

The server provides centralized STOMP (Simple-Text-Oriented-Messaging-Protocol) services, ensuring efficient communication between users. It supports two modes of operation:

- **Thread-Per-Client (TPC)**: Handles each client with a dedicated thread
- **Reactor**: Uses an event-driven model for handling multiple clients efficiently

### 2. Client (C++)

The client allows users to interact with the Emergency Service system. It handles logic for subscribing to channels, sending emergency reports, and receiving updates from the server.

## Protocol

All communication between clients and server adheres to the STOMP protocol, ensuring standardized messaging.

## Requirements

### Server
- Java
- Maven

### Client
- C++
- Boost libraries

## Building the Project

### Server
```bash
cd server
mvn compile
```

### Client
```bash
cd client
make
```

## Running the Project

### Server
For Thread-Per-Client mode:
```bash
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="<port> tpc"
```

For Reactor mode:
```bash
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="<port> reactor"
```

### Client
```bash
bin/StompWCIClient
```

## Client Commands

1. **Login command**
   ```
   login {host:port} {username} {password}
   ```

2. **Join Emergency Channel command**
   ```
   join {channel_name}
   ```

3. **Exit Emergency Channel command**
   ```
   exit {channel_name}
   ```

4. **Report to channel command**
   ```
   report {file}
   ```

5. **Summarize emergency channel command**
   ```
   summary {channel_name} {user} {file}
   ```

6. **Logout Command**
   ```
   logout
   ```

## Project Files

### Server Files
- StompServer.java - Main server implementation
- ConnectionsImpl.java - Manages client connections
- StompMessagingProtocolImp.java - Protocol implementation
- Various frame implementations (ConnectFrame.java, SendFrame.java, etc.)

### Client Files
- StompClient.cpp - Main client implementation
- ConnectionHandler.cpp - Handles client-server connection
- StompProtocol.cpp - Implements the protocol logic
- Event.cpp - Manages emergency events

## License

This project is submitted as part of the SPL course requirements.