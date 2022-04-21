 #include<SoftwareSerial.h>

SoftwareSerial port(3, 2); // Rx, Tx

void setup() {
    pinMode(4, OUTPUT); // HC-11 SET pin
    digitalWrite(4, 0);
    
    Serial.begin(9600); // start serial communication
    Serial.println("Enter AT commands:");
    port.begin(9600);
}

void loop() {
    if(port.available()){
        Serial.print(port.readString());
        }
    if(Serial.available()){
        port.write(Serial.read());
        }
}

// AT+A addres
// AT+C channel
// AT+B baud rate
// AT+Px (1-8)
// AT+RX view configuration
