// Bluetooth
#include <SoftwareSerial.h>
#define BT_INT_NUM 0 // PIN D2.
#define BT_INT_PIN 2 // Respective pin.
SoftwareSerial Bluetooth(4, 3); // RX, TX
bool is_connected;


void bt_set_conncet(){
  if(digitalRead(BT_INT_PIN) == HIGH){
    Serial.println("[Bluetooth]: Bluetooth is connected");
    is_connected = true;
  }
  else{
    Serial.println("[Bluetooth]: Bluetooth is NOT connected");
    is_connected = false;
  }
}


void setup() {
  // Bluetooth
  Bluetooth.begin(9600);
  is_connected = false;
  pinMode(BT_INT_PIN, INPUT);
  attachInterrupt(BT_INT_NUM,
                  bt_set_conncet,
                  CHANGE);

}

void loop() {
  Serial.println(is_connected);
  delay(10);

}
