#include <SPI.h>
#include <MFRC522.h>


// Pin connection RC522:
/*
    SDA   ->    D10
    SCK   ->    D13
    MOSI  ->    D11
    MISO  ->    D12
    RST   ->    D9
*/


#define SS_PIN 10
#define RST_PIN 9

#define BUTTON_PIN 5

#define WL_PIN 7  // Analog pin, Water Level.

#define R_LED_PIN 4
#define Y_LED_PIN 3
#define G_LED_PIN 2

#define UID_LENGHT 7
#define UID_TO_PRINT 4

#define TIME_AFTER_FAILURE 1000
#define TIME_AFTER_SUCCESS 500

const char* START_STRING = "/";
const char* SEPARATOR_STRING = "-";

int state;
unsigned long t_fail;
unsigned long t_success;
byte UID[UID_LENGHT];

/*  State table
    0 -> IDLE.
    1 -> UID identified.
    2 -> Failure.
*/

MFRC522 rfid(SS_PIN, RST_PIN);

void setup() {
  Serial.begin(9600);
  SPI.begin(); // init SPI bus
  rfid.PCD_Init(); // init MFRC522

  //Serial.println(rfid.PCD_GetAntennaGain());
  rfid.PCD_SetRegisterBitMask(rfid.RFCfgReg, (0x07<<4)); // Increase antenna gain to 48db.
  //Serial.println(rfid.PCD_GetAntennaGain());

  pinMode(BUTTON_PIN, INPUT);
  pinMode(R_LED_PIN, OUTPUT);
  pinMode(Y_LED_PIN, OUTPUT);
  pinMode(G_LED_PIN, OUTPUT);

  digitalWrite(R_LED_PIN, LOW);
  digitalWrite(Y_LED_PIN, HIGH);
  digitalWrite(G_LED_PIN, LOW);

  state = 0;
  t_fail = millis();
  t_success = millis();
}

void send_data(){
  Serial.write(START_STRING);
  for(int i = 0; i < UID_TO_PRINT; i++){
    if(rfid.uid.uidByte[i] < 0x10){
      Serial.print("0");
    }
    Serial.print(rfid.uid.uidByte[i], HEX);
  }
  Serial.print(SEPARATOR_STRING);
  Serial.print(map(analogRead(WL_PIN), 0, 1023, 0, 100));
  Serial.write(0xFE);
}

void loop() {
  if((state == 2) && ((millis() - t_fail) > TIME_AFTER_FAILURE)){
    // Back to idle.
    state = 0;
    digitalWrite(R_LED_PIN, LOW);
    digitalWrite(Y_LED_PIN, HIGH);
    digitalWrite(G_LED_PIN, LOW);
  }
  if((digitalRead(BUTTON_PIN) == HIGH) && ((millis() - t_success) > TIME_AFTER_SUCCESS)){
    if(state == 1){
      send_data();
      // Back to idle.
      state = 0;
      digitalWrite(R_LED_PIN, LOW);
      digitalWrite(Y_LED_PIN, HIGH);
      digitalWrite(G_LED_PIN, LOW);
      t_success = millis();
    }
    else{
      // Failure state.
      state = 2;
      digitalWrite(R_LED_PIN, HIGH);
      digitalWrite(Y_LED_PIN, LOW);
      digitalWrite(G_LED_PIN, LOW);
      t_fail = millis();
    }
  }
  if (rfid.PICC_IsNewCardPresent()) { // new tag is available
    if (rfid.PICC_ReadCardSerial()) { // NUID has been readed

      MFRC522::PICC_Type piccType = rfid.PICC_GetType(rfid.uid.sak);

      if(rfid.uid.size != UID_LENGHT){
        // Failure
        state = 2;
        digitalWrite(R_LED_PIN, HIGH);
        digitalWrite(Y_LED_PIN, LOW);
        digitalWrite(G_LED_PIN, LOW);
        t_fail = millis();
      }
      else{
        for (int i = 0; i < rfid.uid.size; i++) {
          UID[i] = rfid.uid.uidByte[i];
          // Serial.println(rfid.uid.uidByte[i], HEX); Debug
        }
        state = 1;
        digitalWrite(R_LED_PIN, LOW);
        digitalWrite(Y_LED_PIN, LOW);
        digitalWrite(G_LED_PIN, HIGH);
      }

      rfid.PICC_HaltA(); // halt PICC
      rfid.PCD_StopCrypto1(); // stop encryption on PCD
    }
  }
}
