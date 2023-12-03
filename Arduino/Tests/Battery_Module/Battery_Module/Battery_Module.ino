#define sensivity (4.2 / 1023.0)
#define maxVoltage 4.2

#define BUFFER_DIM 10
#define READ_PERIOD 1000

float battery_buffer[BUFFER_DIM];
size_t index;

unsigned long start_time;

float mean(){
  double ret = 0;

  for(int i = 0; i < BUFFER_DIM; i++){
    ret += battery_buffer[i];
  }

  return (float) (ret / BUFFER_DIM);
}

void setup() {
  Serial.begin(9600);
  index = 0;
  start_time = millis();

  for(int i = 0; i < BUFFER_DIM; i++){
    battery_buffer[BUFFER_DIM] = 0;
  }
}

void loop() {
  if(index >= BUFFER_DIM){
    Serial.println(mean());
    index = 0;
  }
  if(millis() >= start_time + READ_PERIOD){
    float sensorValue = analogRead(A6);                       // Raw value.
    sensorValue = sensorValue * sensivity;                    // Voltage.
    float percentage = (sensorValue / maxVoltage) * 100.0;    // Percentage.
    battery_buffer[index] = percentage;
    index++;
    start_time = millis();
  }
  /*
  float sensorValue = analogRead(A6);
  Serial.print("Raw adc: ");
  Serial.println(int(sensorValue));
  sensorValue = sensorValue * sensivity;
  Serial.print("Voltage: ");
  Serial.print(sensorValue);
  Serial.println("V");
  // Calculate the percentage level
  float percentage = (sensorValue / maxVoltage) * 100.0;
  Serial.print("Percentage: ");
  Serial.print(percentage);
  Serial.println("%");
  */
  
}