#include "../include/StompProtocol.h"

StompProtocol::Frame StompProtocol::parseFrame(const std::string &frameString) {
    Frame frame;
    std::stringstream ss(frameString);
    std::string line;
    bool bodyStarted = false;

    // Read the command
    if (std::getline(ss, line)) {
        frame.command = line;
    }

    // Read headers
    while (std::getline(ss, line) && line != "" && !bodyStarted) {
        if (line.empty()) {
            bodyStarted = true;
            continue;
        }
        size_t delimiterPos = line.find(':');
        if (delimiterPos != std::string::npos) {
            std::string key = line.substr(0, delimiterPos);
            std::string value = line.substr(delimiterPos + 1);
            frame.headers[key] = value;
        }
    }

    // Read the body
    std::string tempBody;
    while (std::getline(ss, line)) {
        tempBody += line + "\n";
    }
    if (!tempBody.empty()) {
        frame.body = tempBody.substr(0, tempBody.length() - 1);
    }

    return frame;
}

std::string StompProtocol::serializeFrame(const Frame &frame) {
    std::string frameString = frame.command + "\n";
    for (const auto &pair : frame.headers) {
        frameString += pair.first + ":" + pair.second + "\n";
    }
    frameString += "\n" + frame.body + '\0';
    return frameString;
}

std::string StompProtocol::createConnectFrame(const std::string& username, const std::string& password) {
    Frame connectFrame;
    connectFrame.command = "CONNECT";
    connectFrame.headers["accept-version"] = "1.2";
    connectFrame.headers["host"] = "stomp.cs.bgu.ac.il";
    connectFrame.headers["login"] = username;
    connectFrame.headers["passcode"] = password;
    return serializeFrame(connectFrame);
}

std::string StompProtocol::createSubscribeFrame(const std::string& channel, const std::string& subscriptionId, const std::string& receiptId) {
    Frame subscribeFrame;
    subscribeFrame.command = "SUBSCRIBE";
    subscribeFrame.headers["destination"] = "/" + channel;
    subscribeFrame.headers["id"] = subscriptionId;
    subscribeFrame.headers["receipt"] = receiptId;
    return serializeFrame(subscribeFrame);
}

std::string StompProtocol::createUnsubscribeFrame(const std::string& subscriptionId, const std::string& receiptId) {
    Frame unsubscribeFrame;
    unsubscribeFrame.command = "UNSUBSCRIBE";
    unsubscribeFrame.headers["id"] = subscriptionId;
    unsubscribeFrame.headers["receipt"] = receiptId;
    return serializeFrame(unsubscribeFrame);
}

std::string StompProtocol::createSendFrame(const std::string& channel, const std::string& message) {
    Frame sendFrame;
    sendFrame.command = "SEND";
    sendFrame.headers["destination"] = "/" + channel;
    sendFrame.body = message;
    return serializeFrame(sendFrame);
}

std::string StompProtocol::createSendReportFrame(const std::string& channel, const std::string& user, const std::string& city, const std::string& eventName,
                                                  const std::string& dateTime, const std::map<std::string, std::string>& generalInformation, const std::string& description) {
    Frame sendFrame;
    sendFrame.command = "SEND";
    sendFrame.headers["destination"] = "/" + channel;
    sendFrame.body += "user:" + user + "\n";
    sendFrame.body += "city:" + city + "\n";
    sendFrame.body += "event name:" + eventName + "\n";
    sendFrame.body += "date time:" + dateTime + "\n";
    sendFrame.body += "general information:\n";
    for (const auto &[key, value] : generalInformation) {
        sendFrame.body += "\t" + key + ":" + value + "\n";
    }
    sendFrame.body += "description:\n" + description;
    return serializeFrame(sendFrame);
}

std::string StompProtocol::createDisconnectFrame(const std::string& receiptId) {
    Frame disconnectFrame;
    disconnectFrame.command = "DISCONNECT";
    disconnectFrame.headers["receipt"] = receiptId;
    return serializeFrame(disconnectFrame);
}

StompProtocol::Frame StompProtocol::receiveFrame(std::string &response) {
    return parseFrame(response);
}
