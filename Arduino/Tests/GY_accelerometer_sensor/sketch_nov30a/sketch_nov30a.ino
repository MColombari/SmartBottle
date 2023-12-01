// Reference: https://github.com/RobTillaart/GY521
//            https://forum.arduino.cc/t/wire-library-use-on-nano-board/478242

#include<Wire.h>

#define MPU_ADDR 0x68
#define FILTER_DIMENSION 50
#define READING_PERIOD 10
#define PRINT_PERIOD 100

unsigned long start_time_print;
unsigned long start_time_read;
int16_t values_X[FILTER_DIMENSION];
int16_t values_Y[FILTER_DIMENSION];
int16_t values_Z[FILTER_DIMENSION];
int index;


int16_t means(int16_t in[FILTER_DIMENSION]){
  double ret = 0;
  for(int i = 0; i < FILTER_DIMENSION; i++){
    ret += (double) in[i];
  }
  return (int16_t) (ret / FILTER_DIMENSION);
}

void setup(){
  Wire.begin();
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(0x6B);  // PWR_MGMT_1 register
  Wire.write(0);     // set to zero (wakes up the MPU-6050)
  Wire.endTransmission(true);
  Serial.begin(9600);

  start_time_print = millis();
  start_time_read = millis();
  index = 0;

  for (int i = 0; i < FILTER_DIMENSION; i++){
    values_X[i] = 0;
    values_Y[i] = 0;
    values_Z[i] = 0;
  }
}

void loop(){
  if(index >= FILTER_DIMENSION){
    index = 0;
  }

  if(READING_PERIOD <= millis() - start_time_read){
    Wire.beginTransmission(MPU_ADDR);
    Wire.write(0x3B);  // starting with register 0x3B (ACCEL_XOUT_H)
    Wire.endTransmission(false);
    Wire.requestFrom(MPU_ADDR,6,true);  // request a total of 6 registers
    values_X[index]=Wire.read()<<8|Wire.read();  // 0x3B (ACCEL_XOUT_H) & 0x3C (ACCEL_XOUT_L)    
    values_Y[index]=Wire.read()<<8|Wire.read();  // 0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
    values_Z[index]=Wire.read()<<8|Wire.read();  // 0x3F (ACCEL_ZOUT_H) & 0x40 (ACCEL_ZOUT_L)

    index++;
    start_time_read = millis();
  }
  
  if(PRINT_PERIOD <= millis() - start_time_print){
    Serial.print(means(values_X)); Serial.print(",");
    Serial.print(means(values_Y)); Serial.print(",");
    Serial.println(means(values_Z));
    start_time_print = millis();
  }
}
