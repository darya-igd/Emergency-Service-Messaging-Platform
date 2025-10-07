#pragma once

#include "../include/ConnectionHandler.h"
#include <string>
#include <map>
#include <sstream>

// TODO: implement the STOMP protocol
class StompProtocol {
private:
    struct Frame {
        std::string command;
        std::map<std::string, std::string> headers;
        std::string body;
    };

    Frame parseFrame(const std::string &frameString);
    std::string serializeFrame(const Frame &frame);

public:
    // Method to create a CONNECT frame
    std::string createConnectFrame(const std::string& username, const std::string& password);

    // Method to create a SUBSCRIBE frame
    std::string createSubscribeFrame(const std::string& channel, const std::string& subscriptionId, const std::string& receiptId);

    // Method to create an UNSUBSCRIBE frame
    std::string createUnsubscribeFrame(const std::string& subscriptionId, const std::string& receiptId);

    // Method to create a SEND frame
    std::string createSendFrame(const std::string& channel, const std::string& message);

    // Method to create a SEND report frame
    std::string createSendReportFrame(const std::string& channel, const std::string& user, const std::string& city, const std::string& eventName,
                                       const std::string& dateTime, const std::map<std::string, std::string>& generalInformation, const std::string& description);

    // Method to create a DISCONNECT frame
    std::string createDisconnectFrame(const std::string& receiptId);

    Frame receiveFrame(std::string &response);
};
