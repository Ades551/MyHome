/****************************/
// wifi.ino
// Author: Adam Rajko
/*****************************/

#include <ESP8266WiFi.h>

int pin[4] = {13, 12, 14, 15}; // D7, D6, D5, D8
int pin_states[4] = {0, 0, 0, 0};

const String ssid = "SSID";
const String password = "PASSWORD";

IPAddress ip(192, 168, 1, 110); //set static ip
IPAddress gateway(192, 168, 1, 1); //set getteway
IPAddress subnet(255, 255, 255, 0); //set subnet

int port = 5050; // port

WiFiClient client;
WiFiServer server(port);


// from string ex. ("1,0,1,0") -> pin_states = {1, 0, 1, 0}
void zapis(String states){
  int i = 0;
  for(char c : states){
    if(c == ','){
      if(i == 3) i = 0;
      else i++;
    } else {
      //Serial.print(c);
      pin_states[i] = c - '0';
    }
  }
  //Serial.println();
}


void zapni(){
  for(int i = 0; i < 4; i++){
    digitalWrite(pin[i], !pin_states[i]);
  }
}


void setup(){
  // pins
  for(int i = 0; i < 4; i++){
    pinMode(pin[i], OUTPUT);
  }

  //Serial.begin(9600);
  //Serial.println("Ahoj");

  WiFi.hostname("wifi_controll");
  WiFi.config(ip, gateway, subnet);
  WiFi.begin(ssid, password);

  // wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(250);
  }

  server.begin(); // start server

  // send message to raspi
  // get states from room
  if(client.connect("192.168.1.252", 1234)){
    client.write("command");

    String word;

    while(client.available() || client.connected()){
      if(client.available()){
        word.concat((char)client.read());
      }
    }

    client.stop();

    zapis(word);
    zapni();
  }
}


void loop(){
  client = server.available();
  if (client){
    if(client.connected()) {
      String final_word;
      while(client.available() == 0) delay(25);
      while(client.available()){
        final_word.concat((char)client.read());
      }
      
      client.write("done"); // reply to end communication
      zapis(final_word);
      zapni();
    }
  }
  delay(250);
}
