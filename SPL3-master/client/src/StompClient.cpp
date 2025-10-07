#include <iostream>
#include <thread>
#include <mutex>
#include <atomic>
#include <string>
#include <sstream>
#include <fstream>
#include <map>
#include <vector>
#include <cstdlib>
#include <algorithm>
#include <ctime>
#include <iomanip>

#include "../include/ConnectionHandler.h"
#include "../include/StompProtocol.h"
#include "../include/event.h"

// Mutex for shared resources like std::cout and connectionHandler
std::mutex cout_mutex;
std::mutex connection_mutex;

// Atomic counters for generating unique IDs
std::atomic<int> receiptCounter(1);
std::atomic<int> subscriptionCounter(1);

// Global variables (try to minimize these in a larger application)
ConnectionHandler *connectionHandler = nullptr;
StompProtocol stompProtocol;
bool isLoggedIn = false;
std::map<std::string, std::string> channelSubscriptions;
std::map<std::string, std::vector<Event>> userEvents;

// Function to generate a unique receipt ID
std::string generateReceiptId()
{
    return std::to_string(receiptCounter.fetch_add(1));
}

// Function to generate a unique subscription ID
std::string generateSubscriptionId()
{
    return std::to_string(subscriptionCounter.fetch_add(1));
}

std::string epoch_to_date(int epochTime)
{
    std::time_t time = static_cast<std::time_t>(epochTime);
    std::tm *tm = std::localtime(&time);

    std::ostringstream oss;
    oss << std::put_time(tm, "%d/%m/%y %H:%M");
    return oss.str();
}
void connectionThread()
{
    while (1)
    {
        if (connectionHandler != nullptr)
        { 

            std::string frame;
            if (connectionHandler->getFrameAscii(frame, '\0'))
            {

                /*
                {
                    std::lock_guard<std::mutex> lock(cout_mutex);
                    std::cout << "recieved frame:\n"
                              << frame << std::endl; // Print the received frame.
                }
                */

                if (frame.rfind("CONNECTED", 0) == 0)
                {
                    isLoggedIn = true;
                    {
                        std::lock_guard<std::mutex> lock(cout_mutex);
                        std::cout << "Login successful" << std::endl;
                    }
                }
                else if (frame.rfind("MESSAGE", 0) == 0)
                {
                    // Handle MESSAGE frame
                    std::string destination, body;

                    // Extract destination header
                    size_t destPos = frame.find("destination:");
                    if (destPos != std::string::npos)
                    {
                        size_t endPos = frame.find('\n', destPos);
                        destination = frame.substr(destPos + 12, endPos - (destPos + 12));
                    }

                    // Extract body (the actual message content)
                    size_t bodyPos = frame.find("\n\n");
                    if (bodyPos != std::string::npos)
                    {
                        body = frame.substr(bodyPos + 2);
                    }

                    /*
                    {
                        std::lock_guard<std::mutex> lock(cout_mutex);
                        std::cout << "Message from channel \"" << destination << "\":\n"
                                  << body << std::endl;
                    }
                    */
                }
                else if (frame.rfind("ERROR", 0) == 0)
                {
                    isLoggedIn = false;
                    {
                        std::lock_guard<std::mutex> lock(cout_mutex);
                        std::cout << "Error from the server" << std::endl;
                        std::cerr << frame << std::endl;
                    }
                }
                /*
                else if (frame.rfind("RECEIPT", 0) == 0)
                {
                    std::lock_guard<std::mutex> lock(cout_mutex);
                    std::cout << "Receipt received:" << std::endl;
                    std::cout << frame << std::endl;
                }
                */

                std::this_thread::sleep_for(std::chrono::milliseconds(100));
            }
        }

        else
        {
            std::this_thread::sleep_for(std::chrono::milliseconds(100));
        }
    }
}

