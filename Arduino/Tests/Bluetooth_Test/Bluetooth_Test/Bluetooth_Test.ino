#include <SoftwareSerial.h>


#define softrx 2    // Connect to TX of HC-05.
#define softtx 3    // Connect to RX of HC-05.
SoftwareSerial BTSerial(softrx, softtx); // RX | TX

int count;

void setup() {
  
  pinMode(softrx, INPUT);
  pinMode(softtx, OUTPUT);
  
  BTSerial.begin(9600);

  Serial.begin(9600);
  count = 0;

}

void loop() {
  if (BTSerial.available()>0)
  {
    char data = BTSerial.read();
    count++;
    BTSerial.println(count);
    Serial.print(data);
  }
}