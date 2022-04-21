/****************************/
// rf_module_2.ino
// Author: Adam Rajko
// RF receiever (5-6)
/*****************************/

#include <SoftwareSerial.h>

SoftwareSerial mySerial(3, 2); // RX, TX

int switch_pin[4] = {5, 6, 7, 8};
int switch_states[4] = {0, 0, 0, 0};

String command = "command";


void setup() {
    for(int i=0; i<4; i++){
        pinMode(switch_pin[i], OUTPUT);
    }

    for(int i = 0; i < 4; i++){
        digitalWrite(switch_pin[i], 1); // turn off all SSR
    }

    pinMode(4, OUTPUT);
    digitalWrite(4, 1); // HC-11 SET pin set to HIGH
    mySerial.begin(9600);
}


void loop() {
    if(mySerial.available()) {
        String bytes = mySerial.readString(); // expected input garden,x,x,x,x,x,x

        String word;
        for(char c : bytes){
            if(c != ',') word.concat(c);
            else break;
        }

        if(command.equals(word)){
            int i = -1;

            for(char c : bytes) {
                if(c != ','){
                    if(i >= 4){
                        switch_states[i-4] = c - '0'; // change states
                    }
                    // if message was readed
                    if(i == 5){
                        break;
                    }
                }
                if(c == ',') i++;
            }

            for(i = 0; i < 4; i++){
                digitalWrite(switch_pin[i], !switch_states[i]); // set SSR
            }
        }   
    }
    delay(150);
}
