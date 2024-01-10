/*
      PIN Configuration
    Battery:
      A0 -> A6
    Pressure:
      [A3 - A0]
      Vorltage pertition with a Pull-up resistor (10k ohm).
    HC-05:
      State -> D4.
      RX -> D3 (Pull-Down 2k, between 1k).
      TX -> D2.
    Accellerometer:
      SCL -> A5.
      SDA -> A4
*/

/*  -  Hyperparameter  -  */

// GY-521
#define GY_FILTER_DIM 10
#define GY_MEANS_STORE_DIM 10
#define GY_READING_PERIOD 1
#define GY_SAVE_PERIOD 100
#define GY_UPDATE_FLAG_PERIOD 200
#define GY_G_FORCE_VALUE 17000
#define GY_G_FORCE_SLACK 1000
#define GY_ZERO_SLACK 2000
#define GY_MARGIN_DIST 150

// Battery Module
#define B_BUFFER_DIM 10
#define B_READ_PERIOD 1000
#define B_ENERGY_THRESHOLD 0.50


/*  -  Library include and global variable and functions  -  */

// Bluetooth
#include <SoftwareSerial.h>
#define BT_INT_PIN 4             // PIN for state.
SoftwareSerial Bluetooth(2, 3);  // TX, RX (of HC-05).

void blt_print(char* in){
  Serial.print(in);
  if (digitalRead(BT_INT_PIN) == HIGH) {
    Serial.println("\t\tOn BLT");
    Bluetooth.println(in);
  }
  else{
    Serial.println("\t\tOnly Serial");
  }
}

// GY-521, Accellerometer.
#include <Wire.h>
#include <math.h>
#define MPU_ADDR 0x68

unsigned long start_time_read;
unsigned long start_time_save;
unsigned long start_time_update;
int16_t values_X[GY_FILTER_DIM];
int16_t values_Y[GY_FILTER_DIM];
int16_t values_Z[GY_FILTER_DIM];
int index_values;
int16_t means_X[GY_MEANS_STORE_DIM];
int16_t means_Y[GY_MEANS_STORE_DIM];
int16_t means_Z[GY_MEANS_STORE_DIM];
int index_means;
bool is_still;
bool is_vertical;

int16_t max_distance(int16_t in[], size_t dim) {
  int16_t min = in[0];
  int16_t max = in[0];
  for (size_t i = 0; i < dim; i++) {
    if (in[i] > max) { max = in[i]; }
    if (in[i] < min) { min = in[i]; }
  }
  return max - min;
}
int16_t mean(int16_t in[], size_t dim) {
  double ret = 0;
  for (size_t i = 0; i < dim; i++) {
    ret += (double)in[i];
  }
  return (int16_t)(ret / dim);
}

// Battery Module
#define sensivity (4.2 / 1023.0)
#define maxVoltage 4.2

int16_t battery_buffer[B_BUFFER_DIM];
size_t battery_buffer_index;
unsigned long b_start_time;
bool is_power_saving;



void setup() {
  // Serial [DEBUG]
  Serial.begin(9600);

  // Bluetooth
  Bluetooth.begin(9600);
  pinMode(BT_INT_PIN, INPUT);

  // GY-521
  Wire.begin();
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(0x6B);  // PWR_MGMT_1 register
  Wire.write(0);     // set to zero (wakes up the MPU-6050)
  Wire.endTransmission(true);
  start_time_read = millis();
  start_time_save = millis();
  start_time_update = millis();
  index_values = 0;
  index_means = 0;

  for (size_t i = 0; i < GY_FILTER_DIM; i++) {
    values_X[i] = 0;
    values_Y[i] = 0;
    values_Z[i] = 0;
  }
  for (size_t i = 0; i < GY_MEANS_STORE_DIM; i++) {
    means_X[i] = 0;
    means_Y[i] = 0;
    means_Z[i] = 0;
  }

  is_still = false;
  is_vertical = false;

  // Battery Module
  battery_buffer_index = 0;
  b_start_time = millis();
  is_power_saving = false;

  for (int i = 0; i < B_BUFFER_DIM; i++) {
    battery_buffer[B_BUFFER_DIM] = 0;
  }
}

