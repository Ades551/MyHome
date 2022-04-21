/****************************/
// rf_module_transmitter.ino
// Author: Adam Rajko
/*****************************/

#include <SoftwareSerial.h>

SoftwareSerial mySerial(3, 2); // RX, TX

String strings[] = {",0,0,0,0,0,0", ",1,0,0,0,0,0", ",0,1,0,0,0,0", ",0,0,1,0,0,0", ",0,0,0,1,0,0", ",0,0,0,0,1,0", ",0,0,0,0,0,1"};

void setup(){
    mySerial.begin(9600);
    Serial.begin(9600);
}

void loop(){
    for(int i = 0; i < 7; i++){
        mySerial.print(strings[i]);
        Serial.println(strings[i]);
        delay(3000);
    }
}