void keyboardThread()
{
    std::string username;
    while (1)
    {
        std::string line;
        std::getline(std::cin, line);
        std::istringstream iss(line);
        std::string command;
        iss >> command;

        if (command == "login")
        {
            std::string hostPort, usernameInput, password;
            iss >> hostPort >> usernameInput >> password;
            if (hostPort.empty() || usernameInput.empty() || password.empty())
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cout << "Invalid login command. Usage: login <host:port> <username> <password>" << std::endl;
                continue;
            }

            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                if (isLoggedIn)
                {
                    std::cout << "The client is already logged in, log out before trying again" << std::endl;
                    continue;
                }
            }

            size_t colonPos = hostPort.find(':');
            if (colonPos == std::string::npos || colonPos == 0 || colonPos == hostPort.length() - 1)
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cout << "Invalid host:port format. Example: 127.0.0.1:7777" << std::endl;
                continue;
            }

            std::string host = hostPort.substr(0, colonPos);
            short port = atoi(hostPort.substr(colonPos + 1).c_str());

            if (port <= 0)
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cout << "Invalid port number. Please use a positive integer for the port." << std::endl;
                continue;
            }

            // Clean up the old connection handler before creating a new one
            if (connectionHandler != nullptr)
            {
                {
                    std::lock_guard<std::mutex> lock(connection_mutex);
                    connectionHandler->close();
                }
                connectionHandler = nullptr;
            }

            connectionHandler = new ConnectionHandler(host, port);

            if (!connectionHandler->connect())
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
                connectionHandler = nullptr;
                continue;
            }

            username = usernameInput;
            std::string frame = stompProtocol.createConnectFrame(username, password);
            {
                std::lock_guard<std::mutex> lock(connection_mutex);
                if (!connectionHandler->sendFrameAscii(frame, '\0'))
                {
                    std::lock_guard<std::mutex> lock(cout_mutex);
                    std::cerr << "Failed to send CONNECT frame" << std::endl;
                }
            }
        }
        else if (command == "join")
        {
            std::string channel;
            iss >> channel;
            if (channel.empty())
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cout << "Invalid join command. Usage: join <channel>" << std::endl;
                continue;
            }

            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                if (!isLoggedIn)
                {
                    std::cout << "You must be logged in to join a channel" << std::endl;
                    continue;
                }
            }
            if (channelSubscriptions.find(channel) != channelSubscriptions.end())
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cerr << "Already subscribed to channel: " << channel << std::endl;
                continue;
            }

            std::string subscriptionId = generateSubscriptionId();
            std::string receiptId = generateReceiptId();
            channelSubscriptions[channel] = subscriptionId;

            std::string frame = stompProtocol.createSubscribeFrame(channel, subscriptionId, receiptId);
            {
                std::lock_guard<std::mutex> lock(connection_mutex);
                if (!connectionHandler->sendFrameAscii(frame, '\0'))
                {
                    std::lock_guard<std::mutex> lock(cout_mutex);
                    std::cerr << "Error sending subscribe frame." << std::endl;
                }
            }
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cout << "Joined channel " << channel << std::endl;
            }
        }
        else if (command == "exit")
        {
            std::string channel;
            iss >> channel;
            if (channel.empty())
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cout << "Invalid exit command. Usage: exit <channel>" << std::endl;
                continue;
            }
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                if (!isLoggedIn)
                {
                    std::cout << "You need to log in before exiting a channel" << std::endl;
                    continue;
                }
            }

            if (channelSubscriptions.find(channel) == channelSubscriptions.end())
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cerr << "Not subscribed to channel: " << channel << std::endl;
                continue;
            }

            std::string subscriptionId = channelSubscriptions[channel];
            channelSubscriptions.erase(channel);
            std::string receiptId = generateReceiptId();
            std::string frame = stompProtocol.createUnsubscribeFrame(subscriptionId, receiptId);
            {
                std::lock_guard<std::mutex> lock(connection_mutex);
                if (!connectionHandler->sendFrameAscii(frame, '\0'))
                {
                    std::lock_guard<std::mutex> lock(cout_mutex);
                    std::cerr << "Error sending unsubscribe frame." << std::endl;
                }
                std::cout << "Exited channel " << channel << std::endl;
            }
        }
        else if (command == "report")
        {
            std::string filePath;
            iss >> filePath;
            if (filePath.empty())
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cout << "report command needs 1 args: {file}" << std::endl;
                continue;
            }
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                if (!isLoggedIn)
                {
                    std::cout << "You must be logged in to report an event" << std::endl;
                    continue;
                }
            }

            names_and_events nne = parseEventsFile(filePath);
            const std::string &channelName = nne.channel_name;
            for (Event &event : nne.events)
            {
                event.setEventOwnerUser(username);
            }
            userEvents[channelName].insert(userEvents[channelName].end(), nne.events.begin(), nne.events.end());
            for (Event &event : nne.events)
            {
                std::string frame = stompProtocol.createSendReportFrame(
                    channelName, username, event.get_city(),
                    event.get_name(), epoch_to_date(event.get_date_time()),
                    event.get_general_information(), event.get_description());

                {
                    frame = "user: " + event.getEventOwnerUser() + "\n" + frame;
                    std::lock_guard<std::mutex> lock(connection_mutex);
                    if (!connectionHandler->sendFrameAscii(frame, '\0'))
                    {
                        std::lock_guard<std::mutex> lock(cout_mutex);
                        std::cerr << "Error sending report for event: " << event.get_name() << std::endl;
                    }
                }
            }
            std::cout << "reported" << std::endl;
        }
        else if (command == "summary")
        {
            std::string channel, username, filePath;
            iss >> channel >> username >> filePath;
            if (channel.empty() || username.empty() || filePath.empty())
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cout << "Invalid summary command. Usage: summary <channel> <username> <file_path>" << std::endl;
                continue;
            }
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                if (!isLoggedIn)
                {
                    std::cout << "You must be logged in to request a summary" << std::endl;
                    continue;
                }
            }

            // Aggregate events for the user
            std::vector<Event> userSpecificEvents;
            for (const auto &pair : userEvents)
            {
                for (const Event &event : pair.second)
                {
                    if (event.getEventOwnerUser() == username)
                    {

                        userSpecificEvents.push_back(event);
                    }
                }
            }
            std::sort(userSpecificEvents.begin(), userSpecificEvents.end(), [](const Event &a, const Event &b)
                      {
                if (a.get_date_time() != b.get_date_time())
                    return a.get_date_time() < b.get_date_time();
                return a.get_name() < b.get_name(); });
            if (userSpecificEvents.empty())
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cout << "No events found for user \"" << username << "\"" << std::endl;
                continue;
            }
            // Calculate statistics
            int totalReports = userSpecificEvents.size();
            int activeCount = 0;
            int forcesArrivalCount = 0;
            for (const Event &event : userSpecificEvents)
            {
                if (event.get_general_information().at("active").find("true") != std::string::npos)
                    activeCount++;
                if (event.get_general_information().at("forces_arrival_at_scene").find("true") != std::string::npos)
                    forcesArrivalCount++;
            }
            std::stringstream summaryStream;
            summaryStream << "Channel " << channel << "\n";
            summaryStream << "Stats:\n";
            summaryStream << "Total: " << totalReports << "\n";
            summaryStream << "active: " << activeCount << "\n";
            summaryStream << "forces arrival at scene: " << forcesArrivalCount << "\n";
            summaryStream << "Event Reports:\n";

            int reportNumber = 1;
            for (const Event &event : userSpecificEvents)
            {
                std::string description = event.get_description();
                if (description.length() > 27)
                {
                    description = description.substr(0, 27) + "...";
                }

                summaryStream << "Report_" << reportNumber++ << ":\n";
                summaryStream << "city: " << event.get_city() << "\n";
                summaryStream << "date time: " << epoch_to_date(event.get_date_time()) << "\n";
                summaryStream << "event name: " << event.get_name() << "\n";
                summaryStream << "summary: " << description << "\n";
            }

            // Write summary to file
            std::ofstream outputFile(filePath);
            if (outputFile.is_open())
            {
                outputFile << summaryStream.str();
                outputFile.close();
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cout << "Summary for user \"" << username << "\" in channel \"" << channel << "\" written to " << filePath << std::endl;
            }
            else
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                std::cerr << "Unable to open file: " << filePath << std::endl;
            }
        }
        else if (command == "logout")
        {
            {
                std::lock_guard<std::mutex> lock(cout_mutex);
                if (!isLoggedIn)
                {
                    std::cout << "You must be logged in to logout" << std::endl;
                    continue;
                }
            }
            std::string receiptId = generateReceiptId();
            std::string frame = stompProtocol.createDisconnectFrame(receiptId);
            {
                std::lock_guard<std::mutex> lock(connection_mutex);
                if (!connectionHandler->sendFrameAscii(frame, '\0'))
                {
                    std::lock_guard<std::mutex> lock(cout_mutex);
                    std::cerr << "Error sending logout frame." << std::endl;
                }
            }
            isLoggedIn = false;
            connectionHandler = nullptr;
            std::cout << "Logged out" << std::endl;
        }
        else
        {
            std::lock_guard<std::mutex> lock(cout_mutex);
            std::cout << "Unknown command: " << command << std::endl;
        }
    }
}

int main(int argc, char *argv[])
{
    // Start keyboard input thread
    std::thread keybThread(keyboardThread);

    // Start connection thread
    std::thread connThread(connectionThread);

    // Wait for the threads to finish
    keybThread.join();
    connThread.join();

    // Clean up the connection handler if it exists
    if (connectionHandler)
    {
        delete connectionHandler;
        connectionHandler = nullptr;
    }

    std::cout << "Client has exited." << std::endl;
    return 0;
}