void loop() {
  // Index Update.
  if (index_values >= GY_FILTER_DIM) {
    index_values = 0;
  }
  if (index_means >= GY_FILTER_DIM) {
    index_means = 0;
  }
  if (battery_buffer_index >= B_BUFFER_DIM) {
    int16_t value = mean(battery_buffer, B_BUFFER_DIM);
    if (value <= B_ENERGY_THRESHOLD) {
      is_power_saving = true;
    } else {
      is_power_saving = false;
    }
    battery_buffer_index = 0;
    Bluetooth.print("Battery Level: ");
    Bluetooth.print(value);
    Bluetooth.println("%");
  }

  // Read value from the MPU.
  if (GY_READING_PERIOD <= millis() - start_time_read) {
    Wire.beginTransmission(MPU_ADDR);
    Wire.write(0x3B);  // starting with register 0x3B (ACCEL_XOUT_H)
    Wire.endTransmission(false);
    Wire.requestFrom(MPU_ADDR, 6, true);                      // request a total of 6 registers
    values_X[index_values] = Wire.read() << 8 | Wire.read();  // 0x3B (ACCEL_XOUT_H) & 0x3C (ACCEL_XOUT_L)
    values_Y[index_values] = Wire.read() << 8 | Wire.read();  // 0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
    values_Z[index_values] = Wire.read() << 8 | Wire.read();  // 0x3F (ACCEL_ZOUT_H) & 0x40 (ACCEL_ZOUT_L)

    index_values++;
    start_time_read = millis();
  }

  // Save data read from the MPU.
  if (GY_SAVE_PERIOD <= millis() - start_time_save) {
    means_X[index_means] = mean(values_X, GY_FILTER_DIM);
    means_Y[index_means] = mean(values_Y, GY_FILTER_DIM);
    means_Z[index_means] = mean(values_Z, GY_FILTER_DIM);

    /*  Debug
    Serial.print(means_X[index_means]); Serial.print(",");
    Serial.print(means_Y[index_means]); Serial.print(",");
    Serial.print(means_Z[index_means]); Serial.print(",");
    Serial.print(is_still); Serial.print(",");
    Serial.println(is_vertical); */

    index_means++;
    start_time_save = millis();
  }

  // Update "is_vertical" and "is_still" flag.
  if (GY_UPDATE_FLAG_PERIOD <= millis() - start_time_update) {
    int16_t mean_X = mean(means_X, GY_MEANS_STORE_DIM);
    int16_t mean_Y = mean(means_Y, GY_MEANS_STORE_DIM);
    int16_t mean_Z = mean(means_Z, GY_MEANS_STORE_DIM);

    // Check if it's verical.
    if (
      ((mean_X >= -GY_ZERO_SLACK) && (mean_X <= GY_ZERO_SLACK)) && ((mean_Y >= -GY_ZERO_SLACK) && (mean_Y <= GY_ZERO_SLACK)) && ((mean_Z >= GY_G_FORCE_VALUE - GY_G_FORCE_SLACK) && (mean_Z <= GY_G_FORCE_VALUE + GY_G_FORCE_SLACK))) {
      // Print if changed.
      if (is_vertical == false) {
        blt_print("Is vertical");
      }
      is_vertical = true;
    } else {
      if (is_vertical == true) {
        blt_print("Is NOT vertical");
      }
      is_vertical = false;
    }

    // check if it's still.
    int16_t dist_X = max_distance(means_X, GY_MEANS_STORE_DIM);
    int16_t dist_Y = max_distance(means_X, GY_MEANS_STORE_DIM);
    int16_t dist_Z = max_distance(means_X, GY_MEANS_STORE_DIM);

    if (
      (dist_X <= GY_MARGIN_DIST) && (dist_Y <= GY_MARGIN_DIST) && (dist_Z <= GY_MARGIN_DIST)) {
      if (is_still == false) {
        //Serial.println("[GY-521]: Is still");
        blt_print("Is still");
      }
      is_still = true;
    } else {
      if (is_still == true) {
        //Serial.println("[GY-521]: Is NOT still");
        blt_print("Is NOT still");
      }
      is_still = false;
    }

    start_time_update = millis();
  }

  // Read battery value.
  if (millis() >= b_start_time + B_READ_PERIOD) {
    float sensorValue = analogRead(A6);                     // Raw value.
    sensorValue = sensorValue * sensivity;                  // Voltage.
    float percentage = (sensorValue / maxVoltage) * 100.0;  // Percentage.
    battery_buffer[battery_buffer_index] = (int16_t)percentage;
    battery_buffer_index++;
    b_start_time = millis();
  }
}
