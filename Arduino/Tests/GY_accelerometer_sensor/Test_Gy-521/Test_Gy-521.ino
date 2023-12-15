/*
Reference:
    Wire Nano:    https://forum.arduino.cc/t/wire-library-use-on-nano-board/478242
    Doc MPU-6050: https://invensense.tdk.com/wp-content/uploads/2015/02/MPU-6000-Datasheet1.pdf
*/            

#include<Wire.h>
#include<math.h>

#define MPU_ADDR 0x68

#define FILTER_DIM 10
#define MEANS_STORE_DIM 10

#define READING_PERIOD 1
#define SAVE_PERIOD 100
#define UPDATE_FLAG_PERIOD 200

#define G_FORCE 17000
#define SLACK 2000

#define MARGIN_DIST 150

unsigned long start_time_read;
unsigned long start_time_save;
unsigned long start_time_update;

int16_t values_X[FILTER_DIM];
int16_t values_Y[FILTER_DIM];
int16_t values_Z[FILTER_DIM];
int index_values;

int16_t means_X[MEANS_STORE_DIM];
int16_t means_Y[MEANS_STORE_DIM];
int16_t means_Z[MEANS_STORE_DIM];
int index_means;

// Control flag.
bool is_still;
bool is_vertical;

int16_t max_distance(int16_t in[], size_t dim){
  int16_t min = in[0];
  int16_t max = in[0];
  for(size_t i = 0; i < dim; i++){
    if(in[i] > max){ max = in[i]; }
    if(in[i] < min){ min = in[i]; }
  }
  return max - min;
}

double standard_deviation(int16_t in[], int16_t avg, size_t dim){
  double sum = 0;
  for(size_t i = 0; i < dim; i++){
    sum += pow(in[i] - avg, 2);
  }
  return sqrt(sum / dim);
}


int16_t mean(int16_t in[], size_t dim){
  double ret = 0;
  for(size_t i = 0; i < dim; i++){
    ret += (double) in[i];
  }
  return (int16_t) (ret / dim);
}

void setup(){
  Wire.begin();
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(0x6B);  // PWR_MGMT_1 register
  Wire.write(0);     // set to zero (wakes up the MPU-6050)
  Wire.endTransmission(true);
  Serial.begin(9600);

  start_time_read = millis();
  start_time_save = millis();
  start_time_update = millis();
  index_values = 0;
  index_means = 0;

  for (size_t i = 0; i < FILTER_DIM; i++){
    values_X[i] = 0;
    values_Y[i] = 0;
    values_Z[i] = 0;
  }

  for(size_t i = 0; i < MEANS_STORE_DIM; i++){
    means_X[i] = 0;
    means_Y[i] = 0;
    means_Z[i] = 0;
  }

  is_still = false;
  is_vertical = false;
}

void loop(){
  if(index_values >= FILTER_DIM){
    index_values = 0;
  }
  if(index_means >= FILTER_DIM){
    index_means = 0;
  }

  if(READING_PERIOD <= millis() - start_time_read){
    Wire.beginTransmission(MPU_ADDR);
    Wire.write(0x3B);  // starting with register 0x3B (ACCEL_XOUT_H)
    Wire.endTransmission(false);
    Wire.requestFrom(MPU_ADDR,6,true);  // request a total of 6 registers
    values_X[index_values]=Wire.read()<<8|Wire.read();  // 0x3B (ACCEL_XOUT_H) & 0x3C (ACCEL_XOUT_L)    
    values_Y[index_values]=Wire.read()<<8|Wire.read();  // 0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
    values_Z[index_values]=Wire.read()<<8|Wire.read();  // 0x3F (ACCEL_ZOUT_H) & 0x40 (ACCEL_ZOUT_L)

    index_values++;
    start_time_read = millis();
  }
  
  if(SAVE_PERIOD <= millis() - start_time_save){
    means_X[index_means] = mean(values_X, FILTER_DIM);
    means_Y[index_means] = mean(values_Y, FILTER_DIM);
    means_Z[index_means] = mean(values_Z, FILTER_DIM);
    
    
    Serial.print(means_X[index_means]); Serial.print(",");
    Serial.print(means_Y[index_means]); Serial.print(",");
    Serial.print(means_Z[index_means]); Serial.print(",");
    Serial.print(is_still); Serial.print(",");
    Serial.println(is_vertical);
    

    index_means++;
    start_time_save = millis();
  }

  if(UPDATE_FLAG_PERIOD <= millis() - start_time_update){
    int16_t mean_X = mean(means_X, MEANS_STORE_DIM);
    int16_t mean_Y = mean(means_Y, MEANS_STORE_DIM);
    int16_t mean_Z = mean(means_Z, MEANS_STORE_DIM);

    // Check if it's verical.
    if(
      ((mean_X >= -SLACK) && (mean_X <= SLACK)) &&
      ((mean_Y >= -SLACK) && (mean_Y <= SLACK)) &&
      ((mean_Z >= G_FORCE - SLACK) && (mean_Z <= G_FORCE + SLACK))){
        is_vertical = true;
    }
    else{
      is_vertical = false;
    }

    // check if it's still.
    int16_t dist_X = max_distance(means_X, MEANS_STORE_DIM);
    int16_t dist_Y = max_distance(means_X, MEANS_STORE_DIM);
    int16_t dist_Z = max_distance(means_X, MEANS_STORE_DIM);

    if(
      (dist_X <= MARGIN_DIST) &&
      (dist_Y <= MARGIN_DIST) &&
      (dist_Z <= MARGIN_DIST)){
        is_still = true;
    }
    else{
      is_still = false;
    }

    start_time_update = millis();
  }
}
