/****************************/
// rf_module.ino
// Author: Adam Rajko
// RF receiever (1-4)
/*****************************/

#include <SoftwareSerial.h>

SoftwareSerial mySerial(3, 2); // RX, TX

int switch_pin[4] = {5, 6, 7, 8};
int switch_states[4] = {0, 0, 0, 0};
int switch_led_pin = 9;
int switch_led_state = 0;

String command = "command";
String command_led = "command"; 


void setup() {
    for(int i=0; i<4; i++) {
        pinMode(switch_pin[i], OUTPUT); // pins as outputs
    }

    for(int i = 0; i < 4; i++) {
        digitalWrite(switch_pin[i], 1); // turn off all SSR
    }

    pinMode(switch_led_pin, OUTPUT);
    digitalWrite(switch_led_pin, 1);
    
    pinMode(4, OUTPUT);
    digitalWrite(4, 1); // HC-11 SET pin set to HIGH
    mySerial.begin(9600);
}


void loop() {
    if(mySerial.available()) {
        String bytes = mySerial.readString(); // expexted input: garden,x,x,x,x,x,x

        String word;
        for(char c : bytes){
            if(c != ',') word.concat(c);
            else break;
        }

        if(command.equals(word)){

            int i = -1;

            for(char c : bytes) {
                if(c != ','){
                    if( (i < 4) && (i >= 0) ){
                        switch_states[i] = c - '0'; // change states
                    }
                    // if message was readed
                    if(i == 3) {
                        break;
                    }
                }
                if(c == ',') i++;
            }

            for(i = 0; i < 4; i++){
                digitalWrite(switch_pin[i], !switch_states[i]); // set SSR
            }

            // fixed issue with rf_module_2 (problem with receiving)
            delay(500);
            mySerial.print(bytes);
        }

        if(command_led.equals(word)){

            int i = -1;

            for(char c : bytes) {
                if(c != ','){
                    if( (i < 1) && (i >= 0) ){
                        switch_led_state = c - '0'; // change states
                    }
                    // if message was readed
                    if(i == 0) {
                        break;
                    }
                }
                if(c == ',') i++;
            }

            digitalWrite(switch_led_pin, !switch_led_state); // set SSR
        }
    }
    delay(150);
}
